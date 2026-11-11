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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.foundation.lazy.items

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.nio.file.Path
import java.io.File

import mx.unam.ciencias.myp.db.DatabaseDriver
import mx.unam.ciencias.myp.miner.DirConstants
import mx.unam.ciencias.myp.db.Rola
import mx.unam.ciencias.myp.db.Types
import mx.unam.ciencias.myp.db.Person
import mx.unam.ciencias.myp.db.Group
import mx.unam.ciencias.myp.db.GroupWithMembersAdapter

@Composable
fun EditPerformerDialog(
    onPersonConfirmation: (Person) -> Unit,
    onGroupConfirmation: (Group) -> Unit,
    onDismiss: () -> Unit,
    databaseDriver: DatabaseDriver?
) {

    var performerName by remember { mutableStateOf("") }
    var performerType by remember { mutableStateOf(0) } 
    var realName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var deathDate by remember { mutableStateOf("") }
    var groupStartDate by remember { mutableStateOf("") }
    var groupEndDate by remember { mutableStateOf("") }

    var groupMembersString by remember { mutableStateListOf<String>(emptyList()) }
    var groupMembers by remember { mutableStateListOf<String>(emptyList()) }

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Crear artista/banda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Nombre:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    TextField(
                        value = performerName,
                        onValueChange = { performerName = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nombre del artista/banda") },
                        singleLine = true,
                        enabled = performerType == 0 || performerType == 1,
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tipo:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp),
                    )

                    RadioButton(
                        selected = performerType == 0,
                        onClick = { performerType = 0 },
                    )
                    Text(
                        text = "Persona",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.width(120.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    RadioButton(
                        selected = performerType == 1,
                        onClick = { performerType = 1 },
                    )
                    Text(
                        text = "Banda",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.width(120.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (performerType == Types.TYPE_PERSON) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Nombre real:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        TextField(
                            value = realName,
                            onValueChange = { realName = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Nombre real") },
                            singleLine = true,
                            enabled = performerType == 0,
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fecha de nacimiento:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        TextField(
                            value = birthDate,
                            onValueChange = { birthDate = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("DD/MM/AAAA") },
                            singleLine = true,
                            enabled = performerType == 0,
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fecha de fallecimiento:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        TextField(
                            value = deathDate,
                            onValueChange = { deathDate = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("DD/MM/AAAA") },
                            singleLine = true,
                            enabled = performerType == 0,
                        )
                    }
                }
                
                if (performerType == Types.TYPE_GROUP) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fecha de inicio:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        TextField(
                            value = groupStartDate,
                            onValueChange = { groupStartDate = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("DD/MM/AAAA") },
                            singleLine = true,
                            enabled = performerType == 1,
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fecha de fin:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        TextField(
                            value = groupEndDate,
                            onValueChange = { groupEndDate = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("DD/MM/AAAA") },
                            singleLine = true,
                            enabled = performerType == 1,
                        )
                    }
                }
                
                if (performerType == Types.TYPE_GROUP) {
                    Text(
                        text = "Miembros de la banda (separados por comas sin espacios):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(0.dp, 8.dp),
                    )
                    TextField(
                        value = groupMembersString,
                        onValueChange = { 
                            groupMembersString = it
                            groupMembers = groupMembersString.split(",")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        label = { Text("Nombres de los miembros") },
                        singleLine = true,
                        enabled = performerType == 1,
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Cancelar")
                    }
                    ElevatedButton(
                        onClick = {
                            when (performerType) {
                                Types.TYPE_PERSON -> {
                                    val newPerson = Person(
                                        stageName = performerName,
                                        realName = realName,
                                        birthDate = birthDate,
                                        deathDate = deathDate
                                    )
                                    onPersonConfirmation(newPerson)
                                }
                                
                                Types.TYPE_GROUP -> {
                                    val newGroup = Group(
                                        name = performerName,
                                        startDate = groupStartDate,
                                        endDate = groupEndDate
                                    )
                                    onGroupConfirmation(newGroup)
                                }
                            }
                        }
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}
