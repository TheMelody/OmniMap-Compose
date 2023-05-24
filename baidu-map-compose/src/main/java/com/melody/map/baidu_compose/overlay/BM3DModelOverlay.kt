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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.baidu.mapapi.map.BM3DModel
import com.baidu.mapapi.map.BM3DModelOptions
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class BM3DModelNode(
    val bM3DModel: BM3DModel
) : MapNode {
    override fun onRemoved() {
        bM3DModel.remove()
    }
}

/**
 * 3D模型覆盖物
 * @param scale 缩放比例，默认1.0f
 * @param modelPath 模型文件路径
 * @param modelName 模型文件名
 * @param position position
 * @param isZoomFixed scale是否不随地图缩放而变化，默认为 false
 * @param visible BM3DModel 覆盖物可见性，默认 true 显示
 */
@Composable
@BDMapComposable
fun BM3DModelOverlay(
    modelPath: String,
    modelName: String,
    position: LatLng,
    scale: Float = 1.0F,
    visible: Boolean = true,
    isZoomFixed: Boolean = false
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<BM3DModelNode, MapApplier>(
        factory = {
            val bM3DModel = mapApplier?.map?.addOverlay(
                BM3DModelOptions().apply  {
                    setScale(scale)
                    setModelPath(modelPath)
                    setModelName(modelName)
                    setPosition(position)
                    setZoomFixed(isZoomFixed)
                    visible(visible)
                }
            ) as? BM3DModel ?: error("Error adding BM3DModel")
            BM3DModelNode(bM3DModel)
        },
        update = {
            set(scale) { this.bM3DModel.scale = it }
            set(modelPath) { this.bM3DModel.modelPath = it }
            set(modelName) { this.bM3DModel.modelName = it }
            set(position) { this.bM3DModel.position = it }
            set(isZoomFixed) { this.bM3DModel.isZoomFixed = it }
            set(visible) { this.bM3DModel.isVisible = it }
        }
    )
}