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

package com.melody.petal.map_sample.initializer

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.huawei.hms.maps.MapsInitializer
import com.melody.sample.common.utils.SDKUtils

/**
 * AppDataInitStartup
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/08/27 11:20
 */
class AppDataInitStartup : Initializer<Boolean> {

    override fun create(context: Context): Boolean {
        SDKUtils.init(context as Application)
        // 自行授权隐私声明之后进行初始化
        MapsInitializer.initialize(context.applicationContext)
        // TODO:  奇怪，无法读取agconect-service.json,已提工单，待处理，暂时代码设置
        MapsInitializer.setAppId("109050473")
        MapsInitializer.setApiKey("DAEDAPibORK3yWM7w93ZWXBqqO035NVZx2uC3OJkSd1O/pLlQpRJ5ySeMvP1sDZkTU8WhUadMbk19mXrE2NY1X1S4Wvly7mSxen4qQ==")
        return true
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}