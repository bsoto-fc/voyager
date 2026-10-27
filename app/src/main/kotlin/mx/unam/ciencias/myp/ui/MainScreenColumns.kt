package mx.unam.ciencias.myp.ui

import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.foundation.lazy.items

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import javax.swing.*

import eu.iamkonstantin.kotlin.gadulka.*

import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.miner.MusicDbManager
import mx.unam.ciencias.myp.miner.SongMetadata
import mx.unam.ciencias.myp.db.ArtistRow
import mx.unam.ciencias.myp.db.AlbumRow
import mx.unam.ciencias.myp.db.Rola
import mx.unam.ciencias.myp.ui.audio.AudioPlayerUI


@Composable
fun buttonsLeftColumn(
    modifier: Modifier = Modifier,
    onAudioPlayerSelect: () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-button-with-icon-and-text/
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {

            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Información"
                )
                Text(
                    "Información",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
                
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {
                onAudioPlayerSelect()
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Reproductor"
                )
                Text(
                    "Reproductor",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
                
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {

            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Audioteca"
                )
                Text(
                    "Audioteca",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
                
            }
        }
    }
}

@Composable
fun libraryMiddleColumn(
    modifier: Modifier = Modifier,
    currentFoundSong: SongMetadata? = null,
    dbManager: MusicDbManager? = null,
    onSongSelect: (Rola) -> Unit 
) {

    var artists by remember { mutableStateOf<List<ArtistRow>>(emptyList()) }
    var expandedArtistId by remember { mutableStateOf<Int?>(null) }
    var expandedAlbumId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(dbManager, currentFoundSong) {
        if(dbManager != null){
            artists = withContext(Dispatchers.IO) { dbManager.getArtists() }
        }
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
                    contentDescription = if (expanded) "Contraer" else "Expandir"
                )
            }
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

@Composable
fun rightColumn(
    modifier: Modifier = Modifier,
    playingSong: Rola? = null,
    dbManager: MusicDbManager? = null,
    isPlayerSelected: Boolean = false ,
    player: GadulkaPlayer? = null
) {
    Column(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if(isPlayerSelected && playingSong != null && player!=null) {
            AudioPlayerUI(
                mp3Path = playingSong.path,
                player = player
            )
        } else {
            var albums by remember { mutableStateOf<List<AlbumRow>>(emptyList()) }

            LaunchedEffect(dbManager) {
                if(dbManager != null){
                    albums = withContext(Dispatchers.IO) { dbManager.getAllAlbums() }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                // https://stackoverflow.com/questions/68610360/how-to-add-space-between-grid-items-in-jetpack-compose
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(albums) { album ->
                    val artworkName = "${album.path}".hashCode().toString() + ".jpg"
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
                            contentDescription = album.name
                        )
                    }
                }
            }
        }
    }
}
