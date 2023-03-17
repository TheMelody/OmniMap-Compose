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
import com.melody.bdmap.myapplication.contract.SmoothMoveContract
import com.melody.bdmap.myapplication.repo.SmoothMoveRepository
import com.melody.map.baidu_compose.utils.BDSmoothMoveUtils
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * SmoothMoveViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 14:18
 */
class SmoothMoveViewModel : BaseViewModel<SmoothMoveContract.Event,SmoothMoveContract.State,SmoothMoveContract.Effect>() {
    private val bdSmoothUtils = BDSmoothMoveUtils()

    override fun createInitialState(): SmoothMoveContract.State {
        return SmoothMoveContract.State(
            showPauseLabel = false,
            needRestart = true,
            isMapLoaded = false,
            bounds = null,
            trackPoints = emptyList(),
            bitmapTexture = null,
            movingTrackMarker = null,
            trackMarkerPosition = LatLng(0.0,0.0),
            trackMarkerRotate = 0F,
            timeInterval = 40,
            uiSettings = SmoothMoveRepository.initMapUiSettings()
        )
    }

    override fun handleEvents(event: SmoothMoveContract.Event) {
        when(event) {
            is SmoothMoveContract.Event.PlayPauseEvent -> {
                if(currentState.showPauseLabel) {
                    bdSmoothUtils.pauseSmoothMove()
                } else {
                    bdSmoothUtils.startSmoothMove()
                    if(currentState.needRestart) {
                        setEvent(SmoothMoveContract.Event.RestartSmoothMoveEvent)
                    }
                }
                setState { copy(showPauseLabel = !showPauseLabel) }
            }
            is SmoothMoveContract.Event.RestartSmoothMoveEvent -> {
                asyncLaunch(Dispatchers.IO) {
                    bdSmoothUtils.restart(
                        timeInterval = currentState.timeInterval,
                        points = currentState.trackPoints,
                        onPositionCallback = { needRestart, position ->
                            setState { copy(trackMarkerPosition = position, needRestart = needRestart) }
                            if(needRestart) {
                                setState { copy(showPauseLabel = false) }
                            }
                        },
                        onRotateCallback = {
                            setState { copy(trackMarkerRotate = it) }
                        }
                    )
                }
            }
        }
    }

    init {
        asyncLaunch(Dispatchers.IO) {
            val points = SmoothMoveRepository.readLatLngList()
            val bounds = SmoothMoveRepository.calcLatLngBounds(points)
            val bitmapTexture = SmoothMoveRepository.getPolylineTextureBitmap()
            val movingTrackMarker = SmoothMoveRepository.getPointOverLayMarker()
            setState {
                copy(
                    trackPoints = points,
                    bounds = bounds,
                    bitmapTexture = bitmapTexture,
                    trackMarkerPosition = points.getOrNull(0)?: LatLng(0.0,0.0),
                    trackMarkerRotate = bdSmoothUtils.getAngle(0, points),
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
}