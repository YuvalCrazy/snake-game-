package com.example.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class TrophyView extends View {

    private Paint paint;
    private int posX = 0;
    private int screenWidth;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Runnable runnable;

    // Confetti settings
    private static final int CONFETTI_COUNT = 100;
    private float[] confettiX = new float[CONFETTI_COUNT];
    private float[] confettiY = new float[CONFETTI_COUNT];
    private float[] confettiSpeedY = new float[CONFETTI_COUNT];
    private float[] confettiWobble = new float[CONFETTI_COUNT];
    private int[] confettiColors = new int[CONFETTI_COUNT];

    public TrophyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrophyView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setTextSize(100);

        // Initial confetti setup
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            confettiX[i] = random.nextInt(1000);
            confettiY[i] = random.nextInt(1500);
            confettiSpeedY[i] = 5 + random.nextFloat() * 10;
            confettiWobble[i] = random.nextFloat() * 2 - 1;
            confettiColors[i] = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (screenWidth == 0) {
            screenWidth = getWidth();
        }

        // Draw trophy
        canvas.drawText("🏆", posX, getHeight() / 2, paint);

        // Draw confetti
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            paint.setColor(confettiColors[i]);
            canvas.drawCircle(confettiX[i], confettiY[i], 10, paint);
        }
    }

    public void startAnimation() {
        stopAnimation(); // prevent multiple handlers

        runnable = new Runnable() {
            @Override
            public void run() {
                posX += 10;
                if (posX > screenWidth) {
                    posX = -100;
                }

                if (getWidth() > 0 && getHeight() > 0) {
                    for (int i = 0; i < CONFETTI_COUNT; i++) {
                        confettiY[i] += confettiSpeedY[i];
                        confettiX[i] += confettiWobble[i];

                        if (confettiY[i] > getHeight()) {
                            confettiY[i] = 0;
                            confettiX[i] = random.nextInt(getWidth());
                            confettiSpeedY[i] = 5 + random.nextFloat() * 10;
                            confettiWobble[i] = random.nextFloat() * 2 - 1;
                            confettiColors[i] = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        }
                    }
                }

                invalidate();
                handler.postDelayed(this, 30);
            }
        };
        handler.post(runnable);
    }

    public void stopAnimation() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}