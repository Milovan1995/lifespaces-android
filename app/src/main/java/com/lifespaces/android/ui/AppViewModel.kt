package com.lifespaces.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lifespaces.android.data.HomeFeed
import com.lifespaces.android.data.LifeSpacesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val repository: LifeSpacesRepository) : ViewModel() {
    val state: StateFlow<HomeFeed> = repository.homeFeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeFeed(emptyList(), emptyList()),
    )

    fun addDemoItem() {
        viewModelScope.launch {
            repository.createItem("Prvi unos")
        }
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
