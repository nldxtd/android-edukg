package com.lapluma.knowledg.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.ExerciseAdapter;
import com.lapluma.knowledg.data.InstancePropertyAdapter;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.data.RelatedInstanceAdapter;
import com.lapluma.knowledg.model.InstanceProperty;
import com.lapluma.knowledg.model.RelatedInstance;
import com.lapluma.knowledg.util.Cache;
import com.lapluma.knowledg.util.Constant;
import com.lapluma.knowledg.util.EchartOptionUtil;
import com.lapluma.knowledg.util.Network;
import com.lapluma.knowledg.util.Network.ImageLoader;
import com.lapluma.knowledg.view.EchartView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class InstanceInfoActivity extends AppCompatActivity {
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class InstanceInfo implements Serializable {
        /** a serializable class for cache saving. */
        private boolean isStarred;
        private String name;
        private String subject;
        private ArrayList<InstanceProperty> ipList;
        private ArrayList<RelatedInstance> riList;
    }
    private InstanceInfo data;

    private EchartView lineChart;
    private FloatingActionButton starButton;

    private MainPref mainPref;

    private RelatedInstanceAdapter riAdapter;
    private RecyclerView riRecycler;
    private RecyclerView.LayoutManager riManager;

    private InstancePropertyAdapter ipAdapter;
    private RecyclerView ipRecycler;
    private RecyclerView.LayoutManager ipManager;

    private Cache.ObjectCacheHelper<InstanceInfo> cacheHelper;

    private MyHandler handler = new MyHandler(this);

    class MyHandler extends Handler {
        public final static int MSG_RI_DATA_FETCHED = 1;
        public final static int MSG_IP_DATA_FETCHED = 2;
        public final static int MSG_CACHE_FETCHED = 3;
        public final static int MSG_NO_CACHE = 4;
        public final static int MSG_STAR_DATA_FETCHED = 5;
        private final InstanceInfoActivity owner;
        private boolean RI_DATA_FETCHED = false;
        private boolean IP_DATA_FETCHED = false;
        private boolean STAR_DATA_FETCHED = false;
        private boolean DATA_IS_FROM_CACHE = false;

        public MyHandler(InstanceInfoActivity owner) {
            this.owner = owner;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RI_DATA_FETCHED:
                    owner.renderRiRecycler();
                    owner.renderGraph();
                    RI_DATA_FETCHED = true;
                    break;
                case MSG_IP_DATA_FETCHED:
                    owner.renderIpRecycler();
                    IP_DATA_FETCHED = true;
                    break;
                case MSG_CACHE_FETCHED:
                    owner.refreshStarButton();
                    owner.renderIpRecycler();
                    owner.renderRiRecycler();
                    owner.renderGraph();
                    DATA_IS_FROM_CACHE = true;
                    RI_DATA_FETCHED = true;
                    IP_DATA_FETCHED = true;
                    STAR_DATA_FETCHED = true;
                    break;
                case MSG_NO_CACHE:
                    owner.fetchStarData();
                    owner.fetchIpData();
                    owner.fetchRiData();
                    break;
                case MSG_STAR_DATA_FETCHED:
                    owner.refreshStarButton();
                    STAR_DATA_FETCHED = true;
            }
            if (IP_DATA_FETCHED && RI_DATA_FETCHED && STAR_DATA_FETCHED && !DATA_IS_FROM_CACHE) {
                // do cache
                owner.saveCache();
            }
        }
    }

    public static class IntentFields {
        public final static String INSTANCE_NAME = "INSTANCE_NAME";
        public final static String INSTANCE_SUBJECT = "INSTANCE_SUBJECT";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_info);
        mainPref = new MainPref(this);
        cacheHelper = new Cache.ObjectCacheHelper<InstanceInfo>(this, "instance");
        initToolbar();

        data = new InstanceInfo();

        Intent intent = getIntent();
        data.name = intent.getStringExtra(IntentFields.INSTANCE_NAME);
        data.subject = intent.getStringExtra(IntentFields.INSTANCE_SUBJECT);

        ipRecycler = findViewById(R.id.recycler_instance_property);
        riRecycler = findViewById(R.id.recycler_related_instance);
        lineChart = findViewById(R.id.graph_relatives);
        starButton = findViewById(R.id.fab);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Network.JsonPostman postman = new Network.JsonPostman(InstanceInfoActivity.this, data.isStarred? Network.Api.POST_UNSTAR: Network.Api.POST_STAR);
                postman.put("token", mainPref.getToken());
                postman.put("course", data.subject);
                postman.put("name", data.name);
                postman.post(new TypeReference<Network.RestResponse<Object>>() {}, new Network.CallbackOnResponse<Object>() {
                    @Override
                    public void processResponse(Network.RestResponse<Object> restResponse) {
                        data.isStarred = !data.isStarred;
                        runOnUiThread(InstanceInfoActivity.this::refreshStarButton);
                        saveCache();
                    }
                });
            }
        });

        findViewById(R.id.clickable_exercise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intoExercise = new Intent(InstanceInfoActivity.this, ExerciseActivity.class);
                intoExercise.putExtra(ExerciseActivity.IntentFields.INSTANCE_NAME, data.name);
                intoExercise.putExtra(ExerciseActivity.IntentFields.INSTANCE_SUBJECT, data.subject);
                startActivityForResult(intoExercise, 0);
            }
        });

        renderBanner();

        tryLoadCache();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void renderBanner() {
        ((TextView) findViewById(R.id.text_instance_name)).setText(data.name);
        ((TextView) findViewById(R.id.text_instance_subject)).setText(getString(Constant.category2StringId.get(data.subject)));
        ImageLoader bannerLoader = new ImageLoader(this);
        bannerLoader.setTargetView(findViewById(R.id.image_banner));
        bannerLoader.load(data.name);
    }

    private void fetchStarData() {
        Network.JsonPostman postman = new Network.JsonPostman(this, Network.Api.POST_IS_STARRED);
        postman.put("token", mainPref.getToken());
        postman.put("course", data.subject);
        postman.put("name", data.name);
        postman.post(new TypeReference<Network.RestResponse<Boolean>>() {}, new Network.CallbackOnResponse<Boolean>() {
            @Override
            public void processResponse(Network.RestResponse<Boolean> restResponse) {
                data.isStarred = restResponse.getData();
                Message msg = new Message();
                msg.what = MyHandler.MSG_STAR_DATA_FETCHED;
                handler.sendMessage(msg);
            }
        });
    }

    private void fetchIpData() {
        Network.JsonPostman postman = new Network.JsonPostman(this, Network.Api.POST_INFO_PROPERTY);
        postman.put("token", mainPref.getToken());
        postman.put("course", data.subject);
        postman.put("name", data.name);
        postman.post(new TypeReference<Network.RestResponse<ArrayList<InstanceProperty>>>() {}, new Network.CallbackOnResponse<ArrayList<InstanceProperty>>() {
            @Override
            public void processResponse(Network.RestResponse<ArrayList<InstanceProperty>> restResponse) {
                data.ipList = restResponse.getData();
                Message msg = new Message();
                msg.what = MyHandler.MSG_IP_DATA_FETCHED;
                handler.sendMessage(msg);
            }
        });
    }

    private void fetchRiData() {
        Network.JsonPostman postman = new Network.JsonPostman(this, Network.Api.POST_INFO_RELATION);
        postman.put("token", mainPref.getToken());
        postman.put("course", data.subject);
        postman.put("name", data.name);
        postman.post(new TypeReference<Network.RestResponse<ArrayList<RelatedInstance>>>() {}, new Network.CallbackOnResponse<ArrayList<RelatedInstance>>() {
            @Override
            public void processResponse(Network.RestResponse<ArrayList<RelatedInstance>> restResponse) {
                data.riList = restResponse.getData();
                Message msg = new Message();
                msg.what = MyHandler.MSG_RI_DATA_FETCHED;
                handler.sendMessage(msg);
            }
        });
    }

    private void renderGraph() {
        lineChart.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                lineChart.refreshEchartsWithOption(EchartOptionUtil.getGraphOptions(data.name, data.riList));
            }
        });
    }

    private void renderIpRecycler() {
        ipManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        ipAdapter = new InstancePropertyAdapter(data.ipList, this);
        ipRecycler.setLayoutManager(ipManager);
        ipRecycler.setAdapter(ipAdapter);
    }

    private void renderRiRecycler() {
        riManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        riAdapter = new RelatedInstanceAdapter(data.riList, this);
        riRecycler.setLayoutManager(riManager);
        riRecycler.setAdapter(riAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_instance_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_home) {
            setResult(Constant.ResultCode.NAVIGATE_TO_HOME);
            finish();
        } else if (itemId == R.id.action_share) {
            // share api...
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constant.ResultCode.NAVIGATE_TO_HOME) { // meaning that user tapped "home" button in child activity, so do navigate back to home
            setResult(Constant.ResultCode.NAVIGATE_TO_HOME);
            finish();
        }
    }

    public String getSubject() {
        return data.subject;
    }

    public void saveCache() {
        new Thread() {
            @Override
            public void run() {
                cacheHelper.saveObject(data.name+"@"+data.subject, data);
            }
        }.start();
    }

    public void tryLoadCache() {
        new Thread() {
            @Override
            public void run() {
                InstanceInfo cacheData = new InstanceInfo();
                if ((cacheData = cacheHelper.loadObj(data.name+"@"+data.subject)) != null) {
                    data = cacheData;
                    Message msg = new Message();
                    msg.what = MyHandler.MSG_CACHE_FETCHED;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = MyHandler.MSG_NO_CACHE;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    private void refreshStarButton() {
        if (data.isStarred) {
            starButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 50, 50)));
        } else {
            starButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(50, 50, 50)));
        }
    }
}