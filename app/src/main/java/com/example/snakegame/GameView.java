package com.example.snakegame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {
    private enum Direction {
        up, down, left, right
    }

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 60 * 1000; // 60 seconds
    private Paint timerPaint;
    private boolean gameOver = false;  // Game over flag
    private boolean gameWon = false;   // Game won flag

    private Cell[][] cells;
    private Cell player, exit;
    private static int COLS = 7;
    private static int ROWS = 10;
    private int currentLevel = 1; // Track current level
    private int mazeCount = 0;    // Track maze count within level


    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private static final float WALL_THICKNESS = 4;
    private Random random;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);
        random = new Random();
        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);
        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        // Initialize Timer Paint
        timerPaint = new Paint();
        timerPaint.setColor(Color.BLACK); // Set text color to black
        timerPaint.setTextSize(96); // Larger font size (adjust as necessary)
        timerPaint.setTextAlign(Paint.Align.LEFT); // Align text to the left

        startTimer();
        createMaze();
    }
    private void adjustMazeSize() {
        if (currentLevel >= 1 && currentLevel <= 4) {
            COLS = 7;
            ROWS = 10;
        } else if (currentLevel >= 5 && currentLevel <= 8) {
            COLS = 9;
            ROWS = 12;
        } else if (currentLevel >= 9 && currentLevel <= 12) {
            COLS = 12;  // Increase size for higher levels
            ROWS = 15;
        } else if (currentLevel >= 13 && currentLevel <= 16) {
            COLS = 14;  // Even bigger maze
            ROWS = 18;
        } else {
            COLS = 16;  // Maximum size for the largest levels
            ROWS = 20;
        }
    }


    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;

                // Change timer color when 10 seconds or less remain
                if (timeLeftInMillis <= 10000) {
                    timerPaint.setColor(Color.RED);  // Urgent countdown
                } else {
                    timerPaint.setColor(Color.BLACK);  // Default color
                }

                invalidate(); // Redraw the screen to update the timer display
            }

            @Override
            public void onFinish() {
                gameOver = true;
                invalidate(); // Redraw screen to show Game Over
            }
        };

        countDownTimer.start();
    }
    private void restartGame() {
        if (gameWon) {
            mazeCount++;  // Increment maze count

            if (mazeCount == 3) {  // After completing all 3 mazes
                // Level complete, stop the timer
                countDownTimer.cancel();  // Stop the timer only when all 3 mazes are completed

                // Save the time for the level
                saveLevelTimer();

                // Transition to LevelActivity after completing all mazes in the level
                Intent intent = new Intent(getContext(), LevelActivity.class);
                intent.putExtra("timeSpent", timeLeftInMillis);  // Pass the time left for the current level
                intent.putExtra("currentLevel", currentLevel);  // Pass the current level
                getContext().startActivity(intent);  // Transition to LevelActivity
                return;  // Exit early to prevent restarting the game
            }

            // If not the final maze, continue to the next maze in the same level
            adjustMazeSize(); // Adjust maze size based on the level
            createMaze();
            player = cells[0][0];
            gameOver = false;
            gameWon = false;

            invalidate();  // Redraw the screen to reflect the new maze
        }
    }




    private void showFinalVictoryPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Congratulations!")
                .setMessage("You've completed all 10 levels!")
                .setPositiveButton("Restart", (dialog, which) -> {
                    currentLevel = 1;  // Restart at Level 1
                    mazeCount = 0;
                    restartGame();     // Restart the game
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    // Finish the activity or exit the game
                    ((Activity) getContext()).finish();
                })
                .setCancelable(false)  // Disable dismissing by tapping outside
                .show();
    }
    private void saveLevelTimer() {
        // Initialize Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://snake-login-10f36-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference ref = database.getReference("levels");  // Reference to the "levels" node in Firebase

        // Create a LevelData object to hold the level and timer information
        LevelData levelData = new LevelData(currentLevel, timeLeftInMillis);

        // Save the level data to Firebase Realtime Database
        ref.child(String.valueOf(currentLevel)).setValue(levelData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Level data saved successfully for Level " + currentLevel);
                    } else {
                        Log.d("Firebase", "Failed to save level data.");
                    }
                });
    }






    private Cell getNeighbour(Cell cell) {
        ArrayList<Cell> neighbours = new ArrayList<>();

        if (cell.col > 0 && !cells[cell.col - 1][cell.row].visited) {
            neighbours.add(cells[cell.col - 1][cell.row]); // Left
        }
        if (cell.col < COLS - 1 && !cells[cell.col + 1][cell.row].visited) {
            neighbours.add(cells[cell.col + 1][cell.row]); // Right
        }
        if (cell.row > 0 && !cells[cell.col][cell.row - 1].visited) {
            neighbours.add(cells[cell.col][cell.row - 1]); // Top
        }
        if (cell.row < ROWS - 1 && !cells[cell.col][cell.row + 1].visited) {
            neighbours.add(cells[cell.col][cell.row + 1]); // Bottom
        }

        if (!neighbours.isEmpty()) {
            return neighbours.get(random.nextInt(neighbours.size()));
        }
        return null;
    }

    private void removeWall(Cell current, Cell next) {
        if (current.col == next.col) {
            if (current.row == next.row + 1) { // Next is above
                current.topWall = false;
                next.bottomWall = false;
            } else if (current.row == next.row - 1) { // Next is below
                current.bottomWall = false;
                next.topWall = false;
            }
        } else if (current.row == next.row) {
            if (current.col == next.col + 1) { // Next is left
                current.leftWall = false;
                next.rightWall = false;
            } else if (current.col == next.col - 1) { // Next is right
                current.rightWall = false;
                next.leftWall = false;
            }
        }
    }

    private void createMaze() {
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }
        player = cells[0][0];
        exit = cells[COLS - 1][ROWS - 1];

        current = cells[0][0];
        current.visited = true;
        stack.push(current);

        while (!stack.isEmpty()) {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                current = stack.pop();
            }
        }

        postInvalidate(); // Refresh view after maze is created
    }

    private void movePlayer(Direction direction) {
        if (gameOver || gameWon) return;  // Do nothing if the game is over or won

        switch (direction) {
            case up:
                if (!player.topWall) {
                    player = cells[player.col][player.row - 1];
                }
                break;
            case down:
                if (!player.bottomWall) {
                    player = cells[player.col][player.row + 1];
                }
                break;
            case left:
                if (!player.leftWall) {
                    player = cells[player.col - 1][player.row];
                }
                break;
            case right:
                if (!player.rightWall) {
                    player = cells[player.col + 1][player.row];
                }
                break;
        }

        if (player == exit) {
            gameWon = true;

            // Only cancel the timer if it's the 3rd maze completed
            if (mazeCount == 2) { // This is the 3rd maze
                countDownTimer.cancel();  // Stop the timer only after the final maze is completed
                saveLevelTimer(); // Save time for the level
                // Show level complete screen or proceed to next activity
                showLevelCompleteScreen();
            } else {
                // Proceed to next maze without canceling the timer
                restartGame();
            }
        }

        invalidate();  // Redraw the screen
    }
    private void showLevelCompleteScreen() {
        // Save the time spent on the current level
        saveLevelTimer();

        // Create an intent to transition to LevelActivity
        Intent intent = new Intent(getContext(), LevelActivity.class);

        // Pass the current level and time spent as extras
        intent.putExtra("currentLevel", currentLevel);
        intent.putExtra("timeSpent", timeLeftInMillis); // Pass the remaining time as time spent

        // Start the LevelActivity
        getContext().startActivity(intent);
    }




    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        Log.d("GameView", "onDraw() called - Background should be green");
        canvas.drawColor(Color.GREEN); // Set background to green

        int width = getWidth();
        int height = getHeight();

        // Determine cell size based on aspect ratio
        if ((float) width / height < (float) COLS / ROWS) {
            cellSize = width / (COLS + 1);
        } else {
            cellSize = height / (ROWS + 1);
        }

        // Set horizontal margin for centering the maze
        hMargin = (width - COLS * cellSize) / 2;

        // Calculate the height of the timer text using the Paint object
        float timerHeight = timerPaint.getTextSize() * 1.5f; // Make it slightly bigger than the text size

        // Set vertical margin for centering the maze, but with additional space for the timer and level
        vMargin = (height - ROWS * cellSize - timerHeight) / 2; // Space for the timer above

        // Adjust level text size and move it higher
        float levelHeight = 120; // Make the level text larger (increase the size)
        vMargin -= levelHeight + 40; // Move the level higher, give 40px space above the timer


        // Draw the current level info above the timer
        Paint levelPaint = new Paint();
        levelPaint.setColor(Color.BLACK); // Black color for the level text
        levelPaint.setTextSize(120); // Larger text size for the level (adjust as needed)
        levelPaint.setTextAlign(Paint.Align.CENTER); // Center-align text
         // Center the level text horizontally and place it above the timer
        canvas.drawText("Level: " + currentLevel, width / 2, vMargin, levelPaint); // Draw level in the center

        // Now adjust vMargin back to the position for the timer
        vMargin += levelHeight + 20; // Move vMargin down for the timer placement

        // Draw the timer below the level
        canvas.drawText("Time Left: " + (timeLeftInMillis / 1000) + "s", hMargin, vMargin, timerPaint); // Draw timer

        // Adjust the translate for maze drawing: shift the maze down by the timer height
        canvas.translate(hMargin, vMargin + timerHeight);

        // Draw the maze walls and other elements...
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Cell cell = cells[x][y];

                if (cell.topWall) {
                    canvas.drawLine(x * cellSize, y * cellSize, (x + 1) * cellSize, y * cellSize, wallPaint);
                }
                if (cell.bottomWall) {
                    canvas.drawLine(x * cellSize, (y + 1) * cellSize, (x + 1) * cellSize, (y + 1) * cellSize, wallPaint);
                }
                if (cell.rightWall) {
                    canvas.drawLine((x + 1) * cellSize, y * cellSize, (x + 1) * cellSize, (y + 1) * cellSize, wallPaint);
                }
                if (cell.leftWall) {
                    canvas.drawLine(x * cellSize, y * cellSize, x * cellSize, (y + 1) * cellSize, wallPaint);
                }
            }
        }

        // Draw player and exit
        float margin = cellSize / 10;
        canvas.drawRect(player.col * cellSize + margin, player.row * cellSize + margin,
                (player.col + 1) * cellSize - margin, (player.row + 1) * cellSize - margin, playerPaint);
        canvas.drawRect(exit.col * cellSize + margin, exit.row * cellSize + margin,
                (exit.col + 1) * cellSize - margin, (exit.row + 1) * cellSize - margin, exitPaint);

        // If game over or won, stop the game and show message
        if (gameOver) {
            showEndGamePopup(false); // Show "Game Over" message
        } else if (gameWon && !(currentLevel == 10 && mazeCount == 2)) {
            showEndGamePopup(true); // Show "You Win!" message for all levels except the last one
        }
    }
    private void completeLevel() {
        // Save the time spent for the level (in milliseconds)
        long timeSpent = timeLeftInMillis;
        FirebaseScoreManager scoreManager = FirebaseScoreManager.getInstance();
        scoreManager.saveScore(String.valueOf(currentLevel), String.valueOf(timeSpent));

        // After saving, move to the next activity (LevelActivity)
        Intent intent = new Intent(getContext(), LevelActivity.class);
        intent.putExtra("timeSpent", timeSpent);  // Pass the time spent for the level
        intent.putExtra("currentLevel", currentLevel);  // Pass the current level
        getContext().startActivity(intent);  // Transition to LevelActivity
    }



    private void showEndGamePopup(boolean isWin) {
        // Create and show the pop-up dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isWin ? "You Win!" : "Game Over") // Set title based on win/lose
                .setMessage("Would you like to restart?")
                .setPositiveButton("Restart", (dialog, which) -> restartGame()) // Restart the game
                .setNegativeButton("Exit", (dialog, which) -> ((Activity) getContext()).finish()) // Exit the game
                .setCancelable(false) // Disable dismissing by tapping outside
                .show();
    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            float playerCenterX = hMargin + (player.col + 0.5f) * cellSize;
            float playerCentery = vMargin + (player.row + 0.5f) * cellSize;

            float dx = x - playerCenterX;
            float dy = y - playerCentery;
            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);
            if (absDx > cellSize || absDy > cellSize) {
                if (absDx > absDy) {
                    // move x direction
                    if (dx > 0) {
                        movePlayer(Direction.right);
                    } else {
                        movePlayer(Direction.left);
                    }
                } else {
                    // move y direction
                    if (dy > 0) {
                        movePlayer(Direction.down);
                    } else {
                        movePlayer(Direction.up);
                    }
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private static class Cell {
        boolean topWall = true;
        boolean leftWall = true;
        boolean bottomWall = true;
        boolean rightWall = true;
        boolean visited = false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
