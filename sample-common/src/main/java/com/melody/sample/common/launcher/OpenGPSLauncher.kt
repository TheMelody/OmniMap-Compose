package com.melody.sample.common.launcher

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * 打开GPS系统授权页面
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/10 10:10
 */
@Composable
fun handlerGPSLauncher(block: () -> Unit) : ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        block.invoke()
    }
}