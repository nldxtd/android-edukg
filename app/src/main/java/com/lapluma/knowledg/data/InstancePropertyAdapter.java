package com.lapluma.knowledg.data;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.lapluma.knowledg.activity.InstanceInfoActivity;
import com.lapluma.knowledg.model.InstanceProperty;
import com.lapluma.knowledg.model.RelatedInstance;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.util.Tool;

import lombok.*;

import java.util.List;

public class InstancePropertyAdapter extends RecyclerView.Adapter<InstancePropertyAdapter.ViewHolder> {
    private List<InstanceProperty> localDataSet;
    private final InstanceInfoActivity owner;

    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final TextView object;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            label = view.findViewById(R.id.text_label);
            object = view.findViewById(R.id.text_object);
        }
    }
    public InstancePropertyAdapter(List<InstanceProperty> dataSet, InstanceInfoActivity owner) {
        localDataSet = dataSet;
        this.owner = owner;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_instance_property, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // get element from your dataset at this position and replace the contents of the view with that element

        String label = localDataSet.get(position).getLabel();
        String object = localDataSet.get(position).getObject();
        viewHolder.getLabel().setText(label);
        viewHolder.getObject().setText(object);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}