package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Colors extracted from the artwork of the current track. */
data class ArtworkPaletteColors(
    val vibrant: Color? = null,
    val dominant: Color? = null,
    val muted: Color? = null
)

@Composable
fun rememberArtworkPalette(artworkUri: String?): ArtworkPaletteColors {
    val context = LocalContext.current
    var colors by remember(artworkUri) { mutableStateOf(ArtworkPaletteColors()) }

    LaunchedEffect(artworkUri, context) {
        colors = ArtworkPaletteColors()
        if (artworkUri.isNullOrBlank()) return@LaunchedEffect

        val result = ImageLoader(context).execute(
            ImageRequest.Builder(context)
                .data(artworkUri)
                .allowHardware(false)
                .build()
        )
        val bitmap = when (result) {
            is SuccessResult -> result.drawable.toBitmap()
            is ErrorResult -> null
        } ?: return@LaunchedEffect

        colors = withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).maximumColorCount(16).generate()
            ArtworkPaletteColors(
                vibrant = palette.vibrantSwatch?.rgb?.let(::Color),
                dominant = palette.dominantSwatch?.rgb?.let(::Color),
                muted = palette.mutedSwatch?.rgb?.let(::Color)
            )
        }
    }

    return colors
}
