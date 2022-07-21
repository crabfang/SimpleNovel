package com.cabe.app.novel.widget

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.cabe.app.novel.model.NovelContent
import com.cabe.lib.cache.interactor.ViewPresenter
import com.cabe.lib.cache.CacheSource
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.domain.x23us.NovelContent4X23USUseCase
import com.cabe.app.novel.domain.ekxs.NovelContent42kxsUseCase
import com.cabe.app.novel.domain.fpzw.NovelContent4FpzwUseCase
import com.cabe.app.novel.domain.bqg.NovelContent4BqgUseCase
import android.text.TextUtils
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import com.cabe.app.novel.R

/**
 * 作者：沈建芳 on 2018/7/12 19:27
 */
class NovelContentView(context: Context?) : LinearLayout(context) {
    private val viewMain: View
    private val tvTitle: TextView
    private val tvContent: TextView
    private val loading: View
    private val viewError: View
    private var novel: NovelContent? = null
    fun loadContent(novel: NovelContent?) {
        this.novel = novel
        if (novel == null) return
        loading.visibility = VISIBLE
        val presenter: ViewPresenter<NovelContent> = object: ViewPresenter<NovelContent> {
            override fun error(from: CacheSource, code: Int, info: String) {
                viewError.visibility = VISIBLE
                viewMain.visibility = GONE
            }
            override fun load(from: CacheSource, content: NovelContent) {
                val cacheContent = NovelContent()
                cacheContent.title = content.title
                cacheContent.url = content.url
                cacheContent.preUrl = content.preUrl
                cacheContent.nextUrl = content.nextUrl
                cacheContent.source = novel.source
                updateView(content)
                viewError.visibility = GONE
                viewMain.visibility = VISIBLE
            }
            override fun complete(from: CacheSource) {
                loading.visibility = GONE
            }
        }
        val url = novel.url
        when (novel.source) {
            SourceType.X23US -> {
                val useCase = NovelContent4X23USUseCase(url)
                useCase.execute(presenter)
            }
            SourceType.EKXS -> {
                val useCase = NovelContent42kxsUseCase(url)
                useCase.execute(presenter)
            }
            SourceType.FPZW -> {
                val useCase = NovelContent4FpzwUseCase(url)
                useCase.execute(presenter)
            }
            SourceType.BQG -> {
                val useCase = NovelContent4BqgUseCase(url)
                useCase.execute(presenter)
            }
        }
    }

    private fun updateView(content: NovelContent?) {
        if (content == null) return
        tvTitle.text = content.title
        if (!TextUtils.isEmpty(content.content)) {
            tvContent.text = Html.fromHtml(content.content)
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_novel_content, this, true)
        viewMain = findViewById(R.id.fragment_novel_content_main)
        tvTitle = findViewById(R.id.fragment_novel_content_title)
        tvContent = findViewById(R.id.fragment_novel_content_val)
        loading = findViewById(R.id.fragment_novel_content_loading)
        viewError = findViewById(R.id.fragment_novel_content_error)
        val btnRetry = findViewById<View>(R.id.fragment_novel_content_error_btn)
        btnRetry.setOnClickListener { loadContent(novel) }
    }
}