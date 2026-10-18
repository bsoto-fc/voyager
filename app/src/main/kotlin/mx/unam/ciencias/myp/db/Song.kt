package mx.unam.ciencias.myp.db

data class Song(
    val artist: String,
    val album: String,
    val year: String,
    val track: Int,
    val genre: String,
    val path: String
)
