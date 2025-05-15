package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class LevelActivity extends AppCompatActivity {

    private TextView timerTextView;
    private TextView levelTextView;
    private Button startGameButton;
    private int currentLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        timerTextView = findViewById(R.id.timerTextView);
        levelTextView = findViewById(R.id.level);
        startGameButton = findViewById(R.id.button3);



        long timeSpent = getIntent().getLongExtra("timeSpent", 0);
        currentLevel = getIntent().getIntExtra("currentLevel", 1);

        levelTextView.setText("Level: " + currentLevel);
        timerTextView.setText("Time: " + formatTime(timeSpent));

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelActivity.this, game.class);
                Log.d("LevelActivity","sending Game View currentLevel="+currentLevel);
                intent.putExtra("currentLevel", currentLevel);  // ✅ This will now be the correct level
                startActivity(intent);
            }
        });

        boolean showTrophy = getIntent().getBooleanExtra("showTrophy", false);

        if (showTrophy) {
            // Show trophy animation
            TrophyView trophyView = new TrophyView(this);

            trophyView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
            rootView.addView(trophyView);
            trophyView.startAnimation();

            // Delay removal of trophy view
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                rootView.removeView(trophyView);
            }, 5000); // 5-second delay
        }
    }






    // Helper method to format time into "MM:SS" format
    private String formatTime(long timeInMillis) {
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

}
