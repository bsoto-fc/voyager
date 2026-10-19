package mx.unam.ciencias.myp.miner

import java.io.File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.audio.AudioFileIO
import kotlinx.coroutines.*
import java.io.IOException

class Miner(
    private val path: String,
    private val onFound: (String) -> Unit,
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
                            
                            println("Title:  ${tag.getFirst(FieldKey.TITLE)}")
                            println("Artist: ${tag.getFirst(FieldKey.ARTIST)}")
                            println("Album:  ${tag.getFirst(FieldKey.ALBUM)}")
                            println("Track:  ${tag.getFirst(FieldKey.TRACK)}")
                            
                            onFound(tag.getFirst(FieldKey.TITLE))
                        } catch (e: IOException) {
                            // Disco duro lento, cd, etc (Encontrado al usar un directorio dentro de un iPod)
                            println("Error al obtener etiquetas: ${e.message}")
                        } finally {
                            processingJob?.join()
                        }
                    }
            }
        }
    }

    fun stop() {
        processingJob?.cancel()
    }
}
