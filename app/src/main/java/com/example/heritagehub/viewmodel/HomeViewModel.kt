package com.example.heritagehub.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heritagehub.data.ArtworkRepository
import com.example.heritagehub.model.Artwork
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ArtworkRepository = ArtworkRepository()
) : ViewModel() {
    private val _artworks = mutableStateOf<List<Artwork>>(emptyList())
    val artworks: State<List<Artwork>> = _artworks

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadArtworks()
    }

    private fun loadArtworks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _artworks.value = repository.getArtworks()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

