package com.lapluma.knowledg.data.HomeData;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.AdapterListDrag;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Constant;
import com.lapluma.knowledg.util.Network;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ListItem> items = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Activity owner;

    public interface OnItemClickListener {
        void OnItemClick(View view, ListItem obj);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public HistoryAdapter(Context ctx, ArrayList<ListItem> items, Activity owner) {
        this.owner = owner;
        this.items = items;
        this.ctx = ctx;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView label;
        public TextView category;
        public View lyt_parent;

        public ItemViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            label = v.findViewById(R.id.label);
            category = v.findViewById(R.id.category);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_like, parent, false);
        vh = new HistoryAdapter.ItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof HistoryAdapter.ItemViewHolder) {
            HistoryAdapter.ItemViewHolder view = (HistoryAdapter.ItemViewHolder) holder;
            ListItem it = items.get(position);
            view.category.setText(ctx.getResources().getString((Constant.category2StringId.get(it.getCategory()))));
            view.label.setText(it.getLabel());
            Drawable drawable = owner.getResources().getDrawable(R.drawable.bg_no_item_city);
            view.image.setImageDrawable(drawable);
            Network.ImageLoader loader = new Network.ImageLoader(owner);
            loader.setTargetView(view.image);
            loader.load(it.getLabel());
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnItemClickListener != null) {
                        mOnItemClickListener.OnItemClick(view, items.get(position));
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
