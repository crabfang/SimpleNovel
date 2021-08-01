package com.cabe.app.novel.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cabe.app.novel.R
import com.cabe.app.novel.domain.BaseViewModel
import com.cabe.app.novel.domain.LocalNovelsUseCase
import com.cabe.app.novel.domain.bqg.NovelList4BqgUseCase
import com.cabe.app.novel.domain.bqg.Search4BqgUseCase
import com.cabe.app.novel.domain.fpzw.NovelList4FpzwUseCase
import com.cabe.app.novel.domain.ekxs.NovelList42KXSUseCase
import com.cabe.app.novel.domain.ekxs.Search42kxsUseCase
import com.cabe.app.novel.domain.x23us.NovelList4X23USUseCase
import com.cabe.app.novel.model.LocalNovelList
import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.NovelList
import com.cabe.app.novel.model.SourceType
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.ViewPresenter
import com.cabe.lib.cache.interactor.impl.SimpleViewPresenter
import com.google.gson.Gson
import com.pgyer.pgyersdk.PgyerSDKManager
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_home_local_novel.view.*
import java.util.*

class HomeViewModel: BaseViewModel<LocalNovelList>() {
    private var useCase: LocalNovelsUseCase? = null
    fun loadData() {
        useCase?.unsubscribe()

        useCase = LocalNovelsUseCase()
        useCase?.execute(createPresenter())
    }

    fun saveData(newData: LocalNovelList?) {
        newData?.let {
            useCase?.saveCacheDisk(newData)
        }
    }
}
class HomeActivity : BaseActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private var localNovelList: LocalNovelList? = null
    private lateinit var searchInput: EditText
    private lateinit var localSwipe: SwipeRefreshLayout
    private lateinit var recyclerSearch: RecyclerView
    private val adapter = MyAdapter()
    private val adapterSearch = MyAdapter()
    private var flagRemote = true
    private val rankLauncher = registerForActivityResult(RankResultContract()) { jsonStr ->
        if (!TextUtils.isEmpty(jsonStr)) {
            val novelInfo = Gson().fromJson(jsonStr, NovelInfo::class.java)
            addLocalNovel(novelInfo)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setBackable(false)
        initView()
        bindData()
        title = "简易小说"
        loadLocal(false)
        checkUpdate()
    }

    override fun onResume() {
        super.onResume()
        flagRemote = true
        loadLocal(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_novel_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_novel_home_rank -> actionRank()
            R.id.menu_novel_home_check_update -> checkUpdate()
            R.id.menu_novel_home_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        searchInput = findViewById(R.id.activity_home_search_input)
        localSwipe = findViewById(R.id.activity_home_local_swipe)
        localSwipe.setOnRefreshListener {
            remoteUpdateCount = 0
            loadLocal(false)
        }
        activity_home_local_list.adapter = adapter
        adapter.setItemClickListener(object : AdapterClickListener {
            override fun itemOnClick(novelInfo: NovelInfo?) {
                val intent = NovelListActivity.create(context, novelInfo)
                startActivity(intent)
            }
            override fun itemOnLongClick(novelInfo: NovelInfo) {
                val builder = AlertDialog.Builder(context!!)
                builder.setTitle(novelInfo.title)
                builder.setItems(arrayOf("置顶", "删除")
                ) { dialog: DialogInterface, which: Int ->
                    when (which) {
                        0 -> setTopNovel(novelInfo)
                        1 -> removeLocalNovel(novelInfo)
                    }
                    dialog.dismiss()
                }
                builder.create().show()
            }
        })
        recyclerSearch = findViewById(R.id.activity_home_search_list)
        recyclerSearch.adapter = adapterSearch
        adapterSearch.setItemClickListener(object : AdapterClickListener {
            override fun itemOnClick(novelInfo: NovelInfo?) {
                addLocalNovel(novelInfo)
                recyclerSearch.smoothScrollToPosition(0)
                showSearchView(false)
                searchInput.setText("")
            }

            override fun itemOnLongClick(novelInfo: NovelInfo) {}
        })
    }

    private fun bindData() {
        viewModel.liveResponse.observe(this, { data ->
            localNovelList = data
            updateLocalNovel()
            if(flagRemote) {
                flagRemote = false
                updateRemoteData()
            }
        })
        viewModel.liveComplete.observe(this, {
            localSwipe.isRefreshing = false
        })
    }

    private fun actionRank() {
        rankLauncher.launch(null)
    }

    private fun checkUpdate() {
        PgyerSDKManager.checkSoftwareUpdate(this)
    }

    private fun showSearchView(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        recyclerSearch.visibility = visibility
        activity_home_search_btn_close.visibility = visibility
    }

    private fun hiddenKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchInput.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun loadLocal(silent: Boolean) {
        if (!silent) {
            localSwipe.isRefreshing = true
        }
        viewModel.loadData()
    }

    private fun updateLocalNovel() {
        if (localNovelList != null) {
            adapter.setData(localNovelList?.list)
        }
    }

    private var remoteUpdateCount = 0
    private fun updateRemoteData() {
        var needReload = false
        localNovelList?.list?.forEach {
            if(it.url?.contains(SourceType.EKXS.host) == true && it.source != SourceType.EKXS) {
                LocalNovelsUseCase.updateLocalSource(it.url, SourceType.EKXS)
                needReload = true
            } else if(it.url?.contains(SourceType.FPZW.host) == true && it.source != SourceType.FPZW) {
                LocalNovelsUseCase.updateLocalSource(it.url, SourceType.FPZW)
                needReload = true
            } else if(it.source == SourceType.BQG && it.url?.contains(SourceType.BQG.host) == false) {
                LocalNovelsUseCase.updateLocalHost(it.url, SourceType.BQG.host)
                needReload = true
            }
            val presenter: ViewPresenter<NovelList> = object : SimpleViewPresenter<NovelList>() {
                override fun complete(from: CacheSource) {
                    remoteUpdateCount ++
                    if(remoteUpdateCount == localNovelList?.list?.size ?: -1) {
                        loadLocal(true)
                    }
                }
            }
            when (it.source) {
                SourceType.EKXS -> NovelList42KXSUseCase(it.url).execute(presenter)
                SourceType.X23US -> NovelList4X23USUseCase(it.url).execute(presenter)
                SourceType.FPZW -> NovelList4FpzwUseCase(it.url).execute(presenter)
                SourceType.BQG -> NovelList4BqgUseCase(it.url).execute(presenter)
            }
        }
        if(needReload) loadLocal(true)
    }

    private fun addLocalNovel(novelInfo: NovelInfo?) {
        if (localNovelList == null) {
            localNovelList = LocalNovelList()
        }
        localNovelList?.addNovel(novelInfo)
        viewModel.saveData(localNovelList)
        updateLocalNovel()
    }

    private fun removeLocalNovel(novelInfo: NovelInfo) {
        if (localNovelList?.isEmpty == false) {
            localNovelList?.removeNovel(novelInfo)
            viewModel.saveData(localNovelList)
            updateLocalNovel()
        }
    }

    private fun setTopNovel(novelInfo: NovelInfo) {
        if (localNovelList?.isEmpty == false) {
            localNovelList?.setTop(novelInfo)
            viewModel.saveData(localNovelList)
            updateLocalNovel()
        }
    }

    fun onClose(view: View?) {
        searchInput.setText("")
        adapterSearch.setData(null)
        showSearchView(false)
    }

    fun onSearch(view: View?) {
        waiting!!.show()
        hiddenKeyboard()
        searchList.clear()
        adapterSearch.setData(null)
        val inputStr = searchInput.text.toString()
        search42kxs(inputStr)
    }

    private var searchList = mutableListOf<NovelInfo>()
    private fun search42kxs(keyWord: String) {
        val searchUseCase = Search42kxsUseCase(keyWord)
        searchUseCase.execute(object : ViewPresenter<List<NovelInfo>> {
            override fun load(from: CacheSource, data: List<NovelInfo>) {
                if (data.isNotEmpty()) {
                    searchList.addAll(data)
                    adapterSearch.addData(data)
                    showSearchView(true)
                }
            }
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }
            override fun complete(from: CacheSource) {
                search4Bqg(keyWord)
            }
        })
    }

    private fun search4Bqg(keyWord: String) {
        val searchUseCase = Search4BqgUseCase(keyWord)
        searchUseCase.execute(object : ViewPresenter<List<NovelInfo>> {
            override fun load(from: CacheSource, data: List<NovelInfo>) {
                if (data.isNotEmpty()) {
                    searchList.addAll(data)
                    adapterSearch.addData(data)
                    showSearchView(true)
                }
            }
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }
            override fun complete(from: CacheSource) {
                handleSearchResult()
            }
        })
    }

    private fun handleSearchResult() {
        waiting!!.dismiss()
        if(searchList.isEmpty()) {
            toast("找不到相关小说")
        }
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyHolder>() {
        private var listener: AdapterClickListener? = null
        private var novelList: MutableList<NovelInfo>? = null
        fun setItemClickListener(listener: AdapterClickListener) {
            this.listener = listener
        }
        fun setData(list: List<NovelInfo>?) {
            novelList?.clear()
            list?.let {
                novelList = it.toMutableList()
            }
            notifyDataSetChanged()
        }
        fun addData(list: List<NovelInfo>?) {
            if (novelList == null) {
                novelList = ArrayList()
            }
            if (list != null) {
                novelList?.addAll(list)
            }
            notifyDataSetChanged()
        }
        override fun getItemCount(): Int {
            return if (novelList == null || novelList!!.isEmpty()) 0 else novelList!!.size
        }
        private fun getItemData(index: Int): NovelInfo? {
            if (novelList == null || novelList!!.isEmpty()) return null
            return if (index < 0 || index >= novelList!!.size) null else novelList!![index]
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_home_local_novel, parent, false)
            return MyHolder(itemView)
        }
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyHolder, position: Int) {
            val itemData = getItemData(position) ?: return
            Glide.with(holder.itemView.context)
                    .load(itemData.picUrl)
                    .apply(RequestOptions().apply{
                        placeholder(R.drawable.pic_default_novel)
                        error(R.drawable.pic_default_novel)
                    })
                    .into(holder.pic)
            holder.tvTitle.text = itemData.title
            holder.tvAuthor.text = "作者：${itemData.author ?: "--"}"
            holder.tvType.text = "类型：${itemData.type ?: "--"}"
            holder.tvUpdate.text = "更新：${itemData.update ?: "--"}"
            holder.tvState.text = "(${itemData.state ?: "--"})"
            holder.tvSource.text = "来源：${itemData.source ?: "--"}"
            holder.tvChapter.text = "章节：${itemData.lastChapter ?: "--"}"
            holder.tvType.visibility = if (TextUtils.isEmpty(itemData.type)) View.GONE else View.VISIBLE
            holder.itemView.setOnClickListener {
                if (listener != null) {
                    listener!!.itemOnClick(itemData)
                }
            }
            holder.itemView.setOnLongClickListener {
                if (listener != null) {
                    listener!!.itemOnLongClick(itemData)
                }
                false
            }
        }
    }

    private class MyHolder(itemView: View) : ViewHolder(itemView) {
        val pic: ImageView = itemView.item_home_local_novel_pic
        val tvTitle: TextView = itemView.item_home_local_novel_title
        val tvAuthor: TextView = itemView.item_home_local_novel_author
        val tvType: TextView = itemView.item_home_local_novel_type
        val tvState: TextView = itemView.item_home_local_novel_state
        val tvSource: TextView = itemView.item_home_local_novel_source
        val tvUpdate: TextView = itemView.item_home_local_novel_update
        val tvChapter: TextView = itemView.item_home_local_novel_chapter
    }

    private interface AdapterClickListener {
        fun itemOnClick(novelInfo: NovelInfo?)
        fun itemOnLongClick(novelInfo: NovelInfo)
    }
}