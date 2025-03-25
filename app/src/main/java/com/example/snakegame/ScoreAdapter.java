package com.example.snakegame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private List<Score> scoreList;

    // Constructor to initialize the list
    public ScoreAdapter(List<Score> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Score score = scoreList.get(position);
        holder.emailTextView.setText(score.getEmail());
        holder.timeTextView.setText(score.getTime());
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView, timeTextView;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}
