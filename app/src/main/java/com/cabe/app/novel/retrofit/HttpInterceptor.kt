package com.cabe.app.novel.retrofit

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

private val cookieMap= mutableMapOf<String, String>()
class ReceivedCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        //这里获取请求返回的cookie
        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val host = chain.request().url().host()
            val cookieBuffer = StringBuffer()
            originalResponse.headers("Set-Cookie").map { s ->
                val cookieArray = s.split(";".toRegex()).toTypedArray()
                cookieArray[0]
            }.forEach { cookie ->
                cookieBuffer.append(cookie).append(";")
            }
            cookieMap[host] = cookieBuffer.toString()
        }
        return originalResponse
    }
}

class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        val host = chain.request().url().host()
        cookieMap[host]?.let { cookies ->
            builder.addHeader("Cookie", cookies)
        }
        return chain.proceed(builder.build())
    }
}