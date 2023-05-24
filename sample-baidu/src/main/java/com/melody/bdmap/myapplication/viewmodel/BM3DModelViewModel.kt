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
import com.melody.bdmap.myapplication.contract.BM3DModelContract
import com.melody.bdmap.myapplication.model.BM3DDataModel
import com.melody.bdmap.myapplication.repo.BM3DModelRepository
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.utils.SDKUtils
import kotlinx.coroutines.Dispatchers

/**
 * BM3DModelViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 15:28
 */
class BM3DModelViewModel :
    BaseViewModel<BM3DModelContract.Event, BM3DModelContract.State, BM3DModelContract.Effect>() {
    override fun createInitialState(): BM3DModelContract.State {
        return BM3DModelContract.State(
            bM3DModel = null,
            mapUiSettings = BM3DModelRepository.initMapUiSettings()
        )
    }
    override fun handleEvents(event: BM3DModelContract.Event) {
    }

    init {
        asyncLaunch(Dispatchers.IO) {
            BM3DModelRepository.copyFilesAssets(
                context = SDKUtils.getApplicationContext(),
                oldPath = "model3D",
                newPath = SDKUtils.getApplicationContext().filesDir.absolutePath + "/model3D/"
            )
            setState {
                copy(
                    bM3DModel = BM3DDataModel(
                        scale = 50F,
                        modelPath = SDKUtils.getApplicationContext().filesDir.absolutePath + "/model3D",
                        modelName = "among_us",
                        position = LatLng(39.915119, 116.403963)
                    )
                )
            }
        }
    }
}