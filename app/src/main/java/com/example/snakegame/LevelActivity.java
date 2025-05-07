package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LevelActivity extends AppCompatActivity {

    private TextView timerTextView;
    private TextView levelTextView;
    private RecyclerView rv;
    private ScoreAdapter scoreAdpater;
    private Button startGameButton;

    private List<Score> scoreList;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);  // The layout for the level activity

        // Initialize the TextViews and RecyclerView
        timerTextView = findViewById(R.id.timerTextView);
        levelTextView = findViewById(R.id.level);
        rv = findViewById(R.id.recyclerView);
        startGameButton = findViewById(R.id.button3);

        scoreList = new ArrayList<>();
        scoreAdpater = new ScoreAdapter(scoreList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(scoreAdpater);

        // Firebase Database Reference
        database = FirebaseDatabase.getInstance("https://snake-login-10f36-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("scores");

        // Fetch and display scores from Firebase
        loadLeaderboard();

        // Set up start game button click
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When button is clicked, start the game
                Intent intent = new Intent(LevelActivity.this, game.class);  // Replace with your actual GameActivity
                startActivity(intent);
            }
        });

        // Retrieve the timer value and level data passed from the previous activity
        long timeSpent = getIntent().getLongExtra("timeSpent", 0);  // Default to 0 if no value is passed
        int currentLevel = getIntent().getIntExtra("currentLevel", 1);  // Default to level 1 if no value is passed

        // Format the time spent (in milliseconds) to a "minutes:seconds" format
        String formattedTime = formatTime(timeSpent);

        // Display the level and formatted time
        levelTextView.setText("Level: " + currentLevel);
        timerTextView.setText("Time: " + formattedTime);
    }

    private void loadLeaderboard() {
        // Retrieve data from Firebase
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Double> avgTimes = new HashMap<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    long totalTime = 0;
                    int levelCount = 0;

                    // Loop through all levels for the user
                    for (DataSnapshot levelSnapshot : userSnapshot.getChildren()) {
                        String score = levelSnapshot.child("score").getValue(String.class);
                        if (score != null) {
                            totalTime += convertScoreToMillis(score);
                            levelCount++;
                        }
                    }

                    // Calculate average time for the user
                    if (levelCount > 0) {
                        double average = totalTime / (double) levelCount;
                        avgTimes.put(userSnapshot.getKey(), average);
                    }
                }

                // Sort by average time (ascending order)
                List<Map.Entry<String, Double>> sortedList = new ArrayList<>(avgTimes.entrySet());
                sortedList.sort(Map.Entry.comparingByValue());

                // Clear the previous list and update with the top 10 scores
                scoreList.clear();
                int count = 0;
                for (Map.Entry<String, Double> entry : sortedList) {
                    if (count++ >= 10) break;  // Limit to top 10 users
                    String email = entry.getKey().replace("_", ".");  // Assuming email has underscores instead of dots
                    long millis = entry.getValue().longValue();
                    String timeFormatted = formatTime(millis);
                    scoreList.add(new Score(email, timeFormatted));
                }

                // Notify the adapter that the data has changed
                scoreAdpater.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LevelActivity", "Failed to load scores: " + error.getMessage());
            }
        });
    }

    // Convert score in format "MM:SS" to milliseconds
    private long convertScoreToMillis(String score) {
        String[] parts = score.split(":");
        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[1]);
        return (minutes * 60 + seconds) * 1000;  // Convert to milliseconds
    }

    // Helper method to format time into "MM:SS" format
    private String formatTime(long timeInMillis) {
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
