package mx.unam.ciencias.myp.ui.audio

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image as ComposeImage
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.runtime.produceState
import java.io.File
import java.nio.file.Path
import eu.iamkonstantin.kotlin.gadulka.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap

import mx.unam.ciencias.myp.miner.DirConstants

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AudioPlayerUI(
    mp3Path: String
) {
    val player = rememberGadulkaState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val artworkName = "${File(mp3Path).parent}".hashCode().toString() + ".jpg"
        val artworkPath = Path.of(DirConstants.ARTWORK_CACHE_DIR.toAbsolutePath().toString(), artworkName).toString()
        
        val artwork = produceState<ImageBitmap?>(
            initialValue = null,
            key1 = artworkPath
        ) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    SkiaImage.makeFromEncoded(File(artworkPath).readBytes())
                        .toComposeImageBitmap()
                }.getOrNull()
            }
        }.value
        
        artwork?.let {
            ComposeImage(
                bitmap = it,
                contentDescription = mp3Path
            )
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {
                player.play(File(mp3Path).toURI().toString())
            }) {
            Text("Play")
        }
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {
                player.stop()
            }) {
            Text("Stop")
        }
    }
}
