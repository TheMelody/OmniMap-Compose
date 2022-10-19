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

package com.melody.map.myapplication.viewmodel

import com.melody.map.myapplication.contract.SmoothMoveContract
import com.melody.map.myapplication.repo.SmoothMoveRepository
import com.melody.sample.common.base.BaseViewModel

/**
 * SmoothMoveViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 14:18
 */
class SmoothMoveViewModel : BaseViewModel<SmoothMoveContract.Event,SmoothMoveContract.State,SmoothMoveContract.Effect>() {
    override fun createInitialState(): SmoothMoveContract.State {
        return SmoothMoveContract.State(
            isStart = false,
            isMapLoaded = false,
            bounds = null,
            trackPoints = null,
            bitmapTexture = null,
            movingTrackMarker = null,
            totalDuration = 40,
            uiSettings = SmoothMoveRepository.initMapUiSettings()
        )
    }

    override fun handleEvents(event: SmoothMoveContract.Event) {
        when(event) {
            is SmoothMoveContract.Event.PlayPauseEvent -> {
                setState { copy(isStart = !isStart) }
            }
        }
    }

    init {
        asyncLaunch {
            val points = SmoothMoveRepository.readLatLngList()
            val bounds = SmoothMoveRepository.calcLatLngBounds(points)
            val bitmapTexture = SmoothMoveRepository.getPolylineTextureBitmap()
            val movingTrackMarker = SmoothMoveRepository.getPointOverLayMarker()
            setState {
                copy(
                    trackPoints = points,
                    bounds = bounds,
                    bitmapTexture = bitmapTexture,
                    movingTrackMarker = movingTrackMarker
                )
            }
        }
    }

    fun handleMapLoaded() {
        setState { copy(isMapLoaded = true) }
    }

    fun toggle() {
        setEvent(SmoothMoveContract.Event.PlayPauseEvent)
    }

    fun pointOverLayClick() {
        setEffect { SmoothMoveContract.Effect.Toast("可以做一些事情，比如：弹一个Dialog显示车的....某些信息，你觉得呢？") }
    }
}