package com.cabe.app.novel.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cabe.app.novel.R
import com.cabe.app.novel.domain.BaseViewModel
import com.cabe.app.novel.domain.LocalNovelsUseCase
import com.cabe.app.novel.domain.bqg.NovelList4BqgUseCase
import com.cabe.app.novel.domain.fpzw.NovelList4FpzwUseCase
import com.cabe.app.novel.domain.x23us.NovelList4X23USUseCase
import com.cabe.app.novel.model.*
import com.cabe.app.novel.utils.DiskUtils
import com.cabe.app.novel.widget.BaseAdapter
import com.cabe.app.novel.widget.BaseViewHolder
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.ViewPresenter
import com.cabe.lib.cache.interactor.impl.SimpleViewPresenter
import com.google.gson.Gson
import com.pgyer.pgyersdk.PgyerSDKManager
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_home_novel.view.*

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
class HomeActivity: BaseActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private var localNovelList: LocalNovelList? = null
    private lateinit var localSwipe: SwipeRefreshLayout
    private lateinit var adapter: HomeBookAdapter
    private var flagRemote = true
    private val searchLauncher = registerForActivityResult(SearchResultContract()) { jsonStr ->
        if (!TextUtils.isEmpty(jsonStr)) {
            val novelInfo = Gson().fromJson(jsonStr, NovelInfo::class.java)
            addLocalNovel(novelInfo)
        }
    }
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
            R.id.menu_novel_home_search -> actionSearch()
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
        localSwipe = findViewById(R.id.activity_home_local_swipe)
        localSwipe.setOnRefreshListener {
            flagRemote = true
            loadLocal(false)
        }

        adapter = HomeBookAdapter(this)
        adapter.onItemClick = {
            val intent = NovelListActivity.create(context, it)
            startActivity(intent)
        }
        adapter.onItemLongClick = {
            val builder = AlertDialog.Builder(this@HomeActivity)
            builder.setTitle(it.title)
            builder.setItems(arrayOf("置顶", "删除")
            ) { dialog: DialogInterface, which: Int ->
                when (which) {
                    0 -> setTopNovel(it)
                    1 -> removeLocalNovel(it)
                }
                dialog.dismiss()
            }
            builder.create().show()
            true
        }
        activity_home_local_list.adapter = adapter
    }

    private fun bindData() {
        viewModel.liveResponse.observe(this) { data ->
            localNovelList = data
            updateLocalNovel()
            if (flagRemote) {
                flagRemote = false
                updateRemoteData()
            }
        }
        viewModel.liveComplete.observe(this) {
            localSwipe.isRefreshing = false
        }
    }

    private fun actionSearch() {
        searchLauncher.launch(null)
    }

    private fun actionRank() {
        rankLauncher.launch(null)
    }

    private fun checkUpdate() {
        PgyerSDKManager.checkSoftwareUpdate(this)
    }

    private fun loadLocal(silent: Boolean) {
        if (!silent) {
            localSwipe.isRefreshing = true
        }
        viewModel.loadData()
    }

    private fun updateLocalNovel() {
        if (localNovelList != null) {
            localNovelList?.list?.forEach {
                val key = "NovelListActivity#" + it.title + "#" + it.url
                val novelGson = DiskUtils.getData(key)
                if (!TextUtils.isEmpty(novelGson)) {
                    val cacheData = Gson().fromJson(novelGson, NovelContent::class.java)
                    it.readChapter = cacheData.title
                }
            }
            adapter.setData(localNovelList?.list)
        }
    }

    private var remoteUpdateCount = 0
    private fun updateRemoteData() {
        var needReload = false
        remoteUpdateCount = 0
        localNovelList?.list?.forEach {
            if(it.url?.contains(SourceType.FPZW.host) == true && it.source != SourceType.FPZW) {
                LocalNovelsUseCase.updateLocalSource(it.url, SourceType.FPZW)
                needReload = true
            } else if(it.source == SourceType.BQG && it.url?.contains(SourceType.BQG.host) == false) {
                LocalNovelsUseCase.updateLocalHost(it.url, SourceType.BQG.host)
                needReload = true
            } else if(it.source == SourceType.X23US && it.url?.contains(SourceType.X23US.host) == false) {
                LocalNovelsUseCase.updateLocalHost(it.url, SourceType.X23US.host)
                needReload = true
            }
            val presenter: ViewPresenter<NovelList> = object : SimpleViewPresenter<NovelList>() {
                override fun complete(from: CacheSource) {
                    remoteUpdateCount ++
                    if(remoteUpdateCount == (localNovelList?.list?.size ?: -1)) {
                        loadLocal(true)
                    }
                }
            }
            when (it.source) {
                SourceType.X23US -> NovelList4X23USUseCase(it.url).execute(presenter)
                SourceType.FPZW -> NovelList4FpzwUseCase(it.url).execute(presenter)
                SourceType.BQG -> NovelList4BqgUseCase(it.url).execute(presenter)
                else -> {}
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
}

private class HomeBookAdapter(context: Context): BaseAdapter<NovelInfo>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookVH {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_home_novel, parent, false)
        return BookVH(itemView)
    }
}

private class BookVH(itemView: View) : BaseViewHolder<NovelInfo>(itemView) {
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
            novel_read.text = "已读：${data.readChapter ?: "--"}"
            novel_chapter.text = "最新：${data.lastChapter ?: "--"}"
            novel_read.visibility = if (TextUtils.isEmpty(data.readChapter)) View.GONE else View.VISIBLE
            novel_state.visibility = if (TextUtils.isEmpty(data.state)) View.GONE else View.VISIBLE
            novel_type.visibility = if (TextUtils.isEmpty(data.type)) View.GONE else View.VISIBLE
        }
    }
}