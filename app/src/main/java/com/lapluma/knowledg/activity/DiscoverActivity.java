package com.lapluma.knowledg.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.DiscoverAdapter;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Network;
import com.lapluma.knowledg.util.Tool;

import java.util.ArrayList;

public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView resultRecyclerView;
    private DiscoverAdapter discoverAdapter;
    private Button discoverButton;
    private MainPref mainPref;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("实体发现");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tool.setSystemBarColor(this);
    }

    private void initComponent() {
        discoverButton = findViewById(R.id.button_discover);
        editText = findViewById(R.id.edit_text);
        mainPref = new MainPref(this);
        resultRecyclerView = (RecyclerView) findViewById(R.id.discoverRecycler);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        discoverAdapter = new DiscoverAdapter(this, new ArrayList<>());
        resultRecyclerView.setAdapter(discoverAdapter);
        discoverAdapter.setOnItemClickListener(new DiscoverAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ListItem obj, int position) {
                Intent intent = new Intent(DiscoverActivity.this, InstanceInfoActivity.class);
                intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_NAME, obj.getLabel());
                intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_SUBJECT, obj.getCategory());
                startActivity(intent);
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.clearFocus();
                String query_text = editText.getText().toString();
                Network.JsonPostman postman = new Network.JsonPostman(DiscoverActivity.this, Network.Api.POST_DISCOVER);
                postman.put("token", mainPref.getToken());
                postman.put("question", query_text);
                postman.post(new TypeReference<Network.RestResponse<ArrayList<ListItem>>>() {
                }, new Network.CallbackOnResponse<ArrayList<ListItem>>() {
                    @Override
                    public void processResponse(Network.RestResponse<ArrayList<ListItem>> restResponse) {
                        ArrayList<ListItem> items = restResponse.getData();
                        runOnUiThread(() -> {
                            discoverAdapter.setItems(items);
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}