package com.elderly.physical.wellbeing.system.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import androidx.annotation.Nullable;

import com.elderly.physical.wellbeing.system.R;

import java.util.ArrayList;
import java.util.Arrays;

public class GameView extends View {

    /* Instantiation of classes */
    private Bird bird;
    private ArrayList<Pipe> arrPipes;
    private GameActivity1Test g1;

    private Handler handler;
    private Runnable r;

    /* Declaration of variables */
    private int sumPipe, dist;
    public static int score = 0, highScore = 0;
    private boolean start;
    public Context context;
    private int soundJump;
    private float volume;
    private boolean loadedsound;
    private SoundPool soundPool;
    private int count = 0;
    public Boolean testing = false;
    public int diff = 0, diff2 = 0;
    public int counter = 0;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        g1 = new GameActivity1Test();

        SharedPreferences sp = context.getSharedPreferences("gamesetting", context.MODE_PRIVATE);
        if (sp != null) {
            highScore = sp.getInt("highscore", 0);
        }
        score = 0;
        start = false;

        initBird();
        initPipe();

        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };

        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
            this.soundPool = builder.build();
        }
        else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadedsound = true;
            }
        });
        soundJump = this.soundPool.load(context, R.raw.jump_02, 1);
    }

    private void initPipe() {
        sumPipe = 3;
        arrPipes = new ArrayList<>();
        dist = 2000 * Constants.SCREEN_HEIGHT / 1920;

        /* Easy difficulty */
        if (diff2 == 1) {
            for (int i = 0; i < sumPipe; i++) {
                if (i < sumPipe / 2) {
                    this.arrPipes.add(new Pipe(Constants.SCREEN_WIDTH + i * ((Constants.SCREEN_WIDTH + 200 *
                            Constants.SCREEN_WIDTH / 1080) / (sumPipe / 2)), 0, 200 * Constants.SCREEN_WIDTH / 1080,
                            Constants.SCREEN_HEIGHT / 1920));
                    this.arrPipes.get(this.arrPipes.size() - 1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.pipe2));
                    this.arrPipes.get(this.arrPipes.size() - 1).randomY();
                } else {
                    this.arrPipes.add(new Pipe(this.arrPipes.get(i - sumPipe / 2).getX(), this.arrPipes.get(i - sumPipe / 2).getY() +
                            this.arrPipes.get(i - sumPipe / 2).getHeight() + this.dist, 200 * Constants.SCREEN_WIDTH / 1080,
                            Constants.SCREEN_HEIGHT / 2));
                    this.arrPipes.get(this.arrPipes.size() - 1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.pipe1));
                }
            }
        }

        /* Medium difficulty */
        else if (diff2 == 2) {
            for (int i = 0; i < sumPipe; i++) {
                if (i < sumPipe / 2) {
                    this.arrPipes.add(new Pipe(Constants.SCREEN_WIDTH + i * ((Constants.SCREEN_WIDTH + 200 * Constants.SCREEN_WIDTH / 1080) / (sumPipe / 2)),
                            0, 200 * Constants.SCREEN_WIDTH / 1080, Constants.SCREEN_HEIGHT / 4));
                    this.arrPipes.get(this.arrPipes.size() - 1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.pipe2));
                    this.arrPipes.get(this.arrPipes.size() - 1).randomY();
                } else {
                    this.arrPipes.add(new Pipe(this.arrPipes.get(i - sumPipe / 2).getX(), this.arrPipes.get(i - sumPipe / 2).getY() +
                            this.arrPipes.get(i - sumPipe / 2).getHeight() + this.dist, 200 * Constants.SCREEN_WIDTH / 1080,
                            Constants.SCREEN_HEIGHT / 2));
                    this.arrPipes.get(this.arrPipes.size() - 1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.pipe1));
                }
            }
        }

        /* Hard difficulty */
        else if (diff2 == 3) {
            for (int i = 0; i < sumPipe; i++) {
                if (i < sumPipe / 2) {
                    this.arrPipes.add(new Pipe(Constants.SCREEN_WIDTH + i * ((Constants.SCREEN_WIDTH + 200 * Constants.SCREEN_WIDTH / 1080) / (sumPipe / 2)),
                            0, 200 * Constants.SCREEN_WIDTH / 1080, Constants.SCREEN_HEIGHT / 2));
                    this.arrPipes.get(this.arrPipes.size() - 1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.pipe2));
                    this.arrPipes.get(this.arrPipes.size() - 1).randomY();
                } else {
                    this.arrPipes.add(new Pipe(this.arrPipes.get(i - sumPipe / 2).getX(), this.arrPipes.get(i - sumPipe / 2).getY() +
                            this.arrPipes.get(i - sumPipe / 2).getHeight() + this.dist, 200 * Constants.SCREEN_WIDTH / 1080,
                            Constants.SCREEN_HEIGHT / 2));
                    this.arrPipes.get(this.arrPipes.size() - 1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.pipe1));
                }
            }
        }
    }

    private void initBird() {
        bird = new Bird();
        bird.setWidth(100 * Constants.SCREEN_WIDTH/1080);
        bird.setHeight(100 * Constants.SCREEN_HEIGHT/1920);
        bird.setX(100 * Constants.SCREEN_HEIGHT/1080);
        bird.setY(Constants.SCREEN_HEIGHT/2 - bird.getHeight()/2);
        ArrayList<Bitmap> arrBMS = new ArrayList<>();
        arrBMS.add(BitmapFactory.decodeResource(this.getResources(), R.drawable.bird1));
        arrBMS.add(BitmapFactory.decodeResource(this.getResources(), R.drawable.bird2));
        bird.setArrBMS(arrBMS);
    }

    public void draw(Canvas canvas) {
        /* Bird fly and sound made when arm rotation is detected */
        if(testing == true) {
            if (isStart()) {
                bird.setDrop(-6);
                if (loadedsound) {
                    int streamID = this.soundPool.play(this.soundJump, (float) 0.5, (float) 0.5, 1, 0, 1f);
                }
            }
            testing = false;
            counter++; // Record the number of arm rotations made
        }

        super.draw(canvas);
        if (start) {
            bird.draw(canvas);

            /* When bird collides with obstacles or fly off screen */
            for (int i = 0; i < sumPipe; i++) {
                if (bird.getRect().intersect(arrPipes.get(i).getRect()) || bird.getY() + bird.getHeight() < 0 ||
                       bird.getY() > Constants.SCREEN_HEIGHT) {
                    Pipe.speed = 0;
                    setStart(false);
                    /* Easy difficulty */
                    if (diff2 == 1) {
                        GameActivity1Test.ticks2 = (int) Math.ceil((GameActivity1Test.easyTimer - GameActivity1Test.ticks) / 1000.0);
                        GameActivity1Test.calc = (int)GameActivity1Test.ticks2 + counter;
                        score = GameActivity1Test.easyScore * GameActivity1Test.calc;

                        if (GameActivity1Test.over_r2.getVisibility() == INVISIBLE) {
                            GameActivity1Test.txt_timer.setVisibility(INVISIBLE);
                            GameActivity1Test.over_r1.setVisibility(VISIBLE);
                            GameActivity1Test.r1_game_over.setVisibility(VISIBLE);
                        }
                    }

                    /* Medium difficulty */
                    else if (diff2 == 2) {
                        GameActivity1Test.ticks2 = (int) Math.ceil((GameActivity1Test.mediumTimer - GameActivity1Test.ticks) / 1000.0);
                        GameActivity1Test.calc = (int)GameActivity1Test.ticks2 + counter;
                        score = GameActivity1Test.mediumScore * GameActivity1Test.calc;

                        if (GameActivity1Test.over_r2.getVisibility() == INVISIBLE) {
                            GameActivity1Test.txt_timer.setVisibility(INVISIBLE);
                            GameActivity1Test.over_r1.setVisibility(VISIBLE);
                            GameActivity1Test.r1_game_over.setVisibility(VISIBLE);
                        }
                    }

                    /* Hard difficulty */
                    else if (diff2 == 3) {
                        GameActivity1Test.ticks2 = (int) Math.ceil((GameActivity1Test.hardTimer - GameActivity1Test.ticks) / 1000.0);
                        GameActivity1Test.calc = (int)GameActivity1Test.ticks2 + counter;
                        score = GameActivity1Test.hardScore * GameActivity1Test.calc;

                        if (GameActivity1Test.over_r2.getVisibility() == INVISIBLE) {
                            GameActivity1Test.txt_timer.setVisibility(INVISIBLE);
                            GameActivity1Test.over_r1.setVisibility(VISIBLE);
                            GameActivity1Test.r1_game_over.setVisibility(VISIBLE);
                        }
                    }
                    GameActivity1Test.cTimer.cancel();
                    GameActivity1Test.counter = 0;
                    counter = 0;
                }

                SharedPreferences sp = context.getSharedPreferences("gamesetting", context.MODE_PRIVATE);
                if (sp != null) {
                    highScore = sp.getInt("highscore", 0);
                }

                if (score > highScore) {
                    GameActivity1Test.hold = score;
                    highScore = score;
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("highscore", highScore);
                    editor.apply();
                }

                GameActivity1Test.txt_score_over.setText("" + score);
                GameActivity1Test.txt_high_score.setText("High Score: " + highScore);

                if (this.arrPipes.get(i).getX() < -arrPipes.get(i).getWidth()) {
                    this.arrPipes.get(i).setX(Constants.SCREEN_WIDTH);
                    if (i < sumPipe/2) {
                        arrPipes.get(i).randomY();
                    }
                    else {
                        arrPipes.get(i).setY(this.arrPipes.get(i - sumPipe/2).getY() +
                                this.arrPipes.get(i - sumPipe/2).getHeight() + this.dist);
                    }
                }
                this.arrPipes.get(i).draw(canvas);
            }
        }
        else {
            if (bird.getY() > Constants.SCREEN_HEIGHT/2) {
                bird.setDrop(-2 * Constants.SCREEN_HEIGHT/1920);
            }
            bird.draw(canvas);
        }
        handler.postDelayed(r, 10);
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
        diff2 = diff;
        initPipe();
        initBird();
    }

    public void reset() {
        score = 0;
        initPipe();
        initBird();
    }
}
