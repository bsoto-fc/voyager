package mx.unam.ciencias.myp.db

interface SongRepository {
    suspend fun insert(song: Song): Long
    suspend fun update(song: Song): Int
    suspend fun delete(id: String): Int
    suspend fun get(id: String): Song?
    suspend fun search(title: String, artist: String): List<Song>
}
