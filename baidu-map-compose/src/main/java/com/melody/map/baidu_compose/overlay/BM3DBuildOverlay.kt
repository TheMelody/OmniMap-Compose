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
import com.baidu.mapapi.map.Building
import com.baidu.mapapi.map.BuildingOptions
import com.baidu.mapapi.search.core.BuildingInfo
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class BM3DBuildNode(
    val building: Building
) : MapNode {
    override fun onRemoved() {
        building.remove()
    }
}

/**
 * 3D建筑物覆盖物
 * @param floorHeight 设置楼层初始高度
 * @param floorColor 设置楼层颜色
 * @param topFaceColor 设置建筑物顶部颜色
 * @param sideFaceColor 设置建筑物侧面颜色
 * @param buildingInfo 设置建筑物信息
 * @param customSideImage 自定义侧面纹理图片
 * @param enableAnim 设置是否打开建筑物立楼动画
 * @param visible 设置建筑物可见性
 * @param zIndex 设置建筑物开始显示层级
 */
@Composable
@BDMapComposable
fun BM3DBuildOverlay(
    floorHeight: Float,
    floorColor: Color,
    topFaceColor: Color,
    sideFaceColor: Color,
    buildingInfo: BuildingInfo? = null,
    customSideImage: BitmapDescriptor? = null,
    enableAnim: Boolean = false,
    visible: Boolean = true,
    zIndex:Int = 0
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<BM3DBuildNode, MapApplier>(
        factory = {
            val building = mapApplier?.map?.addOverlay(
                BuildingOptions().apply  {
                    setFloorHeight(floorHeight)
                    setFloorColor(floorColor.toArgb())
                    setTopFaceColor(topFaceColor.toArgb())
                    setSideFaceColor(sideFaceColor.toArgb())
                    setBuildingInfo(buildingInfo)
                    customSideImage(customSideImage)
                    isAnimation = enableAnim
                    visible(visible)
                    showLevel = zIndex
                }
            ) as? Building ?: error("Error adding Building")
            BM3DBuildNode(building)
        },
        update = {
            set(floorHeight) { this.building.floorHeight = it }
            set(floorColor) { this.building.floorColor = it.toArgb() }
            set(topFaceColor) { this.building.topFaceColor = it.toArgb() }
            set(sideFaceColor) { this.building.sideFaceColor = it.toArgb() }
            set(buildingInfo) { this.building.buildingInfo = it }
            set(customSideImage) { this.building.customSideImage = it }
            set(enableAnim) { this.building.isAnimation = it }
            set(zIndex) { this.building.showLevel = it }
            set(visible) { this.building.isVisible = it }
        }
    )
}