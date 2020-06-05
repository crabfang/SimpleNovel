package com.cabe.app.novel.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cabe.app.novel.R
import com.cabe.app.novel.model.NovelContent
import com.cabe.app.novel.utils.DiskUtils
import com.cabe.app.novel.widget.NovelContentView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_novel_content.*

class NovelContentActivity : BaseActivity() {
    private var recyclerView: RecyclerView? = null
    private var myAdapter: MyAdapter? = null
    private var keyNovelContent: String? = null
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_NOVEL_CONTENT_ID, keyNovelContent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_content)
        initView()
        updateView(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_novel_content, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_novel_content_theme) {
            actionSwitchTheme()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        activity_novel_content_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var prePosition = -1
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val curIndex = (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
                if (prePosition != curIndex) {
                    prePosition = curIndex
                    val novel = myAdapter!!.getNovelContent(curIndex)
                    if (novel != null) {
                        DiskUtils.saveData(keyNovelContent, novel.toGson())
                        title = novel.title
                    }
                }
            }
        })
        myAdapter = MyAdapter()
        activity_novel_content_recycler.setAdapter(myAdapter)
    }

    private fun updateView(savedInstanceState: Bundle?) {
        var novelContent: NovelContent? = null
        if (savedInstanceState == null) {
            novelContent = getExtraGson(object : TypeToken<NovelContent>() {})
            keyNovelContent = getExtraString(KEY_NOVEL_CONTENT_LAST)
        } else {
            keyNovelContent = savedInstanceState.getString(KEY_NOVEL_CONTENT_ID)
            val novelGson = DiskUtils.getData(keyNovelContent)
            if (!TextUtils.isEmpty(novelGson)) {
                novelContent = Gson().fromJson(novelGson, NovelContent::class.java)
            }
        }
        val index = myAdapter!!.indexPosition(novelContent)
        recyclerView!!.scrollToPosition(index)
    }

    private fun actionSwitchTheme() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        delegate.localNightMode = if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(NovelContentView(parent.context))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.contentView.loadContent(getNovelContent(position))
        }

        override fun getItemCount(): Int {
            return if (novelList == null) 0 else novelList!!.size
        }

        fun getNovelContent(position: Int): NovelContent? {
            var novel: NovelContent? = null
            if (novelList != null) {
                if (position >= 0 && position < novelList!!.size) {
                    novel = novelList!![position]
                }
            }
            return novel
        }

        fun indexPosition(content: NovelContent?): Int {
            var index = 0
            if (novelList != null && content != null) {
                index = novelList!!.indexOf(content)
            }
            return index
        }

        private val novelList: List<NovelContent>?
            private get() = if (NovelListActivity.novelList == null) null else NovelListActivity.novelList!!.list
    }

    private class MyViewHolder(val contentView: NovelContentView) : ViewHolder(contentView)

    companion object {
        private const val KEY_NOVEL_CONTENT_ID = "keyNovelContentID"
        private const val KEY_NOVEL_CONTENT_LAST = "keyNovelContentLast"
        fun create(context: Context?, novelContent: NovelContent?, novelKey: String?): Intent {
            val intent = Intent(context, NovelContentActivity::class.java)
            if (novelContent != null) {
                intent.putExtra(KEY_EXTRA_GSON, novelContent.toGson())
            }
            intent.putExtra(KEY_NOVEL_CONTENT_LAST, novelKey)
            return intent
        }
    }
}