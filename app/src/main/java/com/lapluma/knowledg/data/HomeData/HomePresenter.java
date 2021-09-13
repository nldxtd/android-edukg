package com.lapluma.knowledg.data.HomeData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lapluma.knowledg.activity.InstanceInfoActivity;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Network;

import java.util.List;
import java.util.ArrayList;

public class HomePresenter implements ItemListContract.Presenter {

    private final int PAGE_SIZE = 20;
    private ItemListContract.View mView;
    private String mCategory;
    private String mKeyword;
    private int mPageNo = 1;
    private boolean mLoading = false;
    private long mLastFetchStart;
    private Activity owner;
    private MainPref mainPref;
    private boolean noMoreItems;

    public HomePresenter(ItemListContract.View view, String category, String keyword, Activity owner) {
        this.mView = view;
        this.mCategory = category;
        this.mKeyword = keyword;
        this.owner = owner;
        this.mainPref = new MainPref(owner.getApplicationContext());

        view.setPresenter(this);
    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setKeyword(String keyword) {
        mKeyword = keyword;
        refreshItems();
    }

    @Override
    public void subscribe() {
        refreshItems();
    }

    @Override
    public void unsubscribe() {
        // nothing
    }

    @Override
    public void requireMoreItems() {
        mPageNo++;
        fetchItems();
    }

    @Override
    public void refreshItems() {
        mPageNo = 1;
        fetchItems();
    }

    @Override
    public void openItemDetail(ListItem item, Bundle options) {
        Intent intent = new Intent(owner, InstanceInfoActivity.class);
        intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_NAME, item.getLabel());
        intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_SUBJECT, item.getCategory());
        owner.startActivity(intent);
    }

    @Override
    public void fetchItemRead(int pos) {
        // fetch item as read
    }


    private void fetchItems() {
        // communicate with backend
        // need to fill the ItemList in case of mCategory/mKeyword/page_size
        // todo
        if (mPageNo == 1) {
            final long start = System.currentTimeMillis();
            mLoading = true;
            mLastFetchStart = start;
            Network.JsonPostman postman = new Network.JsonPostman(owner, Network.Api.POST_REFRESH);
            postman.put("token", mainPref.getToken());
            postman.put("course", mCategory);
            postman.put("capacity", 20);
            postman.put("page", mPageNo);
            postman.post(new TypeReference<Network.RestResponse<ArrayList<ListItem>>>() {
            }, new Network.CallbackOnResponse<ArrayList<ListItem>>() {
                @Override
                public void processResponse(Network.RestResponse<ArrayList<ListItem>> restResponse) {
                    ArrayList<ListItem> itemList = restResponse.getData();
                    mView.onSuccess(itemList.isEmpty());
                    owner.runOnUiThread(() -> {
                        mView.setItemList(itemList);
                    });
                }
            });
            mLoading = false;
        } else {
            final long start = System.currentTimeMillis();
            mLoading = true;
            mLastFetchStart = start;
            Network.JsonPostman postman = new Network.JsonPostman(owner, Network.Api.POST_GET_MORE);
            postman.put("token", mainPref.getToken());
            postman.put("course", mCategory);
            postman.put("capacity", 20);
            postman.put("page", mPageNo);
            postman.post(new TypeReference<Network.RestResponse<ArrayList<ListItem>>>() {
            }, new Network.CallbackOnResponse<ArrayList<ListItem>>() {
                @Override
                public void processResponse(Network.RestResponse<ArrayList<ListItem>> restResponse) {
                    ArrayList<ListItem> itemList = restResponse.getData();
                    mView.onSuccess(itemList.isEmpty());
                    owner.runOnUiThread(() -> {
                        mView.appendItemList(itemList);
                    });
                }
            });
            mLoading = false;
        }
    }
}
