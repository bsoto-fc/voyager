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
import androidx.compose.ui.window.Dialog

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
fun audioLibraryColumn(
    modifier: Modifier = Modifier,
    currentFoundSong: SongMetadata? = null,
    dbManager: DatabaseDriver? = null,
    onSongSelect: (Rola) -> Unit 
) {

    var artists by remember { mutableStateOf<List<ArtistRow>>(emptyList()) }
    var expandedArtistId by remember { mutableStateOf<Int?>(null) }
    var expandedAlbumId by remember { mutableStateOf<Int?>(null) }

    var searchQuery by remember { mutableStateOf<String>("") }

    var filters by remember { mutableStateOf<Map<String,Any>>(emptyMap<String,Any>()) }

    var showingFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(dbManager, currentFoundSong) {
        if(dbManager != null){
            artists = withContext(Dispatchers.IO) { dbManager.getArtists() }
        }
    }

    if(showingFilterDialog) {
        FilterDialog(
            onConfirmation = {
                showingFilterDialog = false
            }, filters)
    }
    
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if(currentFoundSong != null) {
            Text("Se encontró ${currentFoundSong.title} de ${currentFoundSong.artist}")
        }
        LazyColumn {

            item {
                Box(
                    content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Buscar") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = "Buscar"
                                    )
                                }
                            )
                            IconButton(
                                onClick = { showingFilterDialog = !showingFilterDialog }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    }
                )
            }

            if(searchQuery != "") {
                
            }
            
            items(artists, key = { it.id }) { artist ->
                val expanded = expandedArtistId == artist.id
                
                expandableRow(
                    text = artist.name,
                    expanded = expanded,
                    indent = 0.dp,
                    onClick = {
                        expandedArtistId = if (expanded) null else artist.id
                        expandedAlbumId = null
                    }
                )
                
                if(expanded && dbManager != null) {
                    val albums by produceState(emptyList<AlbumRow>(), artist.id, currentFoundSong) {
                        value = withContext(Dispatchers.IO) { dbManager.getAlbumsOf(artist.id) }
                    }
                    
                    albums.forEach { album ->
                        val albumExpanded = expandedAlbumId == album.id
                        
                        val artworkName = "${album.path}".hashCode().toString() + ".jpg"
                        println("Searching for $artworkName")
                        val artworkPath = Path.of(DirConstants.ARTWORK_CACHE_DIR.toAbsolutePath().toString(), artworkName).toString()
                        
                        expandableRow(
                            text = album.name + (album.year?.let { " ($it)" } ?: ""),
                            expanded = albumExpanded,
                            indent = 16.dp,
                            isAlbum = true,
                            albumCoverPath = artworkPath,
                            onClick = { expandedAlbumId = if (albumExpanded) null else album.id }
                        )
                        
                        if (albumExpanded) {
                            val songs by produceState(emptyList<Rola>(), artist.id, album.id, currentFoundSong) {
                                value = withContext(Dispatchers.IO) { dbManager.getSongsOf(artist.id, album.id) }
                            }
                            
                            songs.forEach { song ->
                                Text(
                                    text = (song.track.let { "$it. " }) + song.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSongSelect(song) }
                                        .padding(start = 40.dp, top = 4.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun expandableRow(
    text: String,
    expanded: Boolean,
    indent: Dp,
    isAlbum: Boolean = false,
    albumCoverPath: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = indent, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(isAlbum && albumCoverPath != null) {
            val artwork = produceState<ImageBitmap?>(
                initialValue = null,
                key1 = albumCoverPath
            ) {
                value = withContext(Dispatchers.IO) {
                    runCatching {
                        SkiaImage.makeFromEncoded(File(albumCoverPath).readBytes())
                            .toComposeImageBitmap()
                    }.getOrNull()
                }
            }.value
            
            artwork?.let {
                ComposeImage(
                    bitmap = it,
                    modifier = Modifier.size(24.dp),
                    contentDescription = albumCoverPath
                )
            } ?: Icon(
                imageVector = Icons.Filled.PlayArrow,
                modifier = Modifier.size(24.dp),
                contentDescription = albumCoverPath
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Contraer" else "Expandir"
        )
        Spacer(Modifier.width(8.dp))
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// https://developer.android.com/develop/ui/compose/components/dialog
@Composable
fun FilterDialog(
    onConfirmation: () -> Unit,
    filters: Map<String, Any>
) {
    
    var searchByArtist by remember { mutableStateOf(false) }
    var searchByAlbum by remember { mutableStateOf(false) }
    var searchBySong by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = { onConfirmation() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column {
                Text(
                    text = "Búsqueda avanzada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    Text(
                        text = "Artista.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
                    )
                    Switch(
                        checked = searchByArtist,
                        onCheckedChange = {
                            searchByArtist = it
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = { onConfirmation() }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
