package mx.unam.ciencias.myp.miner

import java.io.File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.images.Artwork
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.io.ByteArrayInputStream
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.text.Normalizer

object DirConstants {
    val ARTWORK_CACHE_DIR = Path.of(System.getProperty("user.home"), ".local", "share", "voyager", "cache", "artwork") // XDG_DATA_HOME
}

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

                            if(tag.firstArtwork != null){
                                val byteArtwork = tag.firstArtwork.binaryData;
                                val fileName = "${File(metadata.path).parent}".hashCode().toString() + ".jpg"
                                // https://www.geeksforgeeks.org/java/java-program-to-convert-byte-array-to-image/
                                val inStreambj = ByteArrayInputStream(byteArtwork) 
                                val imageArtwork = ImageIO.read(inStreambj) 
                                if(!Files.exists(DirConstants.ARTWORK_CACHE_DIR)) {
                                    Files.createDirectories(DirConstants.ARTWORK_CACHE_DIR)
                                }
                                ImageIO.write(imageArtwork, "jpg", File(Path.of(DirConstants.ARTWORK_CACHE_DIR.toAbsolutePath().toString(), fileName).toString()))
                            } else {
                                println("Album no tiene portada.")
                            }

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
