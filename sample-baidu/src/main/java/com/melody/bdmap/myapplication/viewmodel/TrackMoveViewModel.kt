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
import com.melody.bdmap.myapplication.contract.TrackMoveContract
import com.melody.bdmap.myapplication.repo.SmoothMoveRepository
import com.melody.map.baidu_compose.utils.BDTrackMoveUtils
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * TrackMoveViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 14:18
 */
class TrackMoveViewModel : BaseViewModel<TrackMoveContract.Event,TrackMoveContract.State,TrackMoveContract.Effect>() {
    private val bdTrackMoveUtils = BDTrackMoveUtils()

    override fun createInitialState(): TrackMoveContract.State {
        return TrackMoveContract.State(
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

    override fun handleEvents(event: TrackMoveContract.Event) {
        when(event) {
            is TrackMoveContract.Event.PlayPauseEvent -> {
                if(currentState.showPauseLabel) {
                    bdTrackMoveUtils.pauseMove()
                } else {
                    bdTrackMoveUtils.startMove()
                    if(currentState.needRestart) {
                        setEvent(TrackMoveContract.Event.RestartTrackMoveEvent)
                    }
                }
                setState { copy(showPauseLabel = !showPauseLabel) }
            }
            is TrackMoveContract.Event.RestartTrackMoveEvent -> {
                asyncLaunch(Dispatchers.Default) {
                    bdTrackMoveUtils.restart(
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
                    trackMarkerRotate = bdTrackMoveUtils.getAngle(0, points),
                    movingTrackMarker = movingTrackMarker
                )
            }
        }
    }

    fun handleMapLoaded() {
        setState { copy(isMapLoaded = true) }
    }

    fun toggle() {
        setEvent(TrackMoveContract.Event.PlayPauseEvent)
    }
}