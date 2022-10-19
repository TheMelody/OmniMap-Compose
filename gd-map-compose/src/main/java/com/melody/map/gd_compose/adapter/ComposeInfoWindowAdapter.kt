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

package com.melody.map.gd_compose.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.Marker
import com.melody.map.gd_compose.overlay.MarkerNode

internal class ComposeInfoWindowAdapter(
    private val mapView: MapView,
    private val markerNodeFinder: (Marker) -> MarkerNode?
) : AMap.InfoWindowAdapter {

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
        // https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker
        // 高德地图的问题：这里必须返回一个空的View，否则getInfoContents回调的Marker是一个SDK默认的：空值Marker
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoContent  = markerNode.infoContent
        if (infoContent == null) { // 这里返回null，是处理getInfoWindow这2个方法谁返回视图的并显示冲突的问题
            return null
        }
        return infoWindowView.applyAndRemove(markerNode.compositionContext) {
            infoContent(marker)
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        // https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker
        // 高德地图的问题：这里必须返回一个空的View，否则getInfoWindow回调的Marker是一个SDK默认的：空值Marker
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoWindow  = markerNode.infoWindow
        if (infoWindow == null) { // 这里返回null，是处理getInfoContents这2个方法谁返回视图的并显示冲突的问题
            return null
        }
        return infoWindowView.applyAndRemove(markerNode.compositionContext) {
            infoWindow(marker)
        }
    }

    private fun ComposeView.applyAndRemove(
        parentContext: CompositionContext,
        content: @Composable () -> Unit
    ): ComposeView {
        (this.parent as? MapView)?.removeView(this)
        val result = this.apply {
            setParentCompositionContext(parentContext)
            setContent {
                // 去除高德地图默认气泡背景
                setBackgroundColor(Color.TRANSPARENT)
                content.invoke()
            }
        }
        return result
    }
}