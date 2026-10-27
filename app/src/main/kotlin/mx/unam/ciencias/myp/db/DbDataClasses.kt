package mx.unam.ciencias.myp.db

import java.sql.ResultSet

data class Types(
    val id: Int,
    val description: String
) {
    companion object { // Companion object evita tener que realizar funciones de extensión (ResultSet.asType() : Type, por ejemplo)
        const val TYPE_PERSON = 0
        const val TYPE_GROUP = 1
        const val TYPE_UNKNOWN = 2
        
        fun getDescriptionById(id: Int): String? = when (id) {
            TYPE_PERSON -> "Person"
            TYPE_GROUP -> "Group"
            TYPE_UNKNOWN -> "Unknown"
            else -> null
        }
        
        fun getIdByDescription(desc: String): Int? = when (desc.lowercase()) {
            "person" -> TYPE_PERSON
            "group" -> TYPE_GROUP
            "unknown" -> TYPE_UNKNOWN
            else -> null
        }
    }
}

data class Performer(
    val idPerformer: Int,
    val idType: Int,
    val name: String
) {
    companion object {
        fun fromSqlResult(rs: ResultSet): Performer {
            return Performer(
                idPerformer = rs.getInt("id_performer"),
                idType = rs.getInt("id_type"),
                name = rs.getString("name")
            )
        }
    }
}

data class Person(
    val idPerson: Int,
    val stageName: String,
    val realName: String?,
    val birthDate: String?,
    val deathDate: String?
) {
    companion object {
        fun fromSqlResult(rs: ResultSet): Person {
            return Person(
                idPerson = rs.getInt("id_person"),
                stageName = rs.getString("stage_name"),
                realName = rs.getString("real_name"),
                birthDate = rs.getString("birth_date"),
                deathDate = rs.getString("death_date")
            )
        }
    }
}

data class Group(
    val idGroup: Int,
    val name: String,
    val startDate: String?,
    val endDate: String?
) {
    companion object {
        fun fromSqlResult(rs: ResultSet): Group {
            return Group(
                idGroup = rs.getInt("id_group"),
                name = rs.getString("name"),
                startDate = rs.getString("start_date"),
                endDate = rs.getString("end_date")
            )
        }
    }
}

data class InGroup(
    val idPerson: Int,
    val idGroup: Int
) {
    companion object {
        fun fromSqlResult(rs: ResultSet): InGroup {
            return InGroup(
                idPerson = rs.getInt("id_person"),
                idGroup = rs.getInt("id_group")
            )
        }
    }
}

data class Album(
    val idAlbum: Int,
    val path: String,
    val name: String,
    val year: Int
) {
    companion object {
        val UNKNOWN_ALBUM = Album(0, "", "Unknown", 0)

        fun fromSqlResult(rs: ResultSet): Album {
            return Album(
                idAlbum = rs.getInt("id_album"),
                path = rs.getString("path"),
                name = rs.getString("name"),
                year = rs.getInt("year")
            )
        }
    }
}

data class Rola(
    val idRola: Int,
    val idPerformer: Int?,
    val idAlbum: Int,
    val path: String,
    val title: String,
    val track: Int,
    val year: Int,
    val genre: String
) {
    companion object {
        fun fromSqlResult(rs: ResultSet): Rola {
            return Rola(
                idRola = rs.getInt("id_rola"),
                idPerformer = rs.getInt("id_performer"),
                idAlbum = rs.getInt("id_album"),
                path = rs.getString("path"),
                title = rs.getString("title"),
                track = rs.getInt("track"),
                year = rs.getInt("year"),
                genre = rs.getString("genre")
            )
        }
    }
}

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
 
