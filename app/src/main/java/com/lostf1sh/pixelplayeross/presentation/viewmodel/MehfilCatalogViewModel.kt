package com.lostf1sh.pixelplayeross.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.data.mehfil.HomeFeedDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilAlbumDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilArtistDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilCatalogRepository
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilPlaylistDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilTrackDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MehfilCatalogUiState(
    val home: HomeFeedDto? = null,
    val artist: MehfilArtistDto? = null,
    val album: MehfilAlbumDto? = null,
    val playlist: MehfilPlaylistDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MehfilCatalogViewModel @Inject constructor(
    private val repository: MehfilCatalogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MehfilCatalogUiState())
    val uiState = _uiState.asStateFlow()

    fun loadHome() {
        if (_uiState.value.home != null || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { repository.home() }.rethrowCancellation()
                .onSuccess { _uiState.value = _uiState.value.copy(home = it, isLoading = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Unable to load catalog")
                }
        }
    }

    fun refreshHome() {
        _uiState.value = _uiState.value.copy(home = null)
        loadHome()
    }

    fun loadArtist(id: String) {
        viewModelScope.launch {
            _uiState.value = MehfilCatalogUiState(isLoading = true)
            runCatching { repository.artist(id) }.rethrowCancellation()
                .onSuccess { _uiState.value = _uiState.value.copy(artist = it, isLoading = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Unable to load artist")
                }
        }
    }

    fun loadItem(kind: String, id: String) {
        viewModelScope.launch {
            _uiState.value = MehfilCatalogUiState(isLoading = true)
            runCatching {
                when (kind) {
                    "album" -> repository.album(id)
                    "playlist", "chart" -> repository.playlist(id)
                    else -> error("Unsupported catalog item: $kind")
                }
            }.rethrowCancellation().onSuccess { item ->
                _uiState.value = when (item) {
                    is MehfilAlbumDto -> _uiState.value.copy(album = item, isLoading = false)
                    is MehfilPlaylistDto -> _uiState.value.copy(playlist = item, isLoading = false)
                    else -> _uiState.value.copy(isLoading = false)
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Unable to load tracklist")
            }
        }
    }

    suspend fun tracklist(kind: String, id: String): List<MehfilTrackDto> = when (kind) {
        "album" -> repository.album(id).songs
        "playlist", "chart" -> repository.playlist(id).songs
        else -> emptyList()
    }

    fun playItem(kind: String, id: String, onTracksReady: (List<MehfilTrackDto>) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            runCatching { tracklist(kind, id) }.rethrowCancellation()
                .onSuccess(onTracksReady)
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to load tracks")
                }
        }
    }

    private fun <T> Result<T>.rethrowCancellation(): Result<T> = onFailure {
        if (it is CancellationException) throw it
    }
}
