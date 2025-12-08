package com.erensaridag.careermatch.data

data class Internship(
    val id: String,
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
            id = "1",
            title = "Android Geliştirici",
            company = "TechCorp",
            location = "Istanbul",
            duration = "3 ay",
            salary = "$800",
            description = "Kotlin ve Jetpack Compose ile mobil uygulama geliştirme"
        ),
        Internship(
            id = "2",
            title = "UI/UX Tasarımcısı",
            company = "DesignCo",
            location = "Uzaktan",
            duration = "6 ay",
            salary = "$600",
            description = "Güzel ve sezgisel kullanıcı arayüzleri oluşturun"
        ),
        Internship(
            id = "3",
            title = "Veri Analisti",
            company = "DataTech",
            location = "Ankara",
            duration = "4 ay",
            salary = "$700",
            description = "Büyük veriyi analiz edin ve iş kararları için içgörüler oluşturun"
        ),
        Internship(
            id = "4",
            title = "Web Geliştirici",
            company = "WebStudio",
            location = "Izmir",
            duration = "5 ay",
            salary = "$750",
            description = "Modern frameworkler kullanarak full-stack web geliştirme"
        ),
        Internship(
            id = "5",
            title = "iOS Geliştirici",
            company = "AppleTech",
            location = "Istanbul",
            duration = "3 ay",
            salary = "$850",
            description = "Swift ve SwiftUI ile native iOS uygulamaları geliştirin"
        ),
        Internship(
            id = "6",
            title = "Backend Geliştirici",
            company = "CloudSys",
            location = "Uzaktan",
            duration = "6 ay",
            salary = "$900",
            description = "Ölçeklenebilir backend sistemleri ve API'ler oluşturun"
        )
    )
}