package com.cabe.app.novel.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cabe.app.novel.R
import com.cabe.app.novel.activity.BaseActivity.Companion.KEY_EXTRA_GSON
import com.cabe.app.novel.domain.BaseViewModel
import com.cabe.app.novel.domain.HotNovelUseCase
import com.cabe.app.novel.domain.HotRank
import com.cabe.app.novel.domain.bqg.Search4BqgUseCase
import com.cabe.app.novel.domain.ekxs.Search42kxsUseCase
import com.cabe.app.novel.domain.x23us.Search4X23USUseCase
import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.widget.BaseAdapter
import com.cabe.app.novel.widget.BaseViewHolder
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.impl.SimpleViewPresenter
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search_hot.view.*
import kotlinx.android.synthetic.main.item_search_novel.view.*
import kotlinx.android.synthetic.main.layout_search_input.*

class SearchVM: BaseViewModel<List<NovelInfo>>() {
    var liveHots= MutableLiveData<List<HotRank>?>()
    fun search(keyWord: String) {
        search42kxs(keyWord)
    }
    private fun search42kxs(keyWord: String) {
        val searchUseCase = Search42kxsUseCase(keyWord)
        searchUseCase.execute(createPresenter {
            search4Bqg(keyWord)
            false
        })
    }

    private fun search4Bqg(keyWord: String) {
        val searchUseCase = Search4BqgUseCase(keyWord)
        searchUseCase.execute(createPresenter {
            search4DD(keyWord)
            false
        })
    }

    private fun search4DD(keyWord: String) {
        val searchUseCase = Search4X23USUseCase(keyWord)
        searchUseCase.execute(createPresenter())
    }

    fun loadHot() {
        HotNovelUseCase().execute(object: SimpleViewPresenter<List<HotRank>>() {
            override fun load(from: CacheSource?, data: List<HotRank>?) {
                super.load(from, data)
                liveHots.postValue(data)
            }
        })
    }
}
class SearchActivity: BaseActivity() {
    private val viewModel: SearchVM by viewModels()
    private lateinit var adapterSearch: SearchBookAdapter
    private lateinit var adapterHot: HotRankAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.layout_search_input)

        initView()
        viewModel.loadHot()
    }

    private fun initView() {
        searchView()?.apply {
            search_input.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    actionSearch()
                    true
                } else false
            }
            search_input.addTextChangedListener {
                val inputLen = it.toString().length
                showResult(inputLen > 0)
                search_btn.text = if(inputLen == 0) "返回" else "搜索"
            }
            search_btn.setOnClickListener {
                if(search_input.text.toString().isNotEmpty()) actionSearch()
                else finish()
            }
        }

        activity_search_swipe.isEnabled = false

        adapterSearch = SearchBookAdapter(this)
        adapterSearch.onItemClick = {
            addNovel(it)
        }
        activity_search_list.adapter = adapterSearch

        adapterHot = HotRankAdapter(this) {
            searchInputView()?.setText(it)
            searchInputView()?.postDelayed({
                actionSearch()
            }, 200)
        }
        activity_hot_recycler.adapter = adapterHot
        showResult(false)

        viewModel.liveResponse.observe(this) {
            adapterSearch.addData(it)
        }
        viewModel.liveError.observe(this) {
            it?.let { toast(it.msg) }
        }
        viewModel.liveComplete.observe(this) {
            handleSearchResult()
        }
        viewModel.liveHots.observe(this) {
            adapterHot.setData(it)
        }
    }

    private fun searchView(): View?= supportActionBar?.customView
    private fun searchInputView(): EditText?= searchView()?.findViewById(R.id.search_input)

    private fun hiddenKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchInputView()?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun showResult(show: Boolean) {
        if(show) {
            activity_search_list.visibility = View.VISIBLE
            activity_hot_recycler.visibility = View.GONE
        } else {
            adapterSearch.setData(null)
            activity_search_list.visibility = View.GONE
            activity_hot_recycler.visibility = View.VISIBLE
        }
    }

    private fun addNovel(novelInfo: NovelInfo) {
        val intent = Intent()
        intent.putExtra(KEY_EXTRA_GSON, novelInfo.toGson())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun actionSearch() {
        activity_search_swipe.isRefreshing = true
        hiddenKeyboard()
        adapterSearch.setData(null)
        val inputStr = searchInputView()?.text.toString()
        viewModel.search(inputStr)
    }

    private fun handleSearchResult() {
        activity_search_swipe.isRefreshing = false
        if(adapterSearch.itemCount == 0) {
            toast("找不到相关小说")
        }
    }

    override fun onBackPressed() {
        if(searchInputView()?.text?.toString()?.isNotEmpty() == true) {
            showResult(false)
            searchInputView()?.setText("")
        } else super.onBackPressed()
    }
}

class SearchResultContract: ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, SearchActivity::class.java)
    }
    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getStringExtra(KEY_EXTRA_GSON)
            else -> null
        }
    }
}

private class SearchBookAdapter(context: Context): BaseAdapter<NovelInfo>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_search_novel, parent, false)
        return SearchVH(itemView)
    }
}

private class SearchVH(itemView: View) : BaseViewHolder<NovelInfo>(itemView) {
    override fun onBindData(data: NovelInfo) {
        itemView.apply {
            Glide.with(context)
                .load(data.picUrl)
                .apply(RequestOptions().apply{
                    placeholder(R.drawable.pic_default_novel)
                    error(R.drawable.pic_default_novel)
                })
                .into(novel_cover)
            novel_title.text = "${data.title}(${data.source})"
            novel_author.text = "作者：${data.author ?: "--"}"
            novel_type.text = "类型：${data.type ?: "--"}"
            novel_update.text = "更新：${data.update ?: "--"}"
            novel_state.text = "(${data.state ?: "--"})"
            novel_chapter.text = "最新：${data.lastChapter ?: "--"}"
            novel_state.visibility = if (TextUtils.isEmpty(data.state)) View.GONE else View.VISIBLE
            novel_type.visibility = if (TextUtils.isEmpty(data.type)) View.GONE else View.VISIBLE
        }
    }
}

private class HotRankAdapter(context: Context, private val labelClick: (label: String) -> Unit): BaseAdapter<HotRank>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotRankVH {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_search_hot, parent, false)
        return HotRankVH(itemView, labelClick)
    }
}

private class HotRankVH(itemView: View, private val labelClick: (label: String) -> Unit): BaseViewHolder<HotRank>(itemView) {
    override fun onBindData(data: HotRank) {
        itemView.apply {
            hot_title.text = data.type
            hot_recycler.adapter = HotLabelAdapter(context, data.bookList).apply {
                onItemClick = {
                    labelClick(it)
                }
            }
        }
    }
}

private class HotLabelAdapter(context: Context, dataList: List<String>?): BaseAdapter<String>(context) {
    init {
        setData(dataList)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_search_hot_label, parent, false)
        return object: BaseViewHolder<String>(itemView) {
            override fun onBindData(data: String) {
                (itemView as TextView).text = data
                itemView.setOnClickListener {
                    onItemClick?.invoke(data)
                }
            }
        }
    }
}