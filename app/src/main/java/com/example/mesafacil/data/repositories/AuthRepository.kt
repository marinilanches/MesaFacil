package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val usersCollection = firestore.collection("usuarios") // 🔥 FIXO

    suspend fun login(email: String, password: String): Result<User> = try {

        auth.signInWithEmailAndPassword(email, password).await()

        val userId = auth.currentUser?.uid
            ?: throw Exception("User ID not found")

        val userRef = usersCollection.document(userId)
        val snapshot = userRef.get().await()

        // 🔥 cria usuário se não existir
        if (!snapshot.exists()) {

            val newUser = User(
                id = userId,
                email = email,
                name = "Admin",
                role = "admin",
                isActive = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            userRef.set(newUser).await()
        }

        val userDoc = userRef.get().await()

        val user = userDoc.toObject(User::class.java)
            ?: throw Exception("User not found")

        Result.success(user)

    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun logout(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            val userDoc = usersCollection.document(userId).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun register(email: String, password: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("UID not found")
        Result.success(uid)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}