package ir.ghiyas.alimaa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform