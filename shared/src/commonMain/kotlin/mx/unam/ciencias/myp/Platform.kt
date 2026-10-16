package mx.unam.ciencias.myp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform