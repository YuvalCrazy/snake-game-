package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
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
import java.util.Map;

public class LevelActivity extends AppCompatActivity {

    private TextView timerTextView;
    private TextView levelTextView;
    private RecyclerView rv;
    private ScoreAdapter scoreAdpater;
    private Button startGameButton;
    private int currentLevel;

    private List<Score> scoreList;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        timerTextView = findViewById(R.id.timerTextView);
        levelTextView = findViewById(R.id.level);
        rv = findViewById(R.id.recyclerView);
        startGameButton = findViewById(R.id.button3);

        scoreList = new ArrayList<>();
        scoreAdpater = new ScoreAdapter(scoreList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(scoreAdpater);

        long timeSpent = getIntent().getLongExtra("timeSpent", 0);
        currentLevel = getIntent().getIntExtra("currentLevel", 1);

        levelTextView.setText("Level: " + currentLevel);
        timerTextView.setText("Time: " + formatTime(timeSpent));

        database = FirebaseDatabase.getInstance("https://snake-login-10f36-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("scores");

        loadLeaderboard();

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelActivity.this, game.class);
                Log.d("LevelActivity","sending Game View currentLevel="+currentLevel);
                intent.putExtra("currentLevel", currentLevel);  // ✅ This will now be the correct level
                startActivity(intent);
            }
        });
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
                        // Check if the score is stored as a Long or String
                        if (levelSnapshot.child("score").getValue() instanceof Long) {
                            // If score is a Long (in milliseconds)
                            Long scoreLong = levelSnapshot.child("score").getValue(Long.class);
                            if (scoreLong != null) {
                                totalTime += scoreLong;  // Add the score
                                levelCount++;
                            }
                        } else if (levelSnapshot.child("score").getValue() instanceof String) {
                            // If score is a String (in MM:SS format)
                            String scoreStr = levelSnapshot.child("score").getValue(String.class);
                            if (scoreStr != null) {
                                long scoreMillis = convertScoreToMillis(scoreStr);  // Convert to milliseconds
                                totalTime += scoreMillis;
                                levelCount++;
                            }
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
