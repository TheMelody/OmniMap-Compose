package com.melody.sample.common.utils

import androidx.annotation.StringRes

object StringUtils {

    fun getString(@StringRes resID: Int): String {
        return SDKUtils.getApplicationContext().getString(resID)
    }

}