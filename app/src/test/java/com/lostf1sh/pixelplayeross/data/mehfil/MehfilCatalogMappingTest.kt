package com.lostf1sh.pixelplayeross.data.mehfil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MehfilCatalogMappingTest {
    @Test
    fun mapsCatalogTrackAndRadioContext() {
        val song = MehfilTrackDto(
            id = "track-1",
            title = "Catalog song",
            artist = "Featured artist",
            primaryArtist = "Main artist",
            artistId = "artist-1",
            album = "Catalog album",
            albumId = "album-1",
            duration = 194,
            language = "hindi",
            mood = "romantic",
            coverArt = "https://example.test/cover.jpg",
            audioUrl = "https://example.test/audio.mp4",
        ).toSong()

        assertEquals("mehfil_track-1", song?.id)
        assertEquals("Main artist", song?.artist)
        assertEquals(194_000L, song?.duration)
        assertEquals("hindi", song?.mehfilLanguage)
        assertEquals("romantic", song?.mehfilMood)
        assertEquals("artist-1", song?.mehfilArtistId)
        assertEquals("album-1", song?.mehfilAlbumId)
        assertEquals("https://example.test/audio.mp4", song?.contentUriString)
    }

    @Test
    fun rejectsTracksWithoutPlayableIdentityOrUrl() {
        assertNull(MehfilTrackDto(title = "missing identity", audioUrl = "https://example.test/audio.mp4").toSong())
        assertNull(MehfilTrackDto(videoId = "track-1", title = "missing URL").toSong())
    }
}
