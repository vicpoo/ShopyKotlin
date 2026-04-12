// data/repository/FirebaseClothRepository.kt
package com.vicpoo.shopy.data.repository

import android.util.Log
import com.google.firebase.database.*
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.repository.ClothRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClothRepository @Inject constructor(
    private val database: FirebaseDatabase
) : ClothRepository {

    private val productsRef = database.getReference("products")
    private val notificationsRef = database.getReference("notifications")

    companion object {
        private const val TAG = "FirebaseClothRepo"
    }

    override suspend fun getAllClothes(): List<Cloth> {
        return try {
            val snapshot = productsRef.get().await()
            snapshot.children.mapNotNull { it.toCloth() }
        } catch (e: Exception) {
            throw Exception("Error al obtener prendas: ${e.message}")
        }
    }

    override suspend fun getClothById(id: String): Cloth {
        return try {
            val snapshot = productsRef.child(id).get().await()
            snapshot.toCloth() ?: throw Exception("Prenda no encontrada")
        } catch (e: Exception) {
            throw Exception("Error al obtener prenda: ${e.message}")
        }
    }

    override fun observeAllClothes(): Flow<List<Cloth>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clothes = snapshot.children.mapNotNull { it.toCloth() }
                Log.d(TAG, "🔥 observeAllClothes: ${clothes.size} productos")
                trySend(clothes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "observeAllClothes cancelado: ${error.message}")
                close(error.toException())
            }
        }

        Log.d(TAG, "📡 Registrando ValueEventListener (todos los productos)")
        productsRef.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "📡 Removiendo ValueEventListener (todos los productos)")
            productsRef.removeEventListener(listener)
        }
    }

    override fun observeClothesBySeller(sellerId: String): Flow<List<Cloth>> = callbackFlow {
        val query = productsRef.orderByChild("sellerId").equalTo(sellerId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clothes = snapshot.children.mapNotNull { it.toCloth() }
                Log.d(TAG, "👤 observeClothesBySeller $sellerId: ${clothes.size} productos")
                trySend(clothes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "observeClothesBySeller cancelado: ${error.message}")
                trySend(emptyList())
            }
        }

        query.addValueEventListener(listener)
        awaitClose {
            try { query.removeEventListener(listener) } catch (_: Exception) { }
        }
    }

    override suspend fun createCloth(cloth: Cloth, imageFile: File?): Cloth {
        return try {
            Log.d(TAG, "Creando producto para seller: ${cloth.sellerId}")

            var base64Image: String? = null
            if (imageFile != null && imageFile.exists()) {
                base64Image = Base64ImageUtils.fileToBase64(imageFile)
                base64Image?.let {
                    Log.d(TAG, "Imagen Base64. Tamaño: ${Base64ImageUtils.getBase64SizeInKB(it)} KB")
                }
            }

            val newProductRef = productsRef.push()
            val productId = newProductRef.key ?: throw Exception("Error al generar ID")

            val clothMap = HashMap<String, Any>().apply {
                put("name", cloth.name)
                put("sellerId", cloth.sellerId)
                put("createdAt", System.currentTimeMillis())
                put("updatedAt", System.currentTimeMillis())
                put("averageRating", 0.0)
                put("totalReviews", 0)
                cloth.description?.let { put("description", it) }
                cloth.size?.let { put("size", it) }
                cloth.price?.let { put("price", it) }
                cloth.stock?.let { put("stock", it) }
                val img = base64Image ?: cloth.image
                if (img != null) put("image", img)
            }

            newProductRef.setValue(clothMap).await()
            Log.d(TAG, "Producto creado: $productId")

            try {
                val notifMap = HashMap<String, Any>().apply {
                    put("title", "Nuevo producto disponible")
                    put("message", "Se agregó '${cloth.name}' a la tienda")
                    put("productId", productId)
                    put("timestamp", System.currentTimeMillis())
                    put("read", false)
                }
                notificationsRef.push().setValue(notifMap).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear notificación", e)
            }

            cloth.copy(id = productId, image = base64Image ?: cloth.image)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear prenda", e)
            throw Exception("Error al crear prenda: ${e.message}")
        }
    }

    override suspend fun updateCloth(id: String, cloth: Cloth, imageFile: File?): Cloth {
        return try {
            var base64Image: String? = cloth.image
            if (imageFile != null && imageFile.exists()) {
                base64Image = Base64ImageUtils.fileToBase64(imageFile)
                base64Image?.let {
                    Log.d(TAG, "Nueva imagen Base64. Tamaño: ${Base64ImageUtils.getBase64SizeInKB(it)} KB")
                }
            }

            val updates = HashMap<String, Any>().apply {
                put("name", cloth.name)
                put("updatedAt", System.currentTimeMillis())
                cloth.description?.let { put("description", it) }
                cloth.size?.let { put("size", it) }
                cloth.price?.let { put("price", it) }
                cloth.stock?.let { put("stock", it) }
                if (base64Image != null) put("image", base64Image)
            }

            productsRef.child(id).updateChildren(updates).await()
            Log.d(TAG, "Producto actualizado: $id")
            cloth.copy(id = id, image = base64Image ?: cloth.image)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar prenda", e)
            throw Exception("Error al actualizar prenda: ${e.message}")
        }
    }

    override suspend fun deleteCloth(id: String): Boolean {
        return try {
            productsRef.child(id).removeValue().await()
            Log.d(TAG, "Producto eliminado: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar producto", e)
            false
        }
    }

    override suspend fun getClothesBySeller(sellerId: String): List<Cloth> {
        return try {
            val snapshot = productsRef.orderByChild("sellerId").equalTo(sellerId).get().await()
            snapshot.children.mapNotNull { it.toCloth() }
        } catch (e: Exception) {
            throw Exception("Error al obtener prendas del vendedor: ${e.message}")
        }
    }

    override suspend fun searchByName(name: String): List<Cloth> {
        return getAllClothes().filter {
            it.name.contains(name, ignoreCase = true) ||
                    (it.description?.contains(name, ignoreCase = true) == true)
        }
    }

    override suspend fun searchBySize(size: String): List<Cloth> {
        return getAllClothes().filter { cloth ->
            cloth.size?.split(",")?.any { it.trim() == size } == true
        }
    }

    override suspend fun searchByPriceRange(minPrice: Double, maxPrice: Double): List<Cloth> {
        return try {
            val snapshot = productsRef.orderByChild("price").get().await()
            snapshot.children.mapNotNull { it.toCloth() }
                .filter { it.price?.let { p -> p in minPrice..maxPrice } == true }
        } catch (e: Exception) {
            throw Exception("Error al buscar por precio: ${e.message}")
        }
    }

    private fun DataSnapshot.toCloth(): Cloth? {
        val id = key ?: return null
        val map = value as? Map<*, *> ?: return null

        return try {
            Cloth(
                id = id,
                name = map["name"] as? String ?: return null,
                description = map["description"] as? String,
                size = map["size"] as? String,
                price = (map["price"] as? Number)?.toDouble(),
                stock = (map["stock"] as? Number)?.toInt(),
                image = map["image"] as? String,
                sellerId = map["sellerId"] as? String ?: "",
                createdAt = (map["createdAt"] as? Long) ?: 0L,
                updatedAt = (map["updatedAt"] as? Long) ?: 0L,
                averageRating = (map["averageRating"] as? Number)?.toDouble() ?: 0.0,
                totalReviews = (map["totalReviews"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapeando producto $id", e)
            null
        }
    }
}