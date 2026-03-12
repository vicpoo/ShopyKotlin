// FirebaseClothRepository.kt
package com.vicpoo.shopy.features.data.repository

import android.util.Base64
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vicpoo.shopy.core.firebase.FirebaseConfig
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.core.utils.NotificationHelper
import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.domain.model.Notification
import com.vicpoo.shopy.features.domain.repository.ClothRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseClothRepository : ClothRepository {

    override suspend fun getAllClothes(): List<Cloth> {
        return try {
            val snapshot = FirebaseConfig.productsRef.get().await()
            snapshot.children.mapNotNull { it.toCloth() }
        } catch (e: Exception) {
            throw Exception("Error al obtener prendas: ${e.message}")
        }
    }

    override suspend fun getClothById(id: String): Cloth {
        return try {
            val snapshot = FirebaseConfig.productsRef.child(id).get().await()
            snapshot.toCloth() ?: throw Exception("Prenda no encontrada")
        } catch (e: Exception) {
            throw Exception("Error al obtener prenda: ${e.message}")
        }
    }

    override suspend fun createCloth(cloth: Cloth, imageFile: File?): Cloth {
        return try {
            android.util.Log.d("FirebaseClothRepo", "Intentando crear producto para seller: ${cloth.sellerId}")

            var base64Image: String? = null
            if (imageFile != null && imageFile.exists()) {
                base64Image = Base64ImageUtils.fileToBase64(imageFile)
                if (base64Image != null) {
                    val sizeKB = Base64ImageUtils.getBase64SizeInKB(base64Image)
                    android.util.Log.d("FirebaseClothRepo", "Imagen convertida a Base64. Tamaño: $sizeKB KB")
                }
            }

            val newProductRef = FirebaseConfig.productsRef.push()
            val productId = newProductRef.key ?: throw Exception("Error al generar ID")

            val clothMap = HashMap<String, Any>().apply {
                put("name", cloth.name)
                put("sellerId", cloth.sellerId)
                put("createdAt", System.currentTimeMillis())
                put("updatedAt", System.currentTimeMillis())

                cloth.description?.let { put("description", it) }
                cloth.size?.let { put("size", it) }
                cloth.price?.let { put("price", it) }
                cloth.stock?.let { put("stock", it) }

                if (base64Image != null) {
                    put("image", base64Image)
                } else {
                    cloth.image?.let { put("image", it) }
                }
            }

            android.util.Log.d("FirebaseClothRepo", "Guardando producto: $clothMap")

            newProductRef.setValue(clothMap).await()

            android.util.Log.d("FirebaseClothRepo", "Producto creado exitosamente con ID: $productId")

            try {
                val notificationRef = FirebaseConfig.notificationsRef.push()
                val notificationMap = HashMap<String, Any>().apply {
                    put("title", "Nuevo producto disponible")
                    put("message", "Se agregó '${cloth.name}' a la tienda")
                    put("productId", productId)
                    put("timestamp", System.currentTimeMillis())
                    put("read", false)
                }
                notificationRef.setValue(notificationMap).await()
                android.util.Log.d("FirebaseClothRepo", "Notificación creada exitosamente en DB")

                android.util.Log.d("FirebaseClothRepo", "Notificación enviada a todos los usuarios")

            } catch (e: Exception) {
                android.util.Log.e("FirebaseClothRepo", "Error al crear notificación", e)
            }

            cloth.copy(
                id = productId,
                image = base64Image ?: cloth.image
            )
        } catch (e: Exception) {
            android.util.Log.e("FirebaseClothRepo", "Error al crear prenda", e)
            throw Exception("Error al crear prenda: ${e.message}")
        }
    }

    override suspend fun updateCloth(id: String, cloth: Cloth, imageFile: File?): Cloth {
        return try {
            android.util.Log.d("FirebaseClothRepo", "Actualizando producto $id")

            var base64Image: String? = cloth.image
            if (imageFile != null && imageFile.exists()) {
                base64Image = Base64ImageUtils.fileToBase64(imageFile)
                if (base64Image != null) {
                    val sizeKB = Base64ImageUtils.getBase64SizeInKB(base64Image)
                    android.util.Log.d("FirebaseClothRepo", "Nueva imagen convertida a Base64. Tamaño: $sizeKB KB")
                }
            }

            val updates = HashMap<String, Any>().apply {
                put("name", cloth.name)
                put("updatedAt", System.currentTimeMillis())

                cloth.description?.let { put("description", it) }
                cloth.size?.let { put("size", it) }
                cloth.price?.let { put("price", it) }
                cloth.stock?.let { put("stock", it) }

                if (base64Image != null) {
                    put("image", base64Image)
                }
            }

            FirebaseConfig.productsRef.child(id).updateChildren(updates).await()

            android.util.Log.d("FirebaseClothRepo", "Producto actualizado exitosamente")

            cloth.copy(id = id, image = base64Image ?: cloth.image)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseClothRepo", "Error al actualizar prenda", e)
            throw Exception("Error al actualizar prenda: ${e.message}")
        }
    }

    override suspend fun deleteCloth(id: String): Boolean {
        return try {
            android.util.Log.d("FirebaseClothRepo", "Eliminando producto $id")
            FirebaseConfig.productsRef.child(id).removeValue().await()
            android.util.Log.d("FirebaseClothRepo", "Producto eliminado exitosamente")
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseClothRepo", "Error al eliminar producto", e)
            false
        }
    }

    override suspend fun getClothesBySeller(sellerId: String): List<Cloth> {
        return try {
            val snapshot = FirebaseConfig.productsRef
                .orderByChild("sellerId")
                .equalTo(sellerId)
                .get()
                .await()

            snapshot.children.mapNotNull { it.toCloth() }
        } catch (e: Exception) {
            throw Exception("Error al obtener prendas del vendedor: ${e.message}")
        }
    }

    override fun observeClothesBySeller(sellerId: String): Flow<List<Cloth>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clothes = snapshot.children.mapNotNull { it.toCloth() }
                trySend(clothes).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val query = FirebaseConfig.productsRef
            .orderByChild("sellerId")
            .equalTo(sellerId)

        query.addValueEventListener(listener)

        awaitClose { query.removeEventListener(listener) }
    }

    override suspend fun searchByName(name: String): List<Cloth> {
        return try {
            val allClothes = getAllClothes()
            allClothes.filter {
                it.name.contains(name, ignoreCase = true) ||
                        (it.description?.contains(name, ignoreCase = true) == true)
            }
        } catch (e: Exception) {
            throw Exception("Error al buscar prendas: ${e.message}")
        }
    }

    override suspend fun searchBySize(size: String): List<Cloth> {
        return try {
            val allClothes = getAllClothes()
            allClothes.filter { cloth ->
                cloth.size?.split(",")?.any { it.trim() == size } == true
            }
        } catch (e: Exception) {
            throw Exception("Error al buscar por talla: ${e.message}")
        }
    }

    override suspend fun searchByPriceRange(minPrice: Double, maxPrice: Double): List<Cloth> {
        return try {
            val snapshot = FirebaseConfig.productsRef
                .orderByChild("price")
                .get()
                .await()

            snapshot.children
                .mapNotNull { it.toCloth() }
                .filter { cloth ->
                    cloth.price?.let { it in minPrice..maxPrice } == true
                }
        } catch (e: Exception) {
            throw Exception("Error al buscar por precio: ${e.message}")
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