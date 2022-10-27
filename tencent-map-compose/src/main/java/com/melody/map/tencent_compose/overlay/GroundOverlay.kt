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
import androidx.compose.ui.geometry.Offset
import com.melody.map.tencent_compose.MapApplier
import com.melody.map.tencent_compose.MapNode
import com.melody.map.tencent_compose.model.TXMapComposable
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.GroundOverlay
import com.tencent.tencentmap.mapsdk.maps.model.GroundOverlayOptions
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds

internal class GroundOverlayNode(
    val groundOverlay: GroundOverlay
) : MapNode {
    override fun onRemoved() {
        groundOverlay.remove()
    }
}

/**
 * 通过create创建LatLngBounds或者Position
 * @param latLngBounds 设置图片展示的经纬度范围，与 position(LatLng) 互斥,如果用户同时调用 position(LatLng) 和 latLngBounds(LatLngBounds) ,
 * sdk 会使用 latLngBounds(LatLngBounds) 的值
 * @param location 设置图片的位置，默认锚点为图片中心点
 */
class GroundOverlayPosition private constructor(
    val latLngBounds: LatLngBounds? = null,
    val location: LatLng? = null
) {
    companion object {
        fun create(latLngBounds: LatLngBounds) : GroundOverlayPosition {
            return GroundOverlayPosition(latLngBounds = latLngBounds)
        }

        fun create(location: LatLng) : GroundOverlayPosition {
            return GroundOverlayPosition(location = location)
        }
    }
}

/**
 * 在地图上绘制一个 Ground 覆盖物（一张图片以合适的大小贴在地图上的图片层），它会跟随地图的缩放而缩放。
 *
 * 使用 LatLngBounds 设置放置图片的经纬度区域。
 * 使用 LatLng 设置图片的锚点的经纬度，默认我们把图片的锚点放在图片的中心，即 (0.5, 0.5),
 * 用户可以通过 GroundOverlayOptions.zoom(float zoom)，设置图片原比例展示的地图级别，sdk 默认是 18 级展示原图尺寸
 *
 * @param position 设置图片的位置，默认锚点为图片中心点
 * @param image 覆盖物的贴图
 * @param anchor 设置图片的锚点, 只有 position(LatLng) 设置后才会生效
 * @param transparency  ground 覆盖物的透明度
 * @param zoom 设置图片不缩放展示的地图级别，默认 18 级，只有 position(LatLng) 设置后才会生效
 * @param visible ground 覆盖物是否可见
 * @param zIndex 展示层级，GroundOverlay 之间的压盖关系
 */
@Composable
@TXMapComposable
fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset = Offset(0.5f, 0.5f),
    transparency: Float = 0f,
    zoom: Float = 18f,
    visible: Boolean = true,
    zIndex: Int = 0
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<GroundOverlayNode, MapApplier>(
        factory = {
            val groundOverlay = mapApplier?.map?.addGroundOverlay(GroundOverlayOptions().apply {
                anchor(anchor.x, anchor.y)
                bitmap(image)
                alpha(transparency)
                visible(visible)
                zoom(zoom)
                zIndex(zIndex)
            }) ?: error("Error adding ground overlay")
            GroundOverlayNode(groundOverlay)
        },
        update = {
            set(image) { this.groundOverlay.setBitmap(it) }
            set(position) { this.groundOverlay.position(it) }
            set(transparency) { this.groundOverlay.setAlpha(it) }
            set(visible) { this.groundOverlay.setVisibility(it) }
            set(zIndex) { this.groundOverlay.setZindex(it) }
            set(zoom) { this.groundOverlay.setZoom(it) }
        }
    )
}

private fun GroundOverlay.position(position: GroundOverlayPosition) {
    if (position.latLngBounds != null) {
        // 设置图片展示的经纬度范围，与 position(LatLng) 互斥,
        // 如果用户同时调用 position(LatLng) 和 latLngBounds(LatLngBounds) , sdk 会使用 latLngBounds(LatLngBounds) 的值
        setLatLongBounds(position.latLngBounds)
        return
    }
    if (position.location != null) {
        // position(LatLng)
        setPosition(position.location)
    }
}