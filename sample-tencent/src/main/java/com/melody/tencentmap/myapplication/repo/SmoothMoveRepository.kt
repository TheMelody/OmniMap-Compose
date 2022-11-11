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

package com.melody.tencentmap.myapplication.repo

import android.graphics.BitmapFactory
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.map.tencent_compose.utils.PathSmoothTool
import com.melody.sample.common.utils.SDKUtils
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds

/**
 * SmoothMoveRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 14:17
 */
object SmoothMoveRepository {

    private val coords: DoubleArray = doubleArrayOf(
        116.3499049793749,
        39.97617053371078,
        116.34978804908442,
        39.97619854213431,
        116.349674596623,
        39.97623045687959,
        116.34955525200917,
        39.97626931100656,
        116.34943728748914,
        39.976285626595036,
        116.34930864705592,
        39.97628129172198,
        116.34918981582413,
        39.976260803938594,
        116.34906721558868,
        39.97623535890678,
        116.34895185151584,
        39.976214717128855,
        116.34886935936889,
        39.976280148755315,
        116.34873954611332,
        39.97628182112874,
        116.34860763527448,
        39.97626038855863,
        116.3484658907622,
        39.976306080391836,
        116.34834585430347,
        39.976358252119745,
        116.34831166130878,
        39.97645709321835,
        116.34827643560175,
        39.97655231226543,
        116.34824186261169,
        39.976658372925556,
        116.34825080406188,
        39.9767570732376,
        116.34825631960626,
        39.976869087779995,
        116.34822111635201,
        39.97698451764595,
        116.34822901510276,
        39.977079745909876,
        116.34822234337618,
        39.97718701787645,
        116.34821627457707,
        39.97730766147824,
        116.34820593515043,
        39.977417746816776,
        116.34821013897107,
        39.97753930933358,
        116.34821304891533,
        39.977652209132174,
        116.34820923399242,
        39.977764016531076,
        116.3482045955917,
        39.97786190186833,
        116.34822159449203,
        39.977958856930286,
        116.3482256370537,
        39.97807288885813,
        116.3482098441266,
        39.978170063673524,
        116.34819564465377,
        39.978266951404066,
        116.34820541974412,
        39.978380693859116,
        116.34819672351216,
        39.97848741209275,
        116.34816588867105,
        39.978593409607825,
        116.34818489339459,
        39.97870216883567,
        116.34818473446943,
        39.978797222300166,
        116.34817728972234,
        39.978893492422685,
        116.34816491505472,
        39.978997133775266,
        116.34815408537773,
        39.97911413849568,
        116.34812908154862,
        39.97920553614499,
        116.34809495907906,
        39.979308267469264,
        116.34805113358091,
        39.97939658036473,
        116.3480310509613,
        39.979491697188685,
        116.3480082124968,
        39.979588529006875,
        116.34799530586834,
        39.979685789111635,
        116.34798818413954,
        39.979801430587926,
        116.3479996420353,
        39.97990758587515,
        116.34798697544538,
        39.980000796262615,
        116.3479912988137,
        39.980116318796085,
        116.34799204219203,
        39.98021407403913,
        116.34798535084123,
        39.980325006125696,
        116.34797702460183,
        39.98042511477518,
        116.34796288754136,
        39.98054129336908,
        116.34797509821901,
        39.980656820423505,
        116.34793922017285,
        39.98074576792626,
        116.34792586413015,
        39.98085620772756,
        116.3478962642899,
        39.98098214824056,
        116.34782449883967,
        39.98108306010269,
        116.34774758827285,
        39.98115277119176,
        116.34761476652932,
        39.98115430642997,
        116.34749135408349,
        39.98114590845294,
        116.34734772765582,
        39.98114337322547,
        116.34722082902628,
        39.98115066909245,
        116.34708205250223,
        39.98114532232906,
        116.346963237696,
        39.98112245161927,
        116.34681500222743,
        39.981136637759604,
        116.34669622104072,
        39.981146248090866,
        116.34658043260109,
        39.98112495260716,
        116.34643721418927,
        39.9811107163792,
        116.34631638374302,
        39.981085081075676,
        116.34614782996252,
        39.98108046779486,
        116.3460256053666,
        39.981049089345206,
        116.34588814050122,
        39.98104839362087,
        116.34575119741586,
        39.9810544889668,
        116.34562885420186,
        39.981040940565734,
        116.34549232235582,
        39.98105271658809,
        116.34537348820508,
        39.981052294975264,
        116.3453513775533,
        39.980956549928244
    )

    fun readLatLngList(): List<LatLng> {
        val points: MutableList<LatLng> = ArrayList()
        var i = 0
        while (i < coords.size) {
            points.add(LatLng(coords[i + 1], coords[i]))
            i += 2
        }
        val pathSmoothTool = PathSmoothTool()
        pathSmoothTool.intensity = 4
        // 使轨迹更加平滑
        return pathSmoothTool.pathOptimize(points)
    }

    fun calcLatLngBounds(points: List<LatLng>): LatLngBounds {
        val builder = LatLngBounds.builder()
        for (index in points.indices) {
            builder.include(points[index])
        }
        return builder.build()
    }

    fun getPolylineTextureBitmap(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                com.melody.ui.components.R.drawable.custtexture
            )
        )
    }

    fun getPointOverLayMarker(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                com.melody.ui.components.R.drawable.ic_map_red_car
            )
        )
    }

    fun initMapUiSettings() : MapUiSettings {
        return MapUiSettings(
            isScrollGesturesEnabled = true,
            isZoomGesturesEnabled = true
        )
    }

}