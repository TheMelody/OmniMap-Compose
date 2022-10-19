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

package com.melody.map.gd_compose.model

import androidx.compose.runtime.Immutable
import com.amap.api.maps.AMap

/**
 * Enumerates the different types of map tiles.
 */
@Immutable
enum class MapType(val value: Int) {
    /**
     * 白昼地图（即普通地图）
     */
    NORMAL(AMap.MAP_TYPE_NORMAL),

    /**
     * 卫星图
     */
    SATELLITE(AMap.MAP_TYPE_SATELLITE),

    /**
     * 导航地图
     */
    NAVI(AMap.MAP_TYPE_NAVI),

    /**
     * 夜景地图
     */
    NIGHT(AMap.MAP_TYPE_NIGHT)
}
