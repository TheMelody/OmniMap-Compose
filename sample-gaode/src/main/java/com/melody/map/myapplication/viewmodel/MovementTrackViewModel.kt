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

import com.melody.map.myapplication.contract.MovementTrackContract
import com.melody.map.myapplication.repo.MovementTrackRepository
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * MovementTrackViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/22 15:03
 */
class MovementTrackViewModel :
    BaseViewModel<MovementTrackContract.Event, MovementTrackContract.State, MovementTrackContract.Effect>() {

    override fun createInitialState(): MovementTrackContract.State {
        return MovementTrackContract.State(
            isLoading = false,
            latLngList = emptyList(),
            trackLatLng = null,
            uiSettings = MovementTrackRepository.initMapUiSettings()
        )
    }

    override fun handleEvents(event: MovementTrackContract.Event) {
    }

    fun loadMovementTrackData() = asyncLaunch(Dispatchers.IO) {
        setState { copy(isLoading = true) }
        val list = MovementTrackRepository.parseLocationsData("MovementTrackTrace.csv")
        val trackLatLng = MovementTrackRepository.getTrackLatLngBounds(list)
        setState { copy(isLoading = false, latLngList = list, trackLatLng = trackLatLng) }
    }
}