package com.lifespaces.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.lifespaces.android.ui.App
import com.lifespaces.android.ui.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory((application as LifeSpacesApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedTextFrom(intent)?.let(viewModel::receiveSharedText)
        setContent {
            App(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedTextFrom(intent)?.let(viewModel::receiveSharedText)
    }
}

internal fun sharedTextFrom(intent: Intent): String? =
    intent.takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
        ?.getStringExtra(Intent.EXTRA_TEXT)
        ?.trim()
        ?.takeIf(String::isNotEmpty)
