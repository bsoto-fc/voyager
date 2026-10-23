package mx.unam.ciencias.myp.ui.audio

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File
import eu.iamkonstantin.kotlin.gadulka.*

@Composable
fun AudioPlayerUI(
    mp3Path: String
) {
    val player = rememberGadulkaState()
    Row {
        Button(
            onClick = {
                player.play(File(mp3Path).toURI().toString())
            }) {
            Text("Play")
        }
        Button(
            onClick = {
                player.stop()
            }) {
            Text("Stop")
        }
    }
}
