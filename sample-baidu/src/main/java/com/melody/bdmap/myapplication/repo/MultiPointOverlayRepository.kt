package com.melody.bdmap.myapplication.repo

import android.graphics.BitmapFactory
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MultiPointItem
import com.baidu.mapapi.model.LatLng
import com.melody.bdmap.myapplication.R
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import java.io.*

/**
 * MultiPointOverlayRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/21 10:55
 */
object MultiPointOverlayRepository {

    fun initMapUiSettings() : MapUiSettings {
        return MapUiSettings(
            isZoomEnabled = true,
            isScrollGesturesEnabled = true,
            isZoomGesturesEnabled = true,
            isScaleControlsEnabled = true
        )
    }

    fun initMultiPointIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(SDKUtils.getApplicationContext().resources, R.drawable.multi_point_blue)
        )
    }

    fun initMultiPointItemList() : List<MultiPointItem> {
        val list: MutableList<MultiPointItem> = mutableListOf()
        val outputStream: FileOutputStream? = null
        var inputStream: InputStream? = null
        try {
            inputStream =
                SDKUtils.getApplicationContext().resources.openRawResource(R.raw.point10w)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val str = line?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }
                    ?.toTypedArray() ?: continue
                val lat = str[1].trim { it <= ' ' }.toDouble()
                val lon = str[0].trim { it <= ' ' }.toDouble()
                val latLng = LatLng(lat, lon)
                val multiPointItem = MultiPointItem(latLng)
                list.add(multiPointItem)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return list
    }
}