package com.cabe.app.novel.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.LocalNovelsUseCase;
import com.cabe.app.novel.domain.SearchUseCase;
import com.cabe.app.novel.model.LocalNovelList;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.app.novel.utils.UrlUtils;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;

import java.util.List;

public class HomeActivity extends BaseActivity {
    private LocalNovelList localNovelList;
    private LocalNovelsUseCase useCase = new LocalNovelsUseCase();

    private EditText searchInput;
    private SwipeRefreshLayout localSwipe;
    private RecyclerView recyclerSearch;

    private MyAdapter adapter = new MyAdapter();
    private MyAdapter adapterSearch = new MyAdapter();

    private ProgressDialog waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();

        waiting = new ProgressDialog(this);
        waiting.setMessage("请稍候");

        loadLocal();
    }

    private void initView() {
        searchInput = (EditText) findViewById(R.id.activity_home_search_input);
        localSwipe = (SwipeRefreshLayout) findViewById(R.id.activity_home_local_swipe);
        localSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadLocal();
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_home_local_list);
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(new AdapterClickListener() {
            @Override
            public void itemOnClick(NovelInfo novelInfo) {
                setTopNovel(novelInfo);
                Intent intent = NovelDetailActivity.create(context, novelInfo);
                if(intent != null) {
                    startActivity(intent);
                }
            }
            @Override
            public void itemOnLongClick(final NovelInfo novelInfo) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(novelInfo.title);
                builder.setItems(new String[]{ "置顶", "删除"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        setTopNovel(novelInfo);
                                        break;
                                    case 1:
                                        removeLocalNovel(novelInfo);
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });

        recyclerSearch = (RecyclerView) findViewById(R.id.activity_home_search_list);
        recyclerSearch.setAdapter(adapterSearch);
        adapterSearch.setItemClickListener(new AdapterClickListener() {
            @Override
            public void itemOnClick(NovelInfo novelInfo) {
                addLocalNovel(novelInfo);
                recyclerSearch.smoothScrollToPosition(0);
                recyclerSearch.setVisibility(View.GONE);
                searchInput.setText("");
            }
            @Override
            public void itemOnLongClick(NovelInfo novelInfo) {
            }
        });

    }

    private void hiddenKeyboard() {
        InputMethodManager inputMethodManager = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void loadLocal() {
        localSwipe.setRefreshing(true);
        useCase.execute(new ViewPresenter<LocalNovelList>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, LocalNovelList data) {
                localNovelList = data;
                updateLocalNovel();
            }
            @Override
            public void complete(CacheSource from) {
                localSwipe.setRefreshing(false);
            }
        });
    }

    private void updateLocalNovel() {
        if(localNovelList != null) {
            adapter.setData(localNovelList.list);
        }
    }

    private void addLocalNovel(NovelInfo novelInfo) {
        if(localNovelList == null) {
            localNovelList = new LocalNovelList();
        }
        localNovelList.addNovel(novelInfo);
        useCase.saveCacheDisk(localNovelList);
        updateLocalNovel();
    }

    private void removeLocalNovel(NovelInfo novelInfo) {
        if(localNovelList != null && !localNovelList.isEmpty()) {
            localNovelList.removeNovel(novelInfo);
            useCase.saveCacheDisk(localNovelList);
            updateLocalNovel();
        }
    }

    private void setTopNovel(NovelInfo novelInfo) {
        if(localNovelList != null && !localNovelList.isEmpty()) {
            localNovelList.setTop(novelInfo);
            useCase.saveCacheDisk(localNovelList);
            updateLocalNovel();
        }
    }

    public void onSearch(View view) {
        waiting.show();
        hiddenKeyboard();
        String inputStr = searchInput.getText().toString();
        SearchUseCase searchUseCase = new SearchUseCase(inputStr);
        searchUseCase.execute(new ViewPresenter<List<NovelInfo>>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, List<NovelInfo> data) {
                if(!data.isEmpty()) {
                    adapterSearch.setData(data);
                    recyclerSearch.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void complete(CacheSource from) {
                waiting.dismiss();
            }
        });
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        private AdapterClickListener listener;
        private List<NovelInfo> novelList;
        private void setItemClickListener(AdapterClickListener listener) {
            this.listener = listener;
        }
        public void setData(List<NovelInfo> list) {
            novelList = list;
            notifyDataSetChanged();
        }
        @Override
        public int getItemCount() {
            if(novelList == null || novelList.isEmpty()) return 0;

            return novelList.size();
        }
        private NovelInfo getItemData(int index) {
            if(novelList == null || novelList.isEmpty()) return null;

            if(index < 0 || index >= novelList.size()) return null;

            return novelList.get(index);
        }
        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_home_local_novel, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            final NovelInfo itemData = getItemData(position);
            if(itemData == null) return;

            Glide.with(context).load(itemData.picUrl).into(holder.pic);
            holder.tvTitle.setText(itemData.title);
            holder.tvAuthor.setText(String.valueOf("作者：" + itemData.title));
            holder.tvType.setText(String.valueOf("类型：" + itemData.author));
            holder.tvWords.setText(String.valueOf("字数：" + itemData.type));
            holder.tvState.setText(String.valueOf("状态：" + itemData.state));
            holder.tvSource.setText(String.valueOf("来源：" + UrlUtils.getHostName(itemData.url)));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        listener.itemOnClick(itemData);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(listener != null) {
                        listener.itemOnLongClick(itemData);
                    }
                    return false;
                }
            });
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        private ImageView pic;
        private TextView tvTitle;
        private TextView tvAuthor;
        private TextView tvType;
        private TextView tvWords;
        private TextView tvState;
        private TextView tvSource;
        private MyHolder(View itemView) {
            super(itemView);
            pic = (ImageView) itemView.findViewById(R.id.item_home_local_novel_pic);
            tvTitle = (TextView) itemView.findViewById(R.id.item_home_local_novel_title);
            tvAuthor = (TextView) itemView.findViewById(R.id.item_home_local_novel_author);
            tvType = (TextView) itemView.findViewById(R.id.item_home_local_novel_type);
            tvWords = (TextView) itemView.findViewById(R.id.item_home_local_novel_words);
            tvState = (TextView) itemView.findViewById(R.id.item_home_local_novel_state);
            tvSource = (TextView) itemView.findViewById(R.id.item_home_local_novel_source);
        }
    }

    private interface AdapterClickListener {
        void itemOnClick(NovelInfo novelInfo);
        void itemOnLongClick(NovelInfo novelInfo);
    }
}
