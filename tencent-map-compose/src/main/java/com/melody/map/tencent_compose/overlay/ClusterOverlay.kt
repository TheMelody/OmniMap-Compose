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

package com.melody.map.tencent_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.melody.map.tencent_compose.MapApplier
import com.melody.map.tencent_compose.MapNode
import com.melody.map.tencent_compose.adapter.ClusterInfoWindowAdapter
import com.melody.map.tencent_compose.model.TXClusterItem
import com.melody.map.tencent_compose.render.CustomClusterRenderer
import com.melody.map.tencent_compose.model.TXMapComposable
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.Cluster
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterManager
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.view.DefaultClusterRenderer

internal class ClusterOverlayNode(
    val defaultClusterRenderer: DefaultClusterRenderer<TXClusterItem>?,
    val clusterManager: ClusterManager<TXClusterItem>
) : MapNode {
    override fun onRemoved() {
        clusterManager.cancel()
    }
}

/**
 * 点聚合效果ClusterOverlay
 * @param minClusterSize 设置最小聚合数量，默认为1，即有1个以上不包括1个marker才会聚合
 * @param maxDistanceAtZoom 设置点聚合生效距离，以dp为单位，默认为35dp
 * @param buckets 定义聚合的分段，默认配置：当超过5个不足10个的时候，显示5+，其他分段同理
 * @param clusterItems 全部的聚合点
 * @param clusterColor 聚合点圆的颜色，圆圈边缘半透明浅白色无法修改
 * @param onClusterItemClick 设置在点击【单个聚合点Marker】项时调用的回调
 * @param onClusterItemInfoWindow 传NULL，点击的【单个聚合点Marker】不会弹出来InfoWindow
 */
@Composable
@TXMapComposable
fun ClusterOverlay(
    minClusterSize: Int = 1,
    maxDistanceAtZoom: Dp = 35.dp,
    buckets: IntArray = intArrayOf(5, 10, 20, 50),
    clusterItems: List<TXClusterItem>,
    clusterColor: Color? = null,
    onClusterItemClick: (TXClusterItem?) -> Boolean = { false },
    onClustersClick: (Cluster<TXClusterItem>?) -> Boolean = { false },
    onClusterItemInfoWindow: (@Composable (TXClusterItem) -> Unit)? = null,
    /*onClustersInfoWindow: (@Composable (Cluster<ClusterItem>?) -> Unit)? = null*/
) {
    val currentOnClustersClick by rememberUpdatedState(newValue = onClustersClick)
    val currentOnClusterItemClick by rememberUpdatedState(newValue = onClusterItemClick)
    val mapApplier = currentComposer.applier as? MapApplier
    val context = LocalContext.current
    val parentContext = rememberCompositionContext()
    ComposeNode<ClusterOverlayNode, MapApplier>(
        factory = {
            val tMap = mapApplier?.map?: error("Error adding ClusterOverlay")
            val clusterManager = ClusterManager<TXClusterItem>(context,tMap)
            val renderer = CustomClusterRenderer(
                context = context,
                tencentMap = tMap,
                clusterColor = clusterColor,
                clusterManager = clusterManager
            )
            // 设置最小聚合数量
            renderer.minClusterSize = minClusterSize
            // 定义聚合的分段
            renderer.buckets = buckets
            // 设置聚合渲染器
            clusterManager.renderer = renderer
            val algorithm = NonHierarchicalDistanceBasedAlgorithm<TXClusterItem>(context)
            // 设置点聚合生效距离，以dp为单位
            algorithm.maxDistanceAtZoom = maxDistanceAtZoom.value.toInt()
            // 设置策略
            clusterManager.algorithm = algorithm
            // 多聚合点,点击事件回调
            clusterManager.setOnClusterClickListener { cluster ->
                currentOnClustersClick.invoke(cluster)
            }
            // 单个聚合点Marker点击的事件回调
            clusterManager.setOnClusterItemClickListener { item ->
                currentOnClusterItemClick.invoke(item)
            }
            // 暂不提供，有这种产品需求的App？保持和其他地图对外提供类似的调用
            /*onClustersInfoWindow?.let {
                // 多聚合点弹出来的InfoWindow
                clusterManager.setClusterInfoWindowAdapter(
                    ClusterInfoWindowAdapter(
                        infoWindowView = mapApplier.mClusterInfoWindowView,
                        compositionContext = parentContext,
                        clusterInfoWindow = onClustersInfoWindow
                    )
                )
            }*/
            onClusterItemInfoWindow?.let {
                // 单个聚合点Marker弹出来的InfoWindow
                clusterManager.setClusterItemInfoWindowAdapter(
                    ClusterInfoWindowAdapter(
                        infoWindowView = mapApplier.mClusterInfoWindowView,
                        compositionContext = parentContext,
                        itemInfoWindow = onClusterItemInfoWindow
                    )
                )
            }
            // 添加聚合
            tMap.setOnCameraChangeListener(clusterManager)
            // 设置单个聚合点Marker的点击事件
            tMap.setOnMarkerClickListener(clusterManager)

            if(onClusterItemInfoWindow != null /*|| onClustersInfoWindow != null*/) {
                // 弹出的InfoWindow的点击事件
                tMap.setOnInfoWindowClickListener(clusterManager)
                // clusterManager托管InfoWindow
                tMap.setInfoWindowAdapter(clusterManager)
            }

            ClusterOverlayNode(renderer, clusterManager)
        },
        update = {
            set(minClusterSize) {
                this.defaultClusterRenderer?.minClusterSize = it
            }
            set(clusterItems) {
                this.clusterManager.clearItems()
                this.clusterManager.addItems(it)
            }
        }
    )
}