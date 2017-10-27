package com.cabe.app.novel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.NovelDetailUseCase;
import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.model.NovelDetail;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.app.novel.utils.DiskUtils;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class NovelDetailActivity extends BaseActivity {
    protected static String TAG = "";
    public static String CUR_NOVEL_DETAIL_KEY = "";

    private TextView tvTips;
    private SwipeRefreshLayout listSwipe;
    private RecyclerView listRecycler;

    private NovelInfo novelInfo;
    private NovelContent lastContent;
    private MyAdapter adapter = new MyAdapter();

    private boolean flagReverse = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        setBackable(true);
        setContentView(R.layout.activity_novel_detail);
        initView();
        loadNovelInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String novelGson = DiskUtils.getData(CUR_NOVEL_DETAIL_KEY);
        if(!TextUtils.isEmpty(novelGson)) {
            lastContent = new Gson().fromJson(novelGson, NovelContent.class);
            adapter.updateLastInfo(lastContent);
        }
    }

    @Override
    protected void initExtra() {
        novelInfo = getExtraGson(new TypeToken<NovelInfo>(){});
        if(novelInfo != null) {
            CUR_NOVEL_DETAIL_KEY = TAG + "#" + novelInfo.title + "#" + novelInfo.url;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_novel_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_novel_detail_order:
                actionOrderReverse();
                return true;
            case R.id.menu_novel_detail_scroll_top:
                actionScrollTop();
                return true;
            case R.id.menu_novel_detail_location:
                actionLocationIndex();
                return true;
            case R.id.menu_novel_detail_source:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        tvTips = (TextView) findViewById(R.id.activity_novel_detail_tips);
        listSwipe = (SwipeRefreshLayout) findViewById(R.id.activity_novel_detail_swipe);
        listRecycler = (RecyclerView) findViewById(R.id.activity_novel_detail_list);
        listRecycler.setAdapter(adapter);
        GridLayoutManager manager = new GridLayoutManager(context, 2);
        listRecycler.setLayoutManager(manager);

        listSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNovelInfo();
            }
        });

        if(novelInfo != null) {
            setTitle(novelInfo.title);
        }
    }

    private void updateView(NovelDetail detail) {
        if(detail == null) return;

        tvTips.setText(detail.getTips());
        adapter.setData(detail.list);
        adapter.updateLastInfo(lastContent);
    }

    private void loadNovelInfo() {
        if(novelInfo != null) {
            listSwipe.setRefreshing(true);
            NovelDetailUseCase useCase = new NovelDetailUseCase(novelInfo.url);
            useCase.execute(new ViewPresenter<NovelDetail>() {
                @Override
                public void load(CacheSource from, NovelDetail data) {
                    updateView(data);
                }
                @Override
                public void error(CacheSource from, int code, String info) {
                    toast(info);
                }
                @Override
                public void complete(CacheSource from) {
                    listSwipe.setRefreshing(false);
                }
            });
        }
    }

    private void gotoContent(NovelContent content) {
        Intent intent = NovelContentActivity.create(context, content);
        startActivity(intent);
    }

    private void actionOrderReverse() {
        flagReverse = !flagReverse;
        adapter.notifyDataSetChanged();
    }

    private void actionLocationIndex() {
        int curIndex = adapter.getCurPosition();
        if(curIndex >= 0) {
            listRecycler.smoothScrollToPosition(adapter.getRealPosition(curIndex));
        }
    }

    private void actionScrollTop() {
        listRecycler.smoothScrollToPosition(0);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        private NovelContent lastContent;
        private List<NovelContent> data;

        private void updateLastInfo(NovelContent lastContent) {
            if(containNovel(lastContent)) {
                this.lastContent = lastContent;
                lastContent.flagLast = true;
                notifyDataSetChanged();
            }
        }

        private int getCurPosition() {
            int cur = -1;
            if(data != null && lastContent != null) {
                cur = data.lastIndexOf(lastContent);
            }
            return cur;
        }

        private void setData(List<NovelContent> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        private NovelContent getItemData(int index) {
            if(lastContent != null) {
                if(index == 0) {
                    return lastContent;
                }

                index --;
            }

            if(index < 0 || index >= getRealItemCount()) return null;

            index = getRealPosition(index);
            return data.get(index);
        }

        private boolean containNovel(NovelContent content) {
            return data != null && content != null && data.indexOf(content) >= 0;
        }

        private int getRealPosition(int position) {
            return flagReverse ? getRealItemCount() - position - 1 : position;
        }

        private int getRealItemCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public int getItemCount() {
            int dataSize = getRealItemCount();
            if(lastContent != null) {
                dataSize ++;
            }
            return dataSize;
        }
        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_novel_detail_info, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            final NovelContent content = getItemData(position);
            if(content == null) return;

            String tips = "";
            if(content.flagLast) {
                tips = "(继续)";
            }
            holder.title.setText(String.valueOf(tips + content.title));
            holder.title.setTextColor(content.equals(lastContent) ? 0xFFF51E51 : 0xFF34488A);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DiskUtils.saveData(CUR_NOVEL_DETAIL_KEY, content.toGson());
                    gotoContent(content);
                }
            });
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private MyHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_novel_detail_info);
        }
    }

    public static Intent create(Context context, NovelInfo novelInfo) {
        Intent intent = new Intent(context, NovelDetailActivity.class);
        if(novelInfo != null) {
            intent.putExtra(KEY_EXTRA_GSON, novelInfo.toGson());
        }
        return intent;
    }
}
