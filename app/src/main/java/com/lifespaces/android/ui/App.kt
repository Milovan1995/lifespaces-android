package com.lifespaces.android.ui

import android.app.DatePickerDialog
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.core.view.WindowCompat
import com.lifespaces.android.R
import com.lifespaces.android.data.Item
import com.lifespaces.android.data.Space
import com.lifespaces.android.data.SpaceCapabilities
import com.lifespaces.android.widget.SpaceWidget
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun App(viewModel: AppViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val calendar by viewModel.calendar.collectAsState()
    val captureText by viewModel.captureText.collectAsState()
    var spaceName by rememberSaveable { mutableStateOf("") }
    var spaceLocation by rememberSaveable { mutableStateOf("") }
    var spaceTemplate by rememberSaveable { mutableStateOf("Shopping") }
    var templateMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var editingItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingItemLabel by rememberSaveable { mutableStateOf<String?>(null) }
    var editingText by rememberSaveable { mutableStateOf("") }
    var deletingItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingSpaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var sortByDate by rememberSaveable { mutableStateOf(false) }
    var expandedSpaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var inboxExpanded by rememberSaveable { mutableStateOf(false) }
    var expandedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var spaceMenuId by rememberSaveable { mutableStateOf<Long?>(null) }
    var addingItemSpaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var newSpaceItemLabel by rememberSaveable { mutableStateOf("") }
    var newSpaceItemText by rememberSaveable { mutableStateOf("") }
    var showSpaceCreator by rememberSaveable { mutableStateOf(false) }
    var createCompletion by rememberSaveable { mutableStateOf(true) }
    var createDate by rememberSaveable { mutableStateOf(true) }
    var createLocation by rememberSaveable { mutableStateOf(true) }
    var createLinks by rememberSaveable { mutableStateOf(false) }
    var createSpaceColor by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingSpaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingSpaceName by rememberSaveable { mutableStateOf("") }
    var editingSpaceLocation by rememberSaveable { mutableStateOf("") }
    var editingSpaceCompletion by rememberSaveable { mutableStateOf(false) }
    var editingSpaceDate by rememberSaveable { mutableStateOf(false) }
    var editingSpaceLocationEnabled by rememberSaveable { mutableStateOf(false) }
    var editingSpaceLinks by rememberSaveable { mutableStateOf(false) }
    var editingSpaceColor by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirmingSpaceEditId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showSystemAlarmDialog by rememberSaveable { mutableStateOf(false) }
    var showingSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showingCalendar by rememberSaveable { mutableStateOf(false) }
    var weekStartEpochDay by rememberSaveable { mutableStateOf(mondayOf(LocalDate.now()).toEpochDay()) }
    var selectedDateEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val homeListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val openItemAtHome: (Item) -> Unit = { item ->
        showingSearch = false
        searchQuery = ""
        expandedItemId = null
        editingItemId = null
        editingItemLabel = null
        addingItemSpaceId = null
        val (targetSpaceId, targetIndex) = searchResultTarget(item, state.spaces)
        inboxExpanded = targetSpaceId == null
        expandedSpaceId = targetSpaceId
        coroutineScope.launch { homeListState.scrollToItem(targetIndex) }
    }
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
    val (todayItems, upcomingItems) = todayAndUpcomingItems(state.items)
    val inboxExpansionRotation by animateFloatAsState(
        targetValue = if (inboxExpanded) 180f else 0f,
        label = "inbox expansion",
    )
    LaunchedEffect(state.spaces) {
        SpaceWidget.updateAll(context)
    }
    BackHandler(enabled = showingCalendar) {
        if (selectedDateEpochDay != null) selectedDateEpochDay = null else showingCalendar = false
    }
    BackHandler(enabled = showingSearch) {
        showingSearch = false
        searchQuery = ""
    }
    LifeSpacesTheme(darkTheme = darkTheme) {
        val defaultSpaceAccent = MaterialTheme.colorScheme.primary
        val inboxAccent = MaterialTheme.colorScheme.secondary
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when {
                                showingSearch -> "Pretraga"
                                showingCalendar -> "Moj kalendar"
                                else -> "LifeSpaces"
                            },
                            modifier = Modifier.clickable(
                                onClickLabel = "Vrati na početni ekran",
                                role = Role.Button,
                            ) {
                                showingCalendar = false
                                showingSearch = false
                                searchQuery = ""
                                selectedDateEpochDay = null
                                expandedItemId = null
                                editingItemId = null
                                editingItemLabel = null
                                addingItemSpaceId = null
                                editingSpaceId = null
                                showSpaceCreator = false
                                spaceMenuId = null
                                expandedSpaceId = null
                                inboxExpanded = false
                                coroutineScope.launch { homeListState.scrollToItem(0) }
                            },
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            if (showingSearch) {
                                showingSearch = false
                                searchQuery = ""
                            } else {
                                showingSearch = true
                                showingCalendar = false
                                selectedDateEpochDay = null
                                expandedItemId = null
                                editingItemId = null
                                editingItemLabel = null
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = if (showingSearch) "Zatvori pretragu" else "Otvori pretragu",
                            )
                        }
                        IconButton(onClick = { showSystemAlarmDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_alarm),
                                contentDescription = "Podesi alarm",
                            )
                        }
                        IconButton(onClick = {
                            showingSearch = false
                            searchQuery = ""
                            showingCalendar = !showingCalendar
                            selectedDateEpochDay = null
                            if (showingCalendar) {
                                val today = LocalDate.now()
                                weekStartEpochDay = mondayOf(today).toEpochDay()
                                viewModel.ensureDefaultShiftTypes()
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar),
                                contentDescription = if (showingCalendar) "Zatvori Moj kalendar" else "Otvori Moj kalendar",
                            )
                        }
                        IconButton(
                            onClick = {
                                darkTheme = !darkTheme
                                appearance.edit().putBoolean("dark_theme", darkTheme).apply()
                            },
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (darkTheme) R.drawable.ic_light_mode else R.drawable.ic_dark_mode,
                                ),
                                contentDescription = if (darkTheme) {
                                    "Prebaci na svijetlu temu"
                                } else {
                                    "Prebaci na tamnu temu"
                                },
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { innerPadding ->
            if (showingCalendar) {
                CalendarScreen(
                    modifier = Modifier.padding(innerPadding),
                    items = state.items,
                    spaces = state.spaces,
                    weekStart = LocalDate.ofEpochDay(weekStartEpochDay),
                    selectedDate = selectedDateEpochDay?.let(LocalDate::ofEpochDay),
                    shiftTypes = calendar.shiftTypes,
                    overrides = calendar.overrides,
                    shiftDays = calendar.shiftDays,
                    onPreviousWeek = { weekStartEpochDay = moveCalendarWeek(LocalDate.ofEpochDay(weekStartEpochDay), -1).toEpochDay() },
                    onNextWeek = { weekStartEpochDay = moveCalendarWeek(LocalDate.ofEpochDay(weekStartEpochDay), 1).toEpochDay() },
                    onToday = {
                        val today = LocalDate.now()
                        weekStartEpochDay = mondayOf(today).toEpochDay()
                        selectedDateEpochDay = null
                    },
                    onDateSelected = { selectedDateEpochDay = it.toEpochDay() },
                    onDateDismissed = { selectedDateEpochDay = null },
                    onItemSelected = { expandedItemId = it.id },
                    onSaveShiftDay = viewModel::saveShiftDay,
                    onClearShiftDay = viewModel::clearShiftDay,
                )
            } else if (showingSearch) {
                SearchScreen(
                    modifier = Modifier.padding(innerPadding),
                    query = searchQuery,
                    results = searchItems(state.items, state.spaces, searchQuery),
                    spaces = state.spaces,
                    onQueryChange = { searchQuery = it },
                    onItemSelected = openItemAtHome,
                )
            } else {
            LazyColumn(
                state = homeListState,
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
                        TimeOverviewSection(
                            todayItems = todayItems,
                            upcomingItems = upcomingItems,
                            spaces = state.spaces,
                            onItemSelected = openItemAtHome,
                        )
                    }
                    item {
                        Card(
                            onClick = {
                                inboxExpanded = !inboxExpanded
                                expandedItemId = null
                                editingItemId = null
                            },
                            shape = LifeSpacesCardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.fillMaxHeight().width(6.dp).background(inboxAccent),
                                )
                                Text(
                                    "Nesortirano (${inboxItems.size})",
                                    modifier = Modifier.weight(1f).padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_expand_more),
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 16.dp).graphicsLayer {
                                        rotationZ = inboxExpansionRotation
                                    },
                                )
                            }
                        }
                    }
                    if (inboxExpanded) {
                        item {
                            TextButton(onClick = { sortByDate = !sortByDate }) {
                                Text(if (sortByDate) "Po datumu" else "Najnovije")
                            }
                            if (inboxItems.isEmpty()) {
                                Text(
                                    "Nesortirano je prazno. Sačuvane stavke će se pojaviti ovdje.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        items(inboxItems, key = { "inbox-${it.id}" }) { item ->
                            SpaceChild(accent = inboxAccent) {
                                ItemCard(
                                    item = item,
                                    spaces = state.spaces,
                                    showCompletion = false,
                                    showDateActions = true,
                                    isEditing = editingItemId == item.id,
                                    editingLabel = if (editingItemId == item.id) editingItemLabel else null,
                                    editingText = if (editingItemId == item.id) editingText else item.text,
                                    onToggleExpanded = {
                                        expandedItemId = item.id
                                        editingItemId = null
                                        editingItemLabel = null
                                    },
                                    onLabelChange = { editingItemLabel = it },
                                    onTextChange = { editingText = it },
                                    onSave = {
                                        viewModel.updateItemText(
                                            item.id,
                                            editingItemLabel?.let { labelledItemText(it, editingText) } ?: editingText,
                                        )
                                        editingItemId = null
                                        editingItemLabel = null
                                    },
                                    onCompletedChange = { viewModel.setItemCompleted(item.id, it) },
                                )
                            }
                        }
                    }
                    item { HorizontalDivider() }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Prostori",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            IconButton(onClick = {
                                spaceName = ""
                                spaceLocation = ""
                                spaceTemplate = "Shopping"
                                createCompletion = true
                                createDate = true
                                createLocation = true
                                createLinks = false
                                createSpaceColor = null
                                editingSpaceId = null
                                expandedItemId = null
                                showSpaceCreator = true
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add),
                                    contentDescription = "Dodaj prostor",
                                )
                            }
                        }
                        if (state.spaces.isEmpty()) {
                            Text(
                                "Još nema prostora. Dodirni + da kreiraš prvi.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    state.spaces.forEach { space ->
                        val spaceAccent = space.color?.let { Color(it.toULong()) } ?: defaultSpaceAccent
                        val capabilities = state.capabilities[space.id] ?: SpaceCapabilities.defaults(space.template)
                        val hasCompletion = SpaceCapabilities.COMPLETION in capabilities
                        val hasDate = SpaceCapabilities.DATE in capabilities
                        val hasLocation = SpaceCapabilities.LOCATION in capabilities
                        val hasLinks = SpaceCapabilities.LINKS in capabilities
                        val spaceItems = state.items.filter { it.spaceId == space.id }
                        val displayedItems = if (sortByDate && hasDate) {
                            spaceItems.sortedWith(compareBy<Item> { it.scheduledAt == null }.thenBy { it.scheduledAt ?: Long.MAX_VALUE })
                        } else {
                            spaceItems
                        }
                        val openItemCount = if (hasCompletion) {
                            spaceItems.count { it.completed != true }
                        } else {
                            spaceItems.size
                        }
                        item(key = "space-${space.id}") {
                            val expansionRotation by animateFloatAsState(
                                targetValue = if (expandedSpaceId == space.id) 180f else 0f,
                                label = "space expansion",
                            )
                            Card(
                                shape = LifeSpacesCardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                ) {
                                    Box(Modifier.fillMaxHeight().width(6.dp).background(spaceAccent))
                                    Row(
                                        modifier = Modifier.weight(1f).clickable(
                                            onClickLabel = if (expandedSpaceId == space.id) {
                                                "Sakrij stavke"
                                            } else {
                                                "Prikaži stavke"
                                            },
                                            role = Role.Button,
                                        ) {
                                            expandedSpaceId = space.id.takeUnless { expandedSpaceId == it }
                                            expandedItemId = null
                                            editingItemId = null
                                            editingSpaceId = null
                                            if (addingItemSpaceId == space.id) {
                                                addingItemSpaceId = null
                                                newSpaceItemLabel = ""
                                                newSpaceItemText = ""
                                            }
                                            spaceMenuId = null
                                        }.padding(16.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Text("${space.name} ($openItemCount)", style = MaterialTheme.typography.titleMedium)
                                            if (hasLocation) {
                                                space.location?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                        Icon(
                                            painter = painterResource(R.drawable.ic_expand_more),
                                            contentDescription = null,
                                            modifier = Modifier.graphicsLayer { rotationZ = expansionRotation },
                                        )
                                    }
                                    IconButton(onClick = {
                                        expandedSpaceId = space.id
                                        addingItemSpaceId = space.id
                                        newSpaceItemLabel = ""
                                        newSpaceItemText = ""
                                        editingSpaceId = null
                                        expandedItemId = null
                                        spaceMenuId = null
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_add),
                                            contentDescription = "Dodaj stavku u ${space.name}",
                                        )
                                    }
                                    Box {
                                        IconButton(onClick = { spaceMenuId = space.id }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_more_vert),
                                                contentDescription = "Akcije prostora ${space.name}",
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = spaceMenuId == space.id,
                                            onDismissRequest = { spaceMenuId = null },
                                        ) {
                                            if (hasDate) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(if (sortByDate) "Prikaži najnovije" else "Sortiraj po datumu")
                                                    },
                                                    onClick = {
                                                        sortByDate = !sortByDate
                                                        spaceMenuId = null
                                                    },
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text("Izmijeni prostor") },
                                                onClick = {
                                                    showSpaceCreator = false
                                                    expandedItemId = null
                                                    editingSpaceId = space.id
                                                    editingSpaceName = space.name
                                                    editingSpaceLocation = space.location.orEmpty()
                                                    editingSpaceCompletion = hasCompletion
                                                    editingSpaceDate = hasDate
                                                    editingSpaceLocationEnabled = hasLocation
                                                    editingSpaceLinks = hasLinks
                                                    editingSpaceColor = space.color
                                                    spaceMenuId = null
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Obriši prostor") },
                                                onClick = {
                                                    deletingSpaceId = space.id
                                                    spaceMenuId = null
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (expandedSpaceId == space.id) {
                            if (addingItemSpaceId == space.id) {
                                item(key = "space-add-item-${space.id}") {
                                    SpaceChild(accent = spaceAccent) {
                                        Card(
                                            shape = LifeSpacesCardShape,
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            ),
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                val saveItem = {
                                                    viewModel.addItemToSpace(
                                                        space.id,
                                                        if (hasLinks) {
                                                            labelledItemText(newSpaceItemLabel, newSpaceItemText)
                                                        } else {
                                                            newSpaceItemText
                                                        },
                                                    )
                                                    newSpaceItemLabel = ""
                                                    newSpaceItemText = ""
                                                    addingItemSpaceId = null
                                                }
                                                if (hasLinks) {
                                                    OutlinedTextField(
                                                        value = newSpaceItemLabel,
                                                        onValueChange = { newSpaceItemLabel = it },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        label = { Text("Labela (opciono)") },
                                                        singleLine = true,
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = newSpaceItemText,
                                                    onValueChange = { newSpaceItemText = it },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    label = {
                                                        Text(
                                                            if (hasLinks) "Novi link ili bilješka" else "Nova stavka u ${space.name}",
                                                        )
                                                    },
                                                    singleLine = !hasLinks,
                                                    minLines = if (hasLinks) 2 else 1,
                                                    keyboardOptions = KeyboardOptions(
                                                        imeAction = if (hasLinks) ImeAction.Default else ImeAction.Done,
                                                    ),
                                                    keyboardActions = KeyboardActions(onDone = {
                                                        if (newSpaceItemText.isNotBlank()) {
                                                            saveItem()
                                                        }
                                                    }),
                                                )
                                                if (hasLinks) {
                                                    Text(
                                                        "Unesi HTTP/HTTPS link ili bilješku; labela nije obavezna.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = saveItem,
                                                        enabled = newSpaceItemText.isNotBlank(),
                                                    ) { Text("Dodaj stavku") }
                                                    TextButton(onClick = {
                                                        newSpaceItemLabel = ""
                                                        newSpaceItemText = ""
                                                        addingItemSpaceId = null
                                                    }) { Text("Otkaži") }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (spaceItems.isEmpty() && addingItemSpaceId != space.id) {
                                item(key = "space-empty-${space.id}") {
                                    SpaceChild(accent = spaceAccent) {
                                        Text("Ovaj prostor još nema stavki.", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            items(displayedItems, key = { "space-${space.id}-item-${it.id}" }) { item ->
                                SpaceChild(accent = spaceAccent) {
                                    ItemCard(
                                    item = item,
                                    spaces = state.spaces,
                                    showCompletion = hasCompletion,
                                    showDateActions = hasDate || item.scheduledAt != null,
                                    isEditing = editingItemId == item.id,
                                    editingLabel = if (editingItemId == item.id) editingItemLabel else null,
                                    editingText = if (editingItemId == item.id) editingText else item.text,
                                    onToggleExpanded = {
                                        expandedItemId = item.id
                                        editingItemId = null
                                        editingItemLabel = null
                                    },
                                    onLabelChange = { editingItemLabel = it },
                                    onTextChange = { editingText = it },
                                    onSave = {
                                        viewModel.updateItemText(
                                            item.id,
                                            editingItemLabel?.let { labelledItemText(it, editingText) } ?: editingText,
                                        )
                                        editingItemId = null
                                        editingItemLabel = null
                                    },
                                    onCompletedChange = { viewModel.setItemCompleted(item.id, it) },
                                    )
                                }
                            }
                        }
                    }
            }
            }
        }
        if (showSpaceCreator) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSpaceCreator = false
                    templateMenuExpanded = false
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Novi prostor", style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(
                        value = spaceName,
                        onValueChange = { spaceName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Naziv prostora") },
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
                            listOf("Shopping", "General", "Links").forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template) },
                                    onClick = {
                                        spaceTemplate = template
                                        createCompletion = template == "Shopping"
                                        createDate = template != "Links"
                                        createLocation = template == "Shopping"
                                        createLinks = template == "Links"
                                        templateMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Text(
                        "Šablon bira početne osobine; možeš ih odmah promijeniti.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    CapabilityCheckbox("Završavanje stavki", createCompletion) { createCompletion = it }
                    CapabilityCheckbox("Datum", createDate) { createDate = it }
                    CapabilityCheckbox("Lokacija", createLocation) { createLocation = it }
                    CapabilityCheckbox("Linkovi", createLinks) { createLinks = it }
                    if (createLocation) {
                        OutlinedTextField(
                            value = spaceLocation,
                            onValueChange = { spaceLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Lokacija (opciono)") },
                            singleLine = true,
                        )
                    }
                    SpaceColorPicker(
                        selectedColor = createSpaceColor,
                        onColorSelected = { createSpaceColor = it },
                    )
                    val previewAccent = createSpaceColor?.let { Color(it.toULong()) } ?: defaultSpaceAccent
                    Card(
                        shape = LifeSpacesCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.fillMaxHeight().width(6.dp).background(previewAccent))
                            Text(
                                spaceName.ifBlank { "Naziv prostora" },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.createSpace(
                                spaceName,
                                spaceTemplate,
                                spaceLocation,
                                selectedCapabilities(createCompletion, createDate, createLocation, createLinks),
                                createSpaceColor,
                            )
                            showSpaceCreator = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = spaceName.isNotBlank(),
                    ) { Text("Kreiraj prostor") }
                    TextButton(
                        onClick = { showSpaceCreator = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Otkaži") }
                }
            }
        } else {
            editingSpaceId?.let { spaceId ->
                state.spaces.firstOrNull { it.id == spaceId }?.let { space ->
                    val capabilities = state.capabilities[spaceId] ?: SpaceCapabilities.defaults(space.template)
                    val hasCompletion = SpaceCapabilities.COMPLETION in capabilities
                    val hasDate = SpaceCapabilities.DATE in capabilities
                    val hasLocation = SpaceCapabilities.LOCATION in capabilities
                    val hasLinks = SpaceCapabilities.LINKS in capabilities
                    val spaceItems = state.items.filter { it.spaceId == spaceId }
                    ModalBottomSheet(onDismissRequest = { editingSpaceId = null }) {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Izmijeni prostor", style = MaterialTheme.typography.headlineSmall)
                            OutlinedTextField(
                                value = editingSpaceName,
                                onValueChange = { editingSpaceName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Naziv prostora") },
                                singleLine = true,
                            )
                            CapabilityCheckbox(
                                "Završavanje stavki",
                                editingSpaceCompletion,
                            ) { editingSpaceCompletion = it }
                            CapabilityCheckbox("Datum", editingSpaceDate) { editingSpaceDate = it }
                            CapabilityCheckbox(
                                "Lokacija",
                                editingSpaceLocationEnabled,
                            ) { editingSpaceLocationEnabled = it }
                            CapabilityCheckbox("Linkovi", editingSpaceLinks) { editingSpaceLinks = it }
                            if (editingSpaceLocationEnabled) {
                                OutlinedTextField(
                                    value = editingSpaceLocation,
                                    onValueChange = { editingSpaceLocation = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Lokacija (opciono)") },
                                    singleLine = true,
                                )
                            }
                            SpaceColorPicker(
                                selectedColor = editingSpaceColor,
                                onColorSelected = { editingSpaceColor = it },
                            )
                            Button(
                                onClick = {
                                    val removesData =
                                        (!editingSpaceCompletion && hasCompletion && spaceItems.any { it.completed != null }) ||
                                            (!editingSpaceDate && hasDate && spaceItems.any { it.scheduledAt != null }) ||
                                            (!editingSpaceLocationEnabled && hasLocation && !space.location.isNullOrBlank())
                                    if (removesData) {
                                        confirmingSpaceEditId = spaceId
                                    } else {
                                        viewModel.updateSpace(
                                            spaceId,
                                            editingSpaceName,
                                            editingSpaceLocation,
                                            editingSpaceColor,
                                            selectedCapabilities(
                                                editingSpaceCompletion,
                                                editingSpaceDate,
                                                editingSpaceLocationEnabled,
                                                editingSpaceLinks,
                                            ),
                                        )
                                        editingSpaceId = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = editingSpaceName.isNotBlank(),
                            ) { Text("Sačuvaj izmjene") }
                            TextButton(
                                onClick = { editingSpaceId = null },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Otkaži") }
                        }
                    }
                }
            }
        }
        state.items.firstOrNull { it.id == expandedItemId }?.let { selectedItem ->
            val showDateActions = selectedItem.spaceId == null ||
                selectedItem.scheduledAt != null ||
                SpaceCapabilities.DATE in state.capabilities[selectedItem.spaceId].orEmpty()
            ItemActionsSheet(
                item = selectedItem,
                spaces = state.spaces,
                showDateActions = showDateActions,
                onDismiss = { expandedItemId = null },
                onEdit = {
                    expandedItemId = null
                    editingItemId = selectedItem.id
                    val (label, content) = linkItemParts(selectedItem.text)
                    editingItemLabel = label
                    editingText = content
                },
                onMove = { destination ->
                    viewModel.moveItem(selectedItem.id, destination)
                    expandedItemId = null
                },
                onSchedule = { scheduledAt ->
                    viewModel.setItemScheduledAt(selectedItem.id, scheduledAt)
                    expandedItemId = null
                },
                onDelete = {
                    expandedItemId = null
                    deletingItemId = selectedItem.id
                },
            )
        }
        if (showSystemAlarmDialog) {
            val tomorrow = LocalDate.now().plusDays(1)
            val suggestion = shiftAlarmSuggestion(
                tomorrow,
                calendar.shiftTypes,
                calendar.overrides,
                calendar.shiftDays,
            )?.takeIf { isSystemAlarmTimeAllowed(it.date, it.minute) }
            SystemAlarmDialog(
                label = suggestion?.let { "${it.shiftName} smjena" } ?: "Alarm",
                preferredDate = suggestion?.date,
                preferredMinute = suggestion?.minute,
                onDismiss = { showSystemAlarmDialog = false },
                onSaveAlarm = { intent ->
                    try {
                        context.startActivity(intent)
                        showSystemAlarmDialog = false
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "Nije pronađena aplikacija za alarm.", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
        deletingItemId?.let { itemId ->
            AlertDialog(
                onDismissRequest = { deletingItemId = null },
                title = { Text("Obriši stavku?") },
                text = { Text("Ova stavka će biti trajno obrisana.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteItem(itemId)
                        if (expandedItemId == itemId) expandedItemId = null
                        if (editingItemId == itemId) editingItemId = null
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
                        if (expandedSpaceId == spaceId) expandedSpaceId = null
                        expandedItemId = null
                        editingItemId = null
                        editingSpaceId = null
                        if (addingItemSpaceId == spaceId) addingItemSpaceId = null
                        deletingSpaceId = null
                    }) { Text("Obriši") }
                },
                dismissButton = {
                    TextButton(onClick = { deletingSpaceId = null }) { Text("Otkaži") }
                },
            )
        }

        confirmingSpaceEditId?.let { spaceId ->
            val space = state.spaces.firstOrNull { it.id == spaceId }
            val spaceItems = state.items.filter { it.spaceId == spaceId }
            val capabilities = state.capabilities[spaceId].orEmpty()
            val completedCount = spaceItems.count { it.completed != null }
            val datedCount = spaceItems.count { it.scheduledAt != null }
            val losses = listOfNotNull(
                if (!editingSpaceCompletion && SpaceCapabilities.COMPLETION in capabilities && completedCount > 0) {
                    "Završavanje: status za $completedCount stavki"
                } else null,
                if (!editingSpaceDate && SpaceCapabilities.DATE in capabilities && datedCount > 0) {
                    "Datum: datum za $datedCount stavki"
                } else null,
                if (!editingSpaceLocationEnabled && SpaceCapabilities.LOCATION in capabilities && !space?.location.isNullOrBlank()) {
                    "Lokacija: sačuvana lokacija"
                } else null,
            )
            AlertDialog(
                onDismissRequest = { confirmingSpaceEditId = null },
                title = { Text("Ukloniti osobine prostora?") },
                text = { Text("Biće trajno uklonjeno:\n${losses.joinToString("\n")}") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateSpace(
                            spaceId,
                            editingSpaceName,
                            editingSpaceLocation,
                            editingSpaceColor,
                            selectedCapabilities(
                                editingSpaceCompletion,
                                editingSpaceDate,
                                editingSpaceLocationEnabled,
                                editingSpaceLinks,
                            ),
                            clearCompleted = !editingSpaceCompletion && SpaceCapabilities.COMPLETION in capabilities,
                            clearScheduledAt = !editingSpaceDate && SpaceCapabilities.DATE in capabilities,
                        )
                        confirmingSpaceEditId = null
                        editingSpaceId = null
                    }) { Text("Ukloni i sačuvaj") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmingSpaceEditId = null }) { Text("Otkaži") }
                },
            )
        }
    }
}

@Composable
private fun SpaceChild(accent: Color, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp).height(IntrinsicSize.Min),
    ) {
        Box(
            Modifier.fillMaxHeight().width(4.dp).background(
                accent.copy(alpha = 0.7f),
                RoundedCornerShape(2.dp),
            ),
        )
        Box(Modifier.weight(1f).padding(start = 10.dp)) { content() }
    }
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    query: String,
    results: List<Item>,
    spaces: List<Space>,
    onQueryChange: (String) -> Unit,
    onItemSelected: (Item) -> Unit,
) {
    val spacesById = spaces.associateBy(Space::id)
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pretraži stavke i prostore") },
                singleLine = true,
            )
        }
        when {
            query.isBlank() -> item {
                Text("Unesi tekst za pretragu.", style = MaterialTheme.typography.bodyMedium)
            }
            results.isEmpty() -> item {
                Text("Nema odgovarajućih stavki.", style = MaterialTheme.typography.bodyMedium)
            }
            else -> items(results, key = Item::id) { item ->
                val space = item.spaceId?.let(spacesById::get)
                Card(
                    onClick = { onItemSelected(item) },
                    shape = LifeSpacesCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(item.text, style = MaterialTheme.typography.titleMedium)
                        Text(
                            space?.name ?: "Nesortirano",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeOverviewSection(
    todayItems: List<Item>,
    upcomingItems: List<Item>,
    spaces: List<Space>,
    onItemSelected: (Item) -> Unit,
) {
    val spacesById = spaces.associateBy(Space::id)
    Card(
        shape = LifeSpacesCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OverviewItems(
                title = "Danas · ${SimpleDateFormat("d. MMM yyyy.", Locale.getDefault()).format(Date())}",
                items = todayItems,
                spacesById = spacesById,
                emptyMessage = "Nema stavki za danas.",
                onItemSelected = onItemSelected,
            )
            HorizontalDivider()
            OverviewItems(
                title = "Predstojeće",
                items = upcomingItems,
                spacesById = spacesById,
                emptyMessage = "Nema predstojećih stavki.",
                showItemDates = true,
                onItemSelected = onItemSelected,
            )
        }
    }
}

@Composable
private fun OverviewItems(
    title: String,
    items: List<Item>,
    spacesById: Map<Long, Space>,
    emptyMessage: String,
    showItemDates: Boolean = false,
    onItemSelected: (Item) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    if (items.isEmpty()) {
        Text(emptyMessage, style = MaterialTheme.typography.bodyMedium)
    } else {
        items.forEach { item ->
            val space = item.spaceId?.let(spacesById::get)
            val completed = item.completed == true
            Column(
                modifier = Modifier.fillMaxWidth().clickable(
                    onClickLabel = "Otvori stavku",
                    role = Role.Button,
                    onClick = { onItemSelected(item) },
                ).padding(vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.text,
                        modifier = Modifier.weight(1f),
                        color = if (completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                    )
                    if (showItemDates) {
                        Text(
                            SimpleDateFormat("d. MMM", Locale.getDefault()).format(Date(requireNotNull(item.scheduledAt))),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    space?.name ?: "Nesortirano",
                    style = MaterialTheme.typography.bodySmall,
                    color = space?.color?.let { Color(it.toULong()) } ?: MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

internal fun searchItems(items: List<Item>, spaces: List<Space>, query: String): List<Item> {
    val needle = query.trim()
    if (needle.isEmpty()) return emptyList()
    val spacesById = spaces.associateBy(Space::id)
    return items.filter { item ->
        item.text.contains(needle, ignoreCase = true) ||
            item.spaceId?.let { spacesById[it]?.name?.contains(needle, ignoreCase = true) } == true
    }
}

internal fun searchResultTarget(item: Item, spaces: List<Space>): Pair<Long?, Int> {
    val spaceIndex = spaces.indexOfFirst { it.id == item.spaceId }
    return if (spaceIndex >= 0) item.spaceId to 5 + spaceIndex else null to 2
}

@Composable
private fun CapabilityCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().toggleable(
            value = checked,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label)
    }
}

private fun selectedCapabilities(
    completion: Boolean,
    date: Boolean,
    location: Boolean,
    links: Boolean,
): Set<String> =
    setOfNotNull(
        SpaceCapabilities.TEXT,
        SpaceCapabilities.COMPLETION.takeIf { completion },
        SpaceCapabilities.DATE.takeIf { date },
        SpaceCapabilities.LOCATION.takeIf { location },
        SpaceCapabilities.LINKS.takeIf { links },
    )

private val SpaceColorOptions = listOf(
    "Bez boje" to null,
    "Ljubičasta" to Color(0xFF7C6DE8).value.toLong(),
    "Plava" to Color(0xFF4C8DFF).value.toLong(),
    "Zelena" to Color(0xFF2EA66F).value.toLong(),
    "Tirkizna" to Color(0xFF16A3A3).value.toLong(),
    "Narandžasta" to Color(0xFFE67E3D).value.toLong(),
    "Roze" to Color(0xFFD85A8B).value.toLong(),
)

@Composable
private fun SpaceColorPicker(selectedColor: Long?, onColorSelected: (Long?) -> Unit) {
    Text("Boja prostora", style = MaterialTheme.typography.labelLarge)
    SpaceColorOptions.chunked(4).forEach { options ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (name, value) ->
                val selected = selectedColor == value
                Column(
                    modifier = Modifier.width(64.dp).selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onColorSelected(value) },
                    ).semantics { contentDescription = name },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(
                            value?.let { Color(it.toULong()) } ?: MaterialTheme.colorScheme.surface,
                            CircleShape,
                        ).border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (value == null) Text("×")
                    }
                    Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    spaces: List<Space>,
    showCompletion: Boolean,
    showDateActions: Boolean,
    isEditing: Boolean,
    editingLabel: String?,
    editingText: String,
    onToggleExpanded: () -> Unit,
    onLabelChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCompletedChange: (Boolean) -> Unit,
) {
    val completed = showCompletion && item.completed == true
    val activeCardColor = spaces.firstOrNull { it.id == item.spaceId }?.color?.let { color ->
        lerp(
            MaterialTheme.colorScheme.surface,
            Color(color.toULong()),
            if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.25f else 0.20f,
        )
    } ?: MaterialTheme.colorScheme.surface
    val cardColor by animateColorAsState(
        targetValue = if (completed) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            activeCardColor
        },
        label = "item color",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (completed) 0.985f else 1f,
        label = "item scale",
    )
    Card(
        onClick = { if (!isEditing) onToggleExpanded() },
        modifier = Modifier.fillMaxWidth().animateContentSize().graphicsLayer {
            scaleX = cardScale
            scaleY = cardScale
        },
        shape = LifeSpacesCardShape,
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isEditing) {
                editingLabel?.let { label ->
                    OutlinedTextField(
                        value = label,
                        onValueChange = onLabelChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Labela (opciono)") },
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = editingLabel == null,
                    label = editingLabel?.let { { Text("Link") } },
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (editingLabel == null) ImeAction.Done else ImeAction.Default,
                    ),
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
                        textDecoration = if (completed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                    )
                }
                item.scheduledAt?.takeIf { showDateActions }?.let {
                    Text(
                        "Datum: ${SimpleDateFormat("d. MMM yyyy.", Locale.getDefault()).format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ItemActionsSheet(
    item: Item,
    spaces: List<Space>,
    showDateActions: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onMove: (Long?) -> Unit,
    onSchedule: (Long?) -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var moveMenuExpanded by rememberSaveable(item.id) { mutableStateOf(false) }
    val linkIntent = createWebLinkIntent(item.text)
    val showDatePicker = {
        val calendar = Calendar.getInstance().apply {
            item.scheduledAt?.let { timeInMillis = it }
        }
        onDismiss()
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
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.text, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge)
            item.scheduledAt?.let {
                Text(
                    SimpleDateFormat("d. MMM yyyy.", Locale.getDefault()).format(Date(it)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SheetAction("Uredi", onClick = onEdit)
            linkIntent?.let { intent ->
                SheetAction("Otvori link") {
                    onDismiss()
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "Nije pronađena aplikacija koja može otvoriti link.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                SheetAction("Kopiraj link") {
                    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(
                        ClipData.newPlainText("Link", intent.data.toString()),
                    )
                    Toast.makeText(context, "Link je kopiran.", Toast.LENGTH_SHORT).show()
                }
            }
            Box {
                SheetAction("Premjesti") { moveMenuExpanded = true }
                DropdownMenu(
                    expanded = moveMenuExpanded,
                    onDismissRequest = { moveMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Nesortirano") },
                        onClick = {
                            moveMenuExpanded = false
                            onMove(null)
                        },
                    )
                    spaces.forEach { space ->
                        DropdownMenuItem(
                            text = { Text(space.name) },
                            onClick = {
                                moveMenuExpanded = false
                                onMove(space.id)
                            },
                        )
                    }
                }
            }
            if (showDateActions) {
                SheetAction(if (item.scheduledAt == null) "Dodaj datum" else "Promijeni datum") {
                    showDatePicker()
                }
                if (item.scheduledAt != null) {
                    SheetAction("Ukloni datum") { onSchedule(null) }
                    SheetAction("Dodaj u kalendar") {
                        onDismiss()
                        val location = spaces.firstOrNull { it.id == item.spaceId }?.location
                        try {
                            context.startActivity(createCalendarInsertIntent(item, location))
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                "Nije pronađena aplikacija kalendara.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
            SheetAction("Obriši", color = MaterialTheme.colorScheme.error, onClick = onDelete)
        }
    }
}

@Composable
private fun SheetAction(
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.fillMaxWidth(), color = color)
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

internal fun safeWebUri(text: String): Uri? {
    val value = text.trim().lineSequence().lastOrNull()?.trim().orEmpty()
    if (value.isEmpty() || value.any(Char::isWhitespace)) return null
    val uri = Uri.parse(value)
    return uri.takeIf {
        it.scheme?.lowercase(Locale.ROOT) in setOf("http", "https") &&
            !it.host.isNullOrBlank() &&
            it.userInfo == null
    }
}

internal fun labelledItemText(label: String, content: String): String =
    label.trim().takeIf(String::isNotEmpty)?.let { "$it\n${content.trim()}" } ?: content.trim()

internal fun linkItemParts(text: String): Pair<String?, String> {
    val value = text.trim()
    val url = safeWebUri(value) ?: return null to value
    val lines = value.lines()
    return lines.dropLast(1).joinToString("\n").trim() to url.toString()
}

internal fun createWebLinkIntent(text: String): Intent? =
    safeWebUri(text)?.let { Intent(Intent.ACTION_VIEW, it) }

internal val LifeSpacesCardShape = RoundedCornerShape(24.dp)

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
internal fun LifeSpacesTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
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
            showDateActions = true,
            isEditing = false,
            editingLabel = null,
            editingText = "Mlijeko",
            onToggleExpanded = {},
            onLabelChange = {},
            onTextChange = {},
            onSave = {},
            onCompletedChange = {},
        )
    }
}
