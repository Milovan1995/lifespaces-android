package com.lifespaces.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifespaces.android.data.Item
import com.lifespaces.android.data.Space
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun mondayOf(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value - 1).toLong())

internal fun calendarWeekDays(weekStart: LocalDate): List<LocalDate> =
    mondayOf(weekStart).let { monday -> (0L..6L).map(monday::plusDays) }

internal fun moveCalendarWeek(weekStart: LocalDate, weeks: Long): LocalDate =
    mondayOf(weekStart).plusWeeks(weeks)

internal fun datedItemsByDate(
    items: List<Item>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Map<LocalDate, List<Item>> = items.mapNotNull { item ->
    item.scheduledAt?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() to item }
}.groupBy({ it.first }, { it.second })

@Composable
internal fun CalendarScreen(
    modifier: Modifier = Modifier,
    items: List<Item>,
    spaces: List<Space>,
    weekStart: LocalDate,
    selectedDate: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onItemSelected: (Item) -> Unit,
) {
    val today = LocalDate.now()
    val days = calendarWeekDays(weekStart)
    val datedItems = datedItemsByDate(items)
    val dateFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.getDefault())
    val weekdayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPreviousWeek) { Text("‹") }
                Text(
                    "${dateFormatter.format(days.first())} – ${dateFormatter.format(days.last())}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onToday) { Text("Danas") }
                TextButton(onClick = onNextWeek) { Text("›") }
            }
        }
        items(days, key = LocalDate::toEpochDay) { date ->
            val isToday = date == today
            val isSelected = date == selectedDate
            Card(
                onClick = { onDateSelected(date) },
                modifier = Modifier.fillMaxWidth().then(
                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, LifeSpacesCardShape) else Modifier,
                ),
                shape = LifeSpacesCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        weekdayFormatter.format(date).replaceFirstChar { it.titlecase(Locale.getDefault()) },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(dateFormatter.format(date), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item { Text(dateFormatter.format(selectedDate), style = MaterialTheme.typography.titleLarge) }
        val selectedItems = datedItems[selectedDate].orEmpty()
        if (selectedItems.isEmpty()) {
            item { Text("Nema stavki za ovaj datum.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(selectedItems, key = Item::id) { item ->
                CalendarItemCard(item, spaces.firstOrNull { it.id == item.spaceId }) { onItemSelected(item) }
            }
        }
    }
}

@Composable
private fun CalendarItemCard(item: Item, space: Space?, onClick: () -> Unit) {
    val accent = space?.color?.let { Color(it.toULong()) } ?: MaterialTheme.colorScheme.secondary
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = LifeSpacesCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.width(6.dp).background(accent)) {}
            Column(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(space?.name ?: "Nesortirano", style = MaterialTheme.typography.labelMedium, color = accent)
                Text(
                    item.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.completed == true) TextDecoration.LineThrough else TextDecoration.None,
                )
            }
        }
    }
}
