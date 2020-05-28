package com.cabe.app.novel.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.cabe.app.novel.R;
import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.utils.DiskUtils;
import com.cabe.app.novel.widget.NovelContentView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NovelContentActivity extends BaseActivity {
    private static String KEY_NOVEL_CONTENT_ID = "keyNovelContentID";
    private static String KEY_NOVEL_CONTENT_LAST = "keyNovelContentLast";

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private String keyNovelContent;

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NOVEL_CONTENT_ID, keyNovelContent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_content);

        initView();
        updateView(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_novel_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_novel_content_theme) {
            actionSwitchTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        recyclerView = findViewById(R.id.activity_novel_content_recycler);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int prePosition = -1;
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int curIndex = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if(prePosition != curIndex) {
                    prePosition = curIndex;
                    NovelContent novel = myAdapter.getNovelContent(curIndex);
                    if(novel != null) {
                        DiskUtils.saveData(keyNovelContent, novel.toGson());
                        setTitle(novel.title);
                    }
                }
            }
        });

        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
    }

    private void updateView(Bundle savedInstanceState) {
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
        int index = myAdapter.indexPosition(novelContent);
        recyclerView.scrollToPosition(index);
    }

    private void actionSwitchTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        getDelegate().setLocalNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_NO
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(new NovelContentView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.contentView.loadContent(getNovelContent(position));
        }

        @Override
        public int getItemCount() {
            return getNovelList() == null ? 0 : getNovelList().size();
        }

        private NovelContent getNovelContent(int position) {
            NovelContent novel = null;
            if(getNovelList() != null) {
                if(position >= 0 && position < getNovelList().size()) {
                    novel = getNovelList().get(position);
                }
            }
            return novel;
        }

        private int indexPosition(NovelContent content) {
            int index = 0;
            if(getNovelList() != null && content != null) {
                index = getNovelList().indexOf(content);
            }
            return index;
        }

        private List<NovelContent> getNovelList() {
            return NovelListActivity.novelList == null ? null : NovelListActivity.novelList.list;
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        private NovelContentView contentView;
        private MyViewHolder(NovelContentView itemView) {
            super(itemView);
            this.contentView = itemView;
        }
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
