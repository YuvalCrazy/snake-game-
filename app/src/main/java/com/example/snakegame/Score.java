package com.example.snakegame;

public class Score {
    // Fields to store email and time
    private String email;
    private String time;

    // Constructor to initialize the fields
    public Score(String email, String time) {
        this.email = email;
        this.time = time;
    }

    // Getter methods to retrieve values
    public String getEmail() {
        return email;
    }

    public String getTime() {
        return time;
    }

    // Setter methods to set values
    public void setEmail(String email) {
        this.email = email;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
