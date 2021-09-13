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
import com.lapluma.knowledg.model.RelatedInstance;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.util.Tool;

import lombok.*;

import java.util.List;

public class RelatedInstanceAdapter extends RecyclerView.Adapter<RelatedInstanceAdapter.ViewHolder> {
    private List<RelatedInstance> localDataSet;
    private final InstanceInfoActivity owner;

    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialRippleLayout ripple;
        private final ImageView icSubject;
        private final ImageView icObject;
        private final TextView name;
        private final TextView description;
        private final ImageView icNext;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            icSubject = view.findViewById(R.id.icon_subject);
            icObject = view.findViewById(R.id.icon_object);
            name = view.findViewById(R.id.text_label);
            description = view.findViewById(R.id.text_description);
            icNext = view.findViewById(R.id.ic_next);
            ripple = view.findViewById(R.id.ripple);
        }
    }
    public RelatedInstanceAdapter(List<RelatedInstance> dataSet, InstanceInfoActivity owner) {
        localDataSet = dataSet;
        this.owner = owner;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_related_instance, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // get element from your dataset at this position and replace the contents of the view with that element

        String label = localDataSet.get(position).getLabel();
        viewHolder.getName().setText(Tool.safeString(label, 15));
        viewHolder.getDescription().setText(localDataSet.get(position).getDes());
        if (localDataSet.get(position).getRel().equals("object")) { // set the icon to objective
            viewHolder.getIcSubject().setVisibility(View.GONE);
            viewHolder.getIcObject().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getIcSubject().setVisibility(View.VISIBLE);
            viewHolder.getIcObject().setVisibility(View.GONE);
        }
        viewHolder.getRipple().setOnClickListener((view) -> {
            Intent intoNextInstance = new Intent(owner, InstanceInfoActivity.class);
            intoNextInstance.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_NAME, label);
            intoNextInstance.putExtra(InstanceInfoActivity.IntentFields.INSTANCE_SUBJECT, owner.getSubject());
            owner.startActivityForResult(intoNextInstance, 0);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
