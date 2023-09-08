// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
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

package com.melody.map.petal_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import com.huawei.hms.maps.model.BitmapDescriptor
import com.huawei.hms.maps.model.GroundOverlay
import com.huawei.hms.maps.model.GroundOverlayOptions
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.LatLngBounds
import com.huawei.hms.maps.model.Marker
import com.melody.map.petal_compose.MapApplier
import com.melody.map.petal_compose.MapNode
import com.melody.map.petal_compose.model.HWMapComposable


internal class GroundOverlayNode(
    val groundOverlay: GroundOverlay,
    val onGroundOverlayClick: (GroundOverlay) -> Unit
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
    val width: Float? = null,
    val height: Float? = null,
) {
    companion object {
        fun create(latLngBounds: LatLngBounds) : GroundOverlayPosition {
            return GroundOverlayPosition(latLngBounds = latLngBounds)
        }

        fun create(location: LatLng, width: Float, height: Float? = null) : GroundOverlayPosition {
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
 * @param image 覆盖物的贴图
 * @param anchor 设置图片的锚点，[0,0]是左上角，[1,1]是右下角
 * @param bearing ground 覆盖物从正北顺时针的角度，相对锚点旋转
 * @param transparency  ground 覆盖物的透明度
 * @param isClickable  ground 覆盖物的可点击性
 * @param visible ground 覆盖物是否可见
 * @param zIndex 展示层级，ground覆盖物之间的压盖关系
 * @param onGroundOverlayClick ground 覆盖物的点击回调
 */
@Composable
@HWMapComposable
fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset = Offset(0.5f, 0.5f),
    bearing: Float = 0f,
    transparency: Float = 0f,
    visible: Boolean = true,
    isClickable: Boolean = true,
    zIndex: Float = 0f,
    onGroundOverlayClick: (GroundOverlay) -> Unit = {}
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<GroundOverlayNode, MapApplier>(
        factory = {
            val groundOverlay = mapApplier?.map?.addGroundOverlay(GroundOverlayOptions().apply {
                anchor(anchor.x, anchor.y)
                bearing(bearing)
                image(image)
                position(position)
                transparency(transparency)
                clickable(isClickable)
                visible(visible)
                zIndex(zIndex)
            }) ?: error("Error adding ground overlay")
            GroundOverlayNode(groundOverlay,onGroundOverlayClick)
        },
        update = {
            set(bearing) { this.groundOverlay.bearing = it }
            set(image) { this.groundOverlay.setImage(it) }
            set(isClickable) { this.groundOverlay.isClickable = it }
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

    if (position.location != null) {
        setPosition(position.location)
    }

    if (position.width != null && position.height == null) {
        setDimensions(position.width)
    } else if (position.width != null && position.height != null) {
        setDimensions(position.width, position.height)
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
        return position(position.location, position.width)
    }

    return position(position.location, position.width, position.height)
}