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

import androidx.compose.ui.unit.IntOffset
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT

/**
 *
 * logoAnchor 地图商标位置
 *
 * TencentMapOptions.LOGO_POSITION_BOTTOM_CENTER 底边中心
 * TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT 左下角
 * TencentMapOptions.LOGO_POSITION_BOTTOM_RIGHT 右下角
 * TencentMapOptions.LOGO_POSITION_TOP_CENTER 顶部中心
 * TencentMapOptions.LOGO_POSITION_TOP_LEFT 左上角
 * TencentMapOptions.LOGO_POSITION_TOP_RIGHT 右上角
 * setLogoPosition(int logoAnchor, int[] marginParams)
 *
 * 这个接口中 logoAnchor 只支持将地图商标放到 MapView 的四个角落，但提供了设置商标与 MapView 边缘的间距的能力，比上一个接口功能更强大。
 *
 * logoAnchor 设置地图商标位置
 *
 * TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT 左下角
 * TencentMapOptions.LOGO_POSITION_BOTTOM_RIGHT 右下角
 * TencentMapOptions.LOGO_POSITION_TOP_LEFT 左上角
 * TencentMapOptions.LOGO_POSITION_TOP_RIGHT 右上角
 * marginParams 是一个长度为 2 的数组，表示商标与 MapView 边缘的间距
 *
 * 若 logoAnchor 为 LOGO_POSITION_BOTTOM_LEFT, 则 Logo 的 bottomMargin 为 marginParams[0], leftMargin 为 marginParams[1]
 * 若 logoAnchor 为 LOGO_POSITION_BOTTOM_RIGHT, 则 Logo 的 bottomMargin 为 marginParams[0], rightMargin 为 marginParams[1]
 * 若 logoAnchor 为 LOGO_POSITION_TOP_RIGHT, 则 Logo 的 topMargin 为 marginParams[0], rightMargin 为 marginParams[1]
 * 若 logoAnchor 为 LOGO_POSITION_TOP_LEFT，则 Logo 的 topMargin 为 marginParams[0], leftMargin 为 marginParams[1]
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/25 16:48
 */
class MapLogoAnchor(val logoPosition: Int = LOGO_POSITION_BOTTOM_LEFT,val offset: IntOffset = IntOffset(15,20))