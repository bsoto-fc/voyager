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
import androidx.compose.ui.window.Dialog

import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.foundation.lazy.items

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import mx.unam.ciencias.myp.db.DatabaseDriver
import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.db.Rola
import mx.unam.ciencias.myp.db.Types

@Composable
fun songInfoColumn(
    modifier: Modifier = Modifier,
    selectedSong: Rola?,
    databaseDriver: DatabaseDriver?
) {

    var showEditPerformerDialog by remember { mutableStateOf(false) }

    if(showEditPerformerDialog) {
        EditPerformerDialog(onConfirmation = { showEditPerformerDialog = false },
                            onDismiss = { showEditPerformerDialog = false },
                            databaseDriver = databaseDriver
        ) 
    }

    LazyColumn(
        modifier.fillMaxWidth().padding(8.dp)
    ) {
        item {
            if(selectedSong == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Selecciona una canción.")
                }
            } else {
                
                val idRola by remember { mutableStateOf(selectedSong.idRola) }
                var idPerformer by remember { mutableStateOf(selectedSong.idPerformer ?: 0) }
                var idAlbum by remember { mutableStateOf(selectedSong.idAlbum) }
                val path by remember { mutableStateOf(selectedSong.path) }
                var title by remember{ mutableStateOf(selectedSong.title) }
                var track by remember {mutableStateOf(selectedSong.track) }
                var year by remember {mutableStateOf(selectedSong.year) }
                var genre by remember {mutableStateOf(selectedSong.genre) }
                
                var albumName by remember { mutableStateOf("") }
                var performerName by remember { mutableStateOf("") }

                var performerType by remember { mutableStateOf(2) }

                var errorMessage by remember { mutableStateOf("") }

                LaunchedEffect(databaseDriver) {
                    databaseDriver?.let {
                        albumName = it.getAlbumNameById(idAlbum)
                        performerName = it.getPerformerNameById(idPerformer)
                        performerType = runCatching { it.getPerformerType(idPerformer) }.getOrElse { 2 }
                    }
                }

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
                        onValueChange = {
                            errorMessage = ""
                            if(it.isEmpty())
                                errorMessage = "El título no puede estar vacío"
                            title = it
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Titulo") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Titulo"
                            )
                        },
                        isError = errorMessage.isNotEmpty()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    
                    var trackString by remember { mutableStateOf(track.toString()) }
                    
                    TextField(
                        value = trackString,
                        onValueChange = { newValue ->
                            errorMessage = ""
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
                    
                    var yearString by remember { mutableStateOf(year.toString()) }
                    
                    TextField(
                        value = yearString,
                        onValueChange = { newValue ->
                            errorMessage = ""
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
                        onValueChange = {
                            errorMessage = ""
                            if(it.isEmpty())
                                errorMessage = "El género no puede estar vacío"
                            genre = it
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Género") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Género"
                            )
                        },
                        isError = errorMessage.isNotEmpty()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = albumName,
                        onValueChange = {
                            errorMessage = ""
                            if(it.isEmpty())
                                errorMessage = "El nombre de álbum no puede estar vacío"
                            albumName = it
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Album") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Album"
                            )
                        },
                        isError = errorMessage.isNotEmpty()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    var showMenu by remember { mutableStateOf(false) }
                    TextField(
                        value = performerName,
                        onValueChange = {
                            errorMessage = ""
                            if(it.isEmpty())
                                errorMessage = "El intérprete no puede estar vacío"
                            performerName = it
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Intérprete") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Intérprete"
                            )
                        },
                        isError = errorMessage.isNotEmpty()
                    )
                    // https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-dropdown-menu.html
                    Box() {
                        IconButton(
                            onClick = { showMenu = !showMenu }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Persona") },
                                onClick = {
                                    performerType = Types.TYPE_PERSON
                                    var personExists = databaseDriver?.personExists(performerName) ?: false
                                    if(!personExists)
                                        errorMessage = "Persona inexistente"
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Grupo") },
                                onClick = {
                                    performerType = Types.TYPE_GROUP
                                    var groupExists = databaseDriver?.groupExists(performerName) ?: false
                                    if(!groupExists)
                                        errorMessage = "Grupo inexistente"
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Desconocido") },
                                onClick = {
                                    performerType = Types.TYPE_UNKNOWN
                                    showMenu = false
                                    errorMessage = ""
                                },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Editor de artistas.") },
                                onClick = {
                                    showEditPerformerDialog = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        onClick = {
                            if(errorMessage.isEmpty()) {
                                idAlbum = databaseDriver?.getAlbumIdByName(albumName, path, year) ?: 0
                                idPerformer = databaseDriver?.getPerformerIdByName(performerName, performerType) ?: 0
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
                            }
                        }) {
                        Text("Guardar")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(errorMessage.isNotEmpty()){
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
