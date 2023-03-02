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

package com.melody.map.tencent_compose.adapter

import android.graphics.Color
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import com.tencent.tencentmap.mapsdk.maps.MapView
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.Cluster
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterItem
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterManager

/**
 * 多个聚合点以及单个聚合点Marker，被点击的时候，弹出来的InfoWindow
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/01 16:48
 */
internal class ClusterInfoWindowAdapter<T : ClusterItem>(
    private val infoWindowView: ComposeView,
    private val compositionContext: CompositionContext,
    private val clusterInfoWindow: @Composable (Cluster<T>?) -> Unit = {},
    private val itemInfoWindow: @Composable (T) -> Unit = {}
) : ClusterManager.ClusterInfoWindowAdapter<T>, ClusterManager.ClusterItemInfoWindowAdapter<T> {

    private fun ComposeView.applyAndRemove(
        parentContext: CompositionContext,
        content: @Composable () -> Unit
    ): ComposeView {
        val result = this.apply {
            setParentCompositionContext(parentContext)
            setContent {
                // 去除地图默认气泡背景, 如果是InfoContent，则只定制内容，不修改窗口背景和样式
                setBackgroundColor(Color.TRANSPARENT)
                content.invoke()
            }
        }
        (this.parent as? MapView)?.removeView(this)
        return result
    }

    override fun getInfoContents(cluster: Cluster<T>?): View? {
        return null
    }

    override fun getInfoWindow(cluster: Cluster<T>?): View {
        return infoWindowView.applyAndRemove(compositionContext) {
            clusterInfoWindow(cluster)
        }
    }

    override fun getInfoWindowPressState(cluster: Cluster<T>?): View? {
        return null
    }

    override fun getInfoContents(t: T): View? {
        return null
    }

    override fun getInfoWindow(t: T): View {
        return infoWindowView.applyAndRemove(compositionContext) {
            itemInfoWindow(t)
        }
    }

    override fun getInfoWindowPressState(t: T): View? {
        return null
    }
}