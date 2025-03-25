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
    public void saveScore(final String level, final String newTime) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                String username = email.replace(".", "_");  // Firebase-safe username
                Log.d("FirebaseScoreManager", "Saving score for username: " + username);  // Log the username being used

                // Proceed to update player score and leaderboard
                updatePlayerScore(username, level, newTime);  // Save user score
                updateLeaderboard(level, username, newTime);  // Update global leaderboard
            } else {
                Log.e("FirebaseScoreManager", "User email is null.");
            }
        } else {
            Log.e("FirebaseScoreManager", "No user is logged in.");
        }
    }


    private void updatePlayerScore(final String username, final String level, final String newTime) {
        DatabaseReference userScoreRef = database.child("scores").child(username).child(level);

        userScoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String existingTime = dataSnapshot.child("score").getValue(String.class);

                // If the time exists, compare it
                if (existingTime != null) {
                    int comparison = compareTimes(existingTime, newTime);
                    if (comparison < 0) {
                        // New time is better (lower), update the score and save the username
                        userScoreRef.child("score").setValue(newTime);
                        userScoreRef.child("username").setValue(username); // Ensure username is saved
                    } else if (comparison > 0) {
                        // New time is worse (higher), do nothing
                        Log.d("FirebaseScoreManager", "New time is worse, no update.");
                    } else {
                        // Times are the same, do nothing
                        Log.d("FirebaseScoreManager", "Times are the same, no update.");
                    }
                } else {
                    // No existing score, save the new score and username
                    userScoreRef.child("score").setValue(newTime);
                    userScoreRef.child("username").setValue(username); // Ensure username is saved
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseScoreManager", "Database error: " + databaseError.getMessage());
            }
        });
    }



    // Update leaderboard to reflect the best score for the level
    // Update leaderboard to reflect the best score for the level
    private void updateLeaderboard(final String level, final String username, final String newTime) {
        final DatabaseReference leaderboardRef = database.child("best_scores").child(level);

        leaderboardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean updated = false;

                // Iterate through existing leaderboard to check if the user is already in the leaderboard
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String existingUsername = snapshot.getKey();
                    String existingTime = snapshot.getValue(String.class);

                    if (existingUsername != null && existingTime != null && existingUsername.equals(username)) {
                        // Compare the times
                        int comparison = compareTimes(existingTime, newTime);
                        if (comparison < 0) {
                            // New time is better, update the leaderboard
                            leaderboardRef.child(username).setValue(newTime);
                            updated = true;
                        } else if (comparison > 0) {
                            // New time is worse, do nothing
                            Log.d("FirebaseScoreManager", "New time is worse, no update in leaderboard.");
                        } else {
                            // Times are the same, do nothing
                            Log.d("FirebaseScoreManager", "Times are the same, no update in leaderboard.");
                        }
                        break;
                    }
                }

                if (!updated) {
                    // If user was not in the leaderboard, add them
                    leaderboardRef.child(username).setValue(newTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseScoreManager", "Database error: " + databaseError.getMessage());
            }
        });
    }


    // Helper method to compare two times in "MM:SS" format
    private int compareTimes(String time1, String time2) {
        int time1InSeconds = timeToSeconds(time1);
        int time2InSeconds = timeToSeconds(time2);
        return Integer.compare(time1InSeconds, time2InSeconds);
    }

    // Convert time from "MM:SS" format to seconds
    private int timeToSeconds(String time) {
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return minutes * 60 + seconds;
    }
}
