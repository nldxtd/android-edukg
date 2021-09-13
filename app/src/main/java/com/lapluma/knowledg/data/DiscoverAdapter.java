package com.lapluma.knowledg.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Constant;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class DiscoverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> items = new ArrayList<>();

    private Context ctx;
    private DiscoverAdapter.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, ListItem obj, int position);
    }

    public void setOnItemClickListener(final DiscoverAdapter.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public DiscoverAdapter(Context context, List<ListItem> items) {
        this.items = items;
        ctx = context;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView badge_new;
        public TextView tv_parent;
        public TextView tv_sub;
        public View lyt_parent;

        public ItemViewHolder(View v) {
            super(v);
            badge_new = v.findViewById(R.id.badge_new);
            tv_parent = v.findViewById(R.id.tv_parent);
            tv_sub = v.findViewById(R.id.tv_sub);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_search, parent, false);
        vh = new DiscoverAdapter.ItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof DiscoverAdapter.ItemViewHolder) {
            DiscoverAdapter.ItemViewHolder view = (DiscoverAdapter.ItemViewHolder) holder;
            ListItem it = items.get(position);
            view.tv_parent.setText(ctx.getResources().getString((Constant.category2StringId.get(it.getCategory()))));
            view.tv_sub.setText(it.getLabel());
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(ArrayList<ListItem> newItems) {
        items = newItems;
        this.notifyDataSetChanged();
    }
}
