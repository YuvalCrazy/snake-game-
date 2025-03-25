package com.example.snakegame;  // Make sure this matches the package of your project


public class LevelData {
    private int level; // The level number
    private long timeSpent; // The time spent for the level (in milliseconds)

    // Default constructor required for Firebase to parse data
    public LevelData() {
    }

    // Constructor to initialize the level and time spent
    public LevelData(int level, long timeSpent) {
        this.level = level;
        this.timeSpent = timeSpent;
    }

    // Getter for level
    public int getLevel() {
        return level;
    }

    // Setter for level
    public void setLevel(int level) {
        this.level = level;
    }

    // Getter for timeSpent
    public long getTimeSpent() {
        return timeSpent;
    }

    // Setter for timeSpent
    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }
}
