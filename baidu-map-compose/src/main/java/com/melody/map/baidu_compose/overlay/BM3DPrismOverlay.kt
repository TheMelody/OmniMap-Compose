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

package com.melody.map.baidu_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.Prism
import com.baidu.mapapi.map.PrismOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.BuildingInfo
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class BM3DPrismNode(
    val prism: Prism
) : MapNode {
    override fun onRemoved() {
        prism.remove()
    }
}

/**
 * 3D棱柱覆盖物
 * @param height 3D棱柱高度
 * @param points 3D棱柱坐标点列表
 * @param topFaceColor 3D棱柱顶面颜色
 * @param sideFaceColor 3D棱柱侧面颜色
 * @param buildingInfo 建筑物3D棱柱
 * @param customSideImage 3D棱柱自定义侧面纹理图片
 * @param isShowMarker 3DMarker是否展示
 * @param enableGrowAnim 是否启用建筑物生长动画
 * @param visible 3D棱柱可见性
 * @param zIndex 建筑物显示层级
 */
@Composable
@BDMapComposable
fun BM3DPrismOverlay(
    height: Float,
    points: List<LatLng>,
    topFaceColor: Color,
    sideFaceColor: Color,
    buildingInfo: BuildingInfo? = null,
    customSideImage: BitmapDescriptor? = null,
    isShowMarker: Boolean = false,
    enableGrowAnim: Boolean = false,
    visible: Boolean = true,
    zIndex:Int = 0
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<BM3DPrismNode, MapApplier>(
        factory = {
            val prism = mapApplier?.map?.addOverlay(
                PrismOptions().apply  {
                    setHeight(height)
                    setPoints(points)
                    setTopFaceColor(topFaceColor.toArgb())
                    setSideFaceColor(sideFaceColor.toArgb())
                    setBuildingInfo(buildingInfo)
                    customSideImage(customSideImage)
                    showMarker(isShowMarker)
                    isAnimation = enableGrowAnim
                    visible(visible)
                    showLevel = zIndex
                }
            ) as? Prism ?: error("Error adding Prism")
            BM3DPrismNode(prism)
        },
        update = {
            set(height) { this.prism.height = it }
            set(points) { this.prism.points = it }
            set(topFaceColor) { this.prism.topFaceColor = it.toArgb() }
            set(sideFaceColor) { this.prism.sideFaceColor = it.toArgb() }
            set(buildingInfo) { this.prism.buildingInfo = it }
            set(customSideImage) { this.prism.customSideImage = it }
            set(enableGrowAnim) { this.prism.isAnimation = it }
            set(zIndex) { this.prism.showLevel = it }
            set(visible) { this.prism.isVisible = it }
        }
    )
}