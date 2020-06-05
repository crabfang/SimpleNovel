package com.cabe.app.novel.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.cabe.app.novel.BuildConfig
import com.cabe.app.novel.R
import com.cabe.app.novel.domain.LocalNovelsUseCase
import com.cabe.app.novel.domain.UpdateUseCase
import com.cabe.app.novel.domain.ekxs.NovelDetail42kxsUseCase
import com.cabe.app.novel.domain.ekxs.NovelList42KXSUseCase
import com.cabe.app.novel.domain.ekxs.Search42kxsUseCase
import com.cabe.app.novel.domain.x23us.NovelList4X23USUseCase
import com.cabe.app.novel.domain.x23us.Search4X23USUseCase
import com.cabe.app.novel.model.LocalNovelList
import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.NovelList
import com.cabe.app.novel.model.SourceType
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.ViewPresenter
import com.cabe.lib.cache.interactor.impl.SimpleViewPresenter
import com.google.gson.Gson
import com.pgyersdk.feedback.PgyerFeedbackManager
import com.pgyersdk.feedback.PgyerFeedbackManager.PgyerFeedbackBuilder
import com.pgyersdk.update.DownloadFileListener
import com.pgyersdk.update.PgyUpdateManager
import com.pgyersdk.update.UpdateManagerListener
import com.pgyersdk.update.javabean.AppBean
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_novel_list.*
import java.io.File
import java.util.*

private const val REQUEST_CODE_RANK = 0x101
class HomeActivity : BaseActivity() {
    private var localNovelList: LocalNovelList? = null
    private val useCase = LocalNovelsUseCase()
    private lateinit var searchInput: EditText
    private lateinit var localSwipe: SwipeRefreshLayout
    private lateinit var recyclerSearch: RecyclerView
    private val adapter = MyAdapter()
    private val adapterSearch = MyAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setBackable(false)
        initView()
        title = "简易小说"
        loadLocal(false)
        checkUpdate(false)
    }

    override fun onResume() {
        super.onResume()
        loadLocal(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_novel_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_novel_home_feedback -> {
                PgyerFeedbackBuilder()
                        .setShakeInvoke(false) //fasle 则不触发摇一摇，最后需要调用 invoke 方法
                        // true 设置需要调用 register 方法使摇一摇生效
                        .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE) //设置以Dialog 的方式打开
                        .setColorDialogTitle("#FF0000") //设置Dialog 标题的字体颜色，默认为颜色为#ffffff
                        .setColorTitleBg("#FF0000") //设置Dialog 标题栏的背景色，默认为颜色为#2E2D2D
                        .setBarBackgroundColor("#FF0000") // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                        .setBarButtonPressedColor("#FF0000") //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                        .setColorPickerBackgroundColor("#FF0000") //设置颜色选择器的背景色,默认颜色为 #272828
                        .setMoreParam("KEY1", "VALUE1") //自定义的反馈数据
                        .setMoreParam("KEY2", "VALUE2") //自定义的反馈数据
                        .builder()
                        .invoke()
                return true
            }
            R.id.menu_novel_home_check_update -> checkUpdate(true)
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
        localSwipe.setOnRefreshListener { loadLocal(false) }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_CODE_RANK) {
            val jsonStr = data!!.getStringExtra(KEY_EXTRA_GSON)
            if (!TextUtils.isEmpty(jsonStr)) {
                val novelInfo = Gson().fromJson(jsonStr, NovelInfo::class.java)
                addLocalNovel(novelInfo)
            }
        }
    }

    private fun checkUpdate(showTips: Boolean) {
        UpdateUseCase().execute(object : SimpleViewPresenter<AppBean>() {
            override fun error(from: CacheSource, code: Int, info: String) {
                if (showTips) {
                    Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
                }
            }

            override fun load(from: CacheSource, data: AppBean) {
                showUpdateInfo(data)
            }
        })
    }

    private fun showUpdateInfo(appBean: AppBean?) {
        if (appBean == null) return
        AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage(appBean.releaseNote)
                .setPositiveButton("更新") { dialog: DialogInterface, which: Int ->
                    PgyUpdateManager.Builder().setDownloadFileListener(object : DownloadFileListener {
                        override fun downloadFailed() {
                            toast("下载失败")
                        }
                        override fun downloadSuccessful(file: File) {
                            UpdateUseCase.updateUpdateBuild()
                            toast("下载成功")
                            Log.d(TAG, "downloadSuccessful : $file")
                            actionInstallApp(Uri.fromFile(file))
                        }
                        override fun onProgressUpdate(vararg args: Int?) {
                            Log.d(TAG, "onProgressUpdate : " + args[0])
                        }
                    }).setUpdateManagerListener(object : UpdateManagerListener {
                        override fun onNoUpdateAvailable() {}
                        override fun onUpdateAvailable(appBean1: AppBean) {}
                        override fun checkUpdateFailed(e: Exception) {}
                    }).setDeleteHistroyApk(true).register()
                    PgyUpdateManager.downLoadApk(appBean.downloadURL)
                    dialog.dismiss()
                    toast("开始下载")
                }
                .setNegativeButton("取消") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.show()
    }

    private fun showSearchView(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        recyclerSearch.visibility = visibility
        activity_home_search_btn_close.visibility = visibility
    }

    private fun actionInstallApp(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val filePath = uri.toString().substring(7)
            val apkFile = File(filePath)
            val contentUri = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        }
        startActivity(intent)
    }

    private fun hiddenKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchInput.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun loadLocal(silent: Boolean) {
        if (!silent) {
            updateRemoteData()
            localSwipe.isRefreshing = true
        }
        useCase.execute(object : ViewPresenter<LocalNovelList> {
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }
            override fun load(from: CacheSource, data: LocalNovelList?) {
                localNovelList = data
                updateLocalNovel()
            }
            override fun complete(from: CacheSource) {
                localSwipe.isRefreshing = false
            }
        })
    }

    private fun updateLocalNovel() {
        if (localNovelList != null) {
            adapter.setData(localNovelList?.list)
        }
    }

    private fun updateRemoteData() {
        localNovelList?.list?.forEach {
            val presenter: ViewPresenter<NovelList> = object : ViewPresenter<NovelList> {
                override fun load(from: CacheSource, data: NovelList?) {
                }
                override fun error(from: CacheSource, code: Int, info: String) {
                }
                override fun complete(from: CacheSource) {
                }
            }
            if (it?.source == SourceType.X23US) {
                NovelList4X23USUseCase(it.url).execute(presenter)
            } else if (it!!.source == SourceType.EKXS) {
                NovelList42KXSUseCase(it.url).execute(presenter)
            }
        }
    }

    private fun addLocalNovel(novelInfo: NovelInfo?) {
        if (localNovelList == null) {
            localNovelList = LocalNovelList()
        }
        localNovelList!!.addNovel(novelInfo)
        useCase.saveCacheDisk(localNovelList)
        updateLocalNovel()
    }

    private fun removeLocalNovel(novelInfo: NovelInfo) {
        if (localNovelList != null && !localNovelList!!.isEmpty) {
            localNovelList!!.removeNovel(novelInfo)
            useCase.saveCacheDisk(localNovelList)
            updateLocalNovel()
        }
    }

    private fun setTopNovel(novelInfo: NovelInfo) {
        if (localNovelList != null && !localNovelList!!.isEmpty) {
            localNovelList!!.setTop(novelInfo)
            useCase.saveCacheDisk(localNovelList)
            updateLocalNovel()
        }
    }

    fun onClose(view: View?) {
        adapterSearch.setData(null)
        showSearchView(false)
    }

    fun onSearch(view: View?) {
        waiting!!.show()
        hiddenKeyboard()
        val inputStr = searchInput.text.toString()
        search4x23us(inputStr)
    }

    fun onRank(view: View?) {
        startActivityForResult(Intent(this, RankActivity::class.java), REQUEST_CODE_RANK)
    }

    private fun search4x23us(keyWord: String) {
        val searchUseCase = Search4X23USUseCase(keyWord)
        searchUseCase.execute(object : ViewPresenter<List<NovelInfo>> {
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }

            override fun load(from: CacheSource, data: List<NovelInfo>) {
                if (data.isNotEmpty()) {
                    adapterSearch.setData(data)
                    showSearchView(true)
                }
            }

            override fun complete(from: CacheSource) {
                search42kxs(keyWord)
            }
        })
    }

    private fun search42kxs(keyWord: String) {
        val searchUseCase = Search42kxsUseCase(keyWord)
        searchUseCase.execute(object : ViewPresenter<List<NovelInfo>> {
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }
            override fun load(from: CacheSource, data: List<NovelInfo>) {
                if (data.isNotEmpty()) {
                    adapterSearch.addData(data)
                    showSearchView(true)
                }
            }
            override fun complete(from: CacheSource) {
                waiting!!.dismiss()
            }
        })
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyHolder>() {
        private var listener: AdapterClickListener? = null
        private var novelList: MutableList<NovelInfo>? = null
        fun setItemClickListener(listener: AdapterClickListener) {
            this.listener = listener
        }

        fun setData(list: List<NovelInfo>?) {
            novelList = list?.toMutableList()
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
            Glide.with(holder.itemView.context).load(itemData.picUrl).into(holder.pic)
            holder.tvTitle.text = itemData.title
            holder.tvAuthor.text = "作者：${itemData.author}"
            holder.tvType.text = "类型：${itemData.type}"
            holder.tvUpdate.text = "更新：${itemData.update}"
            holder.tvState.text = "状态：${itemData.state}"
            holder.tvSource.text = "来源：${itemData.source}"
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
        val pic: ImageView = itemView.findViewById(R.id.item_home_local_novel_pic)
        val tvTitle = itemView.findViewById<TextView>(R.id.item_home_local_novel_title)
        val tvAuthor = itemView.findViewById<TextView>(R.id.item_home_local_novel_author)
        val tvType = itemView.findViewById<TextView>(R.id.item_home_local_novel_type)
        val tvState = itemView.findViewById<TextView>(R.id.item_home_local_novel_state)
        val tvSource = itemView.findViewById<TextView>(R.id.item_home_local_novel_source)
        val tvUpdate = itemView.findViewById<TextView>(R.id.item_home_local_novel_update)

    }

    private interface AdapterClickListener {
        fun itemOnClick(novelInfo: NovelInfo?)
        fun itemOnLongClick(novelInfo: NovelInfo)
    }
}