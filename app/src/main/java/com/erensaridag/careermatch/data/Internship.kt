package com.erensaridag.careermatch.data

data class Internship(
    val id: Int,
    val title: String,
    val company: String,
    val location: String,
    val duration: String,
    val salary: String,
    val description: String
)

fun getSampleInternships(): List<Internship> {
    return listOf(
        Internship(
            id = 1,
            title = "Android Developer",
            company = "TechCorp",
            location = "Istanbul",
            duration = "3 months",
            salary = "$800",
            description = "Mobile app development with Kotlin and Jetpack Compose"
        ),
        Internship(
            id = 2,
            title = "UI/UX Designer",
            company = "DesignCo",
            location = "Remote",
            duration = "6 months",
            salary = "$600",
            description = "Create beautiful and intuitive user interfaces"
        ),
        Internship(
            id = 3,
            title = "Data Analyst",
            company = "DataTech",
            location = "Ankara",
            duration = "4 months",
            salary = "$700",
            description = "Analyze big data and create insights for business decisions"
        ),
        Internship(
            id = 4,
            title = "Web Developer",
            company = "WebStudio",
            location = "Izmir",
            duration = "5 months",
            salary = "$750",
            description = "Full-stack web development using modern frameworks"
        ),
        Internship(
            id = 5,
            title = "iOS Developer",
            company = "AppleTech",
            location = "Istanbul",
            duration = "3 months",
            salary = "$850",
            description = "Develop native iOS applications with Swift and SwiftUI"
        ),
        Internship(
            id = 6,
            title = "Backend Developer",
            company = "CloudSys",
            location = "Remote",
            duration = "6 months",
            salary = "$900",
            description = "Build scalable backend systems and APIs"
        )
    )
}