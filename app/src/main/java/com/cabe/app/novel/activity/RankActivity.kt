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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cabe.app.novel.MyApp
import com.cabe.app.novel.R
import com.cabe.app.novel.activity.BaseActivity.Companion.KEY_EXTRA_GSON
import com.cabe.app.novel.domain.fpzw.Rank4FpzwUseCase
import com.cabe.app.novel.model.NovelInfo
import com.cabe.lib.cache.CacheSource
import com.cabe.lib.cache.interactor.ViewPresenter
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.activity_rank_list.*

data class RankTab(val title: String, val path: String): CustomTabEntity {
    override fun getTabTitle(): String= title
    override fun getTabSelectedIcon(): Int= 0
    override fun getTabUnselectedIcon(): Int= 0
}
object RankCacheVM: AndroidViewModel(MyApp.instance) {
    private var liveMap = mutableMapOf<RankTab, MutableLiveData<List<NovelInfo>?>>()
    fun post(tab: RankTab, data: List<NovelInfo>?) {
        getLive(tab).postValue(data)
    }
    fun getLive(tab: RankTab): MutableLiveData<List<NovelInfo>?> {
        if(liveMap.contains(tab).not()) liveMap[tab] = MutableLiveData<List<NovelInfo>?>()
        return liveMap[tab]!!
    }
}
class RankActivity : BaseActivity() {
    private var myAdapter: MyAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank_list)
        title = "排行榜"
        initView()
    }

    private fun initView() {
        activity_rank_recycler.layoutManager = GridLayoutManager(this, 3)
        myAdapter = MyAdapter()
        activity_rank_recycler.adapter = myAdapter

        val tabList: ArrayList<RankTab> = arrayListOf(
            RankTab("玄幻", "sort1/"),
            RankTab("武侠", "sort2/"),
            RankTab("都市", "sort3/"),
            RankTab("言情", "sort4/"),
            RankTab("历史", "sort5/"),
            RankTab("网游", "sort6/"),
            RankTab("科幻", "sort7/"),
            RankTab("侦探", "sort8/"),
            RankTab("同人", "sort9/"),
            RankTab("悬疑", "sort10/"),
        )
        activity_rank_pager.adapter = object: FragmentPagerAdapter(supportFragmentManager) {
            override fun getCount(): Int= tabList.size
            override fun getItem(position: Int): Fragment= Fragment()
        }
        activity_rank_tab.setViewPager(activity_rank_pager, tabList.map { it.title }.toTypedArray())
        activity_rank_tab.setOnTabSelectListener(object: OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                myAdapter?.setData(null)
                loadRank(tabList[position])
            }
            override fun onTabReselect(position: Int) {
                loadRank(tabList[position])
            }
        })
        loadRank(tabList[0])
        activity_rank_swipe.setOnRefreshListener {
            loadRank(tabList[activity_rank_tab.currentTab])
        }

        activity_rank_empty_btn.setOnClickListener {
            loadRank(tabList[activity_rank_tab.currentTab])
        }
    }

    private fun loadRank(tab: RankTab) {
        RankCacheVM.getLive(tab).value?.let {
            myAdapter?.setData(it)
        }

        activity_rank_swipe.isRefreshing = true
        val useCase = Rank4FpzwUseCase(tab.path)
        useCase.execute(object : ViewPresenter<List<NovelInfo>> {
            override fun error(from: CacheSource, code: Int, info: String) {
                toast(info)
                activity_rank_empty_label.text = info
                showEmpty(true)
            }
            override fun load(from: CacheSource, data: List<NovelInfo>?) {
                myAdapter?.setData(data)
                showEmpty(data.isNullOrEmpty())
                activity_rank_recycler.scrollToPosition(0)
                RankCacheVM.post(tab, data)
            }
            override fun complete(from: CacheSource) {
                activity_rank_swipe.isRefreshing = false
            }
        })
    }

    private fun showEmpty(show: Boolean) {
        if(show) {
            activity_rank_empty_group.visibility = View.VISIBLE
            activity_rank_recycler.visibility = View.GONE
        } else {
            activity_rank_empty_group.visibility = View.GONE
            activity_rank_recycler.visibility = View.VISIBLE
        }
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
            holder.itemView.setOnClickListener {
//                queryNovel(itemData.url)
                operateNovel(itemData)
            }
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