package com.lostf1sh.pixelplayeross.data.navidrome.model

/** Authentication method used for Subsonic requests. */
enum class NavidromeAuthMethod {
    /** Salted MD5 token: `t=md5(password + salt)&s=salt`. */
    TOKEN;

    companion object {
        /** Reads a persisted value, defaulting to [TOKEN] for unknown or missing input. */
        fun fromStorageKey(value: String?): NavidromeAuthMethod =
            entries.firstOrNull { it.name == value } ?: TOKEN
    }
}
