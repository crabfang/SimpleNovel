package com.cabe.app.novel.retrofit

import android.net.Uri
import android.util.Log
import com.cabe.app.novel.model.SourceType
import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

class OkHttpDns private constructor() : Dns {
    companion object {
        val instance: OkHttpDns by lazy {
            OkHttpDns()
        }
    }

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        //通过异步解析接口获取ip
//        val bqgHost = Uri.parse(SourceType.BQG.host).host
//        if (bqgHost == hostname) {
//            try {
//                val ip = "23.225.140.157"
//                //如果ip不为null，直接使用该ip进行网络请求
//                val inetAddresses = listOf(*InetAddress.getAllByName(ip))
//                Log.e("OkHttpDns", "inetAddresses:$inetAddresses")
//                return inetAddresses
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
        //如果返回null，走系统DNS服务解析域名
        return Dns.SYSTEM.lookup(hostname)
    }
}