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

package com.melody.map.gd_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.MultiPointItem
import com.amap.api.maps.model.MultiPointOverlay
import com.amap.api.maps.model.MultiPointOverlayOptions
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.melody.map.gd_compose.model.GDMapComposable

internal class MultiPointOverlayNode(
    val multiPointOverlay: MultiPointOverlay,
    var onPointItemClick: (MultiPointItem) -> Unit
) : MapNode {
    override fun onRemoved() {
        multiPointOverlay.remove()
    }
}

/**
 * MultiPointOverlay
 * @param enable 是否可用
 * @param anchor 锚点
 * @param icon 图标
 * @param multiPointItems 海量点中某个点的位置及其他信息
 */
@Composable
@GDMapComposable
fun MultiPointOverlay(
    enable: Boolean,
    anchor: Offset = Offset(0.5F, 0.5F),
    icon: BitmapDescriptor,
    multiPointItems: List<MultiPointItem>,
    onClick: (MultiPointItem) -> Unit
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<MultiPointOverlayNode, MapApplier>(
        factory = {
            val multiPointOverlay =
                mapApplier?.map?.addMultiPointOverlay(MultiPointOverlayOptions().apply {
                    this.anchor(anchor.x, anchor.y)
                    this.setEnable(enable)
                    this.icon(icon)
                }) ?: error("Error adding MultiPointOverlay")
            multiPointOverlay.items = multiPointItems
            MultiPointOverlayNode(multiPointOverlay, onClick)
        },
        update = {
            update(onClick) { this.onPointItemClick = it }

            set(anchor) { this.multiPointOverlay.setAnchor(it.x, it.y) }
            set(enable) { this.multiPointOverlay.setEnable(it) }
            set(multiPointItems) { this.multiPointOverlay.items = it }
        }
    )
}