package mx.unam.ciencias.myp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mx.unam.ciencias.myp.db.DatabaseDriver
import mx.unam.ciencias.myp.db.initializeDatabase
import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.SongMetadata
import java.io.File
import java.sql.Connection

class MainController {

    var db by mutableStateOf<DatabaseDriver?>(null)
    var isMining by mutableStateOf(false)
    var currentFoundSong by mutableStateOf<SongMetadata?>(null)

    private var connection: Connection? = null
    private var miner: Miner? = null

    @Volatile
    private var runNumber = 0

    init {
        val database = initializeDatabase()
        connection = database.connection
        db = DatabaseDriver(database.connection)
    }

    fun onDirectoryChosen(path: String) {
        val driver = db
        if (driver == null || !File(path).isDirectory) return

        miner?.stop()
        runNumber++
        val thisRun = runNumber
        isMining = true
        currentFoundSong = null

        val newMiner = Miner(
            path = path,
            onFound = { metadata ->
                if (thisRun == runNumber) {
                    try {
                        driver.saveSong(metadata)
                        currentFoundSong = metadata
                    } catch (e: Exception) {
                        println("No se pudo guardar ${metadata.path}: ${e.message}")
                    }
                }
            },
            onFinish = {
                if (thisRun == runNumber) {
                    isMining = false
                    currentFoundSong = null
                }
            }
        )
        miner = newMiner
        newMiner.recursiveMine()
    }

    fun close() {
        miner?.stop()
        runNumber++
        connection?.close()
    }
}
