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

package com.melody.map.myapplication.ui

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
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.model.MapType
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.sample.common.model.ImmutableListWrapper
import com.melody.ui.components.BasicFeatureMenuBar

/**
 * BasicFeatureScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/09 14:06
 */
@Composable
internal fun BasicFeatureScreen() {
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }

    var mapProperties by remember { mutableStateOf(MapProperties()) }

    val menuList by remember {
        mutableStateOf(ImmutableListWrapper(
            listOf(
                "白昼地图",
                "卫星图",
                "导航地图",
                "夜景地图",
                "实时交通状况开关",
                "地图语言切换",
                "显示室内地图开关",
                "设置地图显示范围",
                "地图Logo开关",
                "旋转手势开关",
                "拖拽手势开关",
                "倾斜手势开关",
                "缩放手势开关",
                "缩放按钮开关",
                "指南针控件开关",
                "比例尺控件开关"
            )
        ))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 地图
        GDMap(
            modifier = Modifier.matchParentSize(),
            uiSettings = uiSettings,
            properties = mapProperties
        )

        // 功能菜单选项
        Column(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
            BasicFeatureMenuBar(
                modifier = Modifier.fillMaxWidth().background(color = Color.Black.copy(alpha = 0.3F)),
                listWrapper = menuList,
                onStatusLabel = {
                    Text(
                        text = when (it) {
                            "白昼地图"-> mapProperties.mapType == MapType.NORMAL
                            "卫星图"-> mapProperties.mapType == MapType.SATELLITE
                            "导航地图"-> mapProperties.mapType == MapType.NAVI
                            "夜景地图"-> mapProperties.mapType == MapType.NIGHT
                            "实时交通状况开关" -> mapProperties.isTrafficEnabled
                            "地图语言切换" -> mapProperties.language
                            "显示室内地图开关" -> mapProperties.isIndoorEnabled
                            "设置地图显示范围" -> if(mapProperties.mapShowLatLngBounds == null) null else "王府井区域内"
                            "地图Logo开关" -> uiSettings.showMapLogo
                            "旋转手势开关" -> uiSettings.isRotateGesturesEnabled
                            "拖拽手势开关" -> uiSettings.isScrollGesturesEnabled
                            "倾斜手势开关" -> uiSettings.isTiltGesturesEnabled
                            "缩放手势开关" -> uiSettings.isZoomGesturesEnabled
                            "缩放按钮开关" -> uiSettings.isZoomEnabled
                            "指南针控件开关" -> uiSettings.isCompassEnabled
                            "比例尺控件开关" -> uiSettings.showMapLogo && uiSettings.isScaleControlsEnabled
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
                        "白昼地图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.NORMAL)
                        }
                        "卫星图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.SATELLITE)
                        }
                        "导航地图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.NAVI)
                        }
                        "夜景地图"-> {
                            mapProperties = mapProperties.copy(mapType = MapType.NIGHT)
                        }
                        "实时交通状况开关"-> {
                            mapProperties = mapProperties.copy(isTrafficEnabled = !mapProperties.isTrafficEnabled)
                        }
                        "地图语言切换"-> {
                            mapProperties = mapProperties.copy(language = if(mapProperties.language == AMap.CHINESE) AMap.ENGLISH else AMap.CHINESE)
                        }
                        "显示室内地图开关"-> {
                            mapProperties = mapProperties.copy(isIndoorEnabled = !mapProperties.isIndoorEnabled)
                        }
                        "设置地图显示范围"-> {
                            if(mapProperties.mapShowLatLngBounds == null) {
                                // 设置地图只显示王府井这个区域的地图范围
                                mapProperties = mapProperties.copy(
                                    mapShowLatLngBounds = LatLngBounds(
                                        LatLng(39.935029, 116.384377), LatLng(39.939577, 116.388331)
                                    )
                                )
                            } else {
                                mapProperties = mapProperties.copy(mapShowLatLngBounds = null)
                            }
                        }
                        "地图Logo开关"-> {
                            uiSettings = uiSettings.copy(showMapLogo = !uiSettings.showMapLogo)
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
                        "缩放按钮开关"-> {
                            uiSettings = uiSettings.copy(isZoomEnabled = !uiSettings.isZoomEnabled)
                        }
                        "指南针控件开关"-> {
                            uiSettings = uiSettings.copy(isCompassEnabled = !uiSettings.isCompassEnabled)
                        }
                        "比例尺控件开关"-> {
                            if(!uiSettings.isScaleControlsEnabled) {
                                uiSettings = uiSettings.copy(showMapLogo = true)
                            }
                            uiSettings = uiSettings.copy(isScaleControlsEnabled = !uiSettings.isScaleControlsEnabled)
                        }
                    }
                }
            )
        }
    }
}