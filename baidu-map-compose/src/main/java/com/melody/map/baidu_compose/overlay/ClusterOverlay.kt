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

package com.melody.map.baidu_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable
import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem
import com.melody.map.baidu_compose.utils.clustering.ClusterManager
import com.melody.map.baidu_compose.utils.clustering.view.DefaultClusterRenderer

internal class ClusterOverlayNode(
    val compositionContext: CompositionContext,
    val clusterRenderer: DefaultClusterRenderer<ClusterItem>?,
    val clusterManager: ClusterManager<ClusterItem>,
    val onClusterItemInfoWindow: (@Composable (ClusterItem) -> Unit)?
) : MapNode {
    override fun onRemoved() {
        clusterManager.clearItems()
    }
}

/**
 * 点聚合效果ClusterOverlay
 * @param minClusterSize 设置最小聚合数量，默认为1，即有1个以上不包括1个marker才会聚合
 * @param clusterColor 聚合点圆的颜色
 * @param maxDistanceAtZoom 设置点聚合生效距离，以dp为单位，默认为35dp
 * @param clusterItems 全部的聚合点
 * @param buckets 定义聚合的分段，默认配置：当超过5个不足10个的时候，显示5+，其他分段同理
 * @param onClusterItemClick 点击【单个聚合点Marker】项时调用的回调
 * @param onClustersClick 点击【聚合点】时调用的回调
 * @param onClusterItemInfoWindow 传NULL，点击的【单个聚合点Marker】不会弹出来InfoWindow
 */
@Composable
@BDMapComposable
fun ClusterOverlay(
    minClusterSize: Int = 1,
    clusterColor: Color? = null,
    maxDistanceAtZoom: Dp = 35.dp,
    clusterItems: List<ClusterItem>,
    buckets: IntArray = intArrayOf(5, 10, 20, 50),
    onClusterItemClick: (ClusterItem?) -> Boolean = { false },
    onClustersClick: (Cluster<ClusterItem?>?) -> Boolean = { false },
    onClusterItemInfoWindow: (@Composable (ClusterItem) -> Unit)? = null
) {
    val currentOnClustersClick by rememberUpdatedState(newValue = onClustersClick)
    val currentOnClusterItemClick by rememberUpdatedState(newValue = onClusterItemClick)
    val mapApplier = currentComposer.applier as? MapApplier
    val context = LocalContext.current
    val compositionContext = rememberCompositionContext()
    ComposeNode<ClusterOverlayNode, MapApplier>(
        factory = {
            val map = mapApplier?.map?: error("Error adding ClusterOverlay")
            val clusterManager = ClusterManager<ClusterItem>(context,mapApplier)
            val renderer = DefaultClusterRenderer(
                mapApplier = mapApplier,
                context = context,
                clusterManager = clusterManager
            )
            val clusterOverlayNode = ClusterOverlayNode(
                clusterRenderer = renderer,
                clusterManager = clusterManager,
                compositionContext = compositionContext,
                onClusterItemInfoWindow = onClusterItemInfoWindow
            )
            // 设置聚合点圆颜色
            renderer.setClusterColor(clusterColor?.toArgb() ?: -1)
            // 设置最小聚合数量
            renderer.setMinClusterSize(minClusterSize)
            // 定义聚合的分段
            renderer.setBuckets(buckets)
            renderer.setClusterOverlayNode(clusterOverlayNode)
            // 设置聚合渲染器
            clusterManager.setRenderer(renderer)
            // 设置点聚合生效距离，以dp为单位
            clusterManager.setMaxDistanceAtZoom(maxDistanceAtZoom.value)
            // 多聚合点,点击事件回调
            clusterManager.setOnClusterClickListener(object :ClusterManager.OnClusterClickListener<ClusterItem?>{
                override fun onClusterClick(cluster: Cluster<ClusterItem?>?): Boolean {
                    return currentOnClustersClick.invoke(cluster)
                }
            })
            // 单个聚合点Marker点击的事件回调
            clusterManager.setOnClusterItemClickListener(object :ClusterManager.OnClusterItemClickListener<ClusterItem?>{
                override fun onClusterItemClick(item: ClusterItem?): Boolean {
                    return currentOnClusterItemClick.invoke(item)
                }
            })
            // 设置地图监听，当地图状态发生改变时，进行点聚合运算
            map.setOnMapStatusChangeListener(clusterManager)
            // 设置单个聚合点Marker的点击事件
            map.setOnMarkerClickListener(clusterManager)
            clusterOverlayNode
        },
        update = {
            set(minClusterSize) {
                this.clusterRenderer?.setMinClusterSize(it)
            }
            set(clusterColor) {
                this.clusterRenderer?.setClusterColor(clusterColor?.toArgb() ?: -1)
            }
            set(clusterItems) {
                this.clusterManager.clearItems()
                this.clusterManager.addItems(it)
            }
        }
    )
}