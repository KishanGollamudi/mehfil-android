@file:Suppress("DEPRECATION")

package com.lostf1sh.pixelplayeross.data.mehfil

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MehfilSessionCookieJar @Inject constructor(
    @ApplicationContext context: Context,
) : CookieJar {
    private val preferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "mehfil_catalog_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val savedCookies = cookies.map { it.toString() }
        preferences.edit().putString(url.host, savedCookies.joinToString("\n")).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val serialized = preferences.getString(url.host, null) ?: return emptyList()
        val cookies = serialized.lineSequence()
            .mapNotNull { Cookie.parse(url, it) }
            .filter { it.expiresAt > System.currentTimeMillis() && it.matches(url) }
            .toList()
        if (cookies.isEmpty()) preferences.edit().remove(url.host).apply()
        return cookies
    }
}
