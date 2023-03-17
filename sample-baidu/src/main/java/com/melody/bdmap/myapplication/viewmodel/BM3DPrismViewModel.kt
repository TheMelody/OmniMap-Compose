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
import com.melody.bdmap.myapplication.contract.BM3DPrismContract
import com.melody.bdmap.myapplication.repo.BM3DPrismRepository
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * BM3DPrismViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 16:51
 */
class BM3DPrismViewModel:BaseViewModel<BM3DPrismContract.Event,BM3DPrismContract.State,BM3DPrismContract.Effect>() {
    override fun createInitialState(): BM3DPrismContract.State {
        return BM3DPrismContract.State(
            mapUiSettings = BM3DPrismRepository.initMapUiSettings(),
            mapProperties = BM3DPrismRepository.initMapProperties(),
            searchLatLng = LatLng(23.008468, 113.72953),
            bM3DPrisms = null
        )
    }

    override fun handleEvents(event: BM3DPrismContract.Event) {
    }

    init {
        asyncLaunch(Dispatchers.IO) {
            val result = kotlin.runCatching {
                BM3DPrismRepository.searchBuilding(currentState.searchLatLng)
            }
            if(result.isFailure) {
                setEffect { BM3DPrismContract.Effect.Toast(result.exceptionOrNull()?.message) }
            }
            setState { copy(bM3DPrisms = result.getOrNull()) }
        }
    }
}