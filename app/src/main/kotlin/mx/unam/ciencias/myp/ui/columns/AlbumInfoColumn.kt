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
import mx.unam.ciencias.myp.db.Album
import mx.unam.ciencias.myp.db.Types

@Composable
fun albumInfoColumn(
    modifier: Modifier = Modifier,
    selectedAlbum: Album?,
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
            if(selectedAlbum == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Selecciona un álbum.")
                }
            } else {
                
                var idAlbum by remember { mutableStateOf(selectedAlbum.idAlbum) }
                var path by remember { mutableStateOf(selectedAlbum.path) }
                var name by remember { mutableStateOf(selectedAlbum.name) }
                var year by remember { mutableStateOf(selectedAlbum.year) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val artworkName = "${File(path)}".hashCode().toString() + ".jpg"
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
                            contentDescription = "$name" 
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nombre") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Nombre"
                            )
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    
                    var yearString by remember { mutableStateOf(year.toString()) }
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        onClick = {
                            databaseDriver?.updateAlbum(Album(
                                                           idAlbum = idAlbum,
                                                           path = path,
                                                           year = year,
                                                           name = name
                                                       ))
                        }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
