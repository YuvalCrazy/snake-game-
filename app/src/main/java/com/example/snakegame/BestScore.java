package com.example.snakegame;

public class BestScore {
    private String username;
    private String time;

    public BestScore() {
        // Required empty constructor for Firebase
    }

    public BestScore(String username, String time) {
        this.username = username;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return time;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
