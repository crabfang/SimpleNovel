package com.cabe.app.novel.retrofit

import android.text.TextUtils
import com.cabe.lib.cache.http.RequestParams
import okhttp3.*
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory

/**
 * 作者：沈建芳 on 2018/6/29 17:19
 */
private var myCookies: CookieJar = object : CookieJar {
    private val cookieMap= mutableMapOf<String, List<Cookie>?>()
    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        url.host().let { host ->
            cookieMap[host] = cookies
        }
    }
    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        return cookieMap[url.host()]?.toMutableList() ?: arrayListOf()
    }
}
object RetrofitFactory {
    fun <T> buildApiService(params: RequestParams, converterFactory: Converter.Factory?, clazz: Class<T>): T {
        val host = params.getHost()
        val httpBuilder = OkHttpClient.Builder()
            .dns(OkHttpDns.instance)
            .cookieJar(myCookies)
            .addInterceptor(ReceivedCookiesInterceptor())
            .addInterceptor(AddCookiesInterceptor())
        if(params.head != null) {
            httpBuilder.addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                builder.method(original.method(), original.body())
                params.head.forEach {
                    builder.addHeader(it.key, it.value)
                }
                chain.proceed(builder.build())
            }
        }
        return getRetrofit(httpBuilder.build(), converterFactory, host).create(clazz)
    }

    private fun getRetrofit(httpClient: OkHttpClient, converterFactory: Converter.Factory?, baseUrl: String): Retrofit {
        return if (TextUtils.isEmpty(baseUrl)) {
            throw RuntimeException("baseUrl is null")
        } else {
            val retrofit = Retrofit.Builder()
            retrofit.baseUrl(baseUrl)
            retrofit.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            if (converterFactory != null) {
                retrofit.addConverterFactory(converterFactory)
            }
            retrofit.client(httpClient)
            retrofit.build()
        }
    }
}