package com.cabe.app.novel.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.ekxs.Search42kxsUseCase;
import com.cabe.app.novel.domain.LocalNovelsUseCase;
import com.cabe.app.novel.domain.x23us.Search4X23USUseCase;
import com.cabe.app.novel.model.LocalNovelList;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;
import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.pgyersdk.update.PgyUpdateManager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {
    private LocalNovelList localNovelList;
    private LocalNovelsUseCase useCase = new LocalNovelsUseCase();

    private EditText searchInput;
    private SwipeRefreshLayout localSwipe;
    private RecyclerView recyclerSearch;
    private View btnClose;

    private MyAdapter adapter = new MyAdapter();
    private MyAdapter adapterSearch = new MyAdapter();

    private ProgressDialog waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setBackable(false);
        initView();
        setTitle("简易小说");

        waiting = new ProgressDialog(this);
        waiting.setMessage("请稍候");

        loadLocal();

        new PgyUpdateManager.Builder().register();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_novel_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_novel_home_feedback:
                new PgyerFeedbackManager.PgyerFeedbackBuilder()
                        .setShakeInvoke(false)       //fasle 则不触发摇一摇，最后需要调用 invoke 方法
                        // true 设置需要调用 register 方法使摇一摇生效
                        .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                        .setColorDialogTitle("#FF0000")    //设置Dialog 标题的字体颜色，默认为颜色为#ffffff
                        .setColorTitleBg("#FF0000")        //设置Dialog 标题栏的背景色，默认为颜色为#2E2D2D
                        .setBarBackgroundColor("#FF0000")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                        .setBarButtonPressedColor("#FF0000")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                        .setColorPickerBackgroundColor("#FF0000")   //设置颜色选择器的背景色,默认颜色为 #272828
                        .setMoreParam("KEY1","VALUE1") //自定义的反馈数据
                        .setMoreParam("KEY2","VALUE2") //自定义的反馈数据
                        .builder()
                        .invoke();
                return true;
            case R.id.menu_novel_home_about:
                Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        searchInput = findViewById(R.id.activity_home_search_input);
        localSwipe = findViewById(R.id.activity_home_local_swipe);
        RecyclerView recyclerView = findViewById(R.id.activity_home_local_list);
        btnClose = findViewById(R.id.activity_home_search_btn_close);

        localSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadLocal();
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(new AdapterClickListener() {
            @Override
            public void itemOnClick(final NovelInfo novelInfo) {
                Intent intent = NovelListActivity.create(context, novelInfo);
                if(intent != null) {
                    startActivity(intent);
                }
                localSwipe.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setTopNovel(novelInfo);
                    }
                }, 200);
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

        recyclerSearch = findViewById(R.id.activity_home_search_list);
        recyclerSearch.setAdapter(adapterSearch);
        adapterSearch.setItemClickListener(new AdapterClickListener() {
            @Override
            public void itemOnClick(NovelInfo novelInfo) {
                addLocalNovel(novelInfo);
                recyclerSearch.smoothScrollToPosition(0);
                showSearchView(false);
                searchInput.setText("");
            }
            @Override
            public void itemOnLongClick(NovelInfo novelInfo) {
            }
        });
    }

    private void showSearchView(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        recyclerSearch.setVisibility(visibility);
        btnClose.setVisibility(visibility);
    }

    private void hiddenKeyboard() {
        InputMethodManager inputMethodManager = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
        if(inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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

    public void onClose(View view) {
        showSearchView(false);
    }

    public void onSearch(View view) {
        waiting.show();
        hiddenKeyboard();
        String inputStr = searchInput.getText().toString();
        search4x23us(inputStr);
    }

    private void search4x23us(final String keyWord) {
        Search4X23USUseCase searchUseCase = new Search4X23USUseCase(keyWord);
        searchUseCase.execute(new ViewPresenter<List<NovelInfo>>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, List<NovelInfo> data) {
                if(!data.isEmpty()) {
                    adapterSearch.setData(data);
                    showSearchView(true);
                }
            }
            @Override
            public void complete(CacheSource from) {
                search42kxs(keyWord);
            }
        });
    }

    private void search42kxs(final String keyWord) {
        Search42kxsUseCase searchUseCase = new Search42kxsUseCase(keyWord);
        searchUseCase.execute(new ViewPresenter<List<NovelInfo>>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, List<NovelInfo> data) {
                if(!data.isEmpty()) {
                    adapterSearch.addData(data);
                    showSearchView(true);
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
        public void addData(List<NovelInfo> list) {
            if(novelList == null) {
                novelList = new ArrayList<>();
            }
            if(list != null) {
                novelList.addAll(list);
            }
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

            Glide.with(context).load(itemData.getPicUrl()).into(holder.pic);
            holder.tvTitle.setText(itemData.title);
            holder.tvAuthor.setText(String.valueOf("作者：" + itemData.author));
            holder.tvType.setText(String.valueOf("类型：" + itemData.type));
            holder.tvWords.setText(String.valueOf("字数：" + itemData.wordSize));
            holder.tvState.setText(String.valueOf("状态：" + itemData.state));
            holder.tvSource.setText(String.valueOf("来源：" + itemData.source));
            holder.tvType.setVisibility(TextUtils.isEmpty(itemData.type) ? View.GONE : View.VISIBLE);
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
            pic = itemView.findViewById(R.id.item_home_local_novel_pic);
            tvTitle = itemView.findViewById(R.id.item_home_local_novel_title);
            tvAuthor = itemView.findViewById(R.id.item_home_local_novel_author);
            tvType = itemView.findViewById(R.id.item_home_local_novel_type);
            tvWords = itemView.findViewById(R.id.item_home_local_novel_words);
            tvState = itemView.findViewById(R.id.item_home_local_novel_state);
            tvSource = itemView.findViewById(R.id.item_home_local_novel_source);
        }
    }

    private interface AdapterClickListener {
        void itemOnClick(NovelInfo novelInfo);
        void itemOnLongClick(NovelInfo novelInfo);
    }
}
