package com.lostf1sh.pixelplayeross.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.navidrome.NavidromeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServerSearchViewModel @Inject constructor(
    private val navidromeRepository: NavidromeRepository
) : ViewModel() {
    suspend fun searchSongs(query: String): Result<List<Song>> =
        navidromeRepository.searchSongs(query, limit = 20)
}
