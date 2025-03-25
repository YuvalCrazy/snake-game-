package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LevelActivity extends AppCompatActivity {

    private TextView timerTextView;
    private TextView levelTextView;
    private RecyclerView rv;
    private ScoreAdapter scoreAdpater;

    List<Score> scoreList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);  // The layout for the level activity

        // Initialize the TextViews
        timerTextView = findViewById(R.id.timerTextView);
        levelTextView = findViewById(R.id.level);
        rv = findViewById(R.id.recyclerView);

        scoreList = new ArrayList<>();
        scoreAdpater = new ScoreAdapter(scoreList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(scoreAdpater);



        // Fetch all scores from Firebase


        // Retrieve the timer value and level data passed from the previous activity
        long timeSpent = getIntent().getLongExtra("timeSpent", 0);  // Default to 0 if no value is passed
        int currentLevel = getIntent().getIntExtra("currentLevel", 1);  // Default to level 1 if no value is passed

        // Format the time spent (in milliseconds) to a "minutes:seconds" format
        String formattedTime = formatTime(timeSpent);

        // Display the level and formatted time
        levelTextView.setText("Level: " + currentLevel);
        timerTextView.setText("Time: " + formattedTime);
    }

    // Helper method to format time into "MM:SS" format
    private String formatTime(long timeInMillis) {
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
