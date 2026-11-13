package mx.unam.ciencias.myp.ui

import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.produceState
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.material.icons.filled.PlayArrow
import org.jetbrains.skia.Image as SkiaImage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import javax.swing.*
import java.sql.Connection

import eu.iamkonstantin.kotlin.gadulka.*

import mx.unam.ciencias.myp.controller.MainController
import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.miner.SongMetadata
import mx.unam.ciencias.myp.db.initializeDatabase
import mx.unam.ciencias.myp.db.DatabaseDriver
import mx.unam.ciencias.myp.db.ArtistRow
import mx.unam.ciencias.myp.db.AlbumRow
import mx.unam.ciencias.myp.db.Rola

import mx.unam.ciencias.myp.ui.audio.AudioPlayerUI
import mx.unam.ciencias.myp.ui.columns.navigationColumn
import mx.unam.ciencias.myp.ui.columns.audioLibraryColumn
import mx.unam.ciencias.myp.ui.columns.albumCoversColumn
import mx.unam.ciencias.myp.ui.columns.songInfoColumn
import mx.unam.ciencias.myp.ui.columns.albumInfoColumn
import mx.unam.ciencias.myp.ui.audio.AudioPlayerUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mainScreen() {

    val controller = remember { MainController() }

    var selectedSong by remember { mutableStateOf<Rola?>(null) }
    var selectedAlbumId by remember { mutableStateOf<Int?>(null) }

    var isPlayerSelected by remember { mutableStateOf(false) }

    val player = rememberGadulkaState()

    var isPlaying by remember { mutableStateOf(false) }
    var currentPlayingSong by remember(player) { mutableStateOf<Rola?>(null) }

    var isSongInfoSelected by remember { mutableStateOf(false) }

    var isAlbumInfoSelected by remember { mutableStateOf(false) }

    var playerCanPlayPause by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            controller.close()
        }
    }
 
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Voyager")
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                                         val directory = chooseDirectory()
                                         if(directory != null) {
                                             controller.onDirectoryChosen(directory)
                                         }
                                     }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    navigationColumn(
                        modifier = Modifier.weight(0.4f),
                        onAudioPlayerSelect = { isPlayerSelected = !isPlayerSelected },
                        onSongInfoSelect = {
                            isSongInfoSelected = !isSongInfoSelected
                            isAlbumInfoSelected = false
                        },
                        onAlbumInfoSelect = {
                            isAlbumInfoSelected = !isAlbumInfoSelected
                            isSongInfoSelected = false
                        }
                    )
                    if(isSongInfoSelected) {
                        songInfoColumn(
                            modifier = Modifier.weight(0.4f),
                            selectedSong = selectedSong,
                            databaseDriver = controller.db
                        )
                    } else if (isAlbumInfoSelected) {
                        val album = runCatching {
                            selectedAlbumId?.let { controller.db?.getAlbumFromId(it) }
                        }.getOrNull()
                        albumInfoColumn(
                            modifier = Modifier.weight(0.4f),
                            selectedAlbum = album,
                            databaseDriver = controller.db
                        )
                    } else {
                        audioLibraryColumn(
                            modifier = Modifier.weight(0.4f),
                            currentFoundSong = controller.currentFoundSong,
                            dbManager = controller.db,
                            onSongSelect = { song ->
                                selectedSong = song
                                isPlayerSelected = true
                            }
                        )
                    }
                    if(isPlayerSelected && selectedSong != null) {
                        val song = selectedSong
                        song?.let {
                            AudioPlayerUI(
                                modifier = Modifier.weight(1f),
                                song = it,
                                player = player,
                                currentPlayingSong = currentPlayingSong,
                                onCurrentPlayingSongChange = { currentPlayingSong = it },
                                playing = isPlaying,
                                canPlayPause = playerCanPlayPause,
                                onPlayingChange = {
                                    isPlaying = it
                                },
                                onPlaybackStarted = {
                                    playerCanPlayPause = true
                                },
                                onPlaybackStopped = {
                                    playerCanPlayPause = false
                                },
                                onDifferentSongSelect = { newSong ->
                                    selectedSong = newSong
                                }
                            )
                        }
                    } else {
                        albumCoversColumn(
                            modifier = Modifier.weight(1f),
                            dbManager = controller.db,
                            onAlbumSelect = { id ->
                                selectedAlbumId = id
                            }
                        )
                    }
                }
            }
        }
    }
}

fun chooseDirectory(): String? {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val fileChooser = JFileChooser()
    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val result = fileChooser.showOpenDialog(null)
    if(result == JFileChooser.APPROVE_OPTION) {
        return fileChooser.selectedFile.toString()
    }
    return null
}
