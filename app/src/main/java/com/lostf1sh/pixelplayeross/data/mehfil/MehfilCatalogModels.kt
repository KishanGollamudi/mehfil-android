package com.lostf1sh.pixelplayeross.data.mehfil

import com.google.gson.annotations.SerializedName

data class MehfilTrackDto(
    val id: String? = null,
    val videoId: String? = null,
    val title: String = "",
    val artist: String = "",
    val primaryArtist: String? = null,
    val artistId: String? = null,
    val album: String? = null,
    val albumId: String? = null,
    val duration: Long = 0L,
    val language: String? = null,
    val genre: String? = null,
    val mood: String? = null,
    val coverArt: String? = null,
    val thumbnail: String? = null,
    val audioUrl: String? = null,
)

data class MehfilAlbumDto(
    val id: String,
    val title: String = "",
    val subtitle: String? = null,
    val year: Int = 0,
    val artwork: String? = null,
    val primaryArtists: String = "",
    val primaryArtistId: String? = null,
    val songCount: Int = 0,
    val songs: List<MehfilTrackDto> = emptyList(),
)

data class MehfilArtistDto(
    val id: String,
    val name: String = "",
    val image: String? = null,
    val bio: String? = null,
    val topSongs: List<MehfilTrackDto> = emptyList(),
    val topAlbums: List<MehfilAlbumDto> = emptyList(),
    val singles: List<MehfilAlbumDto> = emptyList(),
)

data class MehfilPlaylistDto(
    val id: String,
    val title: String = "",
    val name: String? = null,
    val subtitle: String? = null,
    val artwork: String? = null,
    val songCount: Int = 0,
    val songs: List<MehfilTrackDto> = emptyList(),
)

data class HomeFeedDto(
    val newReleases: List<MehfilAlbumDto> = emptyList(),
    val trendingAlbums: List<MehfilAlbumDto> = emptyList(),
    val topCharts: List<MehfilPlaylistDto> = emptyList(),
    @SerializedName(value = "curatedPlaylists", alternate = ["playlists"])
    val playlists: List<MehfilPlaylistDto> = emptyList(),
)

data class SearchAllDto(
    val topQuery: MehfilCatalogResultDto? = null,
    val songs: List<MehfilTrackDto> = emptyList(),
    val albums: List<MehfilAlbumDto> = emptyList(),
    val artists: List<MehfilArtistDto> = emptyList(),
    val playlists: List<MehfilPlaylistDto> = emptyList(),
)

data class MehfilCatalogResultDto(
    val type: String = "",
    val id: String? = null,
    val videoId: String? = null,
    val title: String? = null,
    val name: String? = null,
    val artist: String? = null,
    val primaryArtist: String? = null,
    val artwork: String? = null,
    val image: String? = null,
    val audioUrl: String? = null,
)

data class RadioFeedDto(val tracks: List<MehfilTrackDto> = emptyList())

data class MehfilAuthRequest(val username: String, val password: String)
data class MehfilAuthResponse(val user: MehfilUserDto? = null)
data class MehfilUserDto(val id: Long = 0L, val username: String = "", val role: String = "")
