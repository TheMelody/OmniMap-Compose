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

package com.melody.bdmap.myapplication.viewmodel

import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.melody.bdmap.myapplication.contract.OverlayContract
import com.melody.bdmap.myapplication.repo.OverlayRepository
import com.melody.sample.common.base.BaseViewModel

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
            isShowWFJGroupOverlay = false,
            isShowTileOverlay = false,
            lvbuCenter = LatLng(39.903787, 116.426095),
            mapCenter = LatLng(39.91, 116.40),
            wfjCenter = LatLng(39.936713,116.386475),
            wfjLatLngBounds = LatLngBounds.Builder().include(LatLng(39.935029, 116.384377))
                .include(LatLng(39.939577, 116.388331)).build(),
            arcStartPoint = LatLng(39.80, 116.09),
            arcPassPoint = LatLng(39.77, 116.28),
            arcEndPoint = LatLng(39.78, 116.46),
            textOverlayPos = LatLng(39.86923, 116.397428),
            circleGradientPos = LatLng(39.833424, 116.377823),
            holeCirclePos = LatLng(39.97923, 116.357428),
            infoWindowLatLng = LatLng(39.93, 116.13),
            polylineList = listOf(LatLng(39.865, 116.444),LatLng(39.825, 116.494),LatLng(39.855, 116.534),LatLng(39.805, 116.594)),
            polylineColorList = listOf(-0x55010000, -0x55ff0100, -0x55ffff01, -0x55ff0100),
            polygonTriangleList = listOf(LatLng(39.88, 116.41), LatLng(39.87, 116.49), LatLng(39.82, 116.38)),
            polygonCornerLatLng = LatLng(39.982347, 116.305966),
            polygonPointList = OverlayRepository.initPolygonPointList(),
            polygonHoleOption = OverlayRepository.initPolygonHoleOption(),
            circleHoleOptions = OverlayRepository.initCircleHoleOptions()
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