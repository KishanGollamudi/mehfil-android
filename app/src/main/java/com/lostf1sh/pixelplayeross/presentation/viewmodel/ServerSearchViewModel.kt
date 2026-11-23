package com.lostf1sh.pixelplayeross.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilCatalogRepository
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilCatalogResultDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilPlaylistDto
import com.lostf1sh.pixelplayeross.data.mehfil.toSong
import com.lostf1sh.pixelplayeross.data.navidrome.model.NavidromeAlbum
import com.lostf1sh.pixelplayeross.data.navidrome.model.NavidromeArtist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

@Immutable
 data class ServerSearchUiState(
    val query: String = "",
    val artists: List<NavidromeArtist> = emptyList(),
    val albums: List<NavidromeAlbum> = emptyList(),
    val playlists: List<MehfilPlaylistDto> = emptyList(),
    val topQuery: MehfilCatalogResultDto? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ServerSearchViewModel @Inject constructor(
    private val repository: MehfilCatalogRepository
) : ViewModel() {
    private val queryFlow = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ServerSearchUiState())
    val uiState: StateFlow<ServerSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .mapLatest { query ->
                    if (query.isBlank()) {
                        _uiState.value = ServerSearchUiState()
                    } else {
                        _uiState.value = _uiState.value.copy(query = query, isLoading = true, error = null)
                        runCatching { repository.search(query) }.rethrowCancellation()
                            .onSuccess { result ->
                                _uiState.value = ServerSearchUiState(
                                    query = query,
                                    artists = result.artists.map {
                                        NavidromeArtist(
                                            id = it.id,
                                            name = it.name,
                                            coverArt = it.image,
                                            artistImageUrl = it.image,
                                        )
                                    },
                                    albums = result.albums.map {
                                        NavidromeAlbum(
                                            id = it.id,
                                            name = it.title,
                                            artist = it.primaryArtists,
                                            coverArt = it.artwork,
                                            year = it.year,
                                        )
                                    },
                                    playlists = result.playlists,
                                    topQuery = result.topQuery,
                                    songs = result.songs.mapNotNull { it.toSong() },
                                )
                            }
                            .onFailure { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = error.message ?: "Search failed"
                                )
                            }
                    }
                }.collect {}
        }
    }

    fun updateQuery(query: String) {
        queryFlow.value = sanitize(query)
        if (query.isBlank()) _uiState.value = ServerSearchUiState()
    }

    fun refresh() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { repository.search(query) }.rethrowCancellation()
                .onSuccess { result ->
                    _uiState.value = ServerSearchUiState(
                        query = query,
                        artists = result.artists.map {
                            NavidromeArtist(it.id, it.name, coverArt = it.image, artistImageUrl = it.image)
                        },
                        albums = result.albums.map {
                            NavidromeAlbum(it.id, it.title, it.primaryArtists, coverArt = it.artwork, year = it.year)
                        },
                        playlists = result.playlists,
                        topQuery = result.topQuery,
                        songs = result.songs.mapNotNull { it.toSong() },
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Search failed"
                    )
                }
        }
    }

    fun coverArtUrl(coverArt: String?, size: Int = 500): String? =
        coverArt?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    suspend fun searchSongs(query: String): Result<List<Song>> =
        runCatching { repository.search(query).songs.mapNotNull { it.toSong() } }.rethrowCancellation()

    private fun sanitize(query: String): String = Normalizer.normalize(query.trim(), Normalizer.Form.NFKC)
        .replace(Regex("\\s+"), " ")
}

private fun <T> Result<T>.rethrowCancellation(): Result<T> = onFailure {
    if (it is CancellationException) throw it
}
