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

package com.melody.tencentmap.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.melody.map.tencent_compose.TXMap
import com.melody.map.tencent_compose.model.MapType
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.melody.sample.common.model.ImmutableListWrapper
import com.melody.ui.components.BasicFeatureMenuBar
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds

/**
 * BasicFeatureScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/09 14:06
 */
@Composable
internal fun BasicFeatureScreen() {
    val cameraPositionState = rememberCameraPositionState()
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var mapProperties by remember { mutableStateOf(MapProperties()) }

    val menuList by remember {
        mutableStateOf(ImmutableListWrapper(
            listOf(
                "普通地图",
                "卫星图",
                "暗色地图",
                "新3D沉浸地图",
                "3D楼块效果",
                "地图标注及名称",
                "实时交通状况开关",
                "显示室内地图开关",
                "显示手绘图",
                "设置地图显示范围",
                "地图Logo缩放",
                "旋转手势开关",
                "拖拽手势开关",
                "倾斜手势开关",
                "缩放手势开关",
                "指南针控件开关",
                "比例尺控件开关",
                "比例尺淡入淡出"
            )
        ))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 地图
        TXMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties
        )

        // 功能菜单选项
        Column(modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()) {
            BasicFeatureMenuBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Black.copy(alpha = 0.3F)),
                listWrapper = menuList,
                onStatusLabel = {
                    Text(
                        text = when (it) {
                            "普通地图"-> mapProperties.mapType == MapType.NORMAL
                            "卫星图"-> mapProperties.mapType == MapType.SATELLITE
                            "暗色地图"-> mapProperties.mapType == MapType.DARK
                            "新3D沉浸地图"-> mapProperties.mapType == MapType.NEW_3D_IMMERSIVE
                            "3D楼块效果"-> mapProperties.isShowBuildings
                            "地图标注及名称"-> mapProperties.isShowMapLabels
                            "实时交通状况开关" -> mapProperties.isTrafficEnabled
                            "显示室内地图开关" -> mapProperties.isIndoorEnabled
                            "显示手绘图" -> mapProperties.isHandDrawMapEnable
                            "设置地图显示范围" -> if(mapProperties.restrictWidthBounds == null) "" else "腾讯总部大楼"
                            "地图Logo缩放" -> uiSettings.logoScale
                            "旋转手势开关" -> uiSettings.isRotateGesturesEnabled
                            "拖拽手势开关" -> uiSettings.isScrollGesturesEnabled
                            "倾斜手势开关" -> uiSettings.isTiltGesturesEnabled
                            "缩放手势开关" -> uiSettings.isZoomGesturesEnabled
                            "指南针控件开关" -> uiSettings.isCompassEnabled
                            "比例尺控件开关" -> uiSettings.isScaleControlsEnabled
                            "比例尺淡入淡出" -> uiSettings.isScaleViewFadeEnable
                            else -> ""
                        }.toString(),
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Red,
                                offset = Offset.Zero,
                                blurRadius = 20.0f
                            )
                        )
                    )
                },
                onItemClick = {
                    when (it) {
                        "普通地图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.NORMAL)
                        }
                        "卫星图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.SATELLITE)
                        }
                        "暗色地图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.DARK)
                        }
                        "新3D沉浸地图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.NEW_3D_IMMERSIVE)
                        }
                        "3D楼块效果" -> {
                            mapProperties = mapProperties.copy(isShowBuildings = !mapProperties.isShowBuildings)
                        }
                        "地图标注及名称" -> {
                            mapProperties = mapProperties.copy(isShowMapLabels = !mapProperties.isShowMapLabels)
                        }
                        "实时交通状况开关"-> {
                            mapProperties = mapProperties.copy(isTrafficEnabled = !mapProperties.isTrafficEnabled)
                        }
                        "显示室内地图开关"-> {
                            mapProperties = mapProperties.copy(isIndoorEnabled = !mapProperties.isIndoorEnabled)
                        }
                        "显示手绘图"-> {
                            // 注意：手绘图主要用于：景区！！！！！
                            mapProperties = mapProperties.copy(isHandDrawMapEnable = !mapProperties.isHandDrawMapEnable)
                            if(mapProperties.isHandDrawMapEnable) {
                                // 地图视野移动，指定了经纬度和缩放级别
                                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(25.072295,102.761478), 18F))
                            }
                        }
                        "设置地图显示范围"-> {
                            // 设置地图只显示腾讯总部大楼这个区域的地图范围
                            mapProperties = mapProperties.copy(
                                // 这里不能用LatLngBounds构造函数，否则无法构建显示范围，请看源码
                                restrictWidthBounds = LatLngBounds.builder()
                                    .include(LatLng(40.042893, 116.269673))
                                    .include(LatLng(40.038951, 116.275241))
                                    .build(),
                                restrictHeightBounds = LatLngBounds.builder()
                                    .include(LatLng(40.042893, 116.269673))
                                    .include(LatLng(40.038951, 116.275241))
                                    .build()
                            )
                        }
                        "地图Logo缩放"-> {
                            uiSettings = uiSettings.copy(logoScale = if(uiSettings.logoScale == 0.7F) 1.3F else 0.7F)
                        }
                        "旋转手势开关"-> {
                            uiSettings = uiSettings.copy(isRotateGesturesEnabled = !uiSettings.isRotateGesturesEnabled)
                        }
                        "拖拽手势开关"-> {
                            uiSettings = uiSettings.copy(isScrollGesturesEnabled = !uiSettings.isScrollGesturesEnabled)
                        }
                        "倾斜手势开关"-> {
                            uiSettings = uiSettings.copy(isTiltGesturesEnabled = !uiSettings.isTiltGesturesEnabled)
                        }
                        "缩放手势开关"-> {
                            uiSettings = uiSettings.copy(isZoomGesturesEnabled = !uiSettings.isZoomGesturesEnabled)
                        }
                        "指南针控件开关"-> {
                            uiSettings = uiSettings.copy(isCompassEnabled = !uiSettings.isCompassEnabled)
                        }
                        "比例尺控件开关"-> {
                            uiSettings = uiSettings.copy(isScaleControlsEnabled = !uiSettings.isScaleControlsEnabled)
                        }
                        "比例尺淡入淡出"-> {
                            uiSettings = uiSettings.copy(isScaleViewFadeEnable = !uiSettings.isScaleViewFadeEnable)
                        }
                    }
                }
            )
        }
    }
}