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

package com.melody.tencentmap.myapplication.contract

import com.melody.sample.common.state.IUiEffect
import com.melody.sample.common.state.IUiEvent
import com.melody.sample.common.state.IUiState
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds

/**
 * OverlayContract
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/27 15:00
 */
class OverlayContract {
    sealed class Event : IUiEvent {
        object ShowTXDSGroupOverlayEvent : Event()
        object HideTXDSGroupOverlayEvent : Event()
        object ShowTileOverlayEvent : Event()
        object HideTileOverlayEvent : Event()
    }

    data class State(
        val isShowTXDSGroupOverlay: Boolean,
        val isShowTileOverlay: Boolean,
        val circleCenter: LatLng,
        val mapCenter: LatLng,
        val tXDSLatLngBounds: LatLngBounds,
        val arcStartPoint: LatLng,
        val arcPassPoint: LatLng,
        val arcEndPoint: LatLng,
        val infoWindowLatLng: LatLng,
        val polygonCornerLatLng: LatLng,
        val polylineList: List<LatLng>,
        val polygonTriangleList: List<LatLng>,
        val polygonPointList: List<LatLng>,
        val patterns: List<Int>,
    ) : IUiState

    sealed class Effect : IUiEffect
}