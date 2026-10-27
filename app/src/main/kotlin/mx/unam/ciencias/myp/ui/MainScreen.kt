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

import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.miner.initializeDatabase
import mx.unam.ciencias.myp.miner.MusicDbManager
import mx.unam.ciencias.myp.miner.SongMetadata
import mx.unam.ciencias.myp.miner.ArtistRow
import mx.unam.ciencias.myp.miner.AlbumRow
import mx.unam.ciencias.myp.miner.SongRow
import mx.unam.ciencias.myp.ui.audio.AudioPlayerUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mainScreen() {
    
    var addingDirectory by remember { mutableStateOf(false) }
    var selectedDirectory by remember { mutableStateOf<String?>(null) }

    var existingDb by remember { mutableStateOf(false) }

    var dbConnection by remember { mutableStateOf<Connection?>(null) }
    var miner by remember { mutableStateOf<Miner?>(null) }

    var currentFoundSong by remember { mutableStateOf<SongMetadata?>(null) }

    val dbManager = remember(dbConnection) { dbConnection?.let { MusicDbManager(it) } }

    var playingSongPath by remember { mutableStateOf<String?>(null) }

    var isPlayerSelected by remember { mutableStateOf(true) }

    val player = rememberGadulkaState()

    LaunchedEffect(Unit) {
        val init = initializeDatabase()
        existingDb = !init.wasCreated
        dbConnection = init.connection
    }

    DisposableEffect(Unit) {
        onDispose {
            miner?.stop()
            dbConnection?.close()
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Voyager")
                    },
                    actions = {
                        IconButton(onClick = {
                                   }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Dark Mode"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                                         println("Pressed Button")
                                         // https://github.com/JetBrains/compose-multiplatform/issues/176
                                         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                                         val fileChooser = JFileChooser()
                                         fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                         fileChooser.showSaveDialog(null)
                                         selectedDirectory = fileChooser.getSelectedFile()?.toString()
                                         val connection = dbConnection
                                         if(selectedDirectory != null && connection != null) {
                                             miner?.stop()
                                             miner = Miner(
                                                 path = selectedDirectory.toString(),
                                                 onFound = { metadata ->
                                                     dbManager?.saveSong(metadata)
                                                     currentFoundSong = metadata
                                                     println("Se añadió: ${metadata.title} por ${metadata.artist}")
                                                 },
                                                 onFinish = { 
                                                     addingDirectory = false
                                                     currentFoundSong = null
                                                 }
                                             ).also{
                                                 it.recursiveMine()
                                                 addingDirectory = true
                                             }
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

                    buttonsLeftColumn(
                        modifier = Modifier.weight(0.4f),
                        onAudioPlayerSelect = { isPlayerSelected = !isPlayerSelected }
                    )
                    libraryMiddleColumn(
                        modifier = Modifier.weight(0.4f),
                        currentFoundSong = currentFoundSong,
                        dbManager = dbManager,
                        onSongSelect = { path ->
                            playingSongPath = path
                        }
                    )
                    rightColumn(
                        modifier = Modifier.weight(1f),
                        dbManager = dbManager,
                        playingSongPath = playingSongPath,
                        isPlayerSelected = isPlayerSelected,
                        player = player
                    )
                }
            }
        }
    }
}
