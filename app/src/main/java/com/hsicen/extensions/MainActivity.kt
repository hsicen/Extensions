package com.hsicen.extensions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.sdk.open.aweme.authorize.model.Authorization
import com.bytedance.sdk.open.aweme.base.MediaContent
import com.bytedance.sdk.open.aweme.base.VideoObject
import com.bytedance.sdk.open.aweme.share.Share
import com.bytedance.sdk.open.douyin.DouYinOpenApiFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun shareToDouyin() {
        val openApi = DouYinOpenApiFactory.create(this)
        val request = Share.Request()

        val videoObject = VideoObject()
        videoObject.mVideoPaths = arrayListOf("xxx", "xxx", "xxx")

        val mediaContent = MediaContent()
        mediaContent.mMediaObject = videoObject

        request.mMediaContent = mediaContent
        request.callerLocalEntry = "com.hsicen.extensions.DouYinEntryActivity" //指定回调处理类
        request.shareToPublish = true //直接分享到视频发布页
        openApi.share(request)
    }

    private fun loginByDouyin() {
        val openApi = DouYinOpenApiFactory.create(this)
        val request = Authorization.Request()

        request.scope = "user_info" //授权必选权限
        request.state = "authorization"
        request.callerLocalEntry = "com.hsicen.extensions.DouYinEntryActivity" //指定回调处理类

        openApi.authorize(request)
    }
}