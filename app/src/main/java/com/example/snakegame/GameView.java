package com.example.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {
    private  enum Direction {
        up,down,left,right
    }
    private Cell[][] cells;
    private Cell player,exit;
    private static final int COLS = 7, ROWS = 10;

    private float cellSize, hMargin, vMargin;
    private Paint wallPaint,playerPaint,exitPaint;
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
        exitPaint= new Paint();
        exitPaint.setColor(Color.BLUE);


        createMaze();
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
        player =cells[0][0];
        exit =cells[COLS-1][ROWS-1];

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
    private void moveplayer(Direction direction)
    {
        switch (direction)
        {
            case up:
                if(!player.topWall)
                {
                    player =cells[player.col][player.row-1];
                }

                break;
            case down:
                if(!player.bottomWall)
                {
                    player =cells[player.col][player.row+1];
                }
                break;
            case left:
                if(!player.leftWall)
                {
                    player =cells[player.col-1][player.row];
                }
                break;
            case right:
                if(!player.rightWall)
                {
                    player =cells[player.col+1][player.row];
                }
                break;
        }
        invalidate();

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

        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height - ROWS * cellSize) / 2;
        canvas.translate(hMargin, vMargin);

        // Draw the maze walls
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
        float margin =cellSize/10;

        canvas.drawRect(player.col*cellSize+margin,player.row*cellSize+margin,(player.col+1)*cellSize-margin,(player.row+1)*cellSize-margin,playerPaint);
        canvas.drawRect(exit.col*cellSize+margin,exit.row*cellSize+margin,(exit.col+1)*cellSize-margin,(exit.row+1)*cellSize-margin,exitPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN )
        {
            return  true;
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float x = event.getX();
            float y =event.getY();
            float playerCenterX =hMargin +(player.col+0.5f)*cellSize;
            float playerCentery =vMargin +(player.row+0.5f)*cellSize;

            float dx =x -playerCenterX;
            float dy =y -playerCentery;
            float absDx =Math.abs(dx);
            float absDy =Math.abs(dy);
            if(absDx > cellSize || absDy >cellSize)
            {
                if(absDx > absDy)
                {
                    //move x direction
                    if(dx >0)
                    {
                        moveplayer(Direction.right);
                    }
                    else
                    {
                        moveplayer(Direction.left);
                    }
                }
                else
                {
                    //move y direction
                    if(dy >0)
                    {
                        moveplayer(Direction.down);
                    }
                    else
                    {
                        moveplayer(Direction.up);
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
