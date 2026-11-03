package com.lostf1sh.pixelplayeross.data.network.navidrome

import com.google.common.truth.Truth.assertThat
import com.lostf1sh.pixelplayeross.data.navidrome.model.NavidromeAuthMethod
import org.junit.jupiter.api.Test

class NavidromeApiServiceAuthTest {

    @Test
    fun `token auth uses the salted md5 token from the Subsonic spec`() {
        val params = subsonicAuthParams(
            password = "sesame",
            method = NavidromeAuthMethod.TOKEN,
            salt = "c19b2d"
        )

        assertThat(params).containsExactly(
            "t", "26719a1196d2a940705a59634eb18eab",
            "s", "c19b2d"
        )
    }

    @Test
    fun `token auth salt changes between calls`() {
        val first = subsonicAuthParams("sesame", NavidromeAuthMethod.TOKEN)
        val second = subsonicAuthParams("sesame", NavidromeAuthMethod.TOKEN)

        assertThat(first["s"]).isNotEqualTo(second["s"])
    }

    @Test
    fun `stored auth method falls back to token when missing or unknown`() {
        assertThat(NavidromeAuthMethod.fromStorageKey(null)).isEqualTo(NavidromeAuthMethod.TOKEN)
        assertThat(NavidromeAuthMethod.fromStorageKey("nonsense")).isEqualTo(NavidromeAuthMethod.TOKEN)
        assertThat(NavidromeAuthMethod.fromStorageKey("PASSWORD")).isEqualTo(NavidromeAuthMethod.TOKEN)
    }
}
