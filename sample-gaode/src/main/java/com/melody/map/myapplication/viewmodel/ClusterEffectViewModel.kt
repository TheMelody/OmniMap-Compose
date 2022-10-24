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

import android.graphics.drawable.Drawable
import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.model.ClusterItem
import com.melody.map.gd_compose.model.ClusterRender
import com.melody.map.myapplication.contract.ClusterEffectContract
import com.melody.map.myapplication.repo.ClusterEffectRepository
import com.melody.sample.common.base.BaseViewModel

/**
 * ClusterEffectViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/24 11:34
 */
class ClusterEffectViewModel :
    BaseViewModel<ClusterEffectContract.Event, ClusterEffectContract.State, ClusterEffectContract.Effect>(),
    ClusterRender {

    private val mBackDrawables: HashMap<Int, Drawable> = HashMap()

    override fun createInitialState(): ClusterEffectContract.State {
        return ClusterEffectContract.State(
            isLoading = true,
            uiSettings = ClusterEffectRepository.initMapUiSettings(),
            clusterItem = emptyList(),
            clusterBounds = null,
            defaultClusterIcon = ClusterEffectRepository.getDefaultClusterIcon()
        )
    }

    override fun handleEvents(event: ClusterEffectContract.Event) {
        when(event) {
            is ClusterEffectContract.Event.ClusterItemClick -> {
                val bounds = ClusterEffectRepository.getClusterItemClickLatLngBounds(event.list)
                setState { copy(clusterBounds = bounds) }
            }
        }
    }

    init {
        val list = ClusterEffectRepository.initClusterDataList(LatLng(39.474923, 116.027116))
        setState { copy(isLoading = false, clusterItem = list) }
    }

    fun handleClusterClick(list: List<ClusterItem>) {
        setEvent(ClusterEffectContract.Event.ClusterItemClick(list))
    }

    override fun getDrawable(clusterNum: Int): Drawable {
        return ClusterEffectRepository.getClusterRenderDrawable(clusterNum,mBackDrawables) { index,drawable->
            mBackDrawables[index] = drawable
        }
    }
}