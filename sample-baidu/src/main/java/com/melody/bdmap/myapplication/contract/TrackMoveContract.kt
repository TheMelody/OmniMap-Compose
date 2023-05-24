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

package com.melody.bdmap.myapplication.contract

import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.sample.common.state.IUiEffect
import com.melody.sample.common.state.IUiEvent
import com.melody.sample.common.state.IUiState

/**
 * TrackMoveContract
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 14:18
 */
class TrackMoveContract {
    sealed class Event : IUiEvent {
        object PlayPauseEvent: Event()
        object RestartTrackMoveEvent: Event()
    }

    data class State (
        val showPauseLabel: Boolean,
        val needRestart: Boolean,
        val isMapLoaded: Boolean,
        val trackPoints: List<LatLng>,
        val bounds: LatLngBounds?,
        val timeInterval: Long,
        val uiSettings: MapUiSettings,
        val bitmapTexture: BitmapDescriptor?,
        val movingTrackMarker: BitmapDescriptor?,
        val trackMarkerPosition: LatLng,
        val trackMarkerRotate: Float
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal class Toast(val msg: String): Effect()
    }
}