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

package com.melody.map.tencent_compose.render

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.Cluster
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterItem
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterManager
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.view.DefaultClusterRenderer

/**
 * CustomClusterRenderer
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/01 15:34
 */
internal class CustomClusterRenderer(
    context: Context?,
    private val clusterColor: Color?,
    private val clusterItemIcon: BitmapDescriptor?,
    tencentMap: TencentMap?,
    clusterManager: ClusterManager<ClusterItem>?
) : DefaultClusterRenderer<ClusterItem>(context, tencentMap, clusterManager) {

    override fun getColor(value: Int): Int {
        // 修改聚合点的圆颜色
        return clusterColor?.toArgb()?:super.getColor(value)
    }

    override fun onBeforeClusterItemRendered(p0: ClusterItem?, p1: MarkerOptions?) {
        super.onBeforeClusterItemRendered(p0, p1?.apply {
            // 修改单个聚合点Marker的图标
            clusterItemIcon?.let { this.icon(it) }
        })
    }
}