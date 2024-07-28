package com.melody.map.gd_compose.utils

import android.content.Context
import com.amap.api.maps.MapsInitializer

object MapUtils {

    fun setMapPrivacy(context: Context,isAgree: Boolean) {
        MapsInitializer.updatePrivacyShow(context, true, isAgree)
        MapsInitializer.updatePrivacyAgree(context, isAgree)
    }
}