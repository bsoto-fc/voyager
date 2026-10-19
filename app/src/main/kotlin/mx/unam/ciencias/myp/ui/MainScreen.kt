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

import javax.swing.*
import java.sql.Connection

import mx.unam.ciencias.myp.miner.Miner
import mx.unam.ciencias.myp.miner.initializeDatabase
import mx.unam.ciencias.myp.miner.MusicDbManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mainScreen() {
    
    var addingDirectory by remember { mutableStateOf(false) }
    var selectedDirectory by remember { mutableStateOf<String?>(null) }

    var existingDb by remember { mutableStateOf(false) }

    var databaseConnection by remember { mutableStateOf<Connection?>(null) }

    LaunchedEffect(existingDb) {
        val init = initializeDatabase()
        existingDb = !init.wasCreated
        databaseConnection = init.connection
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
                                         if(selectedDirectory != null) {
                                             val dbManager = MusicDbManager(dbConnection)
                                             Miner(
                                                 path = selectedDirectory.toString(),
                                                 onFound = { metadata ->
                                                     dbManager.saveSong(metadata)
                                                     println("Se añadió: ${metadata.title} por ${metadata.artist}")
                                                 }
                                             ).recursiveMine()
                                         }
                                         addingDirectory = true
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
                    libraryMiddleColumn(modifier = Modifier.weight(0.4f))
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Medio")
    }
}

@Composable
fun rightColumn(
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Resto")
    }
}
