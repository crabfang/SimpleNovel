package com.cabe.app.novel.domain.x23us

import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.retrofit.MyHttpFactory
import com.cabe.app.novel.retrofit.MyHttpManager
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.reflect.TypeToken

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class Home4X23USUseCase: HttpCacheUseCase<Void>(object : TypeToken<Void>() {}, null) {
    init {
        val params = RequestParams()
        params.host = SourceType.X23US.host
        params.path = "index.php"
        params.requestMethod = MyHttpFactory.REQUEST_METHOD_GET
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken))
        httpRepository.setResponseTransformer(object : HttpStringTransformer<Void>() {
            override fun buildData(responseStr: String): Void {
                return null as Void
            }
        })
    }
}