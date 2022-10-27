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

package com.melody.map.tencent_compose.model

import android.graphics.Rect
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_LEFT

/**
 * position 设置比例尺位置
 *
 * TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_CENTER 底边中心
 * TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_LEFT 左下角
 * TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_RIGHT 右下角
 * TencentMapOptions.SCALEVIEW_POSITION_TOP_CENTER 顶部中心
 * TencentMapOptions.SCALEVIEW_POSITION_TOP_LEFT 左上角
 * TencentMapOptions.SCALEVIEW_POSITION_TOP_RIGHT 右上角
 *
 * setScaleViewPositionWithMargin(int position, int top, int bottom, int left, int right)
 *
 * position 设置比例尺位置
 *
 * TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_LEFT 左下角
 * TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_RIGHT 右下角
 * TencentMapOptions.SCALEVIEW_POSITION_TOP_LEFT 左上角
 * TencentMapOptions.SCALEVIEW_POSITION_TOP_RIGHT 右上角
 * top position 为 TencentMapOptions.SCALEVIEW_POSITION_TOP_LEFT 或 TencentMapOptions.SCALEVIEW_POSITION_TOP_RIGHT 时，该值生效，不需要偏移请传负数
 *
 * bottom position 为 TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_LEFT 或 TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_RIGHT 时，该值生效，不需要偏移请传负数
 *
 * left position 为 TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_LEFT 或 TencentMapOptions.SCALEVIEW_POSITION_TOP_LEFT 时，该值生效，不需要偏移请传负数
 *
 * right position 为 TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_RIGHT 或 TencentMapOptions.SCALEVIEW_POSITION_TOP_RIGHT 时，该值生效，不需要偏移请传负数
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/25 16:51
 */
class MapScaleViewAnchor(
    val scaleViewPosition: Int = SCALEVIEW_POSITION_BOTTOM_LEFT,
    val ltrb: Rect = Rect(
        /* left = */ -1,
        /* top = */-1,
        /* right = */-1,
        /* bottom = */75
    )
)