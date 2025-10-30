package com.erensaridag.careermatch.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class Application(
    val id: String,
    val studentId: String,
    val internshipId: String,
    val internshipTitle: String,
    val companyName: String,
    val status: String, // "pending", "accepted", "rejected"
    val appliedAt: Long
)

class ApplicationManager {
    private val firestore = FirebaseFirestore.getInstance()

    // Staja başvur
    suspend fun applyToInternship(
        studentId: String,
        internshipId: String,
        internshipTitle: String,
        companyName: String
    ): Result<String> {
        return try {
            // Daha önce başvuru yapılmış mı kontrol et
            val existingApplication = firestore.collection("applications")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("internshipId", internshipId)
                .get()
                .await()

            if (!existingApplication.isEmpty) {
                return Result.failure(Exception("Already applied to this internship"))
            }

            // Yeni başvuru oluştur
            val applicationData = hashMapOf(
                "studentId" to studentId,
                "internshipId" to internshipId,
                "internshipTitle" to internshipTitle,
                "companyName" to companyName,
                "status" to "pending",
                "appliedAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("applications")
                .add(applicationData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Öğrencinin başvurularını getir
    suspend fun getStudentApplications(studentId: String): Result<List<Application>> {
        return try {
            val snapshot = firestore.collection("applications")
                .whereEqualTo("studentId", studentId)
                .orderBy("appliedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val applications = snapshot.documents.mapNotNull { doc ->
                try {
                    Application(
                        id = doc.id,
                        studentId = doc.getString("studentId") ?: "",
                        internshipId = doc.getString("internshipId") ?: "",
                        internshipTitle = doc.getString("internshipTitle") ?: "",
                        companyName = doc.getString("companyName") ?: "",
                        status = doc.getString("status") ?: "pending",
                        appliedAt = doc.getLong("appliedAt") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(applications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staja yapılan başvuruları getir
    suspend fun getInternshipApplications(internshipId: String): Result<List<Application>> {
        return try {
            val snapshot = firestore.collection("applications")
                .whereEqualTo("internshipId", internshipId)
                .orderBy("appliedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val applications = snapshot.documents.mapNotNull { doc ->
                try {
                    Application(
                        id = doc.id,
                        studentId = doc.getString("studentId") ?: "",
                        internshipId = doc.getString("internshipId") ?: "",
                        internshipTitle = doc.getString("internshipTitle") ?: "",
                        companyName = doc.getString("companyName") ?: "",
                        status = doc.getString("status") ?: "pending",
                        appliedAt = doc.getLong("appliedAt") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(applications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Başvuru durumunu güncelle
    suspend fun updateApplicationStatus(
        applicationId: String,
        status: String
    ): Result<Unit> {
        return try {
            firestore.collection("applications")
                .document(applicationId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Başvuru sayısını al
    suspend fun getApplicationCount(studentId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("applications")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bekleyen başvuru sayısını al
    suspend fun getPendingApplicationsCount(companyId: String): Result<Int> {
        return try {
            // Önce şirkete ait stajları bul
            val internshipsSnapshot = firestore.collection("internships")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()

            val internshipIds = internshipsSnapshot.documents.map { it.id }

            if (internshipIds.isEmpty()) {
                return Result.success(0)
            }

            // Bu stajlara yapılan bekleyen başvuruları say
            var totalPending = 0
            for (internshipId in internshipIds) {
                val applicationsSnapshot = firestore.collection("applications")
                    .whereEqualTo("internshipId", internshipId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                totalPending += applicationsSnapshot.size()
            }

            Result.success(totalPending)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}