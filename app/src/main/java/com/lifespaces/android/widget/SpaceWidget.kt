package com.lifespaces.android.widget

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.lifespaces.android.LifeSpacesApplication
import com.lifespaces.android.MainActivity
import com.lifespaces.android.R
import com.lifespaces.android.data.Space
import com.lifespaces.android.ui.LifeSpacesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS = "space_widgets"
private const val SPACE_PREFIX = "space_"

object SpaceWidget {
    fun setSpace(context: Context, appWidgetId: Int, spaceId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong("$SPACE_PREFIX$appWidgetId", spaceId).apply()
    }

    fun remove(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove("$SPACE_PREFIX$appWidgetId").apply()
    }

    fun spaceId(context: Context, appWidgetId: Int): Long? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong("$SPACE_PREFIX$appWidgetId", -1L).takeIf { it >= 0 }

    suspend fun updateOne(context: Context, appWidgetId: Int) {
        val spaces = (context.applicationContext as LifeSpacesApplication).repository.spaces.first()
        withContext(Dispatchers.Main) {
            update(context, AppWidgetManager.getInstance(context), appWidgetId, spaces)
        }
    }

    suspend fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, SpaceWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val spaces = (context.applicationContext as LifeSpacesApplication).repository.spaces.first()
        withContext(Dispatchers.Main) {
            ids.forEach { update(context, manager, it, spaces) }
        }
    }

    fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int, spaces: List<Space>) {
        val configuredSpaceId = spaceId(context, appWidgetId)
        val inbox = configuredSpaceId == 0L
        val space = configuredSpaceId?.takeIf { it > 0 }?.let { id -> spaces.firstOrNull { it.id == id } }
        val configured = inbox || space != null
        val views = RemoteViews(context.packageName, R.layout.space_widget)
        views.setTextViewText(
            R.id.widget_space_name,
            when {
                inbox -> "Nesortirano"
                space != null -> space.name
                else -> "Izaberi prostor"
            },
        )
        views.setInt(
            R.id.widget_accent,
            "setBackgroundColor",
            space?.color?.let { Color(it.toULong()).toArgb() } ?: 0xFF5D4BB7.toInt(),
        )
        views.setViewVisibility(R.id.widget_add, if (configured) View.VISIBLE else View.GONE)

        val titleIntent = if (!configured) {
            Intent(context, SpaceWidgetConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        } else {
            Intent(context, MainActivity::class.java)
        }
        views.setOnClickPendingIntent(
            R.id.widget_space_name,
            PendingIntent.getActivity(
                context,
                appWidgetId,
                titleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        if (configured) {
            val captureIntent = Intent(context, SpaceWidgetCaptureActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            views.setOnClickPendingIntent(
                R.id.widget_add,
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    captureIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }
        manager.updateAppWidget(appWidgetId, views)
    }
}

class SpaceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val result = goAsync()
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                SpaceWidget.updateAll(context)
            } finally {
                result.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { SpaceWidget.remove(context, it) }
    }
}

class SpaceWidgetConfigActivity : ComponentActivity() {
    private val appWidgetId by lazy {
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(
            Activity.RESULT_CANCELED,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val repository = (application as LifeSpacesApplication).repository
        setContent {
            WidgetTheme {
                val spaces by repository.spaces.collectAsState(initial = emptyList())
                SpacePicker(spaces) { spaceId ->
                    SpaceWidget.setSpace(this, appWidgetId, spaceId)
                    lifecycleScope.launch {
                        SpaceWidget.updateOne(this@SpaceWidgetConfigActivity, appWidgetId)
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
                        )
                        finish()
                    }
                }
            }
        }
    }
}

class SpaceWidgetCaptureActivity : ComponentActivity() {
    private val appWidgetId by lazy {
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }
    private var targetSpaceId by mutableStateOf<Long?>(null)
    private var targetName by mutableStateOf("")
    private var validTarget by mutableStateOf(false)
    private var loaded by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as LifeSpacesApplication).repository
        lifecycleScope.launch {
            val configuredSpaceId = SpaceWidget.spaceId(this@SpaceWidgetCaptureActivity, appWidgetId)
            if (configuredSpaceId == 0L) {
                targetName = "Nesortirano"
                validTarget = true
            } else {
                repository.spaces.first().firstOrNull { it.id == configuredSpaceId }?.let { space ->
                    targetSpaceId = space.id
                    targetName = space.name
                    validTarget = true
                }
            }
            loaded = true
            if (!validTarget) SpaceWidget.updateAll(this@SpaceWidgetCaptureActivity)
        }
        setContent {
            WidgetTheme {
                if (!loaded) {
                    Text("Učitavanje…", modifier = Modifier.padding(24.dp))
                } else if (!validTarget) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Ovaj prostor više ne postoji.")
                        Button(onClick = {
                            startActivity(
                                Intent(this@SpaceWidgetCaptureActivity, SpaceWidgetConfigActivity::class.java)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
                            )
                            finish()
                        }) { Text("Izaberi drugi prostor") }
                    }
                } else {
                    CaptureForm(targetName) { text ->
                        lifecycleScope.launch {
                            repository.createItem(text, targetSpaceId)
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpacePicker(spaces: List<Space>, onSelect: (Long) -> Unit) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Izaberi prostor za widget", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Card(onClick = { onSelect(0L) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Nesortirano (podrazumijevano)", modifier = Modifier.padding(18.dp))
                }
            }
            if (spaces.isEmpty()) {
                item { Text("Još nema kreiranih prostora.") }
            } else {
                items(spaces, key = Space::id) { space ->
                    Card(onClick = { onSelect(space.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(space.name, modifier = Modifier.padding(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureForm(spaceName: String, onSave: (String) -> Unit) {
    var text by androidx.compose.runtime.remember { mutableStateOf("") }
    val save = { if (text.isNotBlank()) onSave(text.trim()) }
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nova stavka · $spaceName", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Šta želiš dodati?") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { save() }),
        )
        Button(onClick = save, modifier = Modifier.fillMaxWidth(), enabled = text.isNotBlank()) {
            Text("Sačuvaj")
        }
    }
}

@Composable
private fun WidgetTheme(content: @Composable () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    val dark = if (preferences.contains("dark_theme")) {
        preferences.getBoolean("dark_theme", systemDark)
    } else {
        systemDark
    }
    LifeSpacesTheme(darkTheme = dark, content = content)
}
