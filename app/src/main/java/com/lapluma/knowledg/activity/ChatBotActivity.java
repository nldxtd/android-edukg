package com.lapluma.knowledg.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.model.ChatMessage;
import com.lapluma.knowledg.util.Network;
import com.lapluma.knowledg.util.Tool;
import com.lapluma.knowledg.data.BotAdapter;
import com.lapluma.knowledg.util.Network.JsonPostman;
import com.lapluma.knowledg.util.Network.Api;
import com.lapluma.knowledg.util.Network.RestResponse;
import com.lapluma.knowledg.util.Network.CallbackOnResponse;

public class ChatBotActivity extends AppCompatActivity {

    private FloatingActionButton btn_send;
    private EditText et_content;
    private RecyclerView recycler_view;
    private ActionBar actionBar;
    private BotAdapter adapter;
    private MainPref mainPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainPref = new MainPref(this);
        setContentView(R.layout.activity_chat_bot);
        initComponent();

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle(null);
        Tool.setSystemBarColorInt(this, Color.parseColor("#054D44"));
    }

    public void initComponent() {
        recycler_view = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setHasFixedSize(true);

        adapter = new BotAdapter(this);
        recycler_view.setAdapter(adapter);
        adapter.insertItem(new ChatMessage(adapter.getItemCount(), Tool.getFormattedTimeEvent(System.currentTimeMillis()), "很高兴为您服务，请问有什么想问的？", false, true));

        btn_send = findViewById(R.id.btn_send);
        et_content = findViewById(R.id.text_content);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChat();
            }
        });
        et_content.addTextChangedListener(contentWatcher);

        (findViewById(R.id.lyt_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void sendChat() {
        final String message = et_content.getText().toString();
        if (message.isEmpty()) return;
        adapter.insertItem(
                new ChatMessage(
                        adapter.getItemCount(),
                        Tool.getFormattedTimeEvent(System.currentTimeMillis()),
                        message,
                        true,
                        adapter.getItemCount() % 5 == 0
                )
        );
        et_content.setText("");
        recycler_view.scrollToPosition(adapter.getItemCount() - 1);
        JsonPostman postman = new JsonPostman(ChatBotActivity.this, Api.POST_CHAT);
        postman.put("question", message);
        postman.put("token", mainPref.getToken());
        postman.post(new TypeReference<RestResponse<String>>() {}, new CallbackOnResponse<String>() {
            @Override
            public void processResponse(RestResponse<String> restResponse) {
                adapter.insertItem(
                        new ChatMessage(
                                adapter.getItemCount(),
                                Tool.getFormattedTimeEvent(System.currentTimeMillis()),
                                restResponse.getData(),
                                false,
                                adapter.getItemCount() % 5 == 0
                        )
                );
                runOnUiThread(()->{
                    recycler_view.scrollToPosition(adapter.getItemCount() - 1);
                });
            }
        });
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        hideKeyboard();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private TextWatcher contentWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(@NonNull Editable etd) {
            if (etd.toString().trim().length() == 0) {
                btn_send.setImageResource(R.drawable.ic_mic);
            } else {
                btn_send.setImageResource(R.drawable.ic_send);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}