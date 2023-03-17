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

package com.melody.map.baidu_compose.extensions

import android.os.Bundle
import com.baidu.mapapi.map.Marker

/**
 * 返回Marker覆盖物的标题
 */
fun Marker.getTitleExt(): String? = extraInfo?.getString("title")

/**
 * 返回Marker覆盖物的文字片段
 */
fun Marker.getSnippetExt(): String? = extraInfo?.getString("snippet")

/**
 * 返回Marker覆盖物的附加信息对象
 */
fun Marker.getTag(): Bundle? = extraInfo?.getBundle("tag")

/**
 * 返回Marker覆盖物上面弹出的InfoWindow的y轴偏移量
 */
fun Marker.getInfoWindowYOffset(defaultValue: Int = -52): Int = extraInfo?.getInt("infoWindowYOffset", defaultValue) ?: defaultValue