//FirebaseConfig.kt
package com.vicpoo.shopy.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object FirebaseConfig {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // Referencias a nodos de la base de datos
    val usersRef = database.getReference("users")
    val productsRef = database.getReference("products")
    val notificationsRef = database.getReference("notifications")
}