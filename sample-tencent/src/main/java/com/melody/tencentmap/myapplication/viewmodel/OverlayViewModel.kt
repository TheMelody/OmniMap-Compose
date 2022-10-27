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

package com.melody.tencentmap.myapplication.viewmodel

import com.melody.sample.common.base.BaseViewModel
import com.melody.tencentmap.myapplication.contract.OverlayContract
import com.melody.tencentmap.myapplication.repo.OverlayRepository
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds

/**
 * OverlayViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/27 14:58
 */
class OverlayViewModel : BaseViewModel<OverlayContract.Event,OverlayContract.State,OverlayContract.Effect>() {

    override fun createInitialState(): OverlayContract.State {
        return OverlayContract.State(
            isShowTXDSGroupOverlay = false,
            isShowTileOverlay = false,
            circleCenter = LatLng(39.903787, 116.426095),
            mapCenter = LatLng(39.91, 116.40),
            tXDSLatLngBounds = LatLngBounds(LatLng(40.045226, 116.280069),LatLng(40.038918, 116.271873)),
            arcStartPoint = LatLng(39.80, 116.09),
            arcPassPoint = LatLng(39.77, 116.28),
            arcEndPoint = LatLng(39.78, 116.46),
            infoWindowLatLng = LatLng(39.93, 116.13),
            polylineList = listOf(LatLng(39.92, 116.34),LatLng(39.93, 116.34),LatLng(39.92, 116.35)),
            polygonTriangleList = listOf(LatLng(39.88, 116.41), LatLng(39.87, 116.49), LatLng(39.82, 116.38)),
            polygonCornerLatLng = LatLng(39.982347, 116.305966),
            polygonPointList = OverlayRepository.initPolygonPointList(),
            patterns = listOf(10, 10)
        )
    }

    override fun handleEvents(event: OverlayContract.Event) {
        when(event) {
            is OverlayContract.Event.ShowTXDSGroupOverlayEvent -> {
                setState { copy(isShowTXDSGroupOverlay = true) }
            }
            is OverlayContract.Event.HideTXDSGroupOverlayEvent -> {
                setState { copy(isShowTXDSGroupOverlay = false) }
            }
            is OverlayContract.Event.ShowTileOverlayEvent -> {
                setState { copy(isShowTileOverlay = true) }
            }
            is OverlayContract.Event.HideTileOverlayEvent -> {
                setState { copy(isShowTileOverlay = false) }
            }
        }
    }

    fun toggleGroupOverlay() {
        if(currentState.isShowTXDSGroupOverlay){
            setEvent(OverlayContract.Event.HideTXDSGroupOverlayEvent)
        } else {
            setEvent(OverlayContract.Event.ShowTXDSGroupOverlayEvent)
        }
    }

    fun toggleTileOverlay() {
        if(currentState.isShowTileOverlay){
            setEvent(OverlayContract.Event.HideTileOverlayEvent)
        } else {
            setEvent(OverlayContract.Event.ShowTileOverlayEvent)
        }
    }
}