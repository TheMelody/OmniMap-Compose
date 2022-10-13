package com.melody.map.myapplication.initializer

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.melody.map.myapplication.utils.GDMapUtils
import com.melody.sample.common.utils.SDKUtils

/**
 * MainActivity
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 11:40
 */
class AppDataInitStartup : Initializer<Boolean> {

    override fun create(context: Context): Boolean {
        SDKUtils.init(context as Application)
        GDMapUtils.updateMapViewPrivacy(context.applicationContext)
        return true
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}