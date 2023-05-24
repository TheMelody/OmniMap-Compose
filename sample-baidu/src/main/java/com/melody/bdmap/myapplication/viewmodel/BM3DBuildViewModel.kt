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

package com.melody.bdmap.myapplication.viewmodel

import com.baidu.mapapi.model.LatLng
import com.melody.bdmap.myapplication.contract.BM3DBuildContract
import com.melody.bdmap.myapplication.repo.BM3DBuildRepository
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * BM3DBuildViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/04/24 16:45
 */
class BM3DBuildViewModel:BaseViewModel<BM3DBuildContract.Event,BM3DBuildContract.State,BM3DBuildContract.Effect>() {
    override fun createInitialState(): BM3DBuildContract.State {
        return BM3DBuildContract.State(
            mapUiSettings = BM3DBuildRepository.initMapUiSettings(),
            mapProperties = BM3DBuildRepository.initMapProperties(),
            searchLatLng = LatLng(23.008468, 113.72953),
            bM3DBuilds = null
        )
    }

    override fun handleEvents(event: BM3DBuildContract.Event) {
    }

    init {
        asyncLaunch(Dispatchers.IO) {
            val result = kotlin.runCatching {
                BM3DBuildRepository.searchBuilding(currentState.searchLatLng)
            }
            if(result.isFailure) {
                setEffect { BM3DBuildContract.Effect.Toast(result.exceptionOrNull()?.message) }
            }
            setState { copy(bM3DBuilds = result.getOrNull()) }
        }
    }
}