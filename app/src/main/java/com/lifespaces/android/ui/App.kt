package com.lifespaces.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    MaterialTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("LifeSpaces", style = MaterialTheme.typography.headlineMedium)
                Text("Početni tehnički temelj je aktivan.")
                Text("Prostori: ${state.spaces.size}")
                Text("Stavke: ${state.items.size}")
                Button(onClick = viewModel::addDemoItem, content = { Text("Dodaj demo stavku") })
            }
        }
    }
}
