package com.lapluma.knowledg.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.HomeData.HomeAdapter;
import com.lapluma.knowledg.data.HomeData.HomePresenter;
import com.lapluma.knowledg.data.HomeData.ItemListContract;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Tool;

import java.util.List;

public class HomeListFragment extends Fragment implements ItemListContract.View {

    public ItemListContract.Presenter mPresenter;
    private int mCategory;
    private String mKeyword;
    private String mCourse;
    public Activity owner;
    public TextView mNoMore;

    private int mLastClickPosition = -1;
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;
    private HomeAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private View mTextEmpty;

    public HomeListFragment() {
        // Required empty public constructor
    }

    public static HomeListFragment newInstance(int category, String keyword, String course, Activity owner) {
        Bundle args = new Bundle();
        HomeListFragment fragment = new HomeListFragment();
        fragment.owner = owner;
        args.putInt("category", category);
        args.putString("keyword", keyword);
        args.putString("course", course);
        fragment.setArguments(args);
        return fragment;
    }

    public void setKeyword(String keyword) {
        mKeyword = keyword;
        Bundle args = this.getArguments();
        if(args == null) {
            Bundle newArgs = new Bundle();
            newArgs.putString("keyword", mKeyword);
            this.setArguments(newArgs);
        } else {
            this.getArguments().putString("keyword", mKeyword);
        }
    }

    public String getKeyword() {
        return mKeyword;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // init some params
        super.onCreate(savedInstanceState);
        mLastClickPosition = -1;
        mCategory = getArguments().getInt("category");
        mKeyword = getArguments().getString("keyword");
        mCourse = getArguments().getString("course");
        mPresenter = new HomePresenter(this, mCourse, mKeyword, owner);
        // init adapters and click listener
        mAdapter = new HomeAdapter(this.getActivity());
        mAdapter.setOnItemClickListener((View itemView, int position)->{
            ListItem item = mAdapter.getItem(position);
            if(!item.getHasread()) {
                item.setHasread(true);
                mAdapter.notifyItemChanged(position);
            }
            this.mPresenter.openItemDetail(item, new Bundle());
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // attrs that are not so important
        TypedValue colorPrimary = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

        View view = inflater.inflate(R.layout.fragment_home_list, container, false);

        mTextEmpty = view.findViewById(R.id.text_empty);
        mTextEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

        mSwipeRefreshWidget = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeColors(getResources().getColor(colorPrimary.resourceId));
        mSwipeRefreshWidget.setOnRefreshListener(() -> {
            mSwipeRefreshWidget.setRefreshing(true);
            mPresenter.refreshItems();
        });

        mLayoutManager = new LinearLayoutManager(getContext());
        mNoMore = (TextView) view.findViewById(R.id.no_more_items);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if (lastVisibleItem == mAdapter.getItemCount() - 1 &&
                            mAdapter.isShowFooter()  && !mPresenter.isLoading()) { //delete ISFOOTER
                        mPresenter.requireMoreItems();
                    }
                }
            }
        });
        mPresenter.subscribe();
        return view;
    }

    @Override
    public void setPresenter(ItemListContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    public void start(Intent intent, Bundle options) {
        startActivity(intent, options);
    }

    @Override
    public void setItemList(List<ListItem> list) {
        mAdapter.setData(list);
        mTextEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void appendItemList(List<ListItem> list) {
        mAdapter.appendData(list);
    }

    @Override
    public void resetItemRead(int pos, boolean has_read) {
        if (mAdapter.getItem(pos).getHasread() != has_read) {
            mAdapter.setRead(pos, has_read);
            mAdapter.notifyItemChanged(pos);
        }
    }

    @Override
    public void onSuccess(boolean loadCompleted) {
        mAdapter.setFooterVisible(!loadCompleted);
        mSwipeRefreshWidget.setRefreshing(false);
    }

    @Override
    public void onError() {
        mSwipeRefreshWidget.setRefreshing(false);
        Toast.makeText(getContext(), "获取实体失败，请稍后再试", Toast.LENGTH_SHORT).show();
    }
}