package mx.unam.ciencias.myp.ui.columns

import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.foundation.border

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import eu.iamkonstantin.kotlin.gadulka.*

import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.miner.SongMetadata
import mx.unam.ciencias.myp.db.DatabaseDriver
import mx.unam.ciencias.myp.db.ArtistRow
import mx.unam.ciencias.myp.db.AlbumRow
import mx.unam.ciencias.myp.db.Rola
import mx.unam.ciencias.myp.ui.audio.AudioPlayerUI

@Composable
fun albumCoversColumn(
    modifier: Modifier = Modifier,
    playingSong: Rola? = null,
    dbManager: DatabaseDriver? = null,
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
                song = playingSong,
                player = player
            )
        } else {
            var albums by remember { mutableStateOf<List<AlbumRow>>(emptyList()) }

            albums = albums.filterNot { it.name == "Unknown" }

            val albumArtworkSize = 135.dp

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
                            contentDescription = album.name,
                            modifier = Modifier.size(albumArtworkSize),
                        )
                    } ?: Box(
                        modifier = Modifier.size(albumArtworkSize).border(1.dp, Color.Black),
                        content = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = album.name + (album.year?.let { " - $it" })
                                )
                                Text(
                                    text = album.name + (album.year?.let { " - $it" }),
                                    style = TextStyle(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
                                
                            }
                        }
                    )
                }
            }
        }
    }
}
