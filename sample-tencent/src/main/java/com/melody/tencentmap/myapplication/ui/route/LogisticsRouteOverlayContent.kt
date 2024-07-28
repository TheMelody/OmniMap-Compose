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

package com.melody.tencentmap.myapplication.ui.route

import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import com.melody.map.tencent_compose.model.TXMapComposable
import com.melody.map.tencent_compose.overlay.MarkerInfoWindow
import com.melody.map.tencent_compose.overlay.PolylineRainbow
import com.melody.map.tencent_compose.overlay.rememberMarkerState
import com.melody.sample.common.utils.SDKUtils
import com.melody.tencentmap.myapplication.R
import com.melody.tencentmap.myapplication.model.LogisticsRouteDataState
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory

/**
 * LogisticsRouteOverlayContent
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/23 14:10
 */
@TXMapComposable
@Composable
internal fun LogisticsRouteOverlayContent(dataState: LogisticsRouteDataState) {
    val startState  = rememberMarkerState(position = dataState.startPoint)
    val carState = rememberMarkerState(position = dataState.carLocation)
    val endState  = rememberMarkerState(position = dataState.endPoint)

    LaunchedEffect(Unit) {
        startState.showInfoWindow()
        carState.showInfoWindow()
        endState.showInfoWindow()
    }

    PolylineRainbow(
        isLineCap = true,
        useGradient = false,
        points = dataState.points,
        rainbow = dataState.rainbow
    )

    MarkerInfoWindow(
        anchor = Offset(0.5f,-0.3f),
        draggable = true,
        icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pdd_fahuo_location),
        state = startState,
        zIndex = 0F,
        // 根据你自己业务获取
        title = "上海市",
        tag = R.drawable.ic_pdd_fahuo,
        content = { marker ->
            LogisticsMarkInfoWindowContent(marker.tag as Int, marker.title)
        }
    )

    MarkerInfoWindow(
        anchor = Offset(0.5f,-0.3f),
        draggable = true,
        zIndex = 1F,
        icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pdd_shouhuo_dark_location),
        state = endState,
        title = "收货地址",
        tag = R.drawable.ic_pdd_shou_huo,
        content = { marker ->
            LogisticsMarkInfoWindowContent(marker.tag as Int, marker.title)
        }
    )

    MarkerInfoWindow(
        anchor = Offset(0.25f,0.3f),
        draggable = true,
        icon = BitmapDescriptorFactory.fromAsset("ic_pdd_car.png"),
        state = carState,
        zIndex = 2F,
        rotation = dataState.carRotation.toFloat(),
        title = "派件中，预计后天送达",
        snippet = "当前在宜春市",
        tag = R.drawable.ic_pdd_transit,
        content = { marker ->
            LogisticsMarkInfoWindowContent2(marker.tag as Int, marker.title,marker.snippet)
        }
    )
}

@Composable
private fun LogisticsMarkInfoWindowContent(resource: Int, title: String) {
    // 处理升级compose版本闪退问题
    /*AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {
            TextView(it).apply {
                setTextColor(Color.BLACK)
            }
        }
    )
    {
        it.setBackgroundResource(resource)
        it.text = title
    }*/
    val fromView =
        BitmapDescriptorFactory.fromView(TextView(SDKUtils.getApplicationContext()).apply {
            setTextColor(Color.BLACK)
            setBackgroundResource(resource)
            text = title
        })
    Image(
        modifier = Modifier.wrapContentSize(),
        bitmap = fromView.getBitmap(SDKUtils.getApplicationContext()).asImageBitmap(),
        contentDescription = null
    )
}

@Composable
private fun LogisticsMarkInfoWindowContent2(resource: Int, title: String, snippet: String) {
    // 处理升级compose版本闪退问题
    /*AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {
            LinearLayout(it).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(it).apply {
                    setTextColor(Color.WHITE)
                    textSize = 16F
                })
                addView(TextView(it).apply {
                    setTextColor(Color.WHITE)
                    textSize = 12F
                })
            }
        }
    )
    {
        it.setBackgroundResource(resource)
        (it.getChildAt(0) as TextView).text = title
        (it.getChildAt(1) as TextView).text = snippet
    }*/
    val fromView =
        BitmapDescriptorFactory.fromView(
            LinearLayout(SDKUtils.getApplicationContext()).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(SDKUtils.getApplicationContext()).apply {
                    setTextColor(Color.WHITE)
                    textSize = 16F
                    text = title
                })
                addView(TextView(SDKUtils.getApplicationContext()).apply {
                    setTextColor(Color.WHITE)
                    textSize = 12F
                    text = snippet
                })
                setBackgroundResource(resource)
            }
        )
    Image(
        modifier = Modifier.wrapContentSize(),
        bitmap = fromView.getBitmap(SDKUtils.getApplicationContext()).asImageBitmap(),
        contentDescription = null
    )
}
