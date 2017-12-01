package com.cabe.app.novel.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.NovelContentUseCase;
import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.utils.DiskUtils;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class NovelContentActivity extends BaseActivity {
    private static String KEY_NOVEL_CONTENT_ID = "keyNovelContentID";
    private static String KEY_NOVEL_CONTENT_LAST = "keyNovelContentLast";

    private SwipeRefreshLayout swipeLayout;
    private ScrollView viewScroll;
    private TextView tvTitle;
    private TextView tvContent;
    private View btnPre;
    private View btnNext;
    private View btnPreBottom;
    private View btnNextBottom;

    private String keyNovelContent;
    private NovelContent curContent;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NOVEL_CONTENT_ID, keyNovelContent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_content);
        initView();

        NovelContent novelContent = null;
        if(savedInstanceState == null) {
            novelContent = getExtraGson(new TypeToken<NovelContent>(){});
            keyNovelContent = getExtraString(KEY_NOVEL_CONTENT_LAST);
        } else {
            keyNovelContent = savedInstanceState.getString(KEY_NOVEL_CONTENT_ID);
            String novelGson = DiskUtils.getData(keyNovelContent);
            if(!TextUtils.isEmpty(novelGson)) {
                novelContent = new Gson().fromJson(novelGson, NovelContent.class);
            }
        }

        if(novelContent != null && !TextUtils.isEmpty(novelContent.url)) {
            loadContent(novelContent.url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_novel_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_novel_content_theme:
                actionSwitchTheme();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.activity_novel_content_swipe);
        viewScroll = (ScrollView) findViewById(R.id.activity_novel_content_scroll);
        tvTitle = (TextView) findViewById(R.id.activity_novel_content_title);
        tvContent = (TextView) findViewById(R.id.activity_novel_content_info);
        btnPre = findViewById(R.id.activity_novel_content_preview);
        btnNext = findViewById(R.id.activity_novel_content_next);
        btnPreBottom = findViewById(R.id.activity_novel_content_preview_btoom);
        btnNextBottom = findViewById(R.id.activity_novel_content_next_bottom);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(curContent != null && !TextUtils.isEmpty(curContent.url)) {
                    loadContent(curContent.url);
                }
            }
        });

        btnPre.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
        btnPreBottom.setVisibility(View.INVISIBLE);
        btnNextBottom.setVisibility(View.INVISIBLE);
    }

    private void updateView(NovelContent content) {
        if(content == null) return;

        btnPre.setVisibility(content.preUrl != null && content.preUrl.endsWith("html") ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(content.nextUrl != null && content.nextUrl.endsWith("html") ? View.VISIBLE : View.INVISIBLE);
        btnPreBottom.setVisibility(content.preUrl != null && content.preUrl.endsWith("html") ? View.VISIBLE : View.INVISIBLE);
        btnNextBottom.setVisibility(content.nextUrl != null && content.nextUrl.endsWith("html") ? View.VISIBLE : View.INVISIBLE);

        setTitle(content.title);
        tvTitle.setText(content.title);
        tvContent.setText(Html.fromHtml(content.content));
    }

    private void loadContent(String url) {
        swipeLayout.setRefreshing(true);
        NovelContentUseCase useCase = new NovelContentUseCase(url);
        useCase.execute(new ViewPresenter<NovelContent>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, NovelContent content) {
                btnPre.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                btnPreBottom.setVisibility(View.VISIBLE);
                btnNextBottom.setVisibility(View.VISIBLE);
                viewScroll.fullScroll(ScrollView.FOCUS_UP);

                NovelContent cacheContent = new NovelContent();
                cacheContent.title = content.title;
                cacheContent.url = content.url;
                cacheContent.preUrl = content.preUrl;
                cacheContent.nextUrl = content.nextUrl;
                DiskUtils.saveData(keyNovelContent, cacheContent.toGson());
                curContent = content;
                updateView(content);
            }
            @Override
            public void complete(CacheSource from) {
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void actionSwitchTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        getDelegate().setLocalNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_NO
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void actionPreview(View view) {
        if(curContent == null) return;

        loadContent(curContent.preUrl);
    }

    public void actionNext(View view) {
        if(curContent == null) return;

        loadContent(curContent.nextUrl);
    }

    public static Intent create(Context context, NovelContent novelContent, String novelKey) {
        Intent intent = new Intent(context, NovelContentActivity.class);
        if(novelContent != null) {
            intent.putExtra(KEY_EXTRA_GSON, novelContent.toGson());
        }
        intent.putExtra(NovelContentActivity.KEY_NOVEL_CONTENT_LAST, novelKey);
        return intent;
    }
}
