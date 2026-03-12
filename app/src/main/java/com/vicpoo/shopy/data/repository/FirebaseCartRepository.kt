package com.vicpoo.shopy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vicpoo.shopy.domain.model.CartItem
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.repository.CartRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : CartRepository {

    private val usersRef = database.getReference("users")
    private val productsRef = database.getReference("products")

    private val currentUserCartRef
        get() = auth.currentUser?.uid?.let { uid ->
            usersRef.child(uid).child("cart")
        }

    override fun getCartItems(): Flow<List<CartItem>> = callbackFlow {
        val cartRef = try {
            currentUserCartRef
        } catch (e: Exception) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        if (cartRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cartListener = object : ValueEventListener {
            override fun onDataChange(cartSnapshot: DataSnapshot) {
                val cartMap = cartSnapshot.value as? Map<String, Any> ?: emptyMap()
                val items = mutableListOf<CartItem>()

                if (cartMap.isEmpty()) {
                    trySend(emptyList()).isSuccess
                    return
                }

                var processedCount = 0
                cartMap.forEach { (productId, cartEntry) ->
                    val quantity = when (cartEntry) {
                        is Long -> cartEntry.toInt()
                        is Int -> cartEntry
                        else -> 1
                    }

                    productsRef.child(productId).get().addOnSuccessListener { productSnapshot ->
                        val cloth = productSnapshot.toCloth()
                        if (cloth != null) {
                            items.add(
                                CartItem(
                                    productId = cloth.id,
                                    name = cloth.name,
                                    price = cloth.price ?: 0.0,
                                    image = cloth.image,
                                    quantity = quantity
                                )
                            )
                        }
                        processedCount++
                        if (processedCount == cartMap.size) {
                            trySend(items).isSuccess
                        }
                    }.addOnFailureListener {
                        processedCount++
                        if (processedCount == cartMap.size) {
                            trySend(items).isSuccess
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList()).isSuccess
            }
        }

        cartRef.addValueEventListener(cartListener)
        awaitClose {
            try {
                cartRef.removeEventListener(cartListener)
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun addToCart(productId: String, quantity: Int, selectedSize: String?) {
        val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
        try {
            val snapshot = cartRef.child(productId).get().await()
            val currentQuantity = if (snapshot.exists()) (snapshot.value as Long).toInt() else 0
            cartRef.child(productId).setValue(currentQuantity + quantity).await()
        } catch (e: Exception) {
            throw Exception("Error al agregar al carrito: ${e.message}")
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
        try {
            cartRef.child(productId).removeValue().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar del carrito: ${e.message}")
        }
    }

    override suspend fun updateQuantity(productId: String, newQuantity: Int) {
        val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
        try {
            if (newQuantity <= 0) {
                removeFromCart(productId)
            } else {
                cartRef.child(productId).setValue(newQuantity).await()
            }
        } catch (e: Exception) {
            throw Exception("Error al actualizar cantidad: ${e.message}")
        }
    }

    override suspend fun clearCart() {
        val cartRef = currentUserCartRef ?: throw Exception("Usuario no autenticado")
        try {
            cartRef.removeValue().await()
        } catch (e: Exception) {
            throw Exception("Error al vaciar el carrito: ${e.message}")
        }
    }

    private fun DataSnapshot.toCloth(): Cloth? {
        val id = key ?: return null
        val value = value as? Map<String, Any> ?: return null

        return Cloth(
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