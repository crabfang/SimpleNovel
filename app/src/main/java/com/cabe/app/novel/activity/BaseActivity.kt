package com.cabe.app.novel.activity

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cabe.app.novel.model.BaseObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 作者：沈建芳 on 2017/10/9 16:55
 */
abstract class BaseActivity : AppCompatActivity() {
    var TAG = "BaseActivity"
    var context: Context? = null
    var waiting: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        TAG = javaClass.simpleName
        super.onCreate(savedInstanceState)
        waiting = ProgressDialog(this)
        waiting?.setMessage("请稍候")
        initExtra(savedInstanceState)
        context = this
        setBackable(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        context = null
    }

    protected fun setBackable(backable: Boolean) {
        val delegate = delegate
        val actionBar = delegate.supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(backable)
    }

    protected open fun initExtra(savedInstanceState: Bundle?) {}
    protected fun getExtraString(key: String?): String? {
        return intent.getStringExtra(key)
    }

    protected fun <T : BaseObject?> getExtraGson(token: TypeToken<T>): T? {
        var data: T? = null
        val extraGson = intent.getStringExtra(KEY_EXTRA_GSON)
        if (!TextUtils.isEmpty(extraGson)) {
            data = Gson().fromJson(extraGson, token.type)
        }
        return data
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun toast(info: String?) {
        if(TextUtils.isEmpty(info).not()) Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val KEY_EXTRA_GSON = "extraGson"
    }
}