package com.lostf1sh.pixelplayeross.data.mehfil

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MehfilApiService {
    @POST("api/auth/login")
    suspend fun login(@Body credentials: MehfilAuthRequest): Response<MehfilAuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MehfilAuthResponse>

    @GET("api/home")
    suspend fun getHome(): HomeFeedDto

    @GET("api/search")
    suspend fun search(@Query("q") query: String): SearchAllDto

    @GET("api/artist/{id}")
    suspend fun getArtist(@Path("id") id: String): MehfilArtistDto

    @GET("api/album/{id}")
    suspend fun getAlbum(@Path("id") id: String): MehfilAlbumDto

    @GET("api/playlist/{id}")
    suspend fun getPlaylist(@Path("id") id: String): MehfilPlaylistDto

    @GET("api/radio/{songId}")
    suspend fun getRadio(
        @Path("songId") songId: String,
        @Query("language") language: String? = null,
        @Query("artist") artist: String? = null,
        @Query("mood") mood: String? = null,
    ): RadioFeedDto
}
