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

package com.melody.map.baidu_compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.model.BDCameraPosition
import com.melody.map.baidu_compose.model.MapClickListeners
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.map.baidu_compose.position.CameraPositionState

internal class MapPropertiesNode(
    val map: BaiduMap,
    var clickListeners: MapClickListeners,
    var density: Density,
    var layoutDirection: LayoutDirection,
    cameraPositionState: CameraPositionState
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
        map.setOnMapClickListener(object :BaiduMap.OnMapClickListener{
            override fun onMapClick(point: LatLng?) {
                clickListeners.onMapClick(point)
            }
            override fun onMapPoiClick(poi: MapPoi?) {
                clickListeners.onMapPOIClick(poi)
            }
        })
        map.setOnMapLongClickListener { clickListeners.onMapLongClick(it) }
        map.setOnMapTouchListener { clickListeners.onOnMapTouchEvent(it) }
        map.setOnMapStatusChangeListener(object : OnMapStatusChangeListener {
            override fun onMapStatusChangeStart(status: MapStatus?) {
                //手势操作地图，设置地图状态等操作导致地图状态开始改变。
                status?.let {
                    cameraPositionState.rawPosition = BDCameraPosition.convertMapStatusData(it)
                }
                cameraPositionState.isMoving = true
            }
            override fun onMapStatusChangeStart(status: MapStatus?, reason: Int) {
                //用户手势触发导致的地图状态改变,比如双击、拖拽、滑动底图
                //int REASON_GESTURE = 1;
                //SDK导致的地图状态改变, 比如点击缩放控件、指南针图标
                //int REASON_API_ANIMATION = 2;
                //开发者调用,导致的地图状态改变
                //int REASON_DEVELOPER_ANIMATION = 3;
                onMapStatusChangeStart(status)
            }
            override fun onMapStatusChange(status: MapStatus?) {
                // 地图状态变化中
                onMapStatusChangeStart(status)
            }
            override fun onMapStatusChangeFinish(status: MapStatus?) {
                // 地图状态改变结束
                status?.let {
                    cameraPositionState.rawPosition = BDCameraPosition.convertMapStatusData(it)
                }
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
    mapView: MapView,
    mapUiSettings: MapUiSettings,
    clickListeners: MapClickListeners,
    locationSource: MyLocationData?,
    mapProperties: MapProperties,
    cameraPositionState: CameraPositionState
) {
    val mapApplier = currentComposer.applier as MapApplier
    val map = mapApplier.map
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

        // 地图上控件与地图边界的距离
        set(mapUiSettings.mapViewPadding) {
            map.setViewPadding(it.left, it.top, it.right, it.bottom)
        }
        // 设置定位数据, 只有先允许定位图层后设置数据才会生效
        set(locationSource) { map.setMyLocationData(it) }
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
        set(mapProperties.isMyLocationEnabled) { map.isMyLocationEnabled = it }
        // 修改定位蓝点样式
        set(mapProperties.myLocationStyle) {
            if(null != it) map.setMyLocationConfiguration(it)
        }
        // 是否显示3D楼块,默认不显示
        set(mapProperties.isShowBuildings) {
            map.isBuildingsEnabled = it
            // 设置setBuildingsEnabled之后【必须更新下地图】
            map.setMapStatus(MapStatusUpdateFactory.newMapStatus(map.mapStatus))
        }
        // 是否显示底图标注,默认显示
        set(mapProperties.isShowMapLabels) { map.showMapPoi(it) }
        // 设置室内图标注是否显示,默认显示
        set(mapProperties.isShowMapIndoorLabels) { map.showMapIndoorPoi(it) }
        // 是否显示室内地图
        set(mapProperties.isIndoorEnabled) { map.setIndoorEnable(it)   }
        // 是否显示路况图层
        set(mapProperties.isTrafficEnabled) { map.isTrafficEnabled = it }
        // 是否显示缩放按钮
        set(mapUiSettings.isZoomEnabled) { mapView.showZoomControls(it) }
        // 是否显示比例尺
        set(mapUiSettings.isScaleControlsEnabled) { mapView.showScaleControl(it) }
        // 指南针控件是否可见
        set(mapUiSettings.isCompassEnabled) { map.uiSettings.isCompassEnabled = it }
        // 旋转手势是否可用
        set(mapUiSettings.isRotateGesturesEnabled) { map.uiSettings.isRotateGesturesEnabled = it }
        // 倾斜手势(同地图【俯视手势】（3D）)是否可用
        set(mapUiSettings.isTiltGesturesEnabled) { map.uiSettings.isOverlookingGesturesEnabled = it }
        // 拖拽手势是否可用
        set(mapUiSettings.isScrollGesturesEnabled) { map.uiSettings.isScrollGesturesEnabled = it }
        // 是否允许双击放大地图手势
        set(mapUiSettings.isDoubleClickZoomEnabled) { map.uiSettings.setDoubleClickZoomEnabled(it) }
        // 是否允许抛出手势
        set(mapUiSettings.isFlingEnable) { map.uiSettings.setFlingEnable(it) }
        // 缩放动画惯性开关
        set(mapUiSettings.isInertialAnimation) { map.uiSettings.setInertialAnimation(it) }
        // 缩放手势是否可用
        set(mapUiSettings.isZoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }
        // 设置地图最大缩放级别 缩放级别范围为[4, 21]
        set(mapProperties.maxZoomPreference) { map.setMaxAndMinZoomLevel(it, mapProperties.minZoomPreference) }
        // 设置最小缩放级别 缩放级别范围为[4, 21]
        set(mapProperties.minZoomPreference) { map.setMaxAndMinZoomLevel(mapProperties.maxZoomPreference, it) }
        // 设置地图模式，默认为：MAP_TYPE_NORMAL
        set(mapProperties.mapType) { map.mapType = it.value }
        // 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域
        set(mapProperties.mapShowLatLngBounds) { map.setMapStatusLimits(it) }
        // 设置地图是否允许多InfoWindow模式，默认是false(只允许显示一个InfoWindow)
        set(mapProperties.enableMultipleInfoWindow) { mapApplier.enableMultipleInfoWindow(it) }

        update(cameraPositionState) { this.cameraPositionState = it }
        update(clickListeners) { this.clickListeners = it }
    }
}
