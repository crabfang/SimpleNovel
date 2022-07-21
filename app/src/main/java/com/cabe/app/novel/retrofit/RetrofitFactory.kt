package com.cabe.app.novel.retrofit

import android.text.TextUtils
import android.util.Log
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.repository.OkHttpClientFactory
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.RestAdapter.LogLevel
import retrofit.client.OkClient
import retrofit.converter.Converter

/**
 * 作者：沈建芳 on 2018/6/29 17:19
 */
object RetrofitFactory {
    private val logLevel = LogLevel.FULL
    private val httpClient: OkHttpClient by lazy {
        generate().apply {
            dns = OkHttpDns.instance
        }
    }

    private fun generate(): OkHttpClient= OkHttpClientFactory.create()
    @JvmStatic
    fun <T> buildApiService(params: RequestParams, converter: Converter?, clazz: Class<T>?): T {
        val host = params.getHost()
        val requestInterceptor = RequestInterceptor { request ->
            val headMap = params.head
            if (headMap != null) {
                val it: MutableIterator<Map.Entry<String, String>> = headMap.entries.iterator()
                while (it.hasNext()) {
                    val (key, value) = it.next()
                    request.addHeader(key, value)
                    it.remove()
                }
            }
        }
        return getRetrofit(converter, host, requestInterceptor).create(clazz)
    }

    private fun getRetrofit(converter: Converter?, baseUrl: String, dataInterceptor: RequestInterceptor?): RestAdapter {
        return if (TextUtils.isEmpty(baseUrl)) {
            throw RuntimeException("baseUrl is null")
        } else {
            val retrofit = RestAdapter.Builder()
            retrofit.setLog(HttpLog())
            retrofit.setLogLevel(logLevel)
            retrofit.setEndpoint(baseUrl)
            if (converter != null) {
                retrofit.setConverter(converter)
            }
            retrofit.setClient(OkClient(httpClient))
            if (dataInterceptor != null) {
                retrofit.setRequestInterceptor(dataInterceptor)
            }
            retrofit.build()
        }
    }
}

private class HttpLog : RestAdapter.Log {
    override fun log(message: String) {
        Log.i("RxCache.Http", message)
    }
}