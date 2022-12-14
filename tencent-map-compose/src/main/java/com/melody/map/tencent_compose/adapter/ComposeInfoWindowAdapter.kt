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
import android.view.ViewGroup
import android.widget.Space
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import com.melody.map.tencent_compose.overlay.MarkerNode
import com.tencent.tencentmap.mapsdk.maps.MapView
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.Marker

internal class ComposeInfoWindowAdapter(
    private val mapView: MapView,
    private val markerNodeFinder: (Marker) -> MarkerNode?
) : TencentMap.InfoWindowAdapter {

    private val infoWindowView: ComposeView
        get() = ComposeView(mapView.context).apply {
            mapView.addView(
                this,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

    override fun getInfoContents(marker: Marker): View? {
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoContent  = markerNode.infoContent
        if (infoContent == null) { // 这里返回null，是处理getInfoWindow这2个方法谁返回视图的并显示冲突的问题
            return null
        }
        return infoWindowView.applyAndRemove(false, markerNode.compositionContext) {
            infoContent(marker)
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoWindow  = markerNode.infoWindow
        if (infoWindow == null) { // 这里返回null，是处理getInfoContents这2个方法谁返回视图的并显示冲突的问题
            return null
        }
        return infoWindowView.applyAndRemove(true, markerNode.compositionContext) {
            infoWindow(marker)
        }
    }

    private fun ComposeView.applyAndRemove(
        fromInfoWindow: Boolean,
        parentContext: CompositionContext,
        content: @Composable () -> Unit
    ): ComposeView {
        val result = this.apply {
            setParentCompositionContext(parentContext)
            setContent {
                if(fromInfoWindow) {
                    // 去除地图默认气泡背景, 如果是InfoContent，则只定制内容，不修改窗口背景和样式
                    setBackgroundColor(Color.TRANSPARENT)
                }
                content.invoke()
            }
        }
        (this.parent as? MapView)?.removeView(this)
        return result
    }
}