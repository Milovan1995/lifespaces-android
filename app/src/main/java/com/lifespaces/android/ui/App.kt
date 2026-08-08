package com.lifespaces.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifespaces.android.data.Item
import com.lifespaces.android.data.Space

@Composable
fun App(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val captureText by viewModel.captureText.collectAsState()
    var spaceName by rememberSaveable { mutableStateOf("") }
    var spaceLocation by rememberSaveable { mutableStateOf("") }
    var spaceTemplate by rememberSaveable { mutableStateOf("Shopping") }
    var templateMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var editingItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingText by rememberSaveable { mutableStateOf("") }
    var deletingItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingSpaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    val inboxItems = state.items.filter { it.spaceId == null }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("LifeSpaces") }) },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text("Brzi unos", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Zapiši ideju, zadatak ili podsjetnik. Kasnije ga možeš premjestiti u prostor.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = captureText,
                                onValueChange = viewModel::onCaptureTextChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nova stavka") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { viewModel.saveCapture() }),
                            )
                            Button(
                                onClick = viewModel::saveCapture,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = captureText.isNotBlank(),
                            ) { Text("Sačuvaj") }
                        }
                    }
                }
                item {
                    Text("Nesortirano (${inboxItems.size})", style = MaterialTheme.typography.titleMedium)
                    if (inboxItems.isEmpty()) {
                        Text(
                            "Nesortirano je prazno. Sačuvane stavke će se pojaviti ovdje.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                items(inboxItems, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        spaces = state.spaces,
                        showCompletion = false,
                        isEditing = editingItemId == item.id,
                        editingText = if (editingItemId == item.id) editingText else item.text,
                        onEdit = {
                            editingItemId = item.id
                            editingText = item.text
                        },
                        onTextChange = { editingText = it },
                        onSave = {
                            viewModel.updateItemText(item.id, editingText)
                            editingItemId = null
                        },
                        onMove = { spaceId -> viewModel.moveItem(item.id, spaceId) },
                        onCompletedChange = { viewModel.setItemCompleted(item.id, it) },
                        onDelete = { deletingItemId = item.id },
                    )
                }
                item { HorizontalDivider() }
                item { Text("Prostori (${state.spaces.size})") }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = spaceName,
                                onValueChange = { spaceName = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Novi prostor") },
                                singleLine = true,
                            )
                            Box {
                                Button(onClick = { templateMenuExpanded = true }) {
                                    Text(spaceTemplate)
                                }
                                DropdownMenu(
                                    expanded = templateMenuExpanded,
                                    onDismissRequest = { templateMenuExpanded = false },
                                ) {
                                    listOf("Shopping", "General").forEach { template ->
                                        DropdownMenuItem(
                                            text = { Text(template) },
                                            onClick = {
                                                spaceTemplate = template
                                                templateMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.createSpace(spaceName, spaceTemplate, spaceLocation)
                                spaceName = ""
                                spaceLocation = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = spaceName.isNotBlank(),
                        ) { Text("Kreiraj prostor") }
                        OutlinedTextField(
                            value = spaceLocation,
                            onValueChange = { spaceLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Lokacija (opciono)") },
                            singleLine = true,
                        )
                    }
                }
                if (state.spaces.isEmpty()) {
                    item {
                        Text(
                            "Još nema prostora. Kreiraj prvi iznad da organizuješ stavke.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                state.spaces.forEach { space ->
                    val spaceItems = state.items.filter { it.spaceId == space.id }
                    item(key = "space-${space.id}") {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${space.name} (${spaceItems.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                space.location?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                            TextButton(onClick = { deletingSpaceId = space.id }) {
                                Text("Obriši prostor")
                            }
                        }
                    }
                    if (spaceItems.isEmpty()) {
                        item {
                            Text(
                                "Ovaj prostor još nema stavki.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    items(spaceItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            spaces = state.spaces,
                            showCompletion = space.template == "Shopping",
                            isEditing = editingItemId == item.id,
                            editingText = if (editingItemId == item.id) editingText else item.text,
                            onEdit = {
                                editingItemId = item.id
                                editingText = item.text
                            },
                            onTextChange = { editingText = it },
                            onSave = {
                                viewModel.updateItemText(item.id, editingText)
                                editingItemId = null
                            },
                            onMove = { spaceId -> viewModel.moveItem(item.id, spaceId) },
                            onCompletedChange = { viewModel.setItemCompleted(item.id, it) },
                            onDelete = { deletingItemId = item.id },
                        )
                    }
                }
            }
        }
    }

    deletingItemId?.let { itemId ->
        AlertDialog(
            onDismissRequest = { deletingItemId = null },
            title = { Text("Obriši stavku?") },
            text = { Text("Ova stavka će biti trajno obrisana.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(itemId)
                    deletingItemId = null
                }) { Text("Obriši") }
            },
            dismissButton = {
                TextButton(onClick = { deletingItemId = null }) { Text("Otkaži") }
            },
        )
    }

    deletingSpaceId?.let { spaceId ->
        val space = state.spaces.firstOrNull { it.id == spaceId }
        AlertDialog(
            onDismissRequest = { deletingSpaceId = null },
            title = { Text("Obriši prostor?") },
            text = {
                Text("${space?.name ?: "Ovaj prostor"} će biti obrisan, a njegove stavke vraćene u Nesortirano.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSpace(spaceId)
                    deletingSpaceId = null
                }) { Text("Obriši") }
            },
            dismissButton = {
                TextButton(onClick = { deletingSpaceId = null }) { Text("Otkaži") }
            },
        )
    }
}

@Composable
private fun ItemCard(
    item: Item,
    spaces: List<Space>,
    showCompletion: Boolean,
    isEditing: Boolean,
    editingText: String,
    onEdit: () -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onMove: (Long?) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by rememberSaveable(item.id) { mutableStateOf(false) }
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                )
                Button(
                    onClick = onSave,
                    enabled = editingText.isNotBlank(),
                ) { Text("Sačuvaj izmjenu") }
            } else {
                Row {
                    if (showCompletion) {
                        Checkbox(
                            checked = item.completed == true,
                            onCheckedChange = { onCompletedChange(it) },
                        )
                    }
                    Text(
                        text = item.text,
                        modifier = Modifier.weight(1f).padding(top = 12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (item.completed == true) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) { Text("Uredi") }
                    TextButton(onClick = { menuExpanded = true }) { Text("Premjesti") }
                    TextButton(onClick = onDelete) { Text("Obriši") }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Nesortirano") },
                        onClick = {
                            onMove(null)
                            menuExpanded = false
                        },
                    )
                    spaces.forEach { space ->
                        DropdownMenuItem(
                            text = { Text(space.name) },
                            onClick = {
                                onMove(space.id)
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreview() {
    MaterialTheme {
        ItemCard(
            item = Item(id = 1, text = "Mlijeko"),
            spaces = listOf(Space(id = 2, name = "Voli", template = "Shopping")),
            showCompletion = true,
            isEditing = false,
            editingText = "Mlijeko",
            onEdit = {},
            onTextChange = {},
            onSave = {},
            onMove = {},
            onCompletedChange = {},
            onDelete = {},
        )
    }
}
