package com.lostf1sh.pixelplayeross.di

import com.lostf1sh.pixelplayeross.data.listenbrainz.ListenBrainzApiService
import com.lostf1sh.pixelplayeross.data.listenbrainz.ListenBrainzEndpoint
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilApiService
import com.lostf1sh.pixelplayeross.data.mehfil.MehfilSessionCookieJar
import com.lostf1sh.pixelplayeross.data.navidrome.model.NavidromeCredentials
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @MehfilCatalogRetrofit
    fun provideMehfilCatalogRetrofit(
        okHttpClient: OkHttpClient,
        cookieJar: MehfilSessionCookieJar,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("${NavidromeCredentials.SERVER_URL}/")
        .client(okHttpClient.newBuilder().cookieJar(cookieJar).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideMehfilApiService(
        @MehfilCatalogRetrofit retrofit: Retrofit,
    ): MehfilApiService = retrofit.create(MehfilApiService::class.java)

    @Provides
    @Singleton
    @ListenBrainzRetrofit
    fun provideListenBrainzRetrofit(
        okHttpClient: OkHttpClient,
        endpoint: ListenBrainzEndpoint
    ): Retrofit {
        val client = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val rewritten = endpoint.rewrite(request.url)
                if (rewritten == request.url) {
                    chain.proceed(request)
                } else {
                    chain.proceed(request.newBuilder().url(rewritten).build())
                }
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(ListenBrainzEndpoint.DEFAULT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideListenBrainzApiService(@ListenBrainzRetrofit retrofit: Retrofit): ListenBrainzApiService {
        return retrofit.create(ListenBrainzApiService::class.java)
    }
}
