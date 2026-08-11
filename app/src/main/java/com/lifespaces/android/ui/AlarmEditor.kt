package com.lifespaces.android.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.provider.AlarmClock
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val MAX_ALARM_HOURS = 24L

internal fun isSystemAlarmTimeAllowed(
    date: LocalDate,
    minute: Int,
    now: ZonedDateTime = ZonedDateTime.now(),
): Boolean {
    val target = date.atTime(minute / 60, minute % 60).atZone(now.zone)
    val distance = Duration.between(now.toInstant(), target.toInstant())
    val limit = now.withSecond(0).withNano(0).plusHours(MAX_ALARM_HOURS).toInstant()
    return !distance.isNegative && !distance.isZero && target.toInstant().isBefore(limit)
}

internal fun createSystemAlarmIntent(date: LocalDate, minute: Int, label: String): Intent =
    Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, minute / 60)
        putExtra(AlarmClock.EXTRA_MINUTES, minute % 60)
        putExtra(
            AlarmClock.EXTRA_MESSAGE,
            "$label · ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))}",
        )
        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
    }

private fun defaultSystemAlarmTime(
    preferredDate: LocalDate?,
    preferredMinute: Int?,
    now: ZonedDateTime,
): Pair<LocalDate, Int> {
    if (preferredDate != null && preferredMinute != null && isSystemAlarmTimeAllowed(preferredDate, preferredMinute, now)) {
        return preferredDate to preferredMinute
    }
    val fallback = now.plusMinutes(5)
    if (preferredDate == null || preferredDate == fallback.toLocalDate()) {
        return fallback.toLocalDate() to (fallback.hour * 60 + fallback.minute)
    }
    val latestMinuteTomorrow = now.hour * 60 + now.minute - 1
    return if (preferredDate == now.plusDays(1).toLocalDate() && latestMinuteTomorrow >= 0) {
        preferredDate to latestMinuteTomorrow
    } else {
        fallback.toLocalDate() to (fallback.hour * 60 + fallback.minute)
    }
}

@Composable
internal fun SystemAlarmDialog(
    label: String,
    preferredDate: LocalDate?,
    preferredMinute: Int?,
    onDismiss: () -> Unit,
    onSaveAlarm: (Intent) -> Unit,
) {
    val context = LocalContext.current
    val now = remember { ZonedDateTime.now() }
    val default = remember(label, preferredDate, preferredMinute) {
        defaultSystemAlarmTime(preferredDate, preferredMinute, now)
    }
    var date by rememberSaveable(label) { mutableStateOf(default.first.toString()) }
    var minute by rememberSaveable(label) { mutableStateOf(default.second) }
    var description by rememberSaveable(label) { mutableStateOf(label) }
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val allowed = parsedDate?.let { isSystemAlarmTimeAllowed(it, minute) } == true
    val dateLabel = parsedDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) ?: date

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Podesi alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    val value = parsedDate ?: LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> date = LocalDate.of(year, month + 1, day).toString() },
                        value.year,
                        value.monthValue - 1,
                        value.dayOfMonth,
                    ).apply {
                        datePicker.minDate = now.toLocalDate().atStartOfDay(now.zone).toInstant().toEpochMilli()
                        datePicker.maxDate = now.plusHours(MAX_ALARM_HOURS).toLocalDate()
                            .atStartOfDay(now.zone).toInstant().toEpochMilli()
                    }.show()
                }) { Text("Datum: $dateLabel") }
                Text("Vrijeme: ${formatMinute(minute)}", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AlarmNumberPicker(
                        label = "Sati",
                        value = minute / 60,
                        range = 0..23,
                        onValueChange = { minute = it * 60 + minute % 60 },
                        modifier = Modifier.weight(1f),
                    )
                    AlarmNumberPicker(
                        label = "Minuti",
                        value = minute % 60,
                        range = 0..59,
                        onValueChange = { minute = minute / 60 * 60 + it },
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Naziv alarma") },
                    singleLine = true,
                )
                Text("Alarm će odmah biti dodat u Samsung Clock.", style = MaterialTheme.typography.bodySmall)
                if (!allowed) {
                    Text("Izaberi vrijeme u naredna 24 sata.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    parsedDate?.let {
                        onSaveAlarm(createSystemAlarmIntent(it, minute, description.trim().ifBlank { label }))
                    }
                },
                enabled = allowed,
            ) { Text("Sačuvaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Otkaži") } },
    )
}

@Composable
private fun AlarmNumberPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    displayedValues = range.map { "%02d".format(it) }.toTypedArray()
                    wrapSelectorWheel = true
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                }
            },
            update = { picker ->
                if (picker.value != value) picker.value = value
                picker.setOnValueChangedListener { _, _, selected -> onValueChange(selected) }
            },
        )
    }
}
