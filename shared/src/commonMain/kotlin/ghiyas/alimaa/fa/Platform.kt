package ghiyas.alimaa.fa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform