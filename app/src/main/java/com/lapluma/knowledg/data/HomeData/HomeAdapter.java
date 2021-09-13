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
import androidx.fragment.app.*;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.model.ChatMessage;
import com.lapluma.knowledg.model.ListItem;
import com.lapluma.knowledg.util.Constant;
import com.lapluma.knowledg.util.Network;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FOOTER = 2;

    private Activity owner;
    private List<ListItem> mData = new ArrayList<ListItem>();
    private boolean mIsShowFooter = true;
    private OnItemClickListener mOnItemClickListener;

    public HomeAdapter(Activity owner) {
        this.owner = owner;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public ListItem getItem(int position) {
        return mData.get(position);
    }

    public void setData(List<ListItem> data) {
        mData = new ArrayList<ListItem>(data);
        this.notifyDataSetChanged();
    }

    public void appendData(List<ListItem> data) {
        int pos = mData.size();
        mData.addAll(data);
        this.notifyItemRangeChanged(pos, getItemCount());
    }

    public void removeItem(int position) {
        mData.remove(position);
        this.notifyItemRemoved(position);
    }

    public void setRead(int position, boolean has_read) {
        ListItem item = getItem(position);
        item.setHasread(has_read);
        mData.set(position, item);
    }

    public void setFooterVisible(boolean visible) {
        if (mIsShowFooter != visible) {
            mIsShowFooter = visible;
            if (mIsShowFooter)
                this.notifyItemInserted(mData.size());
            else
                this.notifyItemRemoved(mData.size());
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view;
            if(mIsShowFooter) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nomore, parent, false);
            }
            return new FooterViewHolder(view);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mView;
        TextView mCategory, mLabel;
        ImageView mImage;
        int mCurrentPosition = -1;
        public ItemViewHolder(View view) {
            super(view);
            mView = view;
            mCategory = view.findViewById(R.id.text_category);
            mLabel = view.findViewById(R.id.text_label);
            mImage = view.findViewById(R.id.image_holder);
            view.setOnClickListener(this);
        }

        public void setBackgroundColor(int color) {
            mView.setBackgroundColor(color);
        }

        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, this.getLayoutPosition());
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }
    }

    public boolean isShowFooter() {
        return mIsShowFooter;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ListItem item1 = mData.get(position);
            final ItemViewHolder item = (ItemViewHolder) holder;
            item.mLabel.setText(item1.getLabel());
            item.mCategory.setText(owner.getString(Constant.category2StringId.get(item1.getCategory())));
            Drawable drawable = owner.getResources().getDrawable(R.drawable.bg_no_item_city);
            item.mImage.setImageDrawable(drawable);
            Network.ImageLoader loader = new Network.ImageLoader(owner);
            loader.setTargetView(item.mImage);
            loader.load(item1.getLabel());
            item.setBackgroundColor(owner.getResources().getColor(item1.getHasread() ? R.color.grey_300 : R.color.white)); // text color switch, not background
            item.mCurrentPosition = position;
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position == mData.size())
            return TYPE_FOOTER;
        else
            return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

}
