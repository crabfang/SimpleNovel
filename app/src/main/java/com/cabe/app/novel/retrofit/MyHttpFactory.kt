package com.cabe.app.novel.retrofit

import com.cabe.app.novel.retrofit.RetrofitFactory.buildApiService
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.repository.HttpFactoryInterface
import okhttp3.ResponseBody
import retrofit.converter.Converter
import retrofit.mime.TypedString
import retrofit2.http.*
import rx.Observable
import java.io.InputStream

/**
 * 作者：沈建芳 on 2018/6/29 17:16
 */
class MyHttpFactory: HttpFactoryInterface<InputStream> {
    companion object {
        const val REQUEST_METHOD_GET = RequestParams.REQUEST_METHOD_GET
        const val REQUEST_METHOD_POST = RequestParams.REQUEST_METHOD_POST
        const val REQUEST_METHOD_POST_BODY = RequestParams.REQUEST_METHOD_POST_BODY
        const val REQUEST_METHOD_PUT = RequestParams.REQUEST_METHOD_PUT
        const val REQUEST_METHOD_POST_FORM = 101
    }
    override fun createRequest(params: RequestParams, converter: Converter?): Observable<InputStream> {
        val apiService = buildApiService(params, null, ApiService::class.java)
        val queryMap = mutableMapOf<String, String?>()
        if(params.query != null) queryMap.putAll(params.query)
        val observable: Observable<ResponseBody>  = when (params.requestMethod) {
            REQUEST_METHOD_GET -> apiService[params.getPath(), queryMap]
            REQUEST_METHOD_POST -> apiService.post(params.getPath(), queryMap, params.body)
            REQUEST_METHOD_POST_FORM -> apiService.postForm(params.getPath(), queryMap, params.body)
            REQUEST_METHOD_POST_BODY -> apiService.postBody(params.getPath(), queryMap, TypedString(params.putBody))
            REQUEST_METHOD_PUT -> apiService.put(params.getPath(), queryMap, TypedString(params.putBody))
            else -> apiService[params.getPath(), queryMap]
        }
        return observable.map { it.byteStream() }
    }

    private interface ApiService {
        @GET("/{url}")
        operator fun get(@Path(value = "url", encoded = true) path: String?, @QueryMap(encoded = true) query: Map<String, String?>): Observable<ResponseBody>

        @POST("/{url}")
        fun post(@Path(value = "url", encoded = true) path: String?, @QueryMap query: Map<String, String?>, @Body body: Map<String, String?>?): Observable<ResponseBody>

        @FormUrlEncoded
        @POST("/{url}")
        fun postForm(@Path(value = "url", encoded = true) path: String?, @QueryMap query: Map<String, String?>, @FieldMap(encoded = true) body: Map<String, String?>?): Observable<ResponseBody>

        @FormUrlEncoded
        @POST("/{url}")
        fun postBody(@Path(value = "url", encoded = true) path: String?, @QueryMap query: Map<String, String?>, @Body body: TypedString?): Observable<ResponseBody>

        @PUT("/{url}")
        fun put(@Path(value = "url", encoded = true) path: String?, @QueryMap query: Map<String, String?>, @Body body: TypedString?): Observable<ResponseBody>
    }
}