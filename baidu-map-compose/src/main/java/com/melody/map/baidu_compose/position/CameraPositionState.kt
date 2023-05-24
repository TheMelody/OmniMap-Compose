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

package com.melody.map.baidu_compose.position

import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdate
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Projection
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.model.BDCameraPosition

/**
 * 控制和观察地图的相机状态
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/08 10:10
 */
@Composable
inline fun rememberCameraPositionState(
    key: String? = null,
    crossinline init: CameraPositionState.() -> Unit = {}
): CameraPositionState = rememberSaveable(key = key, saver = CameraPositionState.Saver) {
    CameraPositionState().apply(init)
}

/**
 * 控制和观察地图的相机状态
 */
class CameraPositionState(position: BDCameraPosition = BDCameraPosition(LatLng(39.91, 116.40), 11f, 0f, 0f)) {

    /**
     * Whether the camera is currently moving or not. This includes any kind of movement:
     * panning, zooming, or rotation.
     */
    var isMoving: Boolean by mutableStateOf(false)
        internal set

    /**
     * Returns the current [Projection] to be used for converting between screen
     * coordinates and lat/lng.
     */
    val projection: Projection?
        get() = map?.projection

    val projectionMatrix: FloatArray?
        get() = map?.projectionMatrix

    val viewMatrix: FloatArray?
        get() = map?.viewMatrix

    /**
     * Local source of truth for the current camera position.
     * While [map] is non-null this reflects the current position of [map] as it changes.
     * While [map] is null it reflects the last known map position, or the last value set by
     * explicitly setting [position].
     */
    internal var rawPosition by mutableStateOf(position)

    var position: BDCameraPosition
        get() = rawPosition
        set(value) {
            synchronized(lock) {
                val map = map
                if (map == null) {
                    rawPosition = value
                } else {
                    map.animateMapStatus(
                        MapStatusUpdateFactory.newMapStatus(BDCameraPosition.toMapStatus(value))
                    )
                }
            }
        }

    private var map: BaiduMap? = null

    private val lock = Any()

    internal fun setMap(map: BaiduMap?) {
        synchronized(lock) {
            if (this.map == null && map == null) return
            if (this.map != null && map != null) {
                error("CameraPositionState may only be associated with one BaiduMap at a time")
            }
            this.map = map
            if (map == null) {
                isMoving = false
            } else {
                map.animateMapStatus(
                    MapStatusUpdateFactory.newMapStatus(BDCameraPosition.toMapStatus(position))
                )
            }
        }
    }

    @UiThread
    suspend fun animate(update: MapStatusUpdate, durationMs: Int = Integer.MAX_VALUE) {
        synchronized(lock) {
            if (durationMs == Integer.MAX_VALUE) {
                map?.animateMapStatus(update)
            } else {
                map?.animateMapStatus(update, durationMs)
            }
        }
    }

    @UiThread
    fun move(update: MapStatusUpdate) {
        synchronized(lock) {
            map?.animateMapStatus(update)
        }
    }

    companion object {
        val Saver: Saver<CameraPositionState, BDCameraPosition> = Saver(
            save = { it.position },
            restore = { CameraPositionState(it) }
        )
    }
}