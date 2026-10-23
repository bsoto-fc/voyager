package mx.unam.ciencias.myp.miner

import java.nio.file.Files
import java.nio.file.Path
import java.io.File
import java.sql.DriverManager
import java.sql.Connection
import java.sql.Types.INTEGER
import java.sql.Statement.RETURN_GENERATED_KEYS

import mx.unam.ciencias.myp.db.Types
import mx.unam.ciencias.myp.db.Rola

data class DatabaseInit(
    val path: Path,
    val wasCreated: Boolean,
    val connection: Connection
)


/*
 * Elementos requeridos para interfaz gráfica.
 */
data class ArtistRow(
    val id: Int,
    val name: String
)

data class AlbumRow(
    val id: Int,
    val name: String,
    val year: Int?,
    val path: String
)

data class SongRow(
    val id: Int,
    val title: String,
    val track: Int?,
    val path: String
)
 

fun initializeDatabase(): DatabaseInit {
    val dbFile = Path.of(System.getProperty("user.home"), ".local", "share", "voyager", "songlibrary.db") // XDG_DATA_HOME

    if(!Files.exists(dbFile.parent)) {
        Files.createDirectories(dbFile.parent)
    }
    
    val wasCreated = !Files.exists(dbFile)
    val dbPath = dbFile.toAbsolutePath().toString()
    val url = "jdbc:sqlite:$dbPath"

    val dbConnection = DriverManager.getConnection(url)
    if(wasCreated) {
        dbConnection.createStatement().use { statement ->
                                               statement.executeUpdate(
                                                   """
                CREATE TABLE types (
                  id_type       INTEGER PRIMARY KEY,
                  description   TEXT
                );
                
                INSERT INTO types VALUES(0, 'Person');
                INSERT INTO types VALUES(1, 'Group');
                INSERT INTO types VALUES(2, 'Unknown');;
                
                CREATE TABLE performers (
                  id_performer  INTEGER PRIMARY KEY,
                  id_type       INTEGER,
                  name          TEXT,
                  FOREIGN KEY   (id_type) REFERENCES types(id_type)
                );
                
                CREATE TABLE persons (
                  id_person     INTEGER PRIMARY KEY,
                  stage_name    TEXT,
                  real_name     TEXT,
                  birth_date    TEXT,
                  death_date    TEXT
                );
                
                CREATE TABLE groups (
                  id_group      INTEGER PRIMARY KEY,
                  name          TEXT,
                  start_date    TEXT,
                  end_date      TEXT
                );
                
                CREATE TABLE in_group (
                  id_person     INTEGER,
                  id_group      INTEGER,
                  PRIMARY KEY   (id_person, id_group),
                  FOREIGN KEY   (id_person) REFERENCES persons(id_person)
                  FOREIGN KEY   (id_group) REFERENCES groups(id_group)
                );
                
                CREATE TABLE albums (
                  id_album      INTEGER PRIMARY KEY,
                  path          TEXT,
                  name          TEXT,
                  year          INTEGER
                );
                
                INSERT INTO albums VALUES (0, '', 'Unknown', 0);
                
                CREATE TABLE rolas (
                  id_rola       INTEGER PRIMARY KEY,
                  id_performer  INTEGER,
                  id_album      INTEGER,
                  path          TEXT,
                  title         TEXT,
                  track         INTEGER,
                  year          INTEGER,
                  genre         TEXT,
                  FOREIGN KEY   (id_performer) REFERENCES performers(id_performer)
                  FOREIGN KEY   (id_album) REFERENCES albums(id_album)
                );
                                                   """.trimIndent()
                                               )
        }
    }
    return DatabaseInit(dbFile, wasCreated, dbConnection)
}

class MusicDbManager(private val connection: Connection) {

    fun saveSong(meta: SongMetadata) {
        // Etiquetas no diferencían entre persona y grupo
        val performerId = getOrCreatePerformer(meta.artist, Types.TYPE_UNKNOWN)
        val albumDir = File(meta.path).parentFile
        val albumId = getOrCreateAlbum(albumDir?.name ?: "Unknown", albumDir?.path ?: "", meta.year)
        insertRola(meta, performerId, albumId)
    }

    private fun getOrCreatePerformer(name: String, type: Int): Int {
        val selectSql = "SELECT id_performer FROM performers WHERE name = ?"
        connection.prepareStatement(selectSql).use { stmt ->
            stmt.setString(1, name)
            val rs = stmt.executeQuery()
            if (rs.next())
                return rs.getInt("id_performer")
        }

        val insertSql = "INSERT INTO performers (id_type, name) VALUES (?, ?)"
        connection.prepareStatement(insertSql, RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setInt(1, type)
            stmt.setString(2, name)
            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next())
                return keys.getInt(1)
        }
        return 0 // Fallback a Unknown
    }

    private fun getOrCreateAlbum(name: String, path: String, year: Int?): Int {
        if (name == "Unknown") return 0 // Usar el álbum desconocido ya existente

        val selectSql = "SELECT id_album FROM albums WHERE path = ?"
        connection.prepareStatement(selectSql).use { stmt ->
            stmt.setString(1, path)
            val rs = stmt.executeQuery()
            if (rs.next())
                return rs.getInt("id_album")
        }

        val insertSql = "INSERT INTO albums (path, name, year) VALUES (?, ?, ?)"
        connection.prepareStatement(insertSql, RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, path)
            stmt.setString(2, name)
            if(year != null)
                stmt.setInt(3, year)
            else
                stmt.setNull(3, INTEGER)
            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if(keys.next())
                return keys.getInt(1)
        }
        return 0
    }

    private fun insertRola(meta: SongMetadata, performerId: Int, albumId: Int) {
        val exists = connection.prepareStatement("SELECT 1 FROM rolas WHERE path = ?").use { stmt ->
            stmt.setString(1, meta.path)
            stmt.executeQuery().use { it.next() }
        }
        if(exists) {
            val updateSql = "UPDATE rolas SET title = ?, id_performer = ?, id_album = ? WHERE path = ?"
            connection.prepareStatement(updateSql).use { stmt ->
                stmt.setString(1, meta.title)
                stmt.setInt(2, performerId)
                stmt.setInt(3, albumId)
                stmt.setString(4, meta.path)
                stmt.executeUpdate()
            }
            return
        }
        val sql = """
        INSERT INTO rolas (id_performer, id_album, path, title, track, year, genre) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, performerId)
            stmt.setInt(2, albumId)
            stmt.setString(3, meta.path)
            stmt.setString(4, meta.title)
            if(meta.track != null)
                stmt.setInt(5, meta.track)
            else stmt.setNull(5, INTEGER)
            if(meta.year != null)
                stmt.setInt(6, meta.year)
            else
                stmt.setNull(6, INTEGER)
            stmt.setString(7, meta.genre)
            stmt.executeUpdate()
        }
    }

    fun getArtists(): List<ArtistRow> {
        val sql = "SELECT id_performer, name FROM performers ORDER BY name COLLATE NOCASE"
        return connection.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next())
                        add(ArtistRow(rs.getInt("id_performer"), rs.getString("name")))
                }
            }
        }
    }
    
    fun getAlbumsOf(performerId: Int): List<AlbumRow> {
        val sql = """
        SELECT DISTINCT a.id_album, a.name, a.year, a.path
        FROM albums a JOIN rolas r ON r.id_album = a.id_album
        WHERE r.id_performer = ?
        ORDER BY a.year, a.name COLLATE NOCASE
            """.trimIndent()
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, performerId)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        val year = rs.getInt("year").takeIf { !rs.wasNull() && it != 0 }
                        add(AlbumRow(rs.getInt("id_album"), rs.getString("name"), year, rs.getString("path")))
                    }
                }
            }
        }
    }
    
    fun getSongsOf(performerId: Int, albumId: Int): List<SongRow> {
        val sql = """
        SELECT id_rola, title, track, path FROM rolas
        WHERE id_performer = ? AND id_album = ?
        ORDER BY track, title COLLATE NOCASE
            """.trimIndent()
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, performerId)
            stmt.setInt(2, albumId)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        val track = rs.getInt("track").takeIf { !rs.wasNull() }
                        add(SongRow(rs.getInt("id_rola"), rs.getString("title"), track, rs.getString("path")))
                    }
                }
            }
        }
    }
    
    fun getAllAlbums(): List<AlbumRow> {
        val sql = "SELECT id_album, name, year, path FROM albums ORDER BY name COLLATE NOCASE"
        return connection.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        val year = rs.getInt("year").takeIf { !rs.wasNull() && it != 0 }
                        add(AlbumRow(rs.getInt("id_album"), rs.getString("name"), year, rs.getString("path")))
                    }
                }
            }
        }
    }
}
