package com.lapluma.knowledg.data;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lapluma.knowledg.R;
import com.lapluma.knowledg.activity.ExerciseActivity;
import com.lapluma.knowledg.model.Question;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private List<Question> localDataSet;
    private final ExerciseActivity owner;

    @Getter
    public class ViewHolder extends RecyclerView.ViewHolder {
        @Setter private String answer;
        private final TextView idx;
        private final TextView body;
        private final TextView a;
        private final TextView b;
        private final TextView c;
        private final TextView d;
        private final Button btnA;
        private final Button btnB;
        private final Button btnC;
        private final Button btnD;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            idx = view.findViewById(R.id.text_idx);
            body = view.findViewById(R.id.text_body);
            a = view.findViewById(R.id.text_a);
            b = view.findViewById(R.id.text_b);
            c = view.findViewById(R.id.text_c);
            d = view.findViewById(R.id.text_d);
            btnA = view.findViewById(R.id.btn_a);
            btnA.setBackgroundTintList(ColorStateList.valueOf(view.getContext().getColor(R.color.grey_5)));
            btnA.setOnClickListener(clicked -> {
                revealAnswer(btnA);
            });
            btnB = view.findViewById(R.id.btn_b);
            btnB.setBackgroundTintList(ColorStateList.valueOf(view.getContext().getColor(R.color.grey_5)));
            btnB.setOnClickListener(clicked -> {
                revealAnswer(btnB);
            });
            btnC = view.findViewById(R.id.btn_c);
            btnC.setBackgroundTintList(ColorStateList.valueOf(view.getContext().getColor(R.color.grey_5)));
            btnC.setOnClickListener(clicked -> {
                revealAnswer(btnC);
            });
            btnD = view.findViewById(R.id.btn_d);
            btnD.setBackgroundTintList(ColorStateList.valueOf(view.getContext().getColor(R.color.grey_5)));
            btnD.setOnClickListener(clicked -> {
                revealAnswer(btnD);
            });
        }

        public void revealAnswer(Button clicked) {
            Button correct = btnA;
            if (answer.equals("B")) {
                correct = btnB;
            } else if (answer.equals("C")) {
                correct = btnC;
            } else {
                correct = btnD;
            }
            correct.setBackgroundTintList(ColorStateList.valueOf(correct.getContext().getResources().getColor(R.color.green_500)));
            correct.setTextColor(correct.getContext().getResources().getColor(R.color.white));
            if (clicked != correct) {
                clicked.setBackgroundTintList(ColorStateList.valueOf(correct.getContext().getResources().getColor(R.color.red_500)));
                clicked.setTextColor(correct.getContext().getResources().getColor(R.color.white));
                owner.newAnswerCorrect(false);
            } else {
                owner.newAnswerCorrect(true);
            }
            btnA.setClickable(false);
            btnB.setClickable(false);
            btnC.setClickable(false);
            btnD.setClickable(false);
        }
    }

    public ExerciseAdapter(List<Question> dataSet, ExerciseActivity owner) {
        localDataSet = dataSet;
        this.owner = owner;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ExerciseAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_question, viewGroup, false);
        return new ExerciseAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ExerciseAdapter.ViewHolder viewHolder, final int position) {
        // get element from your dataset at this position and replace the contents of the view with that element

        String idx = String.valueOf(position + 1) + ".";
        String qBody = localDataSet.get(position).getBody();
        String a = localDataSet.get(position).getA();
        String b = localDataSet.get(position).getB();
        String c = localDataSet.get(position).getC();
        String d = localDataSet.get(position).getD();
        viewHolder.setAnswer(localDataSet.get(position).getAnswer());
        viewHolder.getIdx().setText(idx);
        viewHolder.getBody().setText(qBody);
        viewHolder.getA().setText(a);
        viewHolder.getB().setText(b);
        viewHolder.getC().setText(c);
        viewHolder.getD().setText(d);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
