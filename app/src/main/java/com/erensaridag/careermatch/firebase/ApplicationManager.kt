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

            Result.success(applications.sortedByDescending { it.appliedAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staja yapılan başvuruları getir
    suspend fun getInternshipApplications(internshipId: String): Result<List<Application>> {
        return try {
            val snapshot = firestore.collection("applications")
                .whereEqualTo("internshipId", internshipId)
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

    // Başvuruyu sil (reddet)
    suspend fun removeApplication(applicationId: String): Result<Unit> {
        return try {
            firestore.collection("applications")
                .document(applicationId)
                .delete()
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

    // Bir staj ilanına yapılan başvuru sayısını al
    suspend fun getInternshipApplicationCount(internshipId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("applications")
                .whereEqualTo("internshipId", internshipId)
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Şirketin ilanları için toplam başvuru sayısını al
    suspend fun getTotalApplicationsCountForInternships(internshipIds: List<String>): Result<Int> {
        return try {
            if (internshipIds.isEmpty()) {
                return Result.success(0)
            }

            var total = 0
            for (internshipId in internshipIds) {
                val snapshot = firestore.collection("applications")
                    .whereEqualTo("internshipId", internshipId)
                    .get()
                    .await()
                total += snapshot.size()
            }

            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bekleyen başvuru sayısını al (şirket için)
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

    // Başvurucu detaylarını getir (Öğrenci bilgileriyle birlikte)
    suspend fun getApplicantDetails(studentId: String): Result<Map<String, Any>> {
        return try {
            val userDoc = firestore.collection("users")
                .document(studentId)
                .get()
                .await()

            if (userDoc.exists()) {
                Result.success(userDoc.data ?: emptyMap())
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staja ait başvuruları detaylarıyla getir (Şirket için)
    suspend fun getInternshipApplicantsWithDetails(internshipId: String): Result<List<Map<String, Any>>> {
        return try {
            val applicationsSnapshot = firestore.collection("applications")
                .whereEqualTo("internshipId", internshipId)
                .get()
                .await()

            val applicantsWithDetails = applicationsSnapshot.documents.mapNotNull { appDoc ->
                try {
                    val studentId = appDoc.getString("studentId") ?: return@mapNotNull null

                    // Öğrenci bilgilerini çek
                    val userDoc = try {
                        firestore.collection("users")
                            .document(studentId)
                            .get()
                            .await()
                    } catch (e: Exception) {
                        null
                    }

                    val userData = userDoc?.data

                    mapOf(
                        "applicationId" to appDoc.id,
                        "studentId" to studentId,
                        "studentName" to (userData?.get("name") as? String ?: "Unknown Student"),
                        "studentEmail" to (userData?.get("email") as? String ?: "No email"),
                        "studentPhone" to (userData?.get("phone") as? String ?: ""),
                        "studentUniversity" to (userData?.get("university") as? String ?: ""),
                        "studentMajor" to (userData?.get("major") as? String ?: ""),
                        "studentGraduationYear" to (userData?.get("graduationYear") as? String ?: ""),
                        "studentCvUrl" to (userData?.get("cvUrl") as? String ?: ""),
                        "studentSkills" to (userData?.get("skills") as? String ?: ""),
                        "appliedAt" to (appDoc.getLong("appliedAt") ?: 0L),
                        "status" to (appDoc.getString("status") ?: "pending")
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(
                applicantsWithDetails.sortedByDescending { (it["appliedAt"] as? Long) ?: 0L }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}