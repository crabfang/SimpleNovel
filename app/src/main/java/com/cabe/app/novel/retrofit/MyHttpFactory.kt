package com.cabe.app.novel.retrofit

import com.cabe.app.novel.retrofit.RetrofitFactory.buildApiService
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.repository.HttpFactoryInterface
import retrofit.converter.Converter
import retrofit.http.*
import retrofit.mime.TypedString
import rx.Observable
import java.io.InputStream

/**
 * 作者：沈建芳 on 2018/6/29 17:16
 */
class MyHttpFactory: HttpFactoryInterface<InputStream> {
    override fun createRequest(params: RequestParams, converter: Converter?): Observable<InputStream> {
        val apiService = buildApiService(params, converter, ApiService::class.java)
        val observable: Observable<InputStream>  = when (params.requestMethod) {
            RequestParams.REQUEST_METHOD_GET -> apiService[params.getPath(), params.query]
            RequestParams.REQUEST_METHOD_POST -> apiService.post(params.getPath(), params.query, params.body)
            RequestParams.REQUEST_METHOD_POST_BODY -> apiService.postBody(params.getPath(), params.query, TypedString(params.putBody))
            RequestParams.REQUEST_METHOD_PUT -> apiService.put(params.getPath(), params.query, TypedString(params.putBody))
            else -> apiService[params.getPath(), params.query]
        }
        return observable
    }

    private interface ApiService {
        @GET("/{url}")
        operator fun get(@Path(value = "url", encode = false) path: String?, @QueryMap(encodeValues = false) query: Map<String?, String?>?): Observable<InputStream>

        @FormUrlEncoded
        @POST("/{url}")
        fun post(@Path(value = "url", encode = false) path: String?, @QueryMap query: Map<String?, String?>?, @FieldMap body: Map<String?, String?>?): Observable<InputStream>

        @FormUrlEncoded
        @POST("/{url}")
        fun postBody(@Path(value = "url", encode = false) path: String?, @QueryMap query: Map<String?, String?>?, @Body body: TypedString?): Observable<InputStream>

        @PUT("/{url}")
        fun put(@Path(value = "url", encode = false) path: String?, @QueryMap query: Map<String?, String?>?, @Body body: TypedString?): Observable<InputStream>
    }
}