// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.melody.map.compose

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
import com.melody.map.compose.poperties.MapProperties

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

        set(mapUiSettings.showMapLogo) {
            if(mapUiSettings.showMapLogo) {
                map.uiSettings.setLogoLeftMargin(0)
            } else {
                map.uiSettings.setLogoLeftMargin(Int.MAX_VALUE/2)
            }
        }
        // 设置定位监听
        set(locationSource) { map.setLocationSource(it) }
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        set(mapProperties.isMyLocationEnabled) { map.isMyLocationEnabled = it }
        // 设置默认定位按钮是否显示
        set(mapUiSettings.myLocationButtonEnabled) { map.uiSettings.isMyLocationButtonEnabled = it }
        // 修改定位蓝点样式
        set(mapProperties.myLocationStyle) { map.myLocationStyle = it }
        set(mapUiSettings.isCompassEnabled) { map.uiSettings.isCompassEnabled = it }
        set(mapUiSettings.isRotateGesturesEnabled) { map.uiSettings.isRotateGesturesEnabled = it }
        set(mapUiSettings.isTiltGesturesEnabled) { map.uiSettings.isTiltGesturesEnabled = it }
        set(mapUiSettings.isScrollGesturesEnabled) { map.uiSettings.isScrollGesturesEnabled = it }
        set(mapUiSettings.isZoomEnabled) { map.uiSettings.isZoomControlsEnabled = it }
        set(mapUiSettings.isScaleControlsEnabled) { map.uiSettings.isScaleControlsEnabled = it }
        set(mapUiSettings.isZoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }
        set(mapProperties.maxZoomPreference) { map.maxZoomLevel = it }
        set(mapProperties.minZoomPreference) { map.minZoomLevel = it }

        update(cameraPositionState) { this.cameraPositionState = it }
    }
}
