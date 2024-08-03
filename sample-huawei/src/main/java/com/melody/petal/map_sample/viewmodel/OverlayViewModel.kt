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

package com.melody.petal.map_sample.viewmodel

import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.LatLngBounds
import com.melody.petal.map_sample.contract.OverlayContract
import com.melody.petal.map_sample.repo.OverlayRepository
import com.melody.sample.common.base.BaseViewModel

/**
 * OverlayViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/08/27 16:02
 */
class OverlayViewModel : BaseViewModel<OverlayContract.Event,OverlayContract.State,OverlayContract.Effect>() {

    override fun createInitialState(): OverlayContract.State {
        return OverlayContract.State(
            isShowWFJGroupOverlay = false,
            isShowTileOverlay = false,
            circleCenter = LatLng(39.903787, 116.426095),
            mapCenter = LatLng(39.91, 116.40),
            wfjCenter = LatLng(39.936713,116.386475),
            wfjLatLngBounds = LatLngBounds(LatLng(39.935029, 116.384377),LatLng(39.939577, 116.388331)),
            arcStartPoint = LatLng(39.80, 116.09),
            arcPassPoint = LatLng(39.77, 116.28),
            arcEndPoint = LatLng(39.78, 116.46),
            infoWindowLatLng = LatLng(39.93, 116.13),
            polylineList = listOf(LatLng(39.92, 116.34),LatLng(39.93, 116.34),LatLng(39.92, 116.35)),
            polygonTriangleList = listOf(LatLng(39.88, 116.41), LatLng(39.87, 116.49), LatLng(39.82, 116.38)),
            polygonCornerLatLng = LatLng(39.982347, 116.305966),
            polylineAnimPointList = OverlayRepository.initAnimPolylinePointList(),
            polylineRainbow = OverlayRepository.initPolylineRainbow(),
            polygonHolePointList = OverlayRepository.initPolygonHolePointList(),
            polygonPatterns = OverlayRepository.initPolygonPatterns()
        )
    }

    override fun handleEvents(event: OverlayContract.Event) {
        when(event) {
            is OverlayContract.Event.ShowWFJGroupOverlayEvent -> {
                setState { copy(isShowWFJGroupOverlay = true) }
            }
            is OverlayContract.Event.HideWFJGroupOverlayEvent -> {
                setState { copy(isShowWFJGroupOverlay = false) }
            }
            is OverlayContract.Event.ShowTileOverlayEvent -> {
                setState { copy(isShowTileOverlay = true) }
            }
            is OverlayContract.Event.HideTileOverlayEvent -> {
                setState { copy(isShowTileOverlay = false) }
            }
        }
    }

    fun toggleWFJGroupOverlay() {
        if(currentState.isShowWFJGroupOverlay){
            setEvent(OverlayContract.Event.HideWFJGroupOverlayEvent)
        } else {
            setEvent(OverlayContract.Event.ShowWFJGroupOverlayEvent)
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