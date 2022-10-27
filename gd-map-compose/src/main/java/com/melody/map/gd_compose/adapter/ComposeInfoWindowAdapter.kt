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

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
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
        // 当实现此方法并返回有效值时（返回值不为空，则视为有效）,SDK 将不会使用默认的样式，而采用此方法返回的样式
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoContent  = markerNode.infoContent
        if (infoContent == null) { // 这里返回null，是处理getInfoWindow这2个方法谁返回视图的并显示冲突的问题
            if(markerNode.infoWindow == null){
                // 由于上面返回了一个SpaceView，这里返回一个，啥都没有设置的默认视图内容
                return getDefaultInfoContent(marker, mapView.context)
            }
            return null
        }
        return infoWindowView.applyAndRemove(false,markerNode.compositionContext) {
            infoContent(marker)
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        // https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker
        // 当实现此方法并返回有效值时（返回值不为空，则视为有效）,SDK 将不会使用默认的样式，而采用此方法返回的样式
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoWindow  = markerNode.infoWindow
        if (infoWindow == null) { // 这里返回null，是处理getInfoContents这2个方法谁返回视图的并显示冲突的问题
            return null
        }
        return infoWindowView.applyAndRemove(true,markerNode.compositionContext) {
            infoWindow(marker)
        }
    }

    /**
     * 这算是高德地图给开发者的bug，不太灵活，增加这个方法是为了给[com.melody.map.gd_compose.overlay.Marker]这个组合项使用的
     * 返回一个默认的InfoContent视图，保证[com.melody.map.gd_compose.overlay.Marker]能正常弹出信息窗
     */
    private fun getDefaultInfoContent(marker: Marker,context: Context): View {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            addView(getDefaultInfoTextView(context,marker.title))
            if(!marker.snippet.isNullOrBlank()){
                addView(getDefaultInfoTextView(context,marker.snippet))
            }
        }
    }
    private fun getDefaultInfoTextView(context: Context,content: String) : TextView {
        return TextView(context).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            isSingleLine = true
            setTextColor(Color.BLACK)
            text = content
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