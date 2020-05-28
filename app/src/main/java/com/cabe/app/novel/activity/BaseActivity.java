package com.cabe.app.novel.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cabe.app.novel.model.BaseObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pgyersdk.crash.PgyCrashManager;

/**
 * 作者：沈建芳 on 2017/10/9 16:55
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected String TAG = "BaseActivity";
    public final static String KEY_EXTRA_GSON = "extraGson";

    protected Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = getClass().getSimpleName();
        super.onCreate(savedInstanceState);
        initExtra(savedInstanceState);
        context = this;
        setBackable(true);
        PgyCrashManager.register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context = null;
        PgyCrashManager.unregister();
    }

    protected void setBackable(boolean backable) {
        AppCompatDelegate delegate = getDelegate();
        ActionBar actionBar = delegate.getSupportActionBar();
        if(actionBar == null) return;

        actionBar.setDisplayHomeAsUpEnabled(backable);
    }

    protected void initExtra(Bundle savedInstanceState) {}

    protected String getExtraString(String key) {
        return getIntent().getStringExtra(key);
    }

    protected <T extends BaseObject> T getExtraGson(TypeToken<T> token) {
        T data = null;
        String extraGson = getIntent().getStringExtra(KEY_EXTRA_GSON);
        if(!TextUtils.isEmpty(extraGson)) {
            data = new Gson().fromJson(extraGson, token.getType());
        }
        return data;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void toast(String info) {
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
    }
}