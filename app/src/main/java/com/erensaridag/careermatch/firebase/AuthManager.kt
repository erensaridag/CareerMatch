package com.erensaridag.careermatch.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Register - Şifre Firestore'da da sakla
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        userType: String
    ): Result<String> {
        return try {
            android.util.Log.d("AuthManager", "Registering user: $email as $userType")

            // Firebase Auth'da user oluştur
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("User ID not found"))

            // Firestore'da user dökümanı oluştur (şifreyi de sakla)
            val userData = hashMapOf(
                "uid" to userId,
                "email" to email,
                "password" to password,  //  ŞİFRE BURAYA KAYDEDILIYOR
                "name" to name,
                "userType" to userType,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            android.util.Log.d("AuthManager", "User registered successfully: $userId")
            Result.success(userId)
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Registration failed", e)
            Result.failure(e)
        }
    }

    // Login
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            android.util.Log.d("AuthManager", "Signing in user: $email")

            auth.signInWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User ID not found"))

            android.util.Log.d("AuthManager", "User signed in successfully: $userId")
            Result.success(userId)
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Sign in failed", e)
            Result.failure(e)
        }
    }

    // Logout
    fun signOut() {
        auth.signOut()
        android.util.Log.d("AuthManager", "User signed out")
    }

    // Şifre sıfırlama
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Giriş yapan user'ın türünü al
    suspend fun getCurrentUserType(): String? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("userType")
        } catch (e: Exception) {
            null
        }
    }

    // User'ın giriş yapıp yapmadığını kontrol et
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Tüm users'ları getir (Debug için)
    suspend fun getAllUsers(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.data?.apply {
                    put("id", doc.id)
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mevcut kullanıcıyı al
    fun getCurrentUser() = auth.currentUser

    // Kullanıcı verilerini getir
    suspend fun getUserData(userId: String): Result<Map<String, Any>> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                Result.success(doc.data ?: emptyMap())
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcı verilerini güncelle
    suspend fun updateUserData(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
