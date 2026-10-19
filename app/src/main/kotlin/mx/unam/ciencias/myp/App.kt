package mx.unam.ciencias.myp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javax.swing.*

import mx.unam.ciencias.myp.miner.Miner

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Voyager") {
        mainScreen()
    }
}

@Composable
fun mainScreen() {
    
    var addingDirectory by remember { mutableStateOf(false) }
    var selectedDirectory by remember { mutableStateOf<String?>(null) }
    
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
                                         println("Pressed Button")
                                         // https://github.com/JetBrains/compose-multiplatform/issues/176
                                         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                                         val fileChooser = JFileChooser()
                                         fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                         fileChooser.showSaveDialog(null)
                                         selectedDirectory = fileChooser.getSelectedFile()?.toString()
                                         if(selectedDirectory != null)
                                             Miner(selectedDirectory.toString()).recursiveMine()
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
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Prueba"
                )
            }
        }
    }
}

