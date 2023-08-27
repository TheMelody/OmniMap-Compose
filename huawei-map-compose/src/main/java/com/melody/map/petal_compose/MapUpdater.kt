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

package com.melody.map.petal_compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.LocationSource
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.MyLocationStyle
import com.melody.map.petal_compose.model.MapClickListeners
import com.melody.map.petal_compose.poperties.MapProperties
import com.melody.map.petal_compose.poperties.MapUiSettings
import com.melody.map.petal_compose.position.CameraPositionState

internal class MapPropertiesNode(
    val map: HuaweiMap,
    var clickListeners: MapClickListeners,
    cameraPositionState: CameraPositionState,
    var density: Density,
    var layoutDirection: LayoutDirection,
) : MapNode {

    init {
        cameraPositionState.setMap(map)
    }

    var cameraPositionState = cameraPositionState
        set(value) {
            if (value == field) return
            field.setMap(null)
            field = value
            value.setMap(map)
        }

    override fun onAttached() {
        map.setOnMapLoadedCallback { clickListeners.onMapLoaded() }
        map.setOnCameraChangeListener(object : HuaweiMap.OnCameraChangeListener {
            override fun onCameraChange(cameraPosition: CameraPosition?) {
                cameraPositionState.rawPosition = map.cameraPosition
                cameraPositionState.isMoving = true
            }
        })
        map.setOnCameraIdleListener {
            cameraPositionState.rawPosition = map.cameraPosition
            cameraPositionState.isMoving = false
        }
        map.setOnMapClickListener { clickListeners.onMapClick(it) }
        map.setOnMapLongClickListener { clickListeners.onMapLongClick(it) }
        map.setOnPoiClickListener { clickListeners.onMapPOIClick(it) }
    }

    override fun onRemoved() {
        cameraPositionState.setMap(null)
    }
    override fun onCleared() {
        cameraPositionState.setMap(null)
    }
}

@SuppressLint("MissingPermission")
@Suppress("NOTHING_TO_INLINE")
@Composable
internal inline fun MapUpdater(
    mapUiSettings: MapUiSettings,
    clickListeners: MapClickListeners,
    locationSource: LocationSource?,
    mapProperties: MapProperties,
    cameraPositionState: CameraPositionState
) {
    val map = (currentComposer.applier as MapApplier).map
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    ComposeNode<MapPropertiesNode, MapApplier>(
        factory = {
            MapPropertiesNode(
                map = map,
                density = density,
                clickListeners = clickListeners,
                cameraPositionState = cameraPositionState,
                layoutDirection = layoutDirection
            )
        }
    ) {
        update(density) { this.density = it }
        update(layoutDirection) { this.layoutDirection = it }

        // 设置定位监听
        set(locationSource) { map.setLocationSource(it) }
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
        set(mapProperties.isMyLocationEnabled) { map.isMyLocationEnabled = it }
        // 设置默认定位按钮是否显示，非必需设置。
        set(mapUiSettings.myLocationButtonEnabled) { map.uiSettings.isMyLocationButtonEnabled = it }
        // 修改定位蓝点样式
        set(mapProperties.myLocationStyle) {
            map.myLocationStyle = it?: MyLocationStyle()
        }
        // 是否显示3D楼块,默认显示
        set(mapProperties.isShowBuildings) {
            map.isBuildingsEnabled = it
            // 【倾斜手势开启之后】双指向上或者向下，可动态更新倾斜角度
            val cameraPos = CameraPosition(
                map.cameraPosition.target,
                map.cameraPosition.zoom,
                if (it) mapProperties.showBuildingsTilt else 0F,
                map.cameraPosition.bearing
            )
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))
        }
        // 是否显示室内地图
        set(mapProperties.isIndoorEnabled) { map.isIndoorEnabled = it  }
        // 是否显示路况图层
        set(mapProperties.isTrafficEnabled) { map.isTrafficEnabled = it }
        // 指南针控件是否可见
        set(mapUiSettings.isCompassEnabled) { map.uiSettings.isCompassEnabled = it }
        // 旋转手势是否可用
        set(mapUiSettings.isRotateGesturesEnabled) { map.uiSettings.isRotateGesturesEnabled = it }
        // 倾斜手势是否可用
        set(mapUiSettings.isTiltGesturesEnabled) { map.uiSettings.isTiltGesturesEnabled = it }
        // 拖拽手势是否可用
        set(mapUiSettings.isScrollGesturesEnabled) { map.uiSettings.isScrollGesturesEnabled = it }
        // 缩放按钮是否可见
        set(mapUiSettings.isZoomEnabled) { map.uiSettings.isZoomControlsEnabled = it }
        // 缩放手势是否可用
        set(mapUiSettings.isZoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }
        // 设置地图最大缩放级别 缩放级别范围为[3, 20],超出范围将按最大级别计算
        set(mapProperties.maxZoomPreference) { map.setMaxZoomPreference(it) }
        // 设置最小缩放级别 缩放级别范围为[3, 20],超出范围将按最小级别计算
        set(mapProperties.minZoomPreference) { map.setMinZoomPreference(it) }
        // 设置地图模式，默认为：MAP_TYPE_NORMAL
        set(mapProperties.mapType) { map.mapType = it }
        // 设置地图底图语言，目前支持中文底图和英文底图
        set(mapProperties.language) { map.setLanguage(it) }
        // 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域
        set(mapProperties.mapShowLatLngBounds) { map.setLatLngBoundsForCameraTarget(it) }

        update(cameraPositionState) { this.cameraPositionState = it }
        update(clickListeners) { this.clickListeners = it }
    }
}
