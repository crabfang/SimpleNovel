package com.cabe.app.novel.domain.bqg

import android.text.TextUtils
import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.retrofit.MyHttpManager
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class Search4BqgUseCase(key: String?) : HttpCacheUseCase<List<NovelInfo>>(object : TypeToken<List<NovelInfo>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<NovelInfo>? {
        var novelList: MutableList<NovelInfo>? = null
        try {
            val divResult = doc.select("table.grid > tbody > tr")
            if (divResult != null && divResult.size > 0) {
                novelList = ArrayList()
                divResult.forEach { div ->
                    val novelInfo = parseSingle(div)
                    if(TextUtils.isEmpty(novelInfo?.url).not()) novelList.add(novelInfo!!)
                }
                return novelList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelList
    }

    private fun parseSingle(e: Element?): NovelInfo? {
        var novelInfo: NovelInfo? = null
        if (e != null) {
            novelInfo = NovelInfo()
            novelInfo.source = SourceType.BQG
            val eTitle = e.select("td.odd > a")
            if (eTitle?.size ?: 0 > 0) {
                novelInfo.title = eTitle.first().text()
                novelInfo.url = SourceType.BQG.host + eTitle.attr("href").substring(1)
            }
            val eLast = e.select("td.even > a")
            if (eLast?.size ?: 0 > 0) {
                novelInfo.lastChapter = eLast.first().text()
            }
            val eAuthor = e.select("td.odd")
            if (eAuthor?.size ?: 0 > 1) {
                novelInfo.author = eAuthor[1].text()
            }
            val eUpdate = e.select("td.odd")
            if (eUpdate?.size ?: 0 > 2) {
                novelInfo.update = eUpdate[2].text()
            }
            val eState = e.select("td.even")
            if (eState?.size ?: 0 > 1) {
                novelInfo.state = eState[2].text()
            }
        }
        return novelInfo
    }

    init {
        val params = RequestParams()
        params.host = SourceType.BQG.host
        params.path = "/modules/article/search.php"
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        val query: MutableMap<String, String> = HashMap()
        try {
            query["searchkey"] = URLEncoder.encode(key, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        params.query = query
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken, "utf-8"))
        httpRepository.setResponseTransformer(object : HttpStringTransformer<List<NovelInfo>>() {
            override fun buildData(responseStr: String): List<NovelInfo>? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}