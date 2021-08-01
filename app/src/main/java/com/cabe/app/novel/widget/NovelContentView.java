package com.cabe.app.novel.widget;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.bqg.NovelContent4BqgUseCase;
import com.cabe.app.novel.domain.fpzw.NovelContent4FpzwUseCase;
import com.cabe.app.novel.domain.ekxs.NovelContent42kxsUseCase;
import com.cabe.app.novel.domain.x23us.NovelContent4X23USUseCase;
import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.model.SourceType;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;

/**
 * 作者：沈建芳 on 2018/7/12 19:27
 */
public class NovelContentView extends LinearLayout {
    private View viewMain;
    private TextView tvTitle;
    private TextView tvContent;
    private View loading;
    private View viewError;

    private NovelContent novel;
    public NovelContentView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_novel_content, this, true);
        viewMain = findViewById(R.id.fragment_novel_content_main);
        tvTitle = findViewById(R.id.fragment_novel_content_title);
        tvContent = findViewById(R.id.fragment_novel_content_val);
        loading = findViewById(R.id.fragment_novel_content_loading);
        viewError = findViewById(R.id.fragment_novel_content_error);
        View btnRetry = findViewById(R.id.fragment_novel_content_error_btn);
        btnRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContent(novel);
            }
        });
    }

    public void loadContent(final NovelContent novel) {
        this.novel = novel;
        if(novel == null) return;

        loading.setVisibility(View.VISIBLE);
        ViewPresenter<NovelContent> presenter = new ViewPresenter<NovelContent>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                viewError.setVisibility(View.VISIBLE);
                viewMain.setVisibility(View.GONE);
            }
            @Override
            public void load(CacheSource from, NovelContent content) {
                NovelContent cacheContent = new NovelContent();
                cacheContent.title = content.title;
                cacheContent.url = content.url;
                cacheContent.preUrl = content.preUrl;
                cacheContent.nextUrl = content.nextUrl;
                cacheContent.source = novel.source;
                updateView(content);
                viewError.setVisibility(View.GONE);
                viewMain.setVisibility(View.VISIBLE);
            }
            @Override
            public void complete(CacheSource from) {
                loading.setVisibility(View.GONE);
            }
        };
        String url = novel.url;
        if(novel.source == SourceType.X23US) {
            NovelContent4X23USUseCase useCase = new NovelContent4X23USUseCase(url);
            useCase.execute(presenter);
        } else if(novel.source == SourceType.EKXS) {
            NovelContent42kxsUseCase useCase = new NovelContent42kxsUseCase(url);
            useCase.execute(presenter);
        }else if(novel.source == SourceType.FPZW) {
            NovelContent4FpzwUseCase useCase = new NovelContent4FpzwUseCase(url);
            useCase.execute(presenter);
        } else if(novel.source == SourceType.BQG) {
            NovelContent4BqgUseCase useCase = new NovelContent4BqgUseCase(url);
            useCase.execute(presenter);
        }
    }

    private void updateView(NovelContent content) {
        if(content == null) return;

        tvTitle.setText(content.title);
        if(!TextUtils.isEmpty(content.content)) {
            tvContent.setText(Html.fromHtml(content.content));
        }
    }
}
