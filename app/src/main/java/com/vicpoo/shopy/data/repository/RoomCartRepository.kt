// data/repository/RoomCartRepository.kt
package com.vicpoo.shopy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vicpoo.shopy.data.local.dao.CartDao
import com.vicpoo.shopy.data.local.entity.CartItemEntity
import com.vicpoo.shopy.domain.model.CartItem
import com.vicpoo.shopy.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.NonCancellable.key
import android.R.attr.value
import com.google.firebase.database.DataSnapshot


@Singleton
class RoomCartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val cartDao: CartDao
) : CartRepository {

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
            // 1. Obtener información del producto
            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.toCloth()

            if (product != null) {
                // 2. Guardar en Room local
                val cartItem = CartItemEntity(
                    productId = productId,
                    name = product.name,
                    price = product.price ?: 0.0,
                    image = product.image,
                    quantity = quantity,
                    selectedSize = selectedSize
                )
                cartDao.insertOrUpdate(cartItem)
            }

            // 3. Sincronizar con Firebase (si hay conexión)
            try {
                val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
                val snapshot = cartRef.child(productId).get().await()
                val currentQuantity = if (snapshot.exists()) (snapshot.value as Long).toInt() else 0
                cartRef.child(productId).setValue(currentQuantity + quantity).await()
            } catch (e: Exception) {
                // Si falla Firebase, la operación ya está guardada localmente
                android.util.Log.e("RoomCartRepo", "Error sincronizando con Firebase", e)
            }
        } catch (e: Exception) {
            throw Exception("Error al agregar al carrito: ${e.message}")
        }
    }

    override suspend fun removeFromCart(productId: String) {
        try {
            // 1. Eliminar de Room
            cartDao.deleteById(productId)

            // 2. Sincronizar con Firebase
            try {
                val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
                cartRef.child(productId).removeValue().await()
            } catch (e: Exception) {
                android.util.Log.e("RoomCartRepo", "Error sincronizando con Firebase", e)
            }
        } catch (e: Exception) {
            throw Exception("Error al eliminar del carrito: ${e.message}")
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

            // 2. Sincronizar con Firebase
            try {
                val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
                cartRef.child(productId).setValue(newQuantity).await()
            } catch (e: Exception) {
                android.util.Log.e("RoomCartRepo", "Error sincronizando con Firebase", e)
            }
        } catch (e: Exception) {
            throw Exception("Error al actualizar cantidad: ${e.message}")
        }
    }

    override suspend fun clearCart() {
        try {
            // 1. Limpiar Room
            cartDao.clearCart()

            // 2. Sincronizar con Firebase
            try {
                val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
                cartRef.removeValue().await()
            } catch (e: Exception) {
                android.util.Log.e("RoomCartRepo", "Error sincronizando con Firebase", e)
            }
        } catch (e: Exception) {
            throw Exception("Error al vaciar el carrito: ${e.message}")
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