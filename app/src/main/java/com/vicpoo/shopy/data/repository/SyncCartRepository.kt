// data/repository/SyncCartRepository.kt
package com.vicpoo.shopy.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vicpoo.shopy.data.local.dao.CartDao
import com.google.firebase.database.DataSnapshot
import com.vicpoo.shopy.data.local.dao.PendingOperationDao
import com.vicpoo.shopy.data.local.entity.CartItemEntity
import com.vicpoo.shopy.data.local.entity.PendingOperationEntity
import com.vicpoo.shopy.domain.model.CartItem
import com.vicpoo.shopy.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncCartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val cartDao: CartDao,
    private val pendingOperationDao: PendingOperationDao
) : CartRepository {

    companion object {
        private const val TAG = "SyncCartRepo"
    }

    private val usersRef = database.getReference("users")
    private val productsRef = database.getReference("products")

    private val currentUserCartRef
        get() = auth.currentUser?.uid?.let { uid ->
            usersRef.child(uid).child("cart")
        }

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToCart(productId: String, quantity: Int, selectedSize: String?) {
        try {
            // 1. Guardar en Room local
            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.toCloth()

            if (product != null) {
                val existingItem = cartDao.getCartItem(productId)
                val newQuantity = (existingItem?.quantity ?: 0) + quantity

                val cartItem = CartItemEntity(
                    productId = productId,
                    name = product.name,
                    price = product.price ?: 0.0,
                    image = product.image,
                    quantity = newQuantity,
                    selectedSize = selectedSize
                )
                cartDao.insertOrUpdate(cartItem)
            }

            // 2. Guardar operación pendiente
            val operation = PendingOperationEntity(
                operationType = "ADD",
                productId = productId,
                quantity = quantity,
                selectedSize = selectedSize,
                synced = false
            )
            pendingOperationDao.insertOperation(operation)

            // 3. Intentar sincronizar inmediatamente
            trySync()

        } catch (e: Exception) {
            Log.e(TAG, "Error en addToCart (guardado local)", e)
        }
    }

    override suspend fun removeFromCart(productId: String) {
        try {
            // 1. Eliminar de Room
            cartDao.deleteById(productId)

            // 2. Guardar operación pendiente
            val operation = PendingOperationEntity(
                operationType = "REMOVE",
                productId = productId,
                synced = false
            )
            pendingOperationDao.insertOperation(operation)

            // 3. Intentar sincronizar
            trySync()

        } catch (e: Exception) {
            Log.e(TAG, "Error en removeFromCart", e)
        }
    }

    override suspend fun updateQuantity(productId: String, newQuantity: Int) {
        try {
            if (newQuantity <= 0) {
                removeFromCart(productId)
                return
            }

            // 1. Actualizar en Room
            val existingItem = cartDao.getCartItem(productId)
            existingItem?.let {
                val updatedItem = it.copy(quantity = newQuantity)
                cartDao.insertOrUpdate(updatedItem)
            }

            // 2. Guardar operación pendiente
            val operation = PendingOperationEntity(
                operationType = "UPDATE",
                productId = productId,
                quantity = newQuantity,
                synced = false
            )
            pendingOperationDao.insertOperation(operation)

            // 3. Intentar sincronizar
            trySync()

        } catch (e: Exception) {
            Log.e(TAG, "Error en updateQuantity", e)
        }
    }

    override suspend fun clearCart() {
        try {
            // 1. Limpiar Room
            cartDao.clearCart()

            // 2. Guardar operación pendiente
            val operation = PendingOperationEntity(
                operationType = "CLEAR",
                productId = "",
                synced = false
            )
            pendingOperationDao.insertOperation(operation)

            // 3. Intentar sincronizar
            trySync()

        } catch (e: Exception) {
            Log.e(TAG, "Error en clearCart", e)
        }
    }

    suspend fun trySync() {
        try {
            val cartRef = currentUserCartRef ?: return
            val pendingOps = pendingOperationDao.getPendingOperationsOnce()

            if (pendingOps.isEmpty()) return

            Log.d(TAG, "🔄 Sincronizando ${pendingOps.size} operaciones pendientes")

            for (operation in pendingOps) {
                try {
                    when (operation.operationType) {
                        "ADD" -> {
                            val snapshot = cartRef.child(operation.productId).get().await()
                            val currentQuantity = if (snapshot.exists()) (snapshot.value as Long).toInt() else 0
                            val newQuantity = currentQuantity + (operation.quantity ?: 1)
                            cartRef.child(operation.productId).setValue(newQuantity).await()
                            Log.d(TAG, "✅ Sincronizado ADD para ${operation.productId}")
                        }
                        "UPDATE" -> {
                            cartRef.child(operation.productId).setValue(operation.quantity ?: 1).await()
                            Log.d(TAG, "✅ Sincronizado UPDATE para ${operation.productId}")
                        }
                        "REMOVE" -> {
                            cartRef.child(operation.productId).removeValue().await()
                            Log.d(TAG, "✅ Sincronizado REMOVE para ${operation.productId}")
                        }
                        "CLEAR" -> {
                            cartRef.removeValue().await()
                            Log.d(TAG, "✅ Sincronizado CLEAR")
                        }
                    }

                    pendingOperationDao.deleteOperation(operation.id)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error sincronizando operación ${operation.id}", e)
                }
            }

            pendingOperationDao.clearSyncedOperations()

        } catch (e: Exception) {
            Log.e(TAG, "Error en trySync", e)
        }
    }

    suspend fun syncFromFirebase() {
        try {
            val cartRef = currentUserCartRef ?: return

            val cartSnapshot = cartRef.get().await()
            val firebaseCart = cartSnapshot.value as? Map<String, Any> ?: emptyMap()

            if (firebaseCart.isEmpty()) {
                val localItems = cartDao.getAllCartItemsOnce()
                if (localItems.isNotEmpty()) {
                    Log.d(TAG, "📤 Subiendo ${localItems.size} items locales a Firebase")
                    for (item in localItems) {
                        cartRef.child(item.productId).setValue(item.quantity).await()
                    }
                }
                return
            }

            val localItems = cartDao.getAllCartItemsOnce()
            val localMap = localItems.associate { it.productId to it.quantity }
            val mergedQuantities = mutableMapOf<String, Int>()

            firebaseCart.forEach { (productId, quantity) ->
                val qty = when (quantity) {
                    is Long -> quantity.toInt()
                    is Int -> quantity
                    else -> 1
                }
                mergedQuantities[productId] = qty
            }

            localItems.forEach { item ->
                if (!mergedQuantities.containsKey(item.productId)) {
                    mergedQuantities[item.productId] = item.quantity
                }
            }

            for ((productId, quantity) in mergedQuantities) {
                val productSnapshot = productsRef.child(productId).get().await()
                val product = productSnapshot.toCloth()

                if (product != null) {
                    val cartItem = CartItemEntity(
                        productId = productId,
                        name = product.name,
                        price = product.price ?: 0.0,
                        image = product.image,
                        quantity = quantity,
                        selectedSize = null
                    )
                    cartDao.insertOrUpdate(cartItem)
                }
            }

            val mergedProductIds = mergedQuantities.keys
            localItems.forEach { item ->
                if (!mergedProductIds.contains(item.productId)) {
                    cartDao.deleteById(item.productId)
                }
            }

            pendingOperationDao.clearSyncedOperations()
            Log.d(TAG, "✅ Sincronización completa desde Firebase")

        } catch (e: Exception) {
            Log.e(TAG, "Error en syncFromFirebase", e)
        }
    }

    private fun CartItemEntity.toDomain(): CartItem = CartItem(
        productId = productId,
        name = name,
        price = price,
        image = image,
        quantity = quantity,
        selectedSize = selectedSize
    )

    private fun DataSnapshot.toCloth(): com.vicpoo.shopy.domain.model.Cloth? {
        val id = key ?: return null
        val value = value as? Map<String, Any> ?: return null

        return com.vicpoo.shopy.domain.model.Cloth(
            id = id,
            name = value["name"] as? String ?: return null,
            description = value["description"] as? String,
            size = value["size"] as? String,
            price = (value["price"] as? Number)?.toDouble(),
            stock = (value["stock"] as? Number)?.toInt(),
            image = value["image"] as? String,
            sellerId = value["sellerId"] as? String ?: "",
            createdAt = (value["createdAt"] as? Long) ?: 0,
            updatedAt = (value["updatedAt"] as? Long) ?: 0
        )
    }
}