package mx.unam.ciencias.myp.ui.columns

import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.foundation.lazy.items

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.miner.SongMetadata
import mx.unam.ciencias.myp.db.DatabaseDriver
import mx.unam.ciencias.myp.db.ArtistRow
import mx.unam.ciencias.myp.db.AlbumRow
import mx.unam.ciencias.myp.db.Rola

@Composable
fun songInfoColumn(
    modifier: Modifier = Modifier,
    selectedSong: Rola?,
    databaseDriver: DatabaseDriver?
) {

    Column(
        modifier.fillMaxWidth().padding(8.dp)
    ) {
        if(selectedSong == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Selecciona una canción.")
            }
        } else {

            val idRola = selectedSong.idRola
            var idPerformer = selectedSong.idPerformer
            var idAlbum = selectedSong.idAlbum
            val path = selectedSong.path
            var title = selectedSong.title
            var track = selectedSong.track
            var year = selectedSong.year
            var genre = selectedSong.genre
    
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val artworkName = "${File(selectedSong.path).parent}".hashCode().toString() + ".jpg"
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
                        contentDescription = "${selectedSong.track}. ${selectedSong.title}" 
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Titulo") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Titulo"
                        )
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                var trackString by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf("") }
                
                TextField(
                    value = trackString,
                    onValueChange = { newValue ->
                        runCatching { newValue.toInt() }.fold(
                            onSuccess = { 
                                trackString = newValue
                                track = newValue.toInt()
                            },
                            onFailure = { 
                                errorMessage = "Ingresa un valor entero."
                                trackString = newValue
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Pista") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Pista"
                        )
                    },
                    isError = errorMessage.isNotEmpty()
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                var yearString by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf("") }

                TextField(
                    value = yearString,
                    onValueChange = { newValue ->
                        runCatching { newValue.toInt() }.fold(
                            onSuccess = { 
                                yearString = newValue
                                year = newValue.toInt()
                            },
                            onFailure = { 
                                errorMessage = "Ingresa un valor entero."
                                yearString = newValue
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Año") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Año"
                        )
                    },
                    isError = errorMessage.isNotEmpty()
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = genre,
                    onValueChange = { genre = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Género") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Género"
                        )
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    onClick = {
                        databaseDriver?.updateRola(Rola(
                                                      idRola = idRola,
                                                      idPerformer = idPerformer,
                                                      idAlbum = idAlbum,
                                                      path = path,
                                                      title = title,
                                                      track = track,
                                                      year = year,
                                                      genre = genre
                                                  ))
                    }) {
                    Text("Guardar")
                }
            }
        }
    }
}
