package com.cabe.app.novel.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cabe.app.novel.R
import com.cabe.app.novel.activity.BaseActivity.Companion.KEY_EXTRA_GSON
import com.cabe.app.novel.domain.fpzw.NovelDetail4FpzwUseCase
import com.cabe.app.novel.domain.fpzw.Rank4FpzwUseCase
import com.cabe.app.novel.model.NovelInfo
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.ViewPresenter
import kotlinx.android.synthetic.main.activity_rank_list.*
import kotlinx.android.synthetic.main.include_rank_list_type.*

class RankActivity : BaseActivity() {
    private var myAdapter: MyAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank_list)
        title = "排行榜"
        initView()
        changeType(0)
    }

    private fun initView() {
        activity_rank_list_type.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int -> changeType(checkedId) }
        activity_rank_list_swipe.setOnRefreshListener { changeType(activity_rank_list_type.checkedRadioButtonId) }
        activity_rank_list_recycler.layoutManager = GridLayoutManager(this, 3)
        myAdapter = MyAdapter()
        activity_rank_list_recycler.adapter = myAdapter
    }

    private fun changeType(checkedId: Int) {
        var sort = "list/1-1.html"
        when (checkedId) {
            R.id.activity_rank_list_type_xh -> sort = "list/1-1.html"
            R.id.activity_rank_list_type_wx -> sort = "list/2-1.html"
            R.id.activity_rank_list_type_yq -> sort = "list/3-1.html"
            R.id.activity_rank_list_type_ls -> sort = "list/4-1.html"
            R.id.activity_rank_list_type_wy -> sort = "list/5-1.html"
            R.id.activity_rank_list_type_kh -> sort = "list/6-1.html"
            R.id.activity_rank_list_type_kb -> sort = "list/7-1.html"
            R.id.activity_rank_list_type_qt -> sort = "list/8-1.html"
        }
        loadRank(sort)
    }

    private fun loadRank(sort: String) {
        activity_rank_list_recycler.scrollToPosition(0)
        activity_rank_list_swipe.isRefreshing = true
        val useCase = Rank4FpzwUseCase(sort)
        useCase.execute(object : ViewPresenter<List<NovelInfo>> {
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }
            override fun load(from: CacheSource, data: List<NovelInfo>) {
                myAdapter?.setData(data)
            }
            override fun complete(from: CacheSource) {
                activity_rank_list_swipe.isRefreshing = false
            }
        })
    }

    private fun queryNovel(url: String?) {
        waiting?.show()
        val useCase = NovelDetail4FpzwUseCase(url)
        useCase.execute(object : ViewPresenter<NovelInfo> {
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
            }
            override fun load(from: CacheSource, data: NovelInfo) {
                operateNovel(data)
            }
            override fun complete(from: CacheSource) {
                waiting!!.dismiss()
            }
        })
    }

    private fun operateNovel(novelInfo: NovelInfo) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(novelInfo.title)
        builder.setItems(arrayOf("添加书库", "取消")
        ) { dialog: DialogInterface, which: Int ->
            if (which == 0) {
                addNovel(novelInfo)
            }
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun addNovel(novelInfo: NovelInfo) {
        val intent = Intent()
        intent.putExtra(KEY_EXTRA_GSON, novelInfo.toGson())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyHolder>() {
        private var novelList: List<NovelInfo?>? = null
        fun setData(list: List<NovelInfo?>?) {
            novelList = list
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
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_rank_list_novel, parent, false)
            return MyHolder(itemView)
        }

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
            holder.tvAuthor.text = "作者：${itemData.author}"
            holder.itemView.setOnClickListener { queryNovel(itemData.url) }
        }
    }

    private class MyHolder(itemView: View) : ViewHolder(itemView) {
        val pic: ImageView = itemView.findViewById(R.id.item_rank_list_novel_pic)
        val tvTitle: TextView = itemView.findViewById(R.id.item_rank_list_novel_title)
        val tvAuthor: TextView = itemView.findViewById(R.id.item_rank_list_novel_author)
    }
}

class RankResultContract: ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, RankActivity::class.java)
    }
    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getStringExtra(KEY_EXTRA_GSON)
            else -> null
        }
    }

}