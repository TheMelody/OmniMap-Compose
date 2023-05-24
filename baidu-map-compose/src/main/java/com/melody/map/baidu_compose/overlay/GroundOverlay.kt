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
import androidx.compose.ui.geometry.Offset
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.GroundOverlay
import com.baidu.mapapi.map.GroundOverlayOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable
import kotlin.IllegalStateException

internal class GroundOverlayNode(
    val groundOverlay: GroundOverlay
) : MapNode {
    override fun onRemoved() {
        groundOverlay.remove()
    }
}

/**
 * The position of a [GroundOverlay].
 *
 * Use one of the [create] methods to construct an instance of this class.
 */
class GroundOverlayPosition private constructor(
    val latLngBounds: LatLngBounds? = null,
    val location: LatLng? = null,
    val width: Int? = null,
    val height: Int? = null,
) {
    companion object {
        fun create(latLngBounds: LatLngBounds) : GroundOverlayPosition {
            return GroundOverlayPosition(latLngBounds = latLngBounds)
        }

        fun create(location: LatLng, width: Int, height: Int? = null) : GroundOverlayPosition {
            return GroundOverlayPosition(
                location = location,
                width = width,
                height = height
            )
        }
    }
}

/**
 * 在地图上绘制一个 Ground 覆盖物（一张图片以合适的大小贴在地图上的图片层），它会跟随地图的缩放而缩放。
 *
 * @param position ground 覆盖物的位置的锚点
 * @param image 覆盖物的贴图。在添加图片层之前，如果没有设置图片，会报IllegalArgumentException 异常。
 * @param anchor 设置图片的锚点，[0,0]是左上角，[1,1]是右下角
 * @param transparency  ground 覆盖物的透明度
 * @param visible ground 覆盖物是否可见
 * @param zIndex 展示层级，ground覆盖物之间的压盖关系
 */
@Composable
@BDMapComposable
fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset = Offset(0.5f, 0.5f),
    transparency: Float = 1f,
    visible: Boolean = true,
    zIndex: Int = 0
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<GroundOverlayNode, MapApplier>(
        factory = {
            val groundOverlay = mapApplier?.map?.addOverlay(GroundOverlayOptions().apply {
                anchor(anchor.x, anchor.y)
                image(image)
                position(position)
                transparency(transparency)
                visible(visible)
                zIndex(zIndex)
            }) as? GroundOverlay ?: error("Error adding ground overlay")
            GroundOverlayNode(groundOverlay)
        },
        update = {
            set(image) { this.groundOverlay.image = it }
            set(position) { this.groundOverlay.position(it) }
            set(transparency) { this.groundOverlay.transparency = it }
            set(visible) { this.groundOverlay.isVisible = it }
            set(zIndex) { this.groundOverlay.zIndex = it }
        }
    )
}

private fun GroundOverlay.position(position: GroundOverlayPosition) {
    if (position.latLngBounds != null) {
        // 根据矩形区域设置ground 覆盖物的位置
        setPositionFromBounds(position.latLngBounds)
        return
    }
    if (position.width != null && position.height == null) {
        setDimensions(position.width)
    } else if (position.width != null && position.height != null) {
        setDimensions(position.width, position.height)
    }
    if (position.location != null) {
        setPosition(position.location)
    }
}

private fun GroundOverlayOptions.position(position: GroundOverlayPosition): GroundOverlayOptions {
    if (position.latLngBounds != null) {
        // 根据矩形区域设置ground 覆盖物的位置
        return positionFromBounds(position.latLngBounds)
    }
    // 根据位置和宽高设置ground 覆盖物。在显示时，图片会被缩放来适应指定的尺寸
    if (position.location == null || position.width == null) {
        throw IllegalStateException("Invalid position $position")
    }
    if (position.height == null) {
        dimensions(position.width)
    } else {
        dimensions(position.width, position.height)
    }
    return position(position.location)
}