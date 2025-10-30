package com.erensaridag.careermatch.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Mevcut kullanıcıyı al
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Kullanıcı giriş yapmış mı?
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Email ve şifre ile kayıt ol
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        userType: String
    ): Result<String> {
        return try {
            // Firebase Authentication ile kullanıcı oluştur
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID not found"))

            // Firestore'a kullanıcı bilgilerini kaydet
            val userData = hashMapOf(
                "uid" to userId,
                "email" to email,
                "name" to name,
                "userType" to userType,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Email ve şifre ile giriş yap
    suspend fun signIn(email: String, password: String): Result<Pair<String, String>> {
        return try {
            // Firebase Authentication ile giriş yap
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID not found"))

            // Firestore'dan kullanıcı tipini al
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val userType = userDoc.getString("userType") ?: "Student"

            Result.success(Pair(userId, userType))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Çıkış yap
    fun signOut() {
        auth.signOut()
    }

    // Şifre sıfırlama emaili gönder
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcı bilgilerini al
    suspend fun getUserData(userId: String): Result<Map<String, Any>> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                Result.success(document.data ?: emptyMap())
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcı bilgilerini güncelle
    suspend fun updateUserData(userId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}