package com.erensaridag.careermatch.firebase

import com.erensaridag.careermatch.data.Internship
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class InternshipManager {
    private val firestore = FirebaseFirestore.getInstance()

    // Yeni staj ilanı ekle
    suspend fun addInternship(
        title: String,
        company: String,
        location: String,
        duration: String,
        salary: String,
        description: String,
        companyId: String
    ): Result<String> {
        return try {
            android.util.Log.d("InternshipManager", "Adding internship: $title for company: $company")

            val internshipData = hashMapOf(
                "title" to title,
                "company" to company,
                "location" to location,
                "duration" to duration,
                "salary" to salary,
                "description" to description,
                "companyId" to companyId,
                "createdAt" to System.currentTimeMillis(),
                "isActive" to true
            )

            android.util.Log.d("InternshipManager", "Data prepared: $internshipData")

            val docRef = firestore.collection("internships")
                .add(internshipData)
                .await()

            android.util.Log.d("InternshipManager", "Successfully added with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("InternshipManager", "Error adding internship", e)
            Result.failure(e)
        }
    }

    // Tüm aktif stajları getir
    suspend fun getAllInternships(): Result<List<Internship>> {
        return try {
            val snapshot = firestore.collection("internships")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val internships = snapshot.documents.mapNotNull { doc ->
                try {
                    Internship(
                        id = doc.id.hashCode(),
                        title = doc.getString("title") ?: "",
                        company = doc.getString("company") ?: "",
                        location = doc.getString("location") ?: "",
                        duration = doc.getString("duration") ?: "",
                        salary = doc.getString("salary") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(internships)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Şirkete ait stajları getir
    suspend fun getCompanyInternships(companyId: String): Result<List<Internship>> {
        return try {
            val snapshot = firestore.collection("internships")
                .whereEqualTo("companyId", companyId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val internships = snapshot.documents.mapNotNull { doc ->
                try {
                    Internship(
                        id = doc.id.hashCode(),
                        title = doc.getString("title") ?: "",
                        company = doc.getString("company") ?: "",
                        location = doc.getString("location") ?: "",
                        duration = doc.getString("duration") ?: "",
                        salary = doc.getString("salary") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(internships)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staj ilanını sil
    suspend fun deleteInternship(internshipId: String): Result<Unit> {
        return try {
            firestore.collection("internships")
                .document(internshipId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staj ilanını güncelle
    suspend fun updateInternship(
        internshipId: String,
        data: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection("internships")
                .document(internshipId)
                .update(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staj araması yap
    suspend fun searchInternships(query: String): Result<List<Internship>> {
        return try {
            val snapshot = firestore.collection("internships")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val internships = snapshot.documents.mapNotNull { doc ->
                try {
                    val title = doc.getString("title") ?: ""
                    val company = doc.getString("company") ?: ""
                    val location = doc.getString("location") ?: ""

                    // Basit arama filtresi
                    if (title.contains(query, ignoreCase = true) ||
                        company.contains(query, ignoreCase = true) ||
                        location.contains(query, ignoreCase = true)
                    ) {
                        Internship(
                            id = doc.id.hashCode(),
                            title = title,
                            company = company,
                            location = location,
                            duration = doc.getString("duration") ?: "",
                            salary = doc.getString("salary") ?: "",
                            description = doc.getString("description") ?: ""
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(internships)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}