package com.lostf1sh.pixelplayeross.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilAlbumDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilPlaylistDto
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilTrackDto
import com.lostf1sh.pixelplayeross.data.mehfil.toSong
import com.lostf1sh.pixelplayeross.presentation.components.SmartImage
import com.lostf1sh.pixelplayeross.presentation.components.MiniPlayerHeight
import com.lostf1sh.pixelplayeross.presentation.components.resolveNavBarOccupiedHeight
import com.lostf1sh.pixelplayeross.presentation.navigation.Screen
import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafely
import com.lostf1sh.pixelplayeross.presentation.viewmodel.MehfilCatalogViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel

@Composable
fun MehfilHomeCatalogSections(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: MehfilCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.loadHome() }
    val feed = state.home
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            state.isLoading && feed == null -> {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && feed == null -> {
                Text(
                    state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            feed != null -> {
                state.error?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp))
                }
                catalogAlbumCarousel(
                    title = "New Releases",
                    albums = feed.newReleases,
                    kind = "album",
                    navController = navController,
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                )
                catalogAlbumCarousel(
                    title = "Trending Albums",
                    albums = feed.trendingAlbums,
                    kind = "album",
                    navController = navController,
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                )
                catalogPlaylistCarousel(
                    title = "Top Charts",
                    playlists = feed.topCharts,
                    kind = "chart",
                    navController = navController,
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                )
                catalogPlaylistCarousel(
                    title = "Playlists",
                    playlists = feed.playlists,
                    kind = "playlist",
                    navController = navController,
                    viewModel = viewModel,
                    playerViewModel = playerViewModel,
                )
            }
        }
    }
}

@Composable
private fun catalogAlbumCarousel(
    title: String,
    albums: List<MehfilAlbumDto>,
    kind: String,
    navController: NavHostController,
    viewModel: MehfilCatalogViewModel,
    playerViewModel: PlayerViewModel,
) {
    if (albums.isEmpty()) return
    CatalogSectionTitle(title)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            CatalogArtworkCard(
                title = album.title,
                subtitle = album.primaryArtists,
                artwork = album.artwork,
                onClick = {
                    navController.navigateSafely(Screen.MehfilTracklist.createRoute(kind, album.id))
                },
                onPlay = {
                    viewModel.playItem(kind, album.id) { tracks ->
                        playCatalogTracks(playerViewModel, tracks, album.title)
                    }
                },
            )
        }
    }
}

@Composable
private fun catalogPlaylistCarousel(
    title: String,
    playlists: List<MehfilPlaylistDto>,
    kind: String,
    navController: NavHostController,
    viewModel: MehfilCatalogViewModel,
    playerViewModel: PlayerViewModel,
) {
    if (playlists.isEmpty()) return
    CatalogSectionTitle(title)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(playlists, key = { it.id }) { playlist ->
            CatalogArtworkCard(
                title = playlist.title.ifBlank { playlist.name.orEmpty() },
                subtitle = playlist.subtitle.orEmpty(),
                artwork = playlist.artwork,
                onClick = {
                    navController.navigateSafely(Screen.MehfilTracklist.createRoute(kind, playlist.id))
                },
                onPlay = {
                    viewModel.playItem(kind, playlist.id) { tracks ->
                        playCatalogTracks(
                            playerViewModel,
                            tracks,
                            playlist.title.ifBlank { playlist.name.orEmpty() },
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun CatalogSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
    )
}

@Composable
private fun CatalogArtworkCard(
    title: String,
    subtitle: String,
    artwork: String?,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier.width(156.dp),
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box {
            SmartImage(
                model = artwork,
                contentDescription = title,
                modifier = Modifier.fillMaxWidth().height(156.dp),
                contentScale = ContentScale.Crop,
            )
            FilledIconButton(
                onClick = onPlay,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(42.dp),
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play $title")
            }
        }
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 9.dp))
        Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 3.dp, bottom = 10.dp))
    }
}

@Composable
fun MehfilArtistScreen(
    artistId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: MehfilCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(artistId) { viewModel.loadArtist(artistId) }
    val artist = state.artist
    val bottomContentPadding = catalogBottomContentPadding(playerViewModel)
    CatalogScreenContent(state.isLoading, state.error, bottomContentPadding) {
        if (artist != null) {
            item {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    SmartImage(
                        model = artist.image,
                        contentDescription = artist.name,
                        modifier = Modifier.size(184.dp).clip(CircleShape),
                    )
                    Text(artist.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
                    artist.bio?.takeIf(String::isNotBlank)?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                    }
                    val topTracks = artist.topSongs.mapNotNull(MehfilTrackDto::toSong)
                    if (topTracks.isNotEmpty()) {
                        androidx.compose.material3.Button(
                            onClick = { playerViewModel.playSongs(topTracks, topTracks.first(), artist.name) },
                            modifier = Modifier.padding(top = 14.dp),
                        ) { Text("Play All") }
                    }
                }
            }
            item { CatalogSectionTitle("Top Songs") }
            itemsIndexed(artist.topSongs, key = { index, song -> song.videoId ?: song.id ?: index.toString() }) { index, track ->
                CatalogTrackRow(
                    track = track,
                    index = index + 1,
                    onClick = {
                        val songs = artist.topSongs.mapNotNull(MehfilTrackDto::toSong)
                        songs.firstOrNull { it.mehfilVideoId == (track.videoId ?: track.id) }?.let {
                            playerViewModel.playSongs(songs, it, artist.name)
                        }
                    },
                    artistClick = {
                        navController.navigateSafely(Screen.MehfilArtist.createRoute(artist.id))
                    },
                    albumClick = {
                        track.albumId?.takeIf(String::isNotBlank)?.let {
                            navController.navigateSafely(Screen.MehfilTracklist.createRoute("album", it))
                        }
                    },
                )
            }
            val albums = artist.topAlbums + artist.singles
            if (albums.isNotEmpty()) item {
                CatalogSectionTitle("Albums")
                albums.chunked(2).forEach { rowAlbums ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowAlbums.forEach { album ->
                            Box(Modifier.weight(1f)) {
                                CatalogArtworkCard(
                                    title = album.title,
                                    subtitle = album.primaryArtists,
                                    artwork = album.artwork,
                                    onClick = {
                                        navController.navigateSafely(
                                            Screen.MehfilTracklist.createRoute("album", album.id)
                                        )
                                    },
                                    onPlay = {
                                        viewModel.playItem("album", album.id) { tracks ->
                                            playCatalogTracks(playerViewModel, tracks, album.title)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        if (rowAlbums.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MehfilAlbumPlaylistScreen(
    kind: String,
    itemId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: MehfilCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(kind, itemId) { viewModel.loadItem(kind, itemId) }
    val album = state.album
    val playlist = state.playlist
    val title = album?.title ?: playlist?.let { it.title.ifBlank { it.name.orEmpty() } }.orEmpty()
    val tracks = album?.songs ?: playlist?.songs.orEmpty()
    val bottomContentPadding = catalogBottomContentPadding(playerViewModel)
    CatalogScreenContent(state.isLoading, state.error, bottomContentPadding) {
        item {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                SmartImage(
                    model = album?.artwork ?: playlist?.artwork,
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(18.dp)),
                )
                Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 14.dp))
                val artistNames = album?.primaryArtists.orEmpty()
                if (artistNames.isNotBlank()) {
                    Text(
                        artistNames,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.clickable {
                            album?.primaryArtistId?.takeIf(String::isNotBlank)?.let {
                                navController.navigateSafely(Screen.MehfilArtist.createRoute(it))
                            }
                        }.padding(top = 4.dp),
                    )
                }
                val metadata = listOfNotNull(
                    album?.year?.takeIf { it > 0 }?.toString(),
                    tracks.size.takeIf { it > 0 }?.let { "$it tracks" },
                    playlist?.subtitle?.takeIf(String::isNotBlank),
                ).joinToString(" • ")
                if (metadata.isNotBlank()) {
                    Text(metadata, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 5.dp))
                }
                val songs = tracks.mapNotNull(MehfilTrackDto::toSong)
                if (songs.isNotEmpty()) {
                    androidx.compose.material3.Button(
                        onClick = { playerViewModel.playSongs(songs, songs.first(), title) },
                        modifier = Modifier.padding(top = 12.dp),
                    ) { Text("Play All") }
                }
            }
        }
        itemsIndexed(tracks, key = { index, song -> song.videoId ?: song.id ?: index.toString() }) { index, track ->
            CatalogTrackRow(
                track = track,
                index = index + 1,
                onClick = {
                    val songs = tracks.mapNotNull(MehfilTrackDto::toSong)
                    songs.firstOrNull { it.mehfilVideoId == (track.videoId ?: track.id) }?.let {
                        playerViewModel.playSongs(songs, it, title)
                    }
                },
                artistClick = {
                    track.artistId?.takeIf(String::isNotBlank)?.let {
                        navController.navigateSafely(Screen.MehfilArtist.createRoute(it))
                    }
                },
                albumClick = {
                    track.albumId?.takeIf(String::isNotBlank)?.let {
                        navController.navigateSafely(Screen.MehfilTracklist.createRoute("album", it))
                    }
                },
            )
        }
    }
}

@Composable
private fun CatalogScreenContent(
    isLoading: Boolean,
    error: String?,
    bottomContentPadding: Dp,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = bottomContentPadding),
        ) { content() }
    }
}

@Composable
private fun catalogBottomContentPadding(playerViewModel: PlayerViewModel): Dp {
    val compactNavBar by playerViewModel.navBarCompactMode.collectAsStateWithLifecycle()
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return navigationBarInset +
        resolveNavBarOccupiedHeight(navigationBarInset, compactNavBar) +
        MiniPlayerHeight + 24.dp
}

@Composable
private fun CatalogTrackRow(
    track: MehfilTrackDto,
    index: Int,
    onClick: () -> Unit,
    artistClick: (() -> Unit)? = null,
    albumClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(index.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(30.dp))
        SmartImage(
            model = track.coverArt ?: track.thumbnail,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
        )
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Row {
                Text(
                    track.primaryArtist?.takeIf(String::isNotBlank) ?: track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(enabled = artistClick != null) { artistClick?.invoke() },
                )
                if (!track.album.isNullOrBlank()) {
                    Text(" • ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(track.album.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(enabled = albumClick != null) { albumClick?.invoke() })
                }
            }
        }
    }
}

private fun playCatalogTracks(
    playerViewModel: PlayerViewModel,
    tracks: List<MehfilTrackDto>,
    queueName: String,
) {
    val songs = tracks.mapNotNull(MehfilTrackDto::toSong)
    if (songs.isNotEmpty()) playerViewModel.playSongs(songs, songs.first(), queueName)
}
