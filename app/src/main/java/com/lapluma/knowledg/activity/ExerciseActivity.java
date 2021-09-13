package com.lapluma.knowledg.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.ExerciseAdapter;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.data.RelatedInstanceAdapter;
import com.lapluma.knowledg.model.InstanceProperty;
import com.lapluma.knowledg.model.Question;
import com.lapluma.knowledg.model.RelatedInstance;
import com.lapluma.knowledg.util.Cache;
import com.lapluma.knowledg.util.Constant;
import com.lapluma.knowledg.util.Network;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ExerciseActivity extends AppCompatActivity {
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ExerciseData implements Serializable {
        /** a serializable class for cache saving. */
        private String name;
        private String subject;
        private ArrayList<Question> qList;
    }
    int answered = 0;
    int correct = 0;
    int qSize = 0;
    private ExerciseData data;
    private MainPref mainPref;
    private ExerciseAdapter adapter;
    private RecyclerView recycler;
    private RecyclerView.LayoutManager manager;
    private Cache.ObjectCacheHelper<ExerciseData> cacheHelper;

    public static class IntentFields {
        public final static String INSTANCE_NAME = "INSTANCE_NAME";
        public final static String INSTANCE_SUBJECT = "INSTANCE_SUBJECT";
    }

    private MyHandler handler = new MyHandler(this);

    static public class MyHandler extends Handler {
        public final static int MSG_CORRECT_ANSWER = 1;
        public final static int MSG_DATA_FETCHED = 2;
        public final static int MSG_NO_CACHE = 3;
        private final ExerciseActivity owner;
        private boolean DATA_FETCHED = false;
        private boolean DATA_IS_FROM_CACHE = false;

        public MyHandler(ExerciseActivity owner) {
            this.owner = owner;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DATA_FETCHED:
                    owner.renderRecycler();
                    DATA_FETCHED = true;
                    break;
                case MSG_NO_CACHE:
                    owner.fetchRemoteData();
                    break;
            }
            if (DATA_FETCHED && !DATA_IS_FROM_CACHE) {
                // do cache
                owner.saveCache();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        mainPref = new MainPref(this);
        cacheHelper = new Cache.ObjectCacheHelper<ExerciseData>(this, "exercise");
        initToolbar();

        data = new ExerciseData();

        Intent intent = getIntent();
        data.name = intent.getStringExtra(IntentFields.INSTANCE_NAME);
        data.subject = intent.getStringExtra(IntentFields.INSTANCE_SUBJECT);

        recycler = findViewById(R.id.recycler_question);

        findViewById(R.id.grp_ranking).setVisibility(View.GONE);

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
        Network.ImageLoader bannerLoader = new Network.ImageLoader(this);
        bannerLoader.setTargetView(findViewById(R.id.image_banner));
        bannerLoader.load(data.name);
    }

    public void tryLoadCache() {
        new Thread() {
            @Override
            public void run() {
                ExerciseData cacheData = new ExerciseData();
                if ((cacheData = cacheHelper.loadObj(data.name)) != null) {
                    data = cacheData;
                    qSize = data.qList.size();
                    Message msg = new Message();
                    msg.what = MyHandler.MSG_DATA_FETCHED;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = MyHandler.MSG_NO_CACHE;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    private void fetchRemoteData() {
        Network.JsonPostman postman = new Network.JsonPostman(this, Network.Api.POST_QUESTION);
        postman.put("token", mainPref.getToken());
        postman.put("question", data.name);
        postman.post(new TypeReference<Network.RestResponse<ArrayList<Question>>>() {}, new Network.CallbackOnResponse<ArrayList<Question>>() {
            @Override
            public void processResponse(Network.RestResponse<ArrayList<Question>> restResponse) {
                data.qList = restResponse.getData();
                qSize = data.qList.size();
                Message msg = new Message();
                msg.what = MyHandler.MSG_DATA_FETCHED;
                handler.sendMessage(msg);
            }
        });
    }

    private void renderRecycler() {
        if (qSize > 0) {
            manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            adapter = new ExerciseAdapter(data.qList, this);
            recycler.setLayoutManager(manager);
            recycler.setAdapter(adapter);
            findViewById(R.id.tip_no_exercise_fetched).setVisibility(View.GONE);
        }
    }

    public void saveCache() {
        new Thread() {
            @Override
            public void run() {
                cacheHelper.saveObject(data.name, data);
            }
        }.start();
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

    public void newAnswerCorrect(boolean isCorrect) {
        answered++;
        if (isCorrect) correct++;
        if (answered == qSize && qSize > 0) {
            ((TextView) findViewById(R.id.num_correct)).setText(String.valueOf(correct));
            ((TextView) findViewById(R.id.num_wrong)).setText(String.valueOf(qSize - correct));
            String rank = "A";
            if (0.9 * qSize - correct > 0.0) rank = "B";
            if (0.8 * qSize - correct > 0.0) rank = "C";
            if (0.7 * qSize - correct > 0.0) rank = "D";
            if (0.6 * qSize - correct > 0.0) rank = "E";
            ((TextView) findViewById(R.id.text_rank)).setText(rank);
            findViewById(R.id.grp_ranking).setVisibility(View.VISIBLE);
        }
    }

}