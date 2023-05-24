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

package com.melody.map.baidu_compose.overlay

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.mapapi.map.Text
import com.baidu.mapapi.map.TextOptions
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class TextOverlayNode(
    val textOverlay: Text,
) : MapNode {
    override fun onRemoved() {
        textOverlay.remove()
    }
}

/**
 * 文字覆盖物，由[com.baidu.mapapi.map.Text]类定义，在地图上显示文字覆盖物
 *
 * @param text  文字覆盖物文字内容
 * @param position  文字覆盖物的地理坐标
 * @param fontSize  文字覆盖物的字体大小
 * @param fontColor 文字覆盖物的字体颜色
 * @param backgroundColor   文字覆盖物的背景颜色
 * @param alignX    文字覆盖物,X轴对齐方式，默认：[TextOptions.ALIGN_CENTER_HORIZONTAL]
 * @param alignY    文字覆盖物,Y轴对齐方式，默认：[TextOptions.ALIGN_CENTER_HORIZONTAL]
 * @param zIndex    文字覆盖物zIndex
 * @param rotate    文字覆盖物旋转角度，逆时针
 * @param visible   文字覆盖物是否可见
 * @param typeface  文字覆盖物的字体
 */
@Composable
@BDMapComposable
fun TextOverlay(
    text: String,
    position: LatLng,
    fontSize: Int,
    fontColor: Color,
    backgroundColor: Color,
    alignX: Int = TextOptions.ALIGN_CENTER_HORIZONTAL,
    alignY: Int = TextOptions.ALIGN_CENTER_HORIZONTAL,
    zIndex: Int = 0,
    rotate: Float = 0F,
    visible: Boolean = true,
    typeface: Typeface? = null,
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<TextOverlayNode, MapApplier>(
        factory = {
            val textOverlay = mapApplier?.map?.addOverlay(
                TextOptions().apply {
                    text(text)
                    position(position)
                    bgColor(backgroundColor.toArgb())
                    fontColor(fontColor.toArgb())
                    fontSize(fontSize)
                    typeface(typeface)
                    align(alignX, alignY)
                    rotate(rotate)
                    visible(visible)
                    zIndex(zIndex)
                }) as? Text ?: error("Error adding Text")
            TextOverlayNode(textOverlay)
        },
        update = {
            set(text) { this.textOverlay.text = it  }
            set(position) { this.textOverlay.position = it  }
            set(backgroundColor) { this.textOverlay.bgColor = it.toArgb()  }
            set(fontColor) { this.textOverlay.fontColor = it.toArgb()  }
            set(fontSize) { this.textOverlay.fontSize = it  }
            set(typeface) { this.textOverlay.typeface = it  }
            set(alignX) { this.textOverlay.setAlign(alignX,alignY)  }
            set(alignY) { this.textOverlay.setAlign(alignX,alignY)  }
            set(rotate) { this.textOverlay.rotate = it  }
            set(visible) { this.textOverlay.isVisible = it  }
            set(zIndex) { this.textOverlay.zIndex = it  }

        }
    )
}