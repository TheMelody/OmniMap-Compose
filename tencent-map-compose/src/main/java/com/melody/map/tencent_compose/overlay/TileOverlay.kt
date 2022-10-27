// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.map.tencent_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.melody.map.tencent_compose.MapApplier
import com.melody.map.tencent_compose.MapNode
import com.melody.map.tencent_compose.model.TXMapComposable
import com.tencent.tencentmap.mapsdk.maps.model.TileOverlay
import com.tencent.tencentmap.mapsdk.maps.model.TileOverlayOptions
import com.tencent.tencentmap.mapsdk.maps.model.TileProvider

private class TileOverlayNode(
    var tileOverlay: TileOverlay
) : MapNode {
    override fun onRemoved() {
        tileOverlay.remove()
    }
}

/**
 * 瓦片图层
 *
 * @param tileProvider 瓦片图层的提供者
 * @param betterQuality TileOverlay 是否以高清模式加载
 * @param diskCacheDir 瓦片图层的磁盘缓存目录
 * @param memCacheSize 瓦片图层的内存缓存大小，默认值5MB
 * @param zIndex 设置瓦片的显示层级
 */
@Composable
@TXMapComposable
fun TileOverlay(
    tileProvider: TileProvider,
    betterQuality: Boolean = false,
    diskCacheDir: String? = null,
    memCacheSize: Int = 5242880,
    zIndex: Int = 0
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<TileOverlayNode, MapApplier>(
        factory = {
            val tileOverlay = mapApplier?.map?.addTileOverlay(
                TileOverlayOptions().apply {
                    tileProvider(tileProvider)
                    betterQuality(betterQuality)
                    diskCacheDir(diskCacheDir)
                    maxMemoryCacheSize(memCacheSize)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding Tile Overlay")
            TileOverlayNode(tileOverlay)
        },
        update = {

            set(tileProvider) {
                this.tileOverlay.clearTileCache()
                this.tileOverlay.remove()
                this.tileOverlay = mapApplier?.map?.addTileOverlay(
                    TileOverlayOptions().apply {
                        tileProvider(tileProvider)
                        betterQuality(betterQuality)
                        diskCacheDir(diskCacheDir)
                        maxMemoryCacheSize(memCacheSize)
                        zIndex(zIndex)
                    }
                ) ?: error("Error adding Tile Overlay")
            }
            set(zIndex) { this.tileOverlay.setZindex(it) }
        }
    )
}