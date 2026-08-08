package com.lifespaces.android.ui

import android.app.DatePickerDialog
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.lifespaces.android.data.Item
import com.lifespaces.android.data.Space
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun App(viewModel: AppViewModel) {
    val context = LocalContext.current
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
    var sortByDate by rememberSaveable { mutableStateOf(false) }
    val systemDarkTheme = isSystemInDarkTheme()
    val appearance = remember(context) { context.getSharedPreferences("appearance", 0) }
    var darkTheme by rememberSaveable {
        mutableStateOf(
            if (appearance.contains("dark_theme")) appearance.getBoolean("dark_theme", false) else systemDarkTheme,
        )
    }
    val inboxItems = state.items.filter { it.spaceId == null }.let { items ->
        if (sortByDate) {
            items.sortedWith(compareBy<Item> { it.scheduledAt == null }.thenBy { it.scheduledAt ?: Long.MAX_VALUE })
        } else {
            items
        }
    }

    LifeSpacesTheme(darkTheme = darkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("LifeSpaces") },
                    actions = {
                        TextButton(onClick = {
                            darkTheme = !darkTheme
                            appearance.edit().putBoolean("dark_theme", darkTheme).apply()
                        }) {
                            Text(if (darkTheme) "Svijetla" else "Tamna")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ),
                    )
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
                    Card(
                        shape = LifeSpacesCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
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
                        onSchedule = { scheduledAt -> viewModel.setItemScheduledAt(item.id, scheduledAt) },
                        onDelete = { deletingItemId = item.id },
                    )
                }
                item { HorizontalDivider() }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Prostori (${state.spaces.size})", modifier = Modifier.weight(1f))
                        TextButton(onClick = { sortByDate = !sortByDate }) {
                            Text(if (sortByDate) "Prikaz: datum" else "Prikaz: novo")
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = spaceName,
                                onValueChange = { spaceName = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Novi prostor") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            )
                            Box {
                                Button(onClick = { templateMenuExpanded = true }) {
                                    Text("Šablon: $spaceTemplate")
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
                        OutlinedTextField(
                            value = spaceLocation,
                            onValueChange = { spaceLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Lokacija (opciono)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        )
                        Button(
                            onClick = {
                                viewModel.createSpace(spaceName, spaceTemplate, spaceLocation)
                                spaceName = ""
                                spaceLocation = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = spaceName.isNotBlank(),
                        ) { Text("Kreiraj prostor") }
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
                    val displayedSpaceItems = if (sortByDate) {
                        spaceItems.sortedWith(compareBy<Item> { it.scheduledAt == null }.thenBy { it.scheduledAt ?: Long.MAX_VALUE })
                    } else {
                        spaceItems
                    }
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
                    items(displayedSpaceItems, key = { it.id }) { item ->
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
                            onSchedule = { scheduledAt -> viewModel.setItemScheduledAt(item.id, scheduledAt) },
                            onDelete = { deletingItemId = item.id },
                        )
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
    onSchedule: (Long?) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by rememberSaveable(item.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val cardColor by animateColorAsState(
        targetValue = if (item.completed == true) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "item color",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (item.completed == true) 0.985f else 1f,
        label = "item scale",
    )
    Card(
        modifier = Modifier.graphicsLayer {
            scaleX = cardScale
            scaleY = cardScale
        },
        shape = LifeSpacesCardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
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
                item.scheduledAt?.let {
                    Text(
                        "Datum: ${SimpleDateFormat("d. MMM yyyy.", Locale.getDefault()).format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) { Text("Uredi") }
                    TextButton(onClick = { menuExpanded = true }) { Text("Premjesti") }
                    TextButton(onClick = onDelete) { Text("Obriši") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val calendar = Calendar.getInstance().apply {
                            item.scheduledAt?.let { timeInMillis = it }
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                Calendar.getInstance().apply {
                                    set(year, month, day, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                    onSchedule(timeInMillis)
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    }) { Text(if (item.scheduledAt == null) "Dodaj datum" else "Promijeni datum") }
                    if (item.scheduledAt != null) {
                        TextButton(onClick = { onSchedule(null) }) { Text("Ukloni datum") }
                    }
                }
                if (item.scheduledAt != null) {
                    TextButton(onClick = {
                        val location = spaces.firstOrNull { it.id == item.spaceId }?.location
                        try {
                            context.startActivity(createCalendarInsertIntent(item, location))
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "Nije pronađena aplikacija kalendara.", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Dodaj u kalendar") }
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

internal fun createCalendarInsertIntent(item: Item, location: String?): Intent {
    val start = requireNotNull(item.scheduledAt) { "Stavka mora imati datum." }
    val end = Calendar.getInstance().apply {
        timeInMillis = start
        add(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    return Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.Events.TITLE, item.text)
        putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
    }
}

private val LifeSpacesCardShape = RoundedCornerShape(24.dp)

private val LifeSpacesLightColors = lightColorScheme(
    primary = Color(0xFF5D4BB7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7DEFF),
    onPrimaryContainer = Color(0xFF1A075F),
    secondary = Color(0xFF46655D),
    onSecondary = Color.White,
    background = Color(0xFFF9F7FF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFF0ECF8),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
)

private val LifeSpacesDarkColors = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF164A70),
    onPrimaryContainer = Color(0xFFD4E7FF),
    secondary = Color(0xFFB7CADB),
    onSecondary = Color(0xFF22323F),
    secondaryContainer = Color(0xFF384956),
    onSecondaryContainer = Color(0xFFD3E5F5),
    tertiary = Color(0xFFD0BCFF),
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF0C111B),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF141A24),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF202938),
    onSurfaceVariant = Color(0xFFC2CAD7),
    outline = Color(0xFF8B95A5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
private fun LifeSpacesTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) LifeSpacesDarkColors else LifeSpacesLightColors
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        window.statusBarColor = colors.background.toArgb()
        window.navigationBarColor = colors.background.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        shapes = MaterialTheme.shapes.copy(
            medium = LifeSpacesCardShape,
            large = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
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
            onSchedule = {},
            onDelete = {},
        )
    }
}
