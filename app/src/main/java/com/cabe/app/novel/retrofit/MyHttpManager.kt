package com.cabe.app.novel.retrofit

import com.cabe.lib.cache.exception.ExceptionCode
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.StreamConverterFactory
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.http.transformer.Stream2StringTransformer
import com.cabe.lib.cache.interactor.HttpCacheRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit.converter.Converter
import rx.Observable

/**
 * 作者：沈建芳 on 2018/6/29 17:25
 */
class MyHttpManager<T> @JvmOverloads constructor(private val typeToken: TypeToken<T>?, encode: String? = "gb2312") : HttpCacheRepository<String, T> {
    private var encode: String? = null
    private var converter: Converter? = null
    private var transformer: Observable.Transformer<String, T>? = null
    override fun getHttpObservable(params: RequestParams): Observable<T> {
        return MyHttpFactory().createRequest(params, converter).compose(Stream2StringTransformer(encode)).compose(transformer)
    }

    override fun setHttpConverter(converter: Converter) {
        this.converter = converter
    }

    override fun setResponseTransformer(transformer: Observable.Transformer<String, T>) {
        this.transformer = transformer
    }

    fun setStringEncode(encode: String?) {
        this.encode = encode
    }

    init {
        setStringEncode(encode)
        setHttpConverter(StreamConverterFactory.create())
        setResponseTransformer(object : HttpStringTransformer<T>() {
            override fun buildData(responseStr: String): T {
                if (typeToken == null) {
                    throw RxException.build(ExceptionCode.RX_EXCEPTION_TYPE_UNKNOWN, null)
                }
                return if (typeToken.type === String::class.java) {
                    responseStr as T
                } else {
                    try {
                        Gson().fromJson<T>(responseStr, typeToken.type)
                    } catch (e: Exception) {
                        throw RxException.build(HttpExceptionCode.RX_EXCEPTION_TYPE_UNKNOWN, e)
                    }
                }
            }
        })
    }
}