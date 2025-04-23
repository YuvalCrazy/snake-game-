package com.example.snakegame;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class FirebaseScoreManager {

    private static FirebaseScoreManager instance;
    private DatabaseReference database;
    private FirebaseAuth auth;

    private FirebaseScoreManager() {
        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseScoreManager getInstance() {
        if (instance == null) {
            instance = new FirebaseScoreManager();
        }
        return instance;
    }

    // Save score for the user and update leaderboard
    public void saveScore(final String level, final String timeSpent) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            Log.d("FirebaseScoreManager", "Current user: " + email);

            if (email != null) {
                String userId = email.replace(".", "_");  // Firebase-safe username
                Log.d("FirebaseScoreManager", "Saving score for user: " + userId);

                // Convert the timeSpent string to milliseconds
                long timeSpentInMillis = timeToMilliseconds(timeSpent);

                // Save the user score for this level under "scores"
                updateUserScore(userId, level, timeSpentInMillis);

                // Update the leaderboard (if needed)
                updateLeaderboard(level, userId, timeSpentInMillis);
            } else {
                Log.e("FirebaseScoreManager", "User email is null.");
            }
        } else {
            Log.e("FirebaseScoreManager", "No user is logged in.");
        }
    }

    // Convert time from "MM:SS" format to milliseconds
    private long timeToMilliseconds(String time) {
        try {
            String[] parts = time.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return (minutes * 60 + seconds) * 1000L;
        } catch (Exception e) {
            Log.e("FirebaseScoreManager", "Invalid time format: " + time);
            return 0;
        }
    }

    // Save user-specific score for the level
    private void updateUserScore(String userId, String level, long timeSpent) {
        DatabaseReference scoreRef = database.child("scores").child(userId).child("level_" + level).child("timeSpent");

        Log.d("FirebaseScoreManager", "Writing to: scores/" + userId + "/level_" + level + "/timeSpent = " + timeSpent);

        scoreRef.setValue(timeSpent, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e("FirebaseScoreManager", "Data write failed: " + databaseError.getMessage());
            } else {
                Log.d("FirebaseScoreManager", "User score saved successfully.");
            }
        });
    }

    // Update global leaderboard (if necessary)
    private void updateLeaderboard(final String level, final String userId, final long timeSpent) {
        final DatabaseReference leaderboardRef = database.child("best_scores").child("level_" + level);

        leaderboardRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long existingTime = snapshot.getValue(Long.class);

                if (existingTime == null || existingTime > timeSpent) {
                    leaderboardRef.child(userId).setValue(timeSpent, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            Log.e("FirebaseScoreManager", "Leaderboard update failed: " + databaseError.getMessage());
                        } else {
                            Log.d("FirebaseScoreManager", "Leaderboard updated with new/better time.");
                        }
                    });
                } else {
                    Log.d("FirebaseScoreManager", "New time is not better. Leaderboard unchanged.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseScoreManager", "Error reading leaderboard: " + databaseError.getMessage());
            }
        });
    }
}
