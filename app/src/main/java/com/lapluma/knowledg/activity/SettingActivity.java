package com.lapluma.knowledg.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.AdapterListDrag;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.helper.DragItemTouchHelper;
import com.lapluma.knowledg.util.Constant;
import com.lapluma.knowledg.util.Tool;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {

    private RecyclerView ableRecyclerView;
    private RecyclerView unableRecyclerView;
    private AdapterListDrag ableAdapter;
    private AdapterListDrag unableAdapter;
    private ItemTouchHelper ableItemTouchHelper;
    private ItemTouchHelper unableItemTouchHelper;
    private MainPref homePref;

    ArrayList<String> ableList;
    ArrayList<String> unableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        homePref = new MainPref(this);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("学科设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tool.setSystemBarColor(this);
    }

    private void initComponent() {
        ableRecyclerView = (RecyclerView) findViewById(R.id.ableRecyclerView);
        unableRecyclerView = (RecyclerView) findViewById(R.id.unableRecyclerView);
        ableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        unableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ableList = homePref.getList();
        unableList = homePref.getUnavaiList();

        ableRecyclerView.setItemAnimator(new DefaultItemAnimator());
        unableRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ableAdapter = new AdapterListDrag(this, ableList,1);
        unableAdapter = new AdapterListDrag(this, unableList,2);

        ableRecyclerView.setAdapter(ableAdapter);
        unableRecyclerView.setAdapter(unableAdapter);

        ableAdapter.setOnItemClickListener(new AdapterListDrag.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String obj) {
                unableList.add(obj);
                int pos = ableList.indexOf(obj);
                ableList.remove(obj);
                ableAdapter.notifyItemRemoved(pos);
                unableAdapter.notifyItemInserted(unableList.size());
                homePref.setList(ableList);
                homePref.setUnavaList(unableList);
            }
        });

        unableAdapter.setOnItemClickListener(new AdapterListDrag.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String obj) {
                ableList.add(obj);
                int pos = unableList.indexOf(obj);
                unableList.remove(obj);
                unableAdapter.notifyItemRemoved(pos);
                ableAdapter.notifyItemInserted(ableList.size());
                homePref.setList(ableList);
                homePref.setUnavaList(unableList);
            }
        });

        ableAdapter.setDragListener(new AdapterListDrag.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                ableItemTouchHelper.startDrag(viewHolder);
            }
        });

        unableAdapter.setDragListener(new AdapterListDrag.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                unableItemTouchHelper.startDrag(viewHolder);
            }
        });

        ItemTouchHelper.Callback callback = new DragItemTouchHelper(ableAdapter);
        ableItemTouchHelper = new ItemTouchHelper(callback);
        ableItemTouchHelper.attachToRecyclerView(ableRecyclerView);

        ItemTouchHelper.Callback ucallback = new DragItemTouchHelper(unableAdapter);
        unableItemTouchHelper = new ItemTouchHelper(ucallback);
        unableItemTouchHelper.attachToRecyclerView(unableRecyclerView);
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

    @Override
    public void onBackPressed() {
        setResult(Constant.ResultCode.REFRESH_HOME);
        super.onBackPressed();
    }
}