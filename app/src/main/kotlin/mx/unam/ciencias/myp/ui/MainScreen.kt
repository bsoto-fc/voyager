package mx.unam.ciencias.myp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.produceState
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import javax.swing.*
import java.sql.Connection

import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.initializeDatabase
import mx.unam.ciencias.myp.miner.MusicDbManager
import mx.unam.ciencias.myp.miner.SongMetadata
import mx.unam.ciencias.myp.miner.ArtistRow
import mx.unam.ciencias.myp.miner.AlbumRow
import mx.unam.ciencias.myp.miner.SongRow

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

                    buttonsLeftColumn(modifier = Modifier.weight(0.4f))
                    libraryMiddleColumn(
                        modifier = Modifier.weight(0.4f),
                        currentFoundSong = currentFoundSong,
                        dbManager = dbManager
                    )
                    rightColumn(modifier = Modifier.weight(1f))

                }
            }
        }
    }
}

@Composable
fun buttonsLeftColumn(
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Pequeño")

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
    dbManager: MusicDbManager? = null
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
                        
                        expandableRow(
                            text = album.name + (album.year?.let { " ($it)" } ?: ""),
                            expanded = albumExpanded,
                            indent = 16.dp,
                            onClick = { expandedAlbumId = if (albumExpanded) null else album.id }
                        )
                        
                        if (albumExpanded) {
                            val songs by produceState(emptyList<SongRow>(), artist.id, album.id, currentFoundSong) {
                                value = withContext(Dispatchers.IO) { dbManager.getSongsOf(artist.id, album.id) }
                            }
                            
                            songs.forEach { song ->
                                Text(
                                    text = (song.track?.let { "$it. " } ?: "") + song.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {  }
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = indent, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Resto")
    }
}

