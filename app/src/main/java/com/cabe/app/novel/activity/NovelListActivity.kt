package com.cabe.app.novel.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cabe.app.novel.R
import com.cabe.app.novel.domain.BaseViewModel
import com.cabe.app.novel.domain.bqg.NovelList4BqgUseCase
import com.cabe.app.novel.domain.fpzw.NovelList4FpzwUseCase
import com.cabe.app.novel.domain.ekxs.NovelList42KXSUseCase
import com.cabe.app.novel.domain.x23us.NovelList4X23USUseCase
import com.cabe.app.novel.model.NovelContent
import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.NovelList
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.utils.DiskUtils
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_novel_list.*

class ListViewModel: BaseViewModel<NovelList>() {
    private var useCase: HttpCacheUseCase<NovelList>? = null
    fun loadData(novelInfo: NovelInfo?) {
        useCase?.unsubscribe()
        useCase = when(novelInfo?.source) {
            SourceType.EKXS -> NovelList42KXSUseCase(novelInfo.url)
            SourceType.FPZW -> NovelList4FpzwUseCase(novelInfo.url)
            SourceType.BQG -> NovelList4BqgUseCase(novelInfo.url)
            else -> NovelList4X23USUseCase(novelInfo?.url)
        }
        useCase?.execute(createPresenter())
    }
}
class NovelListActivity : BaseActivity() {
    private val viewModel: ListViewModel by viewModels()
    private var keyNovelDetail = ""
    private var novelInfo: NovelInfo? = null
    private var lastContent: NovelContent? = null
    private val adapter = MyAdapter()
    private var flagReverse = true
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_FLAG_SORT_REVERSE, flagReverse)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_list)
        initView()
        bindData()
        loadNovelInfo()
    }

    override fun onResume() {
        super.onResume()
        val novelGson = DiskUtils.getData(keyNovelDetail)
        if (!TextUtils.isEmpty(novelGson)) {
            lastContent = Gson().fromJson(novelGson, NovelContent::class.java)
            adapter.updateLastInfo(lastContent)
        }
    }

    override fun initExtra(savedInstanceState: Bundle?) {
        novelInfo = getExtraGson(object : TypeToken<NovelInfo>() {})
        if (novelInfo != null) {
            keyNovelDetail = TAG + "#" + novelInfo?.title + "#" + novelInfo?.url
        }
        if (savedInstanceState != null) {
            flagReverse = savedInstanceState.getBoolean(KEY_FLAG_SORT_REVERSE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_novel_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_novel_detail_order -> {
                actionOrderReverse()
                true
            }
            R.id.menu_novel_detail_scroll_top -> {
                actionScrollTop()
                true
            }
            R.id.menu_novel_detail_location -> {
                actionLocationIndex()
                true
            }
            R.id.menu_novel_detail_search -> {
                actionSearch()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initView() {
        activity_novel_list_list.adapter = adapter
        val manager = GridLayoutManager(context, 2)
        activity_novel_list_list.layoutManager = manager
        activity_novel_list_swipe.setOnRefreshListener { loadNovelInfo() }
        if (novelInfo != null) {
            title = novelInfo!!.title
        }
    }

    private fun updateView(detail: NovelList?) {
        if (detail == null) return
        novelList = detail
        activity_novel_list_tips.text = detail.tips
        adapter.setData(detail.list)
        adapter.updateLastInfo(lastContent)
    }

    private fun bindData() {
        viewModel.liveResponse.observe(this, {
            updateView(it)
        })
        viewModel.liveError.observe(this, {
            it?.let { toast(it.msg) }
        })
        viewModel.liveComplete.observe(this, {
            activity_novel_list_swipe.isRefreshing = false
        })
    }

    private fun loadNovelInfo() {
        if (novelInfo != null) {
            activity_novel_list_swipe.isRefreshing = true
            viewModel.loadData(novelInfo)
        }
    }

    private fun gotoContent(content: NovelContent) {
        val intent = NovelContentActivity.create(context, content, keyNovelDetail)
        startActivity(intent)
    }

    private fun searchKey(key: String) {
        val position = adapter.indexPosition(key)
        if (position >= 0) {
            activity_novel_list_list.scrollToPosition(adapter.getRealPosition(position))
        }
    }

    private fun actionSearch() {
        val input = EditText(this)
        input.hint = "请输入章节号"
        AlertDialog.Builder(this)
                .setTitle("查找章节")
                .setView(input)
                .setPositiveButton("确定") { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    searchKey(input.text.toString())
                }
                .setNegativeButton("取消") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.show()
    }

    private fun actionOrderReverse() {
        flagReverse = !flagReverse
        adapter.notifyDataSetChanged()
    }

    private fun actionLocationIndex() {
        var curIndex: Int = adapter.curPosition
        val layoutManager = activity_novel_list_list.layoutManager as GridLayoutManager?
        val firstIndex = layoutManager!!.findFirstVisibleItemPosition()
        val lastIndex = layoutManager.findLastVisibleItemPosition()
        val visibilityCount = lastIndex - firstIndex
        val spanCount = layoutManager.spanCount
        if (curIndex < adapter.itemCount - visibilityCount) {
            if (flagReverse) {
                curIndex -= visibilityCount - spanCount
            } else {
                curIndex += visibilityCount - spanCount
            }
        }
        if (curIndex >= 0) {
            activity_novel_list_list.scrollToPosition(adapter.getRealPosition(curIndex))
        }
    }

    private fun actionScrollTop() {
        activity_novel_list_list.scrollToPosition(0)
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyHolder>() {
        private var lastContent: NovelContent? = null
        private var data: List<NovelContent>? = null
        fun updateLastInfo(lastContent: NovelContent?) {
            if (containNovel(lastContent)) {
                this.lastContent = lastContent
                lastContent!!.flagLast = true
                notifyDataSetChanged()
            }
        }

        fun indexPosition(key: String): Int {
            var index = 0
            if (data != null) {
                for (content in data!!) {
                    if (content.title?.contains(key) == true) {
                        break
                    }
                    index++
                }
            }
            return index
        }

        val curPosition: Int
            get() {
                var cur = -1
                if (data != null && lastContent != null) {
                    cur = data!!.lastIndexOf(lastContent!!)
                }
                return cur
            }

        fun setData(data: List<NovelContent>?) {
            this.data = data
            notifyDataSetChanged()
        }

        private fun getItemData(position: Int): NovelContent? {
            var index = position
            if (lastContent != null) {
                if (index == 0) {
                    return lastContent
                }
                index--
            }
            if (index < 0 || index >= realItemCount) return null
            index = getRealPosition(index)
            return data!![index]
        }

        private fun containNovel(content: NovelContent?): Boolean {
            return data != null && content != null && data!!.indexOf(content) >= 0
        }

        fun getRealPosition(position: Int): Int {
            return if (flagReverse) realItemCount - position - 1 else position
        }

        private val realItemCount: Int
            get() = if (data == null) 0 else data!!.size

        override fun getItemCount(): Int {
            var dataSize = realItemCount
            if (lastContent != null) {
                dataSize++
            }
            return dataSize
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_novel_list_info, parent, false)
            return MyHolder(itemView)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyHolder, position: Int) {
            val content = getItemData(position) ?: return
            var tips = ""
            if (content.flagLast) {
                tips = "(继续)"
            }
            holder.title.text = tips + content.title
            holder.title.setTextColor(if (content == lastContent) -0xae1af else -0xcbb776)
            holder.itemView.setOnClickListener {
                DiskUtils.saveData(keyNovelDetail, content.toGson())
                gotoContent(content)
            }
        }
    }

    private class MyHolder(itemView: View) : ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.item_novel_list_info)
    }

    companion object {
        private const val KEY_FLAG_SORT_REVERSE = "keyFlagSortReverse"
        var novelList: NovelList? = null
        fun create(context: Context?, novelInfo: NovelInfo?): Intent {
            val intent = Intent(context, NovelListActivity::class.java)
            if (novelInfo != null) {
                intent.putExtra(KEY_EXTRA_GSON, novelInfo.toGson())
            }
            return intent
        }
    }
}