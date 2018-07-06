package com.cabe.app.novel.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.ekxs.NovelDetail42kxsUseCase;
import com.cabe.app.novel.domain.ekxs.Rank42kxsUseCase;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;

import java.util.List;

public class RankActivity extends BaseActivity {
    private RadioGroup typeRadio;
    private SwipeRefreshLayout rankSwipe;
    private MyAdapter myAdapter;
    private ProgressDialog waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_list);
        setTitle("排行榜");
        initView();

        waiting = new ProgressDialog(this);
        waiting.setMessage("请稍候");

        changeType(0);
    }

    private void initView() {
        typeRadio = findViewById(R.id.activity_rank_list_type);
        rankSwipe = findViewById(R.id.activity_rank_list_swipe);
        RecyclerView rankRecycler = findViewById(R.id.activity_rank_list_recycler);

        typeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeType(checkedId);
            }
        });

        rankSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                changeType(typeRadio.getCheckedRadioButtonId());
            }
        });

        rankRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        myAdapter = new MyAdapter();
        rankRecycler.setAdapter(myAdapter);
    }

    private void changeType(int checkedId) {
        String sort = "sort1";
        switch (checkedId) {
            case R.id.activity_rank_list_type_xh:
                sort = "sort1";
                break;
            case R.id.activity_rank_list_type_wx:
                sort = "sort2";
                break;
            case R.id.activity_rank_list_type_ds:
                sort = "sort3";
                break;
            case R.id.activity_rank_list_type_yq:
                sort = "sort4";
                break;
            case R.id.activity_rank_list_type_ls:
                sort = "sort5";
                break;
            case R.id.activity_rank_list_type_wy:
                sort = "sort6";
                break;
            case R.id.activity_rank_list_type_kh:
                sort = "sort7";
                break;
            case R.id.activity_rank_list_type_zt:
                sort = "sort8";
                break;
            case R.id.activity_rank_list_type_tr:
                sort = "sort9";
                break;
            case R.id.activity_rank_list_type_xy:
                sort = "sort10";
                break;
        }
        loadRank(sort);
    }

    private void loadRank(String sort) {
        rankSwipe.setRefreshing(true);

        Rank42kxsUseCase useCase = new Rank42kxsUseCase(sort);
        useCase.execute(new ViewPresenter<List<NovelInfo>>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, List<NovelInfo> data) {
                myAdapter.setData(data);
            }
            @Override
            public void complete(CacheSource from) {
                rankSwipe.setRefreshing(false);
            }
        });
    }

    private void queryNovel(String url) {
        waiting.show();

        NovelDetail42kxsUseCase useCase = new NovelDetail42kxsUseCase(url);
        useCase.execute(new ViewPresenter<NovelInfo>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, NovelInfo data) {
                operateNovel(data);
            }
            @Override
            public void complete(CacheSource from) {
                waiting.dismiss();
            }
        });
    }

    private void operateNovel(final NovelInfo novelInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(novelInfo.title);
        builder.setItems(new String[]{ "添加书库", "取消"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                addNovel(novelInfo);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void addNovel(NovelInfo novelInfo) {
        Intent intent = new Intent();
        intent.putExtra(BaseActivity.KEY_EXTRA_GSON, novelInfo.toGson());
        setResult(RESULT_OK, intent);
        finish();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        private List<NovelInfo> novelList;
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
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_rank_list_novel, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            final NovelInfo itemData = getItemData(position);
            if(itemData == null) return;

            Glide.with(context).load(itemData.getPicUrl()).into(holder.pic);
            holder.tvTitle.setText(itemData.title);
            holder.tvAuthor.setText(String.valueOf("作者：" + itemData.author));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queryNovel(itemData.url);
                }
            });
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        private ImageView pic;
        private TextView tvTitle;
        private TextView tvAuthor;
        private MyHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.item_rank_list_novel_pic);
            tvTitle = itemView.findViewById(R.id.item_rank_list_novel_title);
            tvAuthor = itemView.findViewById(R.id.item_rank_list_novel_author);
        }
    }
}
