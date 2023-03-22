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

package com.melody.tencentmap.myapplication.viewmodel

import com.melody.sample.common.base.BaseViewModel
import com.melody.tencentmap.myapplication.contract.MarkerAnimationContract
import com.melody.tencentmap.myapplication.repo.MarkerAnimationRepository
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

/**
 * MarkerAnimationViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/28 16:31
 */
class MarkerAnimationViewModel :
    BaseViewModel<MarkerAnimationContract.Event, MarkerAnimationContract.State, MarkerAnimationContract.Effect>() {

    override fun createInitialState(): MarkerAnimationContract.State {
        return MarkerAnimationContract.State(
            mapLoaded = false,
            mapUiSettings = MarkerAnimationRepository.initMapUiSettings(),
            markerDefaultLocation = LatLng(39.984108,116.307557),
            markerAnimation = null
        )
    }

    override fun handleEvents(event: MarkerAnimationContract.Event) {
        if(event is MarkerAnimationContract.Event.StartMarkerAnimation) {
            val animation = MarkerAnimationRepository.prepareMarkerAnimation(
                LatLng(
                    currentState.markerDefaultLocation.latitude + 0.05,
                    currentState.markerDefaultLocation.longitude - 0.05
                ),
                onAnimationEnd = {
                    setEvent(MarkerAnimationContract.Event.FinishMarkerAnimation)
                }
            )
            setState {
                copy(markerAnimation =  animation)
            }
        } else if(event is MarkerAnimationContract.Event.FinishMarkerAnimation) {
            setState {
                copy(
                    markerAnimation = null,
                    markerDefaultLocation = LatLng(
                        markerDefaultLocation.latitude + 0.05,
                        markerDefaultLocation.longitude - 0.05
                    )
                )
            }
        }
    }

    fun startMarkerAnimation() {
        setEvent(MarkerAnimationContract.Event.StartMarkerAnimation)
    }

    fun handleMapLoaded() {
        setState { copy(mapLoaded = true) }
    }

}