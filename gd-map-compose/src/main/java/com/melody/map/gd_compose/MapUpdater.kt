package com.melody.map.gd_compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.amap.api.maps.AMap
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.CameraPosition
import com.melody.map.gd_compose.model.MapClickListeners
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.map.gd_compose.position.CameraPositionState

internal class MapPropertiesNode(
    val map: AMap,
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
        map.setOnMapLoadedListener { clickListeners.onMapLoaded() }
        map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(cameraPosition: CameraPosition?) {
                cameraPositionState.rawPosition = map.cameraPosition
                cameraPositionState.isMoving = true
            }
            override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
                cameraPositionState.rawPosition = map.cameraPosition
                cameraPositionState.isMoving = false
            }
        })
        map.addOnPOIClickListener { clickListeners.onPOIClick(it) }
        //map.setOnPolylineClickListener { clickListeners.onPolyLineClick(it) }
        map.setOnIndoorBuildingActiveListener { clickListeners.indoorBuildingActive(it) }
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

        // 是否在地图上面显示，默认的Logo
        set(mapUiSettings.showMapLogo) {
            if(mapUiSettings.showMapLogo) {
                map.uiSettings.setLogoLeftMargin(0)
            } else {
                map.uiSettings.setLogoLeftMargin(Int.MAX_VALUE/2)
            }
        }
        // 设置定位监听
        set(locationSource) { map.setLocationSource(it) }
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
        set(mapProperties.isMyLocationEnabled) { map.isMyLocationEnabled = it }
        // 设置默认定位按钮是否显示，非必需设置。
        set(mapUiSettings.myLocationButtonEnabled) { map.uiSettings.isMyLocationButtonEnabled = it }
        // 修改定位蓝点样式
        set(mapProperties.myLocationStyle) { map.myLocationStyle = it }
        // 是否显示室内地图
        set(mapProperties.isIndoorEnabled) { map.showIndoorMap(it)  }
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
        // 比例尺控件是否可见
        set(mapUiSettings.isScaleControlsEnabled) { map.uiSettings.isScaleControlsEnabled = it }
        // 缩放手势是否可用
        set(mapUiSettings.isZoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }
        // 设置地图最大缩放级别 缩放级别范围为[3, 20],超出范围将按最大级别计算
        set(mapProperties.maxZoomPreference) { map.maxZoomLevel = it }
        // 设置最小缩放级别 缩放级别范围为[3, 20],超出范围将按最小级别计算
        set(mapProperties.minZoomPreference) { map.minZoomLevel = it }
        // 设置地图模式，默认为：MAP_TYPE_NORMAL
        set(mapProperties.mapType) { map.mapType = it.value }
        // 设置地图底图语言，目前支持中文底图和英文底图
        set(mapProperties.language) { map.setMapLanguage(it) }
        // 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域
        set(mapProperties.mapShowLatLngBounds) { map.setMapStatusLimits(it) }

        update(cameraPositionState) { this.cameraPositionState = it }
        update(clickListeners) { this.clickListeners = it }
    }
}
