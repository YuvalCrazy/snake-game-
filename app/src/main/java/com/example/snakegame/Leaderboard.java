package com.example.snakegame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

public class Leaderboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScoreAdapter adapter;
    private List<Score> scoreList = new ArrayList<>();
    private DatabaseReference database;
    private Button levelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.leaderboardTop);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreAdapter(scoreList);
        recyclerView.setAdapter(adapter);
        levelButton = findViewById(R.id.button2);

        database = FirebaseDatabase.getInstance("https://snake-login-10f36-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("scores");

        loadLeaderboard();

        levelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Leaderboard.this, LevelActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadLeaderboard() {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Double> avgTimes = new HashMap<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    long totalTime = 0;
                    int levelCount = 0;

                    for (DataSnapshot levelSnapshot : userSnapshot.getChildren()) {
                        // Fetch the score as an Object instead of directly as String
                        Object scoreObj = levelSnapshot.child("score").getValue();

                        if (scoreObj != null) {
                            long scoreMillis = 0;

                            // Check if the score is a String (MM:SS format)
                            if (scoreObj instanceof String) {
                                String scoreString = (String) scoreObj;
                                scoreMillis = convertScoreToMillis(scoreString); // Convert MM:SS to milliseconds
                            }
                            // Check if the score is a Long (milliseconds)
                            else if (scoreObj instanceof Long) {
                                scoreMillis = (Long) scoreObj;
                            } else {
                                // If the score is neither a String nor Long, log the error
                                Log.e("Leaderboard", "Unexpected score type: " + scoreObj.getClass().getSimpleName());
                            }

                            totalTime += scoreMillis;
                            levelCount++;
                        }
                    }

                    if (levelCount > 0) {
                        double average = totalTime / (double) levelCount;
                        avgTimes.put(userSnapshot.getKey(), average);
                    }
                }

                // Sort by average time ascending
                List<Map.Entry<String, Double>> sortedList = new ArrayList<>(avgTimes.entrySet());
                sortedList.sort(Map.Entry.comparingByValue());

                scoreList.clear();
                int count = 0;

                for (Map.Entry<String, Double> entry : sortedList) {
                    if (count++ >= 10) break;

                    String email = entry.getKey().replace("_", ".");
                    long millis = entry.getValue().longValue();
                    String timeFormatted = formatTime(millis);
                    scoreList.add(new Score(email, timeFormatted));
                }

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Leaderboard", "Failed to load scores: " + error.getMessage());
            }
        });
    }

    // Convert score in format "MM:SS" to milliseconds
    private long convertScoreToMillis(String score) {
        String[] parts = score.split(":");
        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[1]);
        return (minutes * 60 + seconds) * 1000; // Convert to milliseconds
    }

    // Format milliseconds to MM:SS
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
