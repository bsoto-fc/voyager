package mx.unam.ciencias.myp.ui.audio

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Image as SkiaImage

import eu.iamkonstantin.kotlin.gadulka.*

import java.io.File
import java.nio.file.Path

import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.db.Rola

import kotlinx.coroutines.*

@Composable
fun AudioPlayerUI(
    song: Rola,
    player: GadulkaPlayer
) {

    val mp3Path = song.path
    
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
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
                    modifier = Modifier.size(250.dp),
                    contentDescription = "${song.track}. ${song.title}" 
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${song.track}. ${song.title}" ,
                style = TextStyle(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                onClick = {
                    scope.launch {
                        player.play(File(mp3Path).toURI().toString())
                    }
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
}
