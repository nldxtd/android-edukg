package com.lapluma.knowledg.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.lifecycle.Observer;

import com.google.android.material.tabs.TabLayout;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.HomeData.SearchAdapter;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Constant;
import com.lapluma.knowledg.util.Network;
import com.lapluma.knowledg.util.Network.JsonPostman;
import com.lapluma.knowledg.util.Network.RestResponse;
import com.lapluma.knowledg.util.Network.CallbackOnResponse;
import com.lapluma.knowledg.util.Network.Api;
import com.lapluma.knowledg.util.Tool;
import com.lapluma.knowledg.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private MainPref mainPref; // main preference stores user-auth-related data
    private ActivityMainBinding binding;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private DrawerLayout drawer; // left side drawer

    private TextView mHistoryText;
    private TextView mClearText;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private RecyclerView searchRecycler;
    private SearchAdapter searchAdapter;
    private LinearLayout fragmentWrapper;
    private ImageView mCloseButton;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private String mKeyWord = "";
    private PagerAdapter mPagerAdapter;
    private HomeListFragment mFragment;

    private ArrayList<String> titles = new ArrayList<>();
    public ArrayList<ListItem> search_items = new ArrayList<>();
    private MutableLiveData<Integer> state; // 0 for home page, 1 for history, 2 for search data

    private TextView et_subject;
    private TextView et_sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // render view according to layout
        mainPref = new MainPref(this); // PREF_MAIN
        state = new MutableLiveData<Integer>(0);
        final Observer<Integer> stateObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer newState) {
                switch (newState) {
                    case 0: // home page
                        runOnUiThread(()->{
                            mHistoryText.setVisibility(View.GONE);
                            mClearText.setVisibility(View.GONE);
                            searchRecycler.setVisibility(View.GONE);
                            fragmentWrapper.setVisibility(View.VISIBLE);
                            findViewById(R.id.check_box).setVisibility(View.GONE);
                            et_sort.setText("默认排序");
                            et_subject.setText("所有");
                        });
                        break;
                    case 1: // history page
                        runOnUiThread(()->{
                            mHistoryText.setVisibility(View.GONE);
                            mClearText.setVisibility(View.GONE);
                            searchRecycler.setVisibility(View.VISIBLE);
                            fragmentWrapper.setVisibility(View.GONE);
                            findViewById(R.id.check_box).setVisibility(View.GONE);
                            et_sort.setText("默认排序");
                            et_subject.setText("所有");
                        });
                        break;
                    case 2: // result page
                        runOnUiThread(()->{
                            mHistoryText.setVisibility(View.GONE);
                            mClearText.setVisibility(View.GONE);
                            searchRecycler.setVisibility(View.VISIBLE);
                            fragmentWrapper.setVisibility(View.GONE);
                            findViewById(R.id.check_box).setVisibility(View.VISIBLE);
                        });
                        break;
                }
            }
        };
        state.observe(this, stateObserver);
        state.setValue(0);
        initToolbar();
        initDrawerMenu();
        initComponentMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reRenderDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_main, menu);     // inflater is used for filling a view with an xml file
        Tool.changeMenuIconColor(menu, Color.WHITE);
        Tool.changeOverflowMenuIconColor(toolbar, Color.WHITE);

        mSearchItem = menu.findItem(R.id.action_search);
        mSearchItem.setVisible(true);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mKeyWord = query;
                searchAdapter.setItems(new ArrayList<>());
                mFragment.setKeyword(query);
                state.setValue(2);
                JsonPostman postman = new JsonPostman(MainActivity.this, Api.POST_SEARCH);
                postman.put("token", mainPref.getToken());
                postman.put("course", "chinese");
                postman.put("searchKey", mKeyWord);
                postman.post(new TypeReference<Network.RestResponse<ArrayList<ListItem>>>() {
                }, new Network.CallbackOnResponse<ArrayList<ListItem>>() {
                    @Override
                    public void processResponse(Network.RestResponse<ArrayList<ListItem>> restResponse) {
                        ArrayList<ListItem> itemList = restResponse.getData();
                        if(itemList.size() > 0) {
                            MainActivity.this.search_items = itemList;
                            runOnUiThread(() -> {
                                MainActivity.this.setFilterAndSortItems(itemList, et_subject.getText().toString(), et_sort.getText().toString());
                            });
                        } else {
                            MainActivity.this.search_items = itemList;
                            runOnUiThread(()-> {
                                if (state.getValue() == 2) {
                                    et_sort.setText("默认排序");
                                    et_subject.setText("所有");
                                    findViewById(R.id.check_box).setVisibility(View.GONE);
                                    mHistoryText.setText("没有找到结果～");
                                    mHistoryText.setVisibility(View.VISIBLE);
                                    searchRecycler.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapter.setItems(new ArrayList<>());
                searchAdapter.notifyDataSetChanged();
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                state.setValue(0);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                state.setValue(1);
                JsonPostman postman = new JsonPostman(MainActivity.this, Api.POST_SEARCH_HISTORY);
                postman.put("token", mainPref.getToken());
                postman.post(new TypeReference<Network.RestResponse<ArrayList<ListItem>>>() {
                }, new Network.CallbackOnResponse<ArrayList<ListItem>>() {
                    @Override
                    public void processResponse(Network.RestResponse<ArrayList<ListItem>> restResponse) {
                        ArrayList<ListItem> itemList = restResponse.getData();
                        if(itemList.size() > 0) {
                            if(state.getValue() == 1) {
                                runOnUiThread(() -> {
                                    mHistoryText.setVisibility(View.GONE);
                                    mClearText.setVisibility(View.VISIBLE);
                                    searchAdapter.setItems(itemList);
                                });
                            }
                        } else {
                            if(state.getValue() == 1) {
                                runOnUiThread(()->{
                                    mHistoryText.setText("没有历史记录");
                                    mHistoryText.setVisibility(View.VISIBLE);
                                    mClearText.setVisibility(View.GONE);
                                    searchAdapter.setItems(itemList);
                                });
                            }
                        }
                    }
                });
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /** this func is called by system each time invalidateOptionsMenu() is called.
         */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void reRenderDrawer() {
        initDrawerMenu();
        drawer.closeDrawers();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Tool.setSystemBarColorInt(this, Color.parseColor("#0A0A0A"));
    }

    private void initDrawerMenu() {
        /** init the menu in the drawer, including texts and avatar in the header.
         * if user not signed in, set all the functional menu items as invisible,
         * and set sign in / up item visible.
         * see more: https://developer.android.com/guide/topics/ui/menus?hl=zh-cn
         */
        final NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (!mainPref.isSignedIn()) {
            nav_view.getMenu().setGroupVisible(R.id.mngrp_drawer_1, true);
            nav_view.getMenu().setGroupVisible(R.id.mngrp_drawer_2, false);
            ((TextView) nav_view.getHeaderView(0).findViewById(R.id.header_username)).setText(getString(R.string.text_not_signed_in));
            ((TextView) nav_view.getHeaderView(0).findViewById(R.id.header_tip)).setText(getString(R.string.text_remind_sign_in));
        }
        else {
            nav_view.getMenu().setGroupVisible(R.id.mngrp_drawer_1, false);
            nav_view.getMenu().setGroupVisible(R.id.mngrp_drawer_2, true);
            ((TextView) nav_view.getHeaderView(0).findViewById(R.id.header_username)).setText(mainPref.getUsername());
            ((TextView) nav_view.getHeaderView(0).findViewById(R.id.header_tip)).setText(getString(R.string.text_user_bio));
        }
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            /** handle menu item selected */
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                int item_id = item.getItemId();
                if (item_id == R.id.action_sign_in_up) {
                    Intent goToSignInUpPage = new Intent(MainActivity.this, SignInUpActivity.class);
                    startActivity(goToSignInUpPage);
                }else if (item_id == R.id.action_collection) {
                    Intent intent = new Intent(MainActivity.this, CollectionActivity.class);
                    startActivity(intent);
                }else if (item_id == R.id.action_chat_bot) {
                    Intent goToChatBotPage = new Intent(MainActivity.this, ChatBotActivity.class);
                    startActivity(goToChatBotPage);
                } else if (item_id == R.id.action_history) {
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                } else if (item_id == R.id.action_about) {
                    // todo
                    // about me page
                    Tool.makeSnackBar(MainActivity.this,"Nothing here.");
                } else if (item_id == R.id.action_sign_out) {
                    JsonPostman postman = new JsonPostman(MainActivity.this, Api.POST_SIGN_OUT);
                    postman.put("token", mainPref.getToken());
                    postman.post(new TypeReference<RestResponse<Object>>() {}, new CallbackOnResponse<Object>() {
                        @Override
                        public void processResponse(RestResponse<Object> restResponse) {
                            mainPref.setSignedIn(false);
                            mainPref.setUsername("");
                            mainPref.setToken("");
                            runOnUiThread(MainActivity.this::reRenderDrawer); // only render ui in main thread!
                        }
                    });
                } else if (item_id == R.id.action_setting) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivityForResult(intent, 0);
                } else if (item_id == R.id.action_discover) {
                    Intent intent = new Intent(MainActivity.this, DiscoverActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constant.ResultCode.REFRESH_HOME) {
            initComponentMenu();
        }
    }

    private void initComponentMenu() {
        fragmentWrapper = findViewById(R.id.fragment_wrapper);
        mHistoryText = (TextView) findViewById(R.id.history_list);
        mClearText = (TextView) findViewById(R.id.clear_history);
        mClearText.setOnClickListener((view)->{
            JsonPostman postman = new JsonPostman(MainActivity.this, Api.POST_CLEAR);
            postman.put("token", mainPref.getToken());
            postman.post(new TypeReference<RestResponse<Object>>() {}, new CallbackOnResponse<Object>() {
                @Override
                public void processResponse(RestResponse<Object> restResponse) {
                }
            });
            this.search_items = new ArrayList<>();
            searchAdapter.setItems(this.search_items);
        });
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        et_subject = findViewById(R.id.et_subject);
        et_sort = findViewById(R.id.et_sort);
        titles.clear();
        titles.add("all");
        titles.addAll(mainPref.getList());
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), titles);
        mViewPager.setOffscreenPageLimit(9);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.removeAllTabs();
        for(int i = 0; i < titles.size(); i++)
            mTabLayout.addTab(mTabLayout.newTab().setText(getString(Constant.category2StringId.get(titles.get(i)))));
        mViewPager.setAdapter(mPagerAdapter);

        searchRecycler = findViewById(R.id.search_recycler);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchRecycler.setNestedScrollingEnabled(false);
        searchAdapter = new SearchAdapter(this, search_items);
        searchRecycler.setAdapter(searchAdapter);
        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ListItem obj, int position) {
                if(obj.getCategory().equals("")) {
                    mKeyWord = obj.getLabel();
                    mSearchView.setQuery(mKeyWord, true);
                    return;
                }
                Intent intent = new Intent(MainActivity.this, InstanceInfoActivity.class);
                intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_NAME, obj.getLabel());
                intent.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_SUBJECT, obj.getCategory());
                startActivity(intent);
                et_sort.setText("默认排序");
                et_subject.setText("所有");
            }
        });

        et_subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubjectDialog(v);
            }
        });

        et_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog(v);
            }
        });
    }

    private void showSubjectDialog(View v) {
        final String[] zhArray = new String[] {
                "所有", "语文", "数学", "英语", "物理", "化学", "地理", "生物", "历史", "政治"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("学科");
        builder.setSingleChoiceItems(zhArray, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(zhArray[i]);
                dialogInterface.dismiss();
                setFilterAndSortItems(MainActivity.this.search_items, zhArray[i], et_sort.getText().toString());
            }
        });
        builder.show();
    }

    private void showSortDialog(View v) {
        final String[] array = new String[]{
            "默认排序", "长度升序", "长度降序", "A-Z", "Z-A"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("排序方式");
        builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
                setFilterAndSortItems(MainActivity.this.search_items, et_subject.getText().toString(), et_sort.getText().toString());
            }
        });
        builder.show();
    }

    private void setFilterAndSortItems(ArrayList<ListItem> items, String subject, String sort_type) {
        // items is the init items
        // subject all, chinese ... politics
        // sort_type from 0 ... 4, 0 is the default
        // generate an ArrayList<ListItem> to be set
        List<ListItem> filteredItems;
        if(subject.equals("所有") || subject.equals("all")){
            filteredItems = items;
        } else {
            filteredItems = items.stream().filter((ListItem i) -> {
                return getString(Constant.category2StringId.get((i.getCategory()))).equals(subject);
            }).collect(Collectors.toList());
        }
        switch (sort_type){
            case "默认排序":
                break;
            case "长度升序":
                Collections.sort(filteredItems, new Comparator<ListItem>(){
                    @Override
                    public int compare(ListItem lhs, ListItem rhs){
                        return Integer.compare(lhs.getLabel().length(), rhs.getLabel().length());
                    }
                });
                break;
            case "长度降序":
                Collections.sort(filteredItems, new Comparator<ListItem>(){
                    @Override
                    public int compare(ListItem lhs, ListItem rhs){
                        return Integer.compare(rhs.getLabel().length(), lhs.getLabel().length());
                    }
                });
                break;
            case "A-Z":
                Collections.sort(filteredItems, new Comparator<ListItem>(){
                    @Override
                    public int compare(ListItem lhs, ListItem rhs){
                        try{
                            String s1 = new String(lhs.getLabel().toString().getBytes("GB2312"), "ISO-8859-1");
                            String s2 = new String(rhs.getLabel().toString().getBytes("GB2312"), "ISO-8859-1");
                            return s1.compareTo(s2) >0 ?1:-1;
                        }catch(Exception e){return lhs.getLabel().compareTo(rhs.getLabel())>0?1:-1;}

                    }
                });
                break;
            case "Z-A":
                Collections.sort(filteredItems, new Comparator<ListItem>(){
                    @Override
                    public int compare(ListItem lhs, ListItem rhs){
                        try{
                            String s1 = new String(lhs.getLabel().toString().getBytes("GB2312"), "ISO-8859-1");
                            String s2 = new String(rhs.getLabel().toString().getBytes("GB2312"), "ISO-8859-1");
                            return s1.compareTo(s2) <0 ?1:-1;
                        }catch(Exception e){return lhs.getLabel().compareTo(rhs.getLabel())>0?1:-1;}
                    }
                });
                break;
        }
        searchAdapter.setItems((ArrayList<ListItem>) filteredItems);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {


        private List<String> mCategories;

        public PagerAdapter(FragmentManager fm, ArrayList<String> list) {
            super(fm);
            mCategories = list;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(Constant.category2StringId.get(mCategories.get(position)));
        }

        @Override
        public Fragment getItem(int position) {
            String mCourse = mCategories.get(position);
            mFragment = HomeListFragment.newInstance(position, mKeyWord, mCourse, MainActivity.this);
            return mFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            HomeListFragment f = (HomeListFragment) super.instantiateItem(container, position);
            f.setKeyword(mKeyWord);
            return f;
        }

        @Override
        public int getCount() {
            return mCategories.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

}