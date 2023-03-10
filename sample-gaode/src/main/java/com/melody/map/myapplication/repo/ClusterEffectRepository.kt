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

package com.melody.map.myapplication.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.melody.map.gd_compose.model.ClusterItem
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.myapplication.R
import com.melody.map.myapplication.model.RegionItem
import com.melody.sample.common.utils.SDKUtils

/**
 * ClusterEffectRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/24 11:33
 */
object ClusterEffectRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isScaleControlsEnabled = true,
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isZoomEnabled = true
        )
    }

    fun initClusterDataList(centerLatLng: LatLng):List<ClusterItem> {
        val items: MutableList<ClusterItem> = mutableListOf()
        //随机10000个点
        for (i in 0..9999) {
            val lat = Math.random() + centerLatLng.latitude
            val lon = Math.random() + centerLatLng.longitude
            val latLng = LatLng(lat, lon, false)
            val regionItem = RegionItem(
                latLng,
                "test$i"
            )
            items.add(regionItem)
        }
        return items
    }

    fun getClusterItemClickLatLngBounds(clusterItems: List<ClusterItem>): LatLngBounds {
        val builder = LatLngBounds.Builder()
        for (clusterItem in clusterItems) {
            builder.include(clusterItem.getPosition())
        }
        return builder.build()
    }

    fun getDefaultClusterIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(SDKUtils.getApplicationContext().resources,R.drawable.ic_defaultcluster)
        )
    }

    inline fun getClusterRenderDrawable(
        clusterNum: Int,
        backDrawable: Map<Int, Drawable?>,
        block: (Int, Drawable) -> Unit
    ): Drawable {
        val scale = SDKUtils.getApplicationContext().resources.displayMetrics.density
        val radius = (80f * scale + 0.5f).toInt()
        return if (clusterNum == 1) {
            var bitmapDrawable: Drawable? = backDrawable[1]
            if (bitmapDrawable == null) {
                bitmapDrawable = SDKUtils.getApplicationContext().resources.getDrawable(
                    R.drawable.icon_openmap_mark, null
                )
                block.invoke(1, bitmapDrawable)
            }
            bitmapDrawable!!
        } else if (clusterNum < 5) {
            var bitmapDrawable: Drawable? = backDrawable[2]
            if (bitmapDrawable == null) {
                bitmapDrawable = BitmapDrawable(
                    null, drawCircle(
                        radius,
                        Color.argb(159, 210, 154, 6)
                    )
                )
                block.invoke(2, bitmapDrawable)
            }
            bitmapDrawable
        } else if (clusterNum < 10) {
            var bitmapDrawable: Drawable? = backDrawable[3]
            if (bitmapDrawable == null) {
                bitmapDrawable = BitmapDrawable(
                    null, drawCircle(
                        radius,
                        Color.argb(199, 217, 114, 0)
                    )
                )
                block.invoke(3, bitmapDrawable)
            }
            bitmapDrawable
        } else {
            var bitmapDrawable: Drawable? = backDrawable[4]
            if (bitmapDrawable == null) {
                bitmapDrawable = BitmapDrawable(
                    null, drawCircle(
                        radius,
                        Color.argb(235, 215, 66, 2)
                    )
                )
                block.invoke(4, bitmapDrawable)
            }
            bitmapDrawable
        }
    }

    fun drawCircle(radius: Int, color: Int): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            radius * 2, radius * 2,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val rectF = RectF(0f, 0f, (radius * 2).toFloat(), (radius * 2).toFloat())
        paint.color = color
        canvas.drawArc(rectF, 0f, 360f, true, paint)
        return bitmap
    }

}