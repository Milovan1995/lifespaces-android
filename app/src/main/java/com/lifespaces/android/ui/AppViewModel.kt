package com.lifespaces.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lifespaces.android.data.HomeFeed
import com.lifespaces.android.data.LifeSpacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val repository: LifeSpacesRepository) : ViewModel() {
    private val _captureText = MutableStateFlow("")
    val captureText: StateFlow<String> = _captureText.asStateFlow()

    val state: StateFlow<HomeFeed> = repository.homeFeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeFeed(emptyList(), emptyList()),
    )

    fun onCaptureTextChange(value: String) {
        _captureText.value = value
    }

    fun saveCapture() {
        val text = captureText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            repository.createItem(text)
            _captureText.update { "" }
        }
    }

    fun addItemToSpace(spaceId: Long, value: String) {
        val text = value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch { repository.createItem(text, spaceId) }
    }

    fun createSpace(name: String, template: String, location: String = "") {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.createSpace(trimmed, template, location) }
    }

    fun createSpace(
        name: String,
        template: String,
        location: String,
        capabilities: Set<String>,
        color: Long? = null,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.createSpace(trimmed, template, location, capabilities, color) }
    }

    fun updateSpace(
        spaceId: Long,
        name: String,
        location: String,
        color: Long?,
        capabilities: Set<String>,
        clearCompleted: Boolean = false,
        clearScheduledAt: Boolean = false,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateSpace(
                spaceId,
                trimmed,
                location,
                color,
                capabilities,
                clearCompleted,
                clearScheduledAt,
            )
        }
    }

    fun updateItemText(itemId: Long, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.updateItemText(itemId, trimmed) }
    }

    fun moveItem(itemId: Long, spaceId: Long?) {
        viewModelScope.launch { repository.moveItem(itemId, spaceId) }
    }

    fun setItemCompleted(itemId: Long, completed: Boolean) {
        viewModelScope.launch { repository.setItemCompleted(itemId, completed) }
    }

    fun setItemScheduledAt(itemId: Long, scheduledAt: Long?) {
        viewModelScope.launch { repository.setItemScheduledAt(itemId, scheduledAt) }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch { repository.deleteItem(itemId) }
    }

    fun deleteSpace(spaceId: Long) {
        viewModelScope.launch { repository.deleteSpace(spaceId) }
    }

    companion object {
        fun factory(repository: LifeSpacesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(repository) as T
                }
            }
    }
}
