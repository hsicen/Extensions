package com.hsicen.extensions

import android.app.Application
import com.bytedance.sdk.open.douyin.DouYinOpenApiFactory
import com.bytedance.sdk.open.douyin.DouYinOpenConfig

/**
 * 作者：hsicen  7/14/21 14:42
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：Extensions
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initDouyin()
    }

    private fun initDouyin() {
        DouYinOpenApiFactory.init(DouYinOpenConfig(DOUYIN_KEY))
    }

    companion object {
        const val DOUYIN_KEY = "awlqfbi77cx7onrv"
    }
}