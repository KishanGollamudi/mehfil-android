package com.lostf1sh.pixelplayeross.data.mehfil

import com.lostf1sh.pixelplayeross.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class MehfilCatalogRepository @Inject constructor(
    private val api: MehfilApiService,
    private val cookieJar: MehfilSessionCookieJar,
) {
    suspend fun authenticate(username: String, password: String): Result<Unit> = try {
        val response = api.login(MehfilAuthRequest(username, password))
        check(response.isSuccessful) { "Catalog sign-in failed (HTTP ${response.code()})" }
        Result.success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    suspend fun logout(): Result<Unit> {
        val result = try {
            val response = api.logout()
            check(response.isSuccessful) { "Catalog sign-out failed (HTTP ${response.code()})" }
            Result.success(Unit)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
        cookieJar.clear()
        return result
    }

    suspend fun home(): HomeFeedDto = api.getHome()
    suspend fun search(query: String): SearchAllDto = api.search(query)
    suspend fun artist(id: String): MehfilArtistDto = api.getArtist(id)
    suspend fun album(id: String): MehfilAlbumDto = api.getAlbum(id)
    suspend fun playlist(id: String): MehfilPlaylistDto = api.getPlaylist(id)

    suspend fun radio(song: Song): List<Song> {
        val videoId = song.mehfilVideoId ?: return emptyList()
        return radio(videoId, song.mehfilLanguage, song.primaryArtist.name, song.mehfilMood)
    }

    suspend fun radio(
        videoId: String,
        language: String?,
        artist: String?,
        mood: String?,
    ): List<Song> {
        return api.getRadio(
            songId = videoId,
            language = language,
            artist = artist,
            mood = mood,
        ).tracks.mapNotNull(MehfilTrackDto::toSong)
    }
}

fun MehfilTrackDto.toSong(): Song? {
    val videoId = (videoId ?: id)?.takeIf(String::isNotBlank) ?: return null
    val uri = audioUrl?.takeIf(String::isNotBlank) ?: return null
    val displayArtist = primaryArtist?.takeIf(String::isNotBlank) ?: artist
    return Song(
        id = "mehfil_$videoId",
        title = title,
        artist = displayArtist,
        artistId = artistId?.toLongOrNull() ?: -1L,
        album = album.orEmpty(),
        albumId = albumId?.toLongOrNull() ?: -1L,
        path = "",
        contentUriString = uri,
        albumArtUriString = coverArt?.takeIf(String::isNotBlank)
            ?: thumbnail?.takeIf(String::isNotBlank),
        duration = duration * 1000L,
        genre = genre,
        mimeType = "audio/mp4",
        bitrate = null,
        sampleRate = null,
        mehfilVideoId = videoId,
        mehfilLanguage = language,
        mehfilMood = mood,
        mehfilArtistId = artistId,
        mehfilAlbumId = albumId,
    )
}
