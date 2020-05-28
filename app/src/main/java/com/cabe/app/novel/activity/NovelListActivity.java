package com.cabe.app.novel.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.ekxs.NovelList42KXSUseCase;
import com.cabe.app.novel.domain.x23us.NovelList4X23USUseCase;
import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.app.novel.model.NovelList;
import com.cabe.app.novel.model.SourceType;
import com.cabe.app.novel.utils.DiskUtils;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NovelListActivity extends BaseActivity {
    private static String KEY_FLAG_SORT_REVERSE = "keyFlagSortReverse";

    public static NovelList novelList;

    private TextView tvTips;
    private SwipeRefreshLayout listSwipe;
    private RecyclerView listRecycler;

    private String keyNovelDetail = "";
    private NovelInfo novelInfo;
    private NovelContent lastContent;
    private MyAdapter adapter = new MyAdapter();

    private boolean flagReverse = true;

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FLAG_SORT_REVERSE, flagReverse);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_list);
        initView();
        loadNovelInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String novelGson = DiskUtils.getData(keyNovelDetail);
        if(!TextUtils.isEmpty(novelGson)) {
            lastContent = new Gson().fromJson(novelGson, NovelContent.class);
            adapter.updateLastInfo(lastContent);
        }
    }

    @Override
    protected void initExtra(Bundle savedInstanceState) {
        novelInfo = getExtraGson(new TypeToken<NovelInfo>(){});
        if(novelInfo != null) {
            keyNovelDetail = TAG + "#" + novelInfo.title + "#" + novelInfo.url;
        }
        if(savedInstanceState != null) {
            flagReverse = savedInstanceState.getBoolean(KEY_FLAG_SORT_REVERSE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_novel_list, menu);
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
            case R.id.menu_novel_detail_search:
                actionSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        tvTips = findViewById(R.id.activity_novel_list_tips);
        listSwipe = findViewById(R.id.activity_novel_list_swipe);
        listRecycler = findViewById(R.id.activity_novel_list_list);
        listRecycler.setAdapter(adapter);
        GridLayoutManager manager = new GridLayoutManager(context, 2);
        listRecycler.setLayoutManager(manager);

        listSwipe.setOnRefreshListener(this::loadNovelInfo);

        if(novelInfo != null) {
            setTitle(novelInfo.title);
        }
    }

    private void updateView(NovelList detail) {
        if(detail == null) return;

        novelList = detail;
        tvTips.setText(detail.getTips());
        adapter.setData(detail.list);
        adapter.updateLastInfo(lastContent);
    }

    private void loadNovelInfo() {
        if(novelInfo != null) {
            listSwipe.setRefreshing(true);
            ViewPresenter<NovelList> presenter = new ViewPresenter<NovelList>() {
                @Override
                public void load(CacheSource from, NovelList data) {
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
            };
            if(novelInfo.source == SourceType.X23US) {
                NovelList4X23USUseCase useCase = new NovelList4X23USUseCase(novelInfo.url);
                useCase.execute(presenter);
            } else if(novelInfo.source == SourceType.EKXS) {
                NovelList42KXSUseCase useCase = new NovelList42KXSUseCase(novelInfo.url);
                useCase.execute(presenter);
            }
        }
    }

    private void gotoContent(NovelContent content) {
        Intent intent = NovelContentActivity.create(context, content, keyNovelDetail);
        startActivity(intent);
    }

    private void searchKey(String key) {
        int position = adapter.indexPosition(key);
        if(position >= 0) {
            listRecycler.scrollToPosition(adapter.getRealPosition(position));
        }
    }

    private void actionSearch() {
        final EditText input = new EditText(this);
        input.setHint("请输入章节号");
        new AlertDialog.Builder(this)
                .setTitle("查找章节")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    searchKey(input.getText().toString());
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
    }

    private void actionOrderReverse() {
        flagReverse = !flagReverse;
        adapter.notifyDataSetChanged();
    }

    private void actionLocationIndex() {
        int curIndex = adapter.getCurPosition();
        GridLayoutManager layoutManager = (GridLayoutManager) listRecycler.getLayoutManager();
        int firstIndex = layoutManager.findFirstVisibleItemPosition();
        int lastIndex = layoutManager.findLastVisibleItemPosition();
        int visibilityCount = lastIndex - firstIndex;
        int spanCount = layoutManager.getSpanCount();
        if(curIndex < adapter.getItemCount() - visibilityCount) {
            if(flagReverse) {
                curIndex -= visibilityCount - spanCount;
            } else {
                curIndex += visibilityCount - spanCount;
            }
        }
        if(curIndex >= 0) {
            listRecycler.scrollToPosition(adapter.getRealPosition(curIndex));
        }
    }

    private void actionScrollTop() {
        listRecycler.scrollToPosition(0);
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

        private int indexPosition(String key) {
            int index = 0;
            if(data != null) {
                for(NovelContent content : data) {
                    if(content.title.contains(key)) {
                        break;
                    }
                    index ++;
                }
            }
            return index;
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
        @NotNull
        @Override
        public MyHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_novel_list_info, parent, false);
            return new MyHolder(itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NotNull MyHolder holder, int position) {
            final NovelContent content = getItemData(position);
            if(content == null) return;

            String tips = "";
            if(content.flagLast) {
                tips = "(继续)";
            }
            holder.title.setText(tips + content.title);
            holder.title.setTextColor(content.equals(lastContent) ? 0xFFF51E51 : 0xFF34488A);
            holder.itemView.setOnClickListener(v -> {
                DiskUtils.saveData(keyNovelDetail, content.toGson());
                gotoContent(content);
            });
        }
    }

    private static class MyHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private MyHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_novel_list_info);
        }
    }

    public static Intent create(Context context, NovelInfo novelInfo) {
        Intent intent = new Intent(context, NovelListActivity.class);
        if(novelInfo != null) {
            intent.putExtra(KEY_EXTRA_GSON, novelInfo.toGson());
        }
        return intent;
    }
}
