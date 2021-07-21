package com.hsicen.extensions

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.sdk.open.aweme.CommonConstants
import com.bytedance.sdk.open.aweme.authorize.model.Authorization
import com.bytedance.sdk.open.aweme.common.handler.IApiEventHandler
import com.bytedance.sdk.open.aweme.common.model.BaseReq
import com.bytedance.sdk.open.aweme.common.model.BaseResp
import com.bytedance.sdk.open.aweme.share.Share
import com.bytedance.sdk.open.douyin.DouYinOpenApiFactory

/**
 * 作者：hsicen  7/14/21 15:15
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：自定义抖音分享处理类
 */
class DouYinEntryActivity : AppCompatActivity(), IApiEventHandler {
    private val douApi by lazy {
        DouYinOpenApiFactory.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            douApi.handleIntent(intent, this)
        }
    }

    override fun onReq(p0: BaseReq?) {
        //发起分享请求
    }

    override fun onResp(p0: BaseResp?) {
        when (p0?.type) {
            //分享结果回调
            CommonConstants.ModeType.SHARE_CONTENT_TO_TT_RESP -> {
                (p0 as? Share.Response)?.let { response ->

                }
            }
            //授权结果回调
            CommonConstants.ModeType.SEND_AUTH_RESPONSE -> {
                (p0 as? Authorization.Response)?.let { response ->

                }
            }
            //其它请求结果回调
            else -> {
            }
        }
    }

    override fun onErrorIntent(p0: Intent?) {
        //分享出错回调
        finish()
    }
}