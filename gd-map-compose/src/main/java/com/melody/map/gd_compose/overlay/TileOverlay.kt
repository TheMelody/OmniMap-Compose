package com.melody.map.gd_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.amap.api.maps.model.TileOverlay
import com.amap.api.maps.model.TileOverlayOptions
import com.amap.api.maps.model.TileProvider
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.melody.map.gd_compose.model.GDMapComposable

private class TileOverlayNode(
    var tileOverlay: TileOverlay,
    var onTileOverlayClick: (TileOverlay) -> Unit
) : MapNode {
    override fun onRemoved() {
        tileOverlay.remove()
    }
}

/**
 * A composable for a tile overlay on the map.
 *
 * @param tileProvider the tile provider to use for this tile overlay
 * @param visible the visibility of the tile overlay
 * @param zIndex the z-index of the tile overlay
 * @param onClick a lambda invoked when the tile overlay is clicked
 */
@Composable
@GDMapComposable
fun TileOverlay(
    tileProvider: TileProvider,
    visible: Boolean = true,
    memoryCacheEnabled: Boolean = true,
    diskCacheEnabled: Boolean = true,
    diskCacheDir: String? = null,
    memCacheSize: Int = 5242880,
    diskCacheSize: Int = 20971520,
    zIndex: Float = 0f,
    onClick: (TileOverlay) -> Unit = {},
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<TileOverlayNode, MapApplier>(
        factory = {
            val tileOverlay = mapApplier?.map?.addTileOverlay(
                TileOverlayOptions().apply {
                    tileProvider(tileProvider)
                    memoryCacheEnabled(memoryCacheEnabled)
                    diskCacheEnabled(diskCacheEnabled)
                    diskCacheDir(diskCacheDir)
                    memCacheSize(memCacheSize)
                    diskCacheSize(diskCacheSize)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding tile overlay")
            TileOverlayNode(tileOverlay, onClick)
        },
        update = {
            update(onClick) { this.onTileOverlayClick = it }

            set(tileProvider) {
                this.tileOverlay.remove()
                this.tileOverlay = mapApplier?.map?.addTileOverlay(
                    TileOverlayOptions().apply {
                        tileProvider(tileProvider)
                        memoryCacheEnabled(memoryCacheEnabled)
                        diskCacheEnabled(diskCacheEnabled)
                        diskCacheDir(diskCacheDir)
                        memCacheSize(memCacheSize)
                        diskCacheSize(diskCacheSize)
                        visible(visible)
                        zIndex(zIndex)
                    }
                ) ?: error("Error adding tile overlay")
            }
            set(visible) { this.tileOverlay.isVisible = it }
            set(zIndex) { this.tileOverlay.zIndex = it }
        }
    )
}