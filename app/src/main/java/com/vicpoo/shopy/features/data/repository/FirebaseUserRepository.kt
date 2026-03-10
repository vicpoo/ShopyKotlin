//FirebaseUserRepository.kt
package com.vicpoo.shopy.features.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vicpoo.shopy.core.firebase.FirebaseConfig
import com.vicpoo.shopy.features.domain.model.*
import com.vicpoo.shopy.features.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

class FirebaseUserRepository : UserRepository {

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return try {
            // 1. Crear usuario en Firebase Auth
            val authResult = FirebaseConfig.auth
                .createUserWithEmailAndPassword(request.email, request.password)
                .await()

            val firebaseUser = authResult.user ?: throw Exception("Error al crear usuario")

            // 2. Actualizar perfil con nombre completo
            request.name?.let { name ->
                if (name.isNotBlank()) {
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = name
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                }
            }

            // 3. Guardar datos en Realtime Database - CORREGIDO: Especificar tipos correctamente
            val userMap = HashMap<String, Any>()
            userMap["email"] = request.email
            userMap["role"] = "user" // SIEMPRE user por defecto
            userMap["cart"] = emptyMap<String, Any>()

            if (!request.name.isNullOrBlank()) {
                userMap["name"] = request.name!!
            }

            FirebaseConfig.usersRef
                .child(firebaseUser.uid)
                .setValue(userMap)
                .await()

            // Pequeña pausa para asegurar que la base de datos se actualice
            delay(500)

            AuthResponse(
                uid = firebaseUser.uid,
                email = request.email,
                name = request.name,
                role = "user" // SIEMPRE user por defecto
            )
        } catch (e: Exception) {
            throw Exception("Error al registrar: ${e.message}")
        }
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        return try {
            val authResult = FirebaseConfig.auth
                .signInWithEmailAndPassword(request.email, request.password)
                .await()

            val firebaseUser = authResult.user ?: throw Exception("Credenciales inválidas")

            // Obtener datos de Realtime Database
            val userSnapshot = try {
                FirebaseConfig.usersRef
                    .child(firebaseUser.uid)
                    .get()
                    .await()
            } catch (e: Exception) {
                null
            }

            // Si no existe, crear el usuario en la base de datos
            if (userSnapshot == null || !userSnapshot.exists()) {
                val userMap = HashMap<String, Any>()
                userMap["email"] = firebaseUser.email ?: request.email
                userMap["role"] = "user" // SIEMPRE user por defecto
                userMap["cart"] = emptyMap<String, Any>()

                firebaseUser.displayName?.let { displayName ->
                    if (displayName.isNotBlank()) {
                        userMap["name"] = displayName
                    }
                }

                FirebaseConfig.usersRef
                    .child(firebaseUser.uid)
                    .setValue(userMap)
                    .await()

                // Pequeña pausa para asegurar que la base de datos se actualice
                delay(500)
            }

            // Obtener los datos actualizados
            val finalSnapshot = FirebaseConfig.usersRef
                .child(firebaseUser.uid)
                .get()
                .await()

            val userData = finalSnapshot.value as? Map<String, Any> ?: emptyMap()

            val name = (userData["name"] as? String)?.takeIf { it.isNotBlank() }
                ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: ""

            val role = (userData["role"] as? String)?.takeIf { it.isNotBlank() } ?: "user"

            AuthResponse(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: request.email,
                name = name,
                role = role
            )
        } catch (e: Exception) {
            throw Exception("Credenciales inválidas")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResponse {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseConfig.auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al iniciar sesión con Google")

            // Verificar si el usuario ya existe en Realtime Database
            val userSnapshot = try {
                FirebaseConfig.usersRef.child(firebaseUser.uid).get().await()
            } catch (e: Exception) {
                null
            }

            var userData: Map<String, Any>? = null

            if (userSnapshot == null || !userSnapshot.exists()) {
                // Crear nuevo usuario SIEMPRE con role "user" - CORREGIDO: Especificar tipos
                val userMap = HashMap<String, Any>()
                userMap["email"] = firebaseUser.email ?: ""
                userMap["role"] = "user" // ¡FORZAMOS role "user" explícitamente!
                userMap["cart"] = emptyMap<String, Any>()

                firebaseUser.displayName?.let { displayName ->
                    if (displayName.isNotBlank()) {
                        userMap["name"] = displayName
                    }
                }

                // IMPORTANTE: Esperar a que se complete la escritura
                FirebaseConfig.usersRef
                    .child(firebaseUser.uid)
                    .setValue(userMap)
                    .await()

                // Pequeña pausa para asegurar que la base de datos se actualice
                delay(500)

                // Usar el mapa que acabamos de crear
                userData = userMap
            } else {
                // Usuario ya existe, obtener sus datos
                userData = userSnapshot.value as? Map<String, Any>
            }

            // Asegurarnos de que el role sea "user" si no existe
            val name = (userData?.get("name") as? String)?.takeIf { it.isNotBlank() }
                ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: ""

            val role = (userData?.get("role") as? String)?.takeIf { it.isNotBlank() } ?: "user"

            // Si por alguna razón el role no es "user" en la BD, lo actualizamos
            if (role != "user") {
                val updates = HashMap<String, Any>()
                updates["role"] = "user"
                FirebaseConfig.usersRef
                    .child(firebaseUser.uid)
                    .updateChildren(updates)
                    .await()
            }

            AuthResponse(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = name,
                role = "user" // Forzamos "user" en la respuesta
            )
        } catch (e: Exception) {
            throw Exception("Error al iniciar sesión con Google: ${e.message}")
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val currentUser = FirebaseConfig.auth.currentUser
                ?: throw Exception("No autenticado")

            val currentUserSnapshot = FirebaseConfig.usersRef
                .child(currentUser.uid)
                .get()
                .await()

            val role = currentUserSnapshot.child("role").value as? String

            if (role != "admin") {
                throw Exception("No autorizado")
            }

            val snapshot = FirebaseConfig.usersRef.get().await()

            snapshot.children.mapNotNull { dataSnapshot ->
                val userData = dataSnapshot.value as? Map<String, Any> ?: return@mapNotNull null
                val uid = dataSnapshot.key ?: return@mapNotNull null

                User(
                    uid = uid,
                    email = userData["email"] as? String ?: "",
                    name = userData["name"] as? String,
                    role = userData["role"] as? String ?: "user",
                    cart = userData["cart"] as? Map<String, Any> ?: emptyMap()
                )
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener usuarios: ${e.message}")
        }
    }

    override suspend fun getUserById(id: String): User {
        return try {
            val snapshot = FirebaseConfig.usersRef.child(id).get().await()

            if (!snapshot.exists()) {
                throw Exception("Usuario no encontrado")
            }

            val userData = snapshot.value as? Map<String, Any>
                ?: throw Exception("Datos de usuario inválidos")

            User(
                uid = id,
                email = userData["email"] as? String ?: "",
                name = userData["name"] as? String,
                role = userData["role"] as? String ?: "user",
                cart = userData["cart"] as? Map<String, Any> ?: emptyMap()
            )
        } catch (e: Exception) {
            throw Exception("Error al obtener usuario: ${e.message}")
        }
    }

    override suspend fun createUser(user: User): User {
        return try {
            val currentUser = FirebaseConfig.auth.currentUser
                ?: throw Exception("No autenticado")

            if (currentUser.uid != user.uid) {
                val currentUserSnapshot = FirebaseConfig.usersRef
                    .child(currentUser.uid)
                    .get()
                    .await()

                val role = currentUserSnapshot.child("role").value as? String

                if (role != "admin") {
                    throw Exception("No autorizado para crear otros usuarios")
                }
            }

            val userMap = HashMap<String, Any>()
            userMap["email"] = user.email
            userMap["name"] = user.name ?: ""
            userMap["role"] = user.role
            userMap["cart"] = user.cart

            FirebaseConfig.usersRef.child(user.uid).setValue(userMap).await()
            user
        } catch (e: Exception) {
            throw Exception("Error al crear usuario: ${e.message}")
        }
    }

    override suspend fun updateUser(user: User): User {
        return try {
            val currentUser = FirebaseConfig.auth.currentUser
                ?: throw Exception("No autenticado")

            if (currentUser.uid != user.uid) {
                val currentUserSnapshot = FirebaseConfig.usersRef
                    .child(currentUser.uid)
                    .get()
                    .await()

                val role = currentUserSnapshot.child("role").value as? String

                if (role != "admin") {
                    throw Exception("No autorizado para actualizar otros usuarios")
                }
            }

            val updates = HashMap<String, Any>()

            if (!user.name.isNullOrBlank()) {
                updates["name"] = user.name!!
            }

            if (user.email.isNotBlank()) {
                updates["email"] = user.email
            }

            // Permite actualizar el rol si es necesario
            if (user.role.isNotBlank() && user.role != "user") {
                updates["role"] = user.role
            }

            if (updates.isNotEmpty()) {
                FirebaseConfig.usersRef
                    .child(user.uid)
                    .updateChildren(updates)
                    .await()
            }

            user
        } catch (e: Exception) {
            throw Exception("Error al actualizar usuario: ${e.message}")
        }
    }

    override suspend fun deleteUser(id: String): Boolean {
        return try {
            val currentUser = FirebaseConfig.auth.currentUser ?: return false

            if (currentUser.uid != id) {
                val currentUserSnapshot = FirebaseConfig.usersRef
                    .child(currentUser.uid)
                    .get()
                    .await()

                val role = currentUserSnapshot.child("role").value as? String

                if (role != "admin") {
                    return false
                }
            }

            FirebaseConfig.usersRef.child(id).removeValue().await()

            if (currentUser.uid == id) {
                currentUser.delete().await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        var valueEventListener: ValueEventListener? = null
        var isActive = true
        var isFirstEmission = true

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            if (!isActive) return@AuthStateListener

            val firebaseUser = auth.currentUser

            if (firebaseUser != null) {
                val userRef = FirebaseConfig.usersRef.child(firebaseUser.uid)

                valueEventListener?.let { userRef.removeEventListener(it) }

                valueEventListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!isActive) return

                        if (snapshot.exists()) {
                            val userData = snapshot.value as? Map<String, Any>

                            val user = User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = (userData?.get("name") as? String)?.takeIf { it.isNotBlank() }
                                    ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                                    ?: "",
                                role = (userData?.get("role") as? String)?.takeIf { it.isNotBlank() } ?: "user",
                                cart = userData?.get("cart") as? Map<String, Any> ?: emptyMap()
                            )

                            trySend(user).isSuccess
                        } else if (!isFirstEmission) {
                            // Si no existe pero no es la primera emisión, enviamos null
                            trySend(null).isSuccess
                        }
                        // Si es la primera emisión y no existe, esperamos a que se cree
                        isFirstEmission = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (isActive && !isFirstEmission) {
                            trySend(null).isSuccess
                        }
                    }
                }

                userRef.addValueEventListener(valueEventListener!!)
            } else {
                trySend(null).isSuccess
            }
        }

        FirebaseConfig.auth.addAuthStateListener(authListener)

        awaitClose {
            isActive = false
            FirebaseConfig.auth.removeAuthStateListener(authListener)

            val firebaseUser = FirebaseConfig.auth.currentUser
            if (firebaseUser != null && valueEventListener != null) {
                FirebaseConfig.usersRef
                    .child(firebaseUser.uid)
                    .removeEventListener(valueEventListener!!)
            }
        }
    }

    override suspend fun logout() {
        try {
            FirebaseConfig.auth.signOut()
        } catch (e: Exception) {
            // Ignorar error
        }
    }
}