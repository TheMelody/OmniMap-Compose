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

package com.melody.map.baidu_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.baidu.mapapi.map.TileOverlay
import com.baidu.mapapi.map.TileOverlayOptions
import com.baidu.mapapi.map.TileProvider
import com.baidu.mapapi.model.LatLngBounds
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

private class TileOverlayNode(
    var tileOverlay: TileOverlay
) : MapNode {
    override fun onRemoved() {
        tileOverlay.removeTileOverlay()
    }
}

/**
 * 瓦片图层
 *
 * @param tileProvider 瓦片图层的提供者
 * @param latLngBounds (**不支持二次更新**)设置TileOverlay的显示区域，瓦片图会以多个瓦片图连接并覆盖该区域 默认值为世界范围显示瓦片图
 * @param cacheSize (**不支持二次更新**)设置在线瓦片图的内存缓存大小瓦片图默认缓存为200MB
 */
@Composable
@BDMapComposable
fun TileOverlay(
    tileProvider: TileProvider,
    latLngBounds: LatLngBounds,
    cacheSize: Int = 20 * 1024 * 1024
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<TileOverlayNode, MapApplier>(
        factory = {
            val tileOverlay = mapApplier?.map?.addTileLayer(
                TileOverlayOptions().apply {
                    tileProvider(tileProvider)
                    setPositionFromBounds(latLngBounds)
                    setMaxTileTmp(cacheSize)
                }
            ) ?: error("Error adding tile overlay")
            TileOverlayNode(tileOverlay)
        },
        update = {
            set(tileProvider) {
                this.tileOverlay.clearTileCache()
                this.tileOverlay.removeTileOverlay()
                this.tileOverlay = mapApplier?.map?.addTileLayer(
                    TileOverlayOptions().apply {
                        tileProvider(tileProvider)
                        setPositionFromBounds(latLngBounds)
                        setMaxTileTmp(cacheSize)
                    }
                ) ?: error("Error adding tile overlay")
            }
        }
    )
}