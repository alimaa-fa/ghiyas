package ir.ghias.alimaa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform