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

package com.melody.map.tencent_compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.melody.map.tencent_compose.model.MapClickListeners
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.map.tencent_compose.position.CameraPositionState
import com.tencent.tencentmap.mapsdk.maps.LocationSource
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMap.OnMapLoadedCallback
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationConfig
import com.tencent.tencentmap.mapsdk.maps.model.RestrictBoundsFitMode

internal class MapPropertiesNode(
    val map: TencentMap,
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
        // 设置地图加载完成回调接口
        map.addOnMapLoadedCallback(object :OnMapLoadedCallback{
            override fun onMapLoaded() {
                clickListeners.onMapLoaded()
                // 移除监听
                map.removeOnMapLoadedCallback(this)
            }
        })
        map.setOnMapClickListener { clickListeners.onMapClick(it) }
        map.setOnMapLongClickListener { clickListeners.onMapLongClick(it) }
        map.setOnMapPoiClickListener { clickListeners.onMapPOIClick(it) }
        map.setOnCameraChangeListener(object : TencentMap.OnCameraChangeListener {
            override fun onCameraChange(cameraPosition: CameraPosition?) {
                cameraPositionState.transformToTxCameraPosition(map.cameraPosition)
                cameraPositionState.isMoving = true
            }

            override fun onCameraChangeFinished(cameraPosition: CameraPosition?) {
                cameraPositionState.transformToTxCameraPosition(map.cameraPosition)
                cameraPositionState.isMoving = false
            }
        })
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

        // 设置Logo的缩放比例，比例范围(0.7~1.3)
        set(mapUiSettings.logoScale) { map.uiSettings.setLogoScale(it) }
        // 设置定位监听
        set(locationSource) { map.myLocationConfig = MyLocationConfig.newBuilder(map.myLocationConfig).setLocationSource(it).build() }
        // 设置地图是否允许多InfoWindow模式，默认是false(只允许显示一个InfoWindow)
        set(mapProperties.enableMultipleInfoWindow) { map.enableMultipleInfowindow(it) }
        // 是否显示地图标注及名称
        set(mapProperties.isShowMapLabels) { map.setPoisEnabled(it) }
        // 基于宽度限制地图显示范围
        set(mapProperties.restrictWidthBounds) {
            if(null != it) {
                map.setRestrictBounds(it, RestrictBoundsFitMode.FIT_WIDTH)
            }
        }
        // 基于高度显示地图范围
        set(mapProperties.restrictHeightBounds) {
            if(null != it) {
                map.setRestrictBounds(it, RestrictBoundsFitMode.FIT_HEIGHT)
            }
        }
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
        set(mapProperties.isMyLocationEnabled) { map.myLocationConfig = MyLocationConfig.newBuilder(map.myLocationConfig).setMyLocationEnabled(it).build() }
        // 设置默认定位按钮是否显示，非必需设置。
        set(mapUiSettings.myLocationButtonEnabled) { map.uiSettings.isMyLocationButtonEnabled = it }
        // 修改定位蓝点样式
        set(mapProperties.myLocationStyle) { map.myLocationConfig = MyLocationConfig.newBuilder(map.myLocationConfig).setMyLocationStyle(it).build() }
        // 是否显示3D楼块,默认显示
        set(mapProperties.isShowBuildings) { map.setBuilding3dEffectEnable(it) }
        // 是否显示室内地图
        set(mapProperties.isIndoorEnabled) { map.setIndoorEnabled(it)  }
        // 是否显示手绘图，**手绘图的主要应用场景是：景区**
        set(mapProperties.isHandDrawMapEnable) { map.isHandDrawMapEnable = it }
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
        // 缩放按钮是否可见，SDK从4.3.1弃用，Android对ZoomButton已不维护，建议使用自定义View
        //set(mapUiSettings.isZoomEnabled) { map.uiSettings.isZoomControlsEnabled = it }
        // 比例尺控件是否可见
        set(mapUiSettings.isScaleControlsEnabled) { map.uiSettings.isScaleViewEnabled = it }
        // 比例尺是否淡出
        set(mapUiSettings.isScaleViewFadeEnable) { map.uiSettings.setScaleViewFadeEnable(it) }
        // 缩放手势是否可用
        set(mapUiSettings.isZoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }
        // 设置地图最大缩放级别 缩放级别范围为[3, 22],超出范围将按最大级别计算
        set(mapProperties.maxZoomPreference) { map.setMaxZoomLevel(it.toInt()) }
        // 设置最小缩放级别 缩放级别范围为[3, 22],超出范围将按最小级别计算
        set(mapProperties.minZoomPreference) { map.setMinZoomLevel(it.toInt()) }
        // 设置地图模式，默认为：MAP_TYPE_NORMAL
        set(mapProperties.mapType) { map.mapType = it.value }
        // 设置地图Logo位置
        set(mapUiSettings.mapLogoAnchor) {
            map.uiSettings.setLogoPosition(it.logoPosition, intArrayOf(it.offset.x,it.offset.y))
        }
        // 设置比例尺位置
        set(mapUiSettings.mapScaleViewAnchor) {
            // setScaleViewPositionWithMargin(int position, int top, int bottom, int left, int right)
            map.uiSettings.setScaleViewPositionWithMargin(
                it.scaleViewPosition,
                it.ltrb.top,
                it.ltrb.bottom,
                it.ltrb.left,
                it.ltrb.right
            )
        }

        update(cameraPositionState) { this.cameraPositionState = it }
        update(clickListeners) { this.clickListeners = it }
    }
}
