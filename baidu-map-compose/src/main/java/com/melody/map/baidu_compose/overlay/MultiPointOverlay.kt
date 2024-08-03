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

import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.MultiPoint
import com.baidu.mapapi.map.MultiPointItem
import com.baidu.mapapi.map.MultiPointOption
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class MultiPointOverlayNode(
    val multiPointOverlay: MultiPoint,
    var onPointItemClick: (MultiPointItem) -> Unit
) : MapNode {
    override fun onRemoved() {
        multiPointOverlay.remove()
    }
}

/**
 * 海量点覆盖物
 * @param anchor 锚点
 * @param icon 图标
 * @param textureSize 纹理渲染大小，【不设置，默认为icon图片大小】
 * @param multiPointItems 海量点中某个点的位置及其他信息
 */
@Composable
@BDMapComposable
fun MultiPointOverlay(
    anchor: Offset = Offset(0.5F, 0.5F),
    icon: BitmapDescriptor,
    textureSize: Size? = null,
    multiPointItems: List<MultiPointItem>,
    onClick: (MultiPointItem) -> Unit
) {
    // Fix：新版本SDK为空的时候不给渲染的错误
    if(multiPointItems.isEmpty()) return
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<MultiPointOverlayNode, MapApplier>(
        factory = {
            val multiPointOverlay =
                mapApplier?.map?.addOverlay(MultiPointOption().apply {
                    this.setAnchor(anchor.x, anchor.y)
                    // 纹理渲染大小，默认为icon图片大小
                    textureSize?.let {
                        this.setPointSize(it.width,it.height)
                    }
                    this.multiPointItems = multiPointItems
                    this.icon = icon
                }) as? MultiPoint ?: error("Error adding MultiPointOverlay")
            MultiPointOverlayNode(multiPointOverlay, onClick)
        },
        update = {
            update(onClick) { this.onPointItemClick = it }

            set(anchor) { this.multiPointOverlay.anchor(it.x, it.y) }
            set(icon) { this.multiPointOverlay.icon = it }
            set(textureSize) {
                it?.apply { multiPointOverlay.setPointSize(this.width,this.height) }
            }
            set(multiPointItems) { this.multiPointOverlay.multiPointItems = it }
        }
    )
}