package com.lapluma.knowledg.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.HomeData.HistoryAdapter;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Cache;
import com.lapluma.knowledg.util.Network;
import com.lapluma.knowledg.util.Tool;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.*;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private MainPref mainPref;
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class HistoryInfo implements Serializable {
        private ArrayList<ListItem> historyList;
    }
    private HistoryInfo data;
    private Cache.ObjectCacheHelper<HistoryActivity.HistoryInfo> cacheHelper;
    private MyHandler handler = new HistoryActivity.MyHandler(this);

    class MyHandler extends Handler {
        public final static int MSG_NETWORK_OK = 2;
        public final static int MSG_CACHE_FETCHED = 3;
        public final static int MSG_SAVE_CACHE = 5;
        private final HistoryActivity owner;
        public MyHandler(HistoryActivity owner) {
            this.owner = owner;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_NETWORK_OK:
                    owner.fetchData();
                    break;
                case MSG_CACHE_FETCHED:
                    historyAdapter.setItems(data.historyList);
                    break;
                case MSG_SAVE_CACHE:
                    owner.saveCache();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        cacheHelper = new Cache.ObjectCacheHelper<HistoryInfo>(this, "history");
        data = new HistoryInfo();
        initToolbar();
        initComponent();
        tryLoadCache();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("历史记录");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tool.setSystemBarColor(this);
    }

    private void initComponent() {
        mainPref = new MainPref(this);
        historyRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, new ArrayList<ListItem>(), this);
        historyRecyclerView.setAdapter(historyAdapter);
        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, ListItem obj) {
                Intent intent = new Intent(HistoryActivity.this, InstanceInfoActivity.class);
                intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_NAME, obj.getLabel());
                intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_SUBJECT, obj.getCategory());
                startActivity(intent);
            }
        });
    }

    public void fetchData() {
        Network.JsonPostman postman = new Network.JsonPostman(this, Network.Api.POST_HISTORY);
        postman.put("token", mainPref.getToken());
        postman.post(new TypeReference<Network.RestResponse<ArrayList<ListItem>>>() {
        }, new Network.CallbackOnResponse<ArrayList<ListItem>>() {
            @Override
            public void processResponse(Network.RestResponse<ArrayList<ListItem>> restResponse) {
                ArrayList<ListItem> items = restResponse.getData();
                data.historyList = items;
                Message msg = new Message();
                msg.what = MyHandler.MSG_SAVE_CACHE;
                handler.sendMessage(msg);
                runOnUiThread(() -> {
                    historyAdapter.setItems(items);
                });
            }
        });
    }

    public void tryLoadCache() {
        new Thread() {
            @Override
            public void run() {
                HistoryInfo cacheData = new HistoryInfo();
                if(Network.isNetworkAvailable()) {
                    Message msg = new Message();
                    msg.what = MyHandler.MSG_NETWORK_OK;
                    handler.sendMessage(msg);
                } else {
                    if((cacheData = cacheHelper.loadObj("historyCache")) != null) {
                        Tool.makeSnackBar(HistoryActivity.this, "无网络链接，从缓存中获取");
                        data = cacheData;
                        Message msg = new Message();
                        msg.what = MyHandler.MSG_CACHE_FETCHED;
                        handler.sendMessage(msg);
                    } else {
                        Tool.makeSnackBar(HistoryActivity.this, "缓存中没有数据");
                    }
                }
            }
        }.start();
    }

    public void saveCache() {
        new Thread() {
            @Override
            public void run() {
                cacheHelper.saveObject("historyCache", data);
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return true; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}