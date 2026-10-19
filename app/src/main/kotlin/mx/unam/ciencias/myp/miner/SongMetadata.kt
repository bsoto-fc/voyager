package mx.unam.ciencias.myp.miner

data class SongMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val track: Int?,
    val year: Int?,
    val genre: String,
    val path: String
)
