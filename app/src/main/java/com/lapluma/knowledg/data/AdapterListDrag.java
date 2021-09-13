package com.lapluma.knowledg.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.helper.DragItemTouchHelper;
import com.lapluma.knowledg.util.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterListDrag extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DragItemTouchHelper.MoveHelperAdapter {

    private ArrayList<String> items = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private OnStartDragListener mDragStartListener = null;
    private int view_type;
    private MainPref mainPref;

    public interface OnItemClickListener {
        void onItemClick(View view, String obj);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListDrag(Context context, ArrayList<String> items, int view_type) {
        this.items = items;
        this.view_type = view_type;
        this.mainPref = new MainPref(context);
        ctx = context;
    }

    public void setDragListener(OnStartDragListener dragStartListener) {
        this.mDragStartListener = dragStartListener;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder implements DragItemTouchHelper.TouchViewHolder {
        public ImageView image;
        public TextView name;
        public ImageButton bt_move;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            bt_move = (ImageButton) v.findViewById(R.id.bt_move);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(ctx.getResources().getColor(R.color.grey_5));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v;
        if(viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drag, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drag2, parent, false);
        }
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            final String p = items.get(position);
            view.name.setText(ctx.getResources().getString(Constant.category2StringId.get(p)));
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p);
                    }
                }
            });

            // Start a drag whenever the handle view it touched
            view.bt_move.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN && mDragStartListener != null) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.view_type;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        if(view_type == 1){
            mainPref.setList(items);
        } else {
            mainPref.setUnavaList(items);
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
}
