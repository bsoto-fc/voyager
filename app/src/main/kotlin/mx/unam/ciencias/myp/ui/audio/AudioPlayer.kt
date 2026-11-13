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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Clear
import org.jetbrains.skia.Image as SkiaImage

import eu.iamkonstantin.kotlin.gadulka.*

import java.io.File
import java.nio.file.Path

import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.db.Rola

import kotlinx.coroutines.*

@Composable
fun AudioPlayerUI(
    modifier: Modifier = Modifier,
    song: Rola,
    player: GadulkaPlayer,
    currentPlayingSong: Rola?,
    onCurrentPlayingSongChange: (Rola?) -> Unit,
    playing: Boolean,
    canPlayPause: Boolean,
    onPlayingChange: (Boolean) -> Unit,
    onDifferentSongSelect: (Rola) -> Unit,
    onPlaybackStarted: () -> Unit,
    onPlaybackStopped: () -> Unit
) {
    var currentSelectedSong by remember(song) { mutableStateOf<Rola>(song) }

    val mp3Path by remember(currentSelectedSong.path) { mutableStateOf<String>(currentSelectedSong.path) }
    
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var songDuration by remember { mutableStateOf(0L) }
    var songPosition by remember { mutableStateOf(0L) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    Column(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                    contentDescription = "${currentSelectedSong.track}. ${currentSelectedSong.title}" 
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${currentSelectedSong.track}. ${currentSelectedSong.title}" ,
                style = TextStyle(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isScrubbing by remember { mutableStateOf(false) }
            
            LaunchedEffect(currentSelectedSong, currentPlayingSong, playing, player) {
                if(currentSelectedSong.path != currentPlayingSong?.path) {
                    sliderPosition = 0f
                    songPosition = 0L
                    songDuration = 0L
                    return@LaunchedEffect
                }
                while (playing) {
                    val duration = player.currentDuration() ?: 0L
                    val position = player.currentPosition() ?: 0L
                    
                    if (duration > 0L) {
                        songDuration = duration
                        songPosition = position
                        
                        if (!isScrubbing) {
                            sliderPosition =
                                (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                        }
                    }
                    
                    kotlinx.coroutines.delay(250)
                }
            }
            
            Column {
                Slider(
                    value = sliderPosition,
                    valueRange = 0f..1f,
                    onValueChange = { newValue ->
                        isScrubbing = true
                        sliderPosition = newValue
                    },
                    onValueChangeFinished = {
                        if (playing && songDuration > 0L) {
                            player.seekTo((sliderPosition * songDuration).toLong())
                        }
                        isScrubbing = false
                    },
                    enabled = songDuration > 0L,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.Black,
                        activeTickColor = Color.Black,
                        thumbColor = Color.Black
                    )
                )
                val posMinutes = songPosition / 1000 / 60
                val posSeconds = songPosition / 1000 % 60
                val durMinutes = songDuration / 1000 / 60
                val durSeconds = songDuration / 1000 % 60
                Text(text = "$posMinutes:$posSeconds/$durMinutes:$durSeconds")
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                onClick = {
                    onCurrentPlayingSongChange(currentSelectedSong)
                    onPlaybackStarted()
                    onPlayingChange(true)
                    scope.launch {
                        player.play(File(mp3Path).toURI().toString())
                    }
                }) {
                Text("Reproducir")
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                onClick = {
                    player.stop()
                    onPlayingChange(false)
                    onPlaybackStopped()
                    songPosition = 0L
                    sliderPosition = 0f
                }) {
                Text("Detener")
            }
            if(canPlayPause){
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    onClick = {
                        if(playing)
                            player.pause()
                        else
                            player.play()
                        onPlayingChange(!playing)
                    }) {
                    val text = if(playing) "Pausar" else "Continuar"
                    Text(text = text)
                }
            }
            currentPlayingSong?.let {
                if(it.path != currentSelectedSong.path) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        onClick = {
                            currentSelectedSong = it
                            onDifferentSongSelect(currentSelectedSong)
                        }) {
                        Icon(Icons.Default.Clear, contentDescription = "Regresar")
                    }
                }
            }
        }
    }
}
