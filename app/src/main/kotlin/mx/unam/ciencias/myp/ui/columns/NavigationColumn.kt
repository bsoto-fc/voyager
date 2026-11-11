package mx.unam.ciencias.myp.ui.columns

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Image as SkiaImage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import javax.swing.*

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
fun navigationColumn(
    modifier: Modifier = Modifier,
    onAudioPlayerSelect: () -> Unit,
    onInfoSelect: () -> Unit,
){
    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-button-with-icon-and-text/
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {
                onInfoSelect()
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
    }
}
