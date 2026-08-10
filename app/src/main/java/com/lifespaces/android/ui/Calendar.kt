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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifespaces.android.data.Item
import com.lifespaces.android.data.Space
import com.lifespaces.android.data.ShiftDay
import com.lifespaces.android.data.ShiftType
import com.lifespaces.android.data.ShiftWeekdayOverride
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
    shiftTypes: List<ShiftType>,
    overrides: List<ShiftWeekdayOverride>,
    shiftDays: List<ShiftDay>,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onItemSelected: (Item) -> Unit,
    onSaveShiftDay: (String, Long?, String?) -> Unit,
    onClearShiftDay: (String) -> Unit,
) {
    val today = LocalDate.now()
    val days = calendarWeekDays(weekStart)
    val datedItems = datedItemsByDate(items)
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.", Locale.getDefault())
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
                Button(onClick = onToday) { Text("Danas") }
                TextButton(onClick = onNextWeek) { Text("›") }
            }
        }
        items(days, key = LocalDate::toEpochDay) { date ->
            val isToday = date == today
            val isSelected = date == selectedDate
            val day = shiftDays.firstOrNull { it.localDate == date.toString() }
            val shift = shiftTypes.firstOrNull { it.id == day?.shiftTypeId }
            val times = shift?.let { effectiveShiftTimes(it, overrides, date) }
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
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.width(6.dp).background(shift?.color?.let(::shiftColor) ?: Color.Transparent)) {}
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                weekdayName(date),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(dateFormatter.format(date), style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(
                            when {
                                day == null -> "Nije uneseno"
                                shift == null -> "Slobodan dan"
                                else -> "${shift.name} · ${formatMinute(times!!.start)}–${formatMinute(times.end)}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = shift?.color?.let(::shiftColor) ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            val day = shiftDays.firstOrNull { it.localDate == selectedDate.toString() }
            val shift = shiftTypes.firstOrNull { it.id == day?.shiftTypeId }
            val times = shift?.let { effectiveShiftTimes(it, overrides, selectedDate) }
            var changingSchedule by rememberSaveable(selectedDate.toString(), day?.id) { mutableStateOf(day == null) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LifeSpacesCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(dateFormatter.format(selectedDate), style = MaterialTheme.typography.titleLarge)
                    Text(
                        when {
                            day == null -> "Nije uneseno"
                            shift == null -> "Slobodan dan"
                            else -> "${shift.name}: ${formatMinute(times!!.start)}–${formatMinute(times.end)}"
                        },
                        color = shift?.color?.let(::shiftColor) ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    day?.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    if (!changingSchedule) {
                        Button(
                            onClick = { changingSchedule = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Promijeni raspored") }
                    } else {
                        Text("Izaberi raspored", style = MaterialTheme.typography.titleMedium)
                        shiftTypes.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                row.forEach { type ->
                                    Button(
                                        onClick = { onSaveShiftDay(selectedDate.toString(), type.id, day?.note) },
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = shiftColor(type.color),
                                            contentColor = Color.White,
                                        ),
                                    ) { Text(type.name) }
                                }
                            }
                        }
                        Button(
                            onClick = { onSaveShiftDay(selectedDate.toString(), null, day?.note) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Slobodan dan") }
                        if (day != null) {
                            TextButton(onClick = { onClearShiftDay(selectedDate.toString()) }) { Text("Poništi") }
                        }
                    }
                }
            }
        }
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

private data class ShiftTimes(val start: Int, val end: Int, val alarm: Int)

private fun shiftColor(value: Long): Color =
    if (value ushr 32 == 0L) Color(value.toInt()) else Color(value.toULong())

private fun effectiveShiftTimes(
    shift: ShiftType,
    overrides: List<ShiftWeekdayOverride>,
    date: LocalDate,
): ShiftTimes {
    val override = overrides.firstOrNull { it.shiftTypeId == shift.id && it.weekday == date.dayOfWeek.value }
    return ShiftTimes(
        start = override?.startMinute ?: shift.defaultStartMinute,
        end = override?.endMinute ?: shift.defaultEndMinute,
        alarm = override?.alarmMinute ?: shift.defaultAlarmMinute,
    )
}

private fun formatMinute(value: Int): String = "%02d:%02d".format(value / 60, value % 60)

private fun weekdayName(date: LocalDate): String = listOf(
    "Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja",
)[date.dayOfWeek.value - 1]
