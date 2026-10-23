package mx.unam.ciencias.myp.miner

import java.io.File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.images.Artwork
import kotlinx.coroutines.*
import java.io.IOException

class Miner(
    private val path: String,
    private val onFound: (SongMetadata) -> Unit,
    private var onFinish: () -> Unit
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var processingJob: Job? = null
    
    fun recursiveMine() {
        processingJob?.cancel()
        processingJob = null
        
        processingJob = scope.launch{
            val dir = File(path)
            if(dir.exists() && dir.isDirectory) {
                File(path).walkTopDown()
                    .filter {
                        it.extension.equals("mp3", ignoreCase = true) ||
                            it.extension.equals("flac",ignoreCase = true)
                    }
                    .forEach {
                        try {
                            val audioFile = AudioFileIO.read(it)
                            val tag = audioFile.tag
                            
                            val metadata = SongMetadata(
                                title = tag.getFirst(FieldKey.TITLE) ?: "Unknown",
                                artist = tag.getFirst(FieldKey.ARTIST) ?: "Unknown",
                                album = tag.getFirst(FieldKey.ALBUM) ?: "Unknown",
                                track = tag.getFirst(FieldKey.TRACK)?.toIntOrNull(),
                                year = tag.getFirst(FieldKey.YEAR)?.toIntOrNull(),
                                genre = tag.getFirst(FieldKey.GENRE) ?: "Unknown",
                                path = it.absolutePath
                            )
                            println("Se encontró $metadata.")
                            onFound(metadata)
                        } catch (e: IOException) {
                            // Disco duro lento, cd, etc (Encontrado al usar un directorio dentro de un iPod)
                            println("Error al obtener etiquetas: ${e.message}")
                        } 
                    }
                onFinish()
                println("Minero finalizó.")
            }
        }
    }

    fun stop() {
        processingJob?.cancel()
    }
}
