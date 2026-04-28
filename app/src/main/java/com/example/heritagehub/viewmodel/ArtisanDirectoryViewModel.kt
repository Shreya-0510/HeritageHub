package com.example.heritagehub.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heritagehub.data.ArtisanRepository
import com.example.heritagehub.model.Artisan
import kotlinx.coroutines.launch

class ArtisanDirectoryViewModel(
    private val repository: ArtisanRepository = ArtisanRepository()
) : ViewModel() {

    private val _allArtisans = mutableStateOf<List<Artisan>>(emptyList())
    val allArtisans: State<List<Artisan>> = _allArtisans

    private val _visibleArtisans = mutableStateOf<List<Artisan>>(emptyList())
    val visibleArtisans: State<List<Artisan>> = _visibleArtisans

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val artisans = repository.getArtisans()
                _allArtisans.value = artisans
                applyFilter(_searchQuery.value)
            } catch (e: Exception) {
                _allArtisans.value = emptyList()
                _visibleArtisans.value = emptyList()
                _error.value = e.message ?: "Failed to load artisans"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
        applyFilter(query)
    }

    private fun applyFilter(rawQuery: String) {
        val query = rawQuery.trim().lowercase()
        if (query.isBlank()) {
            // Initially show 5 artisans in the visible list
            _visibleArtisans.value = _allArtisans.value.take(5)
            return
        }

        // Show all matches when user starts typing
        _visibleArtisans.value = _allArtisans.value.filter { artisan ->
            artisan.artistName.lowercase().contains(query) ||
                artisan.categories.any { it.lowercase().contains(query) } ||
                artisan.skills.any { it.lowercase().contains(query) }
        }
    }
}
