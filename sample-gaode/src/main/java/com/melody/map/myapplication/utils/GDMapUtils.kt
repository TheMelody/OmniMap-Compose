package com.melody.map.myapplication.utils

import android.content.Context
import com.amap.api.maps.MapsInitializer

/**
 * GDMapUtils
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/08 17:20
 */
object GDMapUtils {
    /**
     * 更新地图的隐私合规，【不调用地图无法正常显示】只要用到地图SDK的地方，都要先调用它
     */
    fun updateMapViewPrivacy(context: Context) {
        MapsInitializer.updatePrivacyShow(context.applicationContext, true, true)
        MapsInitializer.updatePrivacyAgree(context.applicationContext, true)
    }
}