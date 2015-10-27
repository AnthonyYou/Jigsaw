package com.frontier.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.frontier.jigsaw.R;
import com.frontier.model.Global;
import com.frontier.model.Sprite;
import com.frontier.model.TextSprite;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Created by frontier on 9/29/15.
 */
public class GameLogic implements SurfaceHolder.Callback, View.OnTouchListener {
    private SurfaceHolder sfh = null;
    private Bitmap bmp = null;
    private Thread drawThread = null;
    private boolean stopped = false;
    private Bitmap scene = null;
    private Canvas sceneCanvas = null;
    private float scaleFactor = 1;
    private Matrix sceneMatrix = null;
    private int fragmentCount = 5;
    private int selectedIndex = -1;
    private Sprite selectedSprite = null;
    private int[] indexes = null;

    private Sprite winSprite = null;
    private Sprite pauseSprite = null;
    private Sprite tipSprite = null;
    private Sprite picSprite = null;
    private Sprite grayBgSprite = null;
    private TextSprite timeSprite = null;
    private TextSprite stepSprite = null;

    private long frames = 0;

    private Map<Integer, Sprite> sprites = new TreeMap<Integer, Sprite>(new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return lhs.compareTo(rhs);
        }
    });
    private Map<Integer, Sprite> fragments = new HashMap<Integer, Sprite>();

    private int timeElapsed = 0;
    private Thread countThread = null;
    private boolean countStopped = false;

    private int steps = 0;

    private enum GAME_STATUS {
        PREPARE,
        GAMING,
        SHOW_TIP,
        PAUSE,
        WIN,
        TOUCH_SWALLOW
    }

    private GAME_STATUS gameStatus = GAME_STATUS.PREPARE;

    private long frameDuration = 10;

    public GameLogic()
    {
        Bitmap win = BitmapFactory.decodeResource(Global.context.getResources(), R.drawable.victory);
        winSprite = new Sprite(win);
        winSprite.setAnchor(0.5f, 0.5f);
        winSprite.setScale(0.8f);
        sprites.put(102, winSprite);

        Bitmap pause = BitmapFactory.decodeResource(Global.context.getResources(), R.drawable.pause);
        pauseSprite = new Sprite(pause);
        pauseSprite.setVisible(false);
        pauseSprite.setAnchor(0.5f, 0.5f);
        pauseSprite.setScale(0.8f);
        sprites.put(103, pauseSprite);

        timeSprite = new TextSprite("Time:00(S)");
        timeSprite.setPosition(10, 30);
        sprites.put(104, timeSprite);

        stepSprite = new TextSprite("Steps:00");
        stepSprite.setPosition(10, 60);
        stepSprite.setTextColor(Color.RED);
        sprites.put(105, stepSprite);

        Bitmap tip = BitmapFactory.decodeResource(Global.context.getResources(), R.drawable.tips);
        tipSprite = new Sprite(tip, 1, 2, 2000);
        tipSprite.setAnchor(0.5f, 1.2f);
        sprites.put(106, tipSprite);

        Bitmap grayBg = Bitmap.createBitmap(getWindowWidth(), getWindowHeight(), Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(grayBg);
        canvas.drawARGB(120, 0, 0, 0);
        grayBgSprite = new Sprite(grayBg);
        grayBgSprite.setAnchor(0.5f, 0.5f);
        grayBgSprite.setVisible(false);
        sprites.put(107, grayBgSprite);
    }

    public long getFPS()
    {
        return 1000 / frameDuration;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        sfh = holder;
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        sfh = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(gameStatus != GAME_STATUS.WIN) {
            enterPauseState();
        }
    }

    public void enterPauseState()
    {
        grayBgSprite.setVisible(true);
        pauseSprite.setPosition(scene.getWidth() / 2, scene.getHeight() / 2);
        pauseSprite.setVisible(true);
        gameStatus = GAME_STATUS.PAUSE;
        stop();
        stopCountTime();
    }

    public void setBitmap(Bitmap bmp)
    {
        gameStatus = GAME_STATUS.PREPARE;
        timeElapsed = 0;
        timeSprite.setString("Time:00(S)");
        steps = 0;
        stepSprite.setString("Steps:00");
        winSprite.setVisible(false);
        pauseSprite.setVisible(false);
        grayBgSprite.setVisible(false);
        int sceneWidth = bmp.getWidth() / fragmentCount * fragmentCount;
        int sceneHeight = (int)((float)sceneWidth / getWindowWidth() * getWindowHeight());
        scaleFactor = getWindowWidth() / (float)sceneWidth;
        sceneMatrix = new Matrix();
        sceneMatrix.setScale(scaleFactor, scaleFactor);
        scene = Bitmap.createBitmap(sceneWidth, sceneHeight, Bitmap.Config.ARGB_8888);
        sceneCanvas = new Canvas(scene);
        this.bmp = bmp;
        prepareFragments();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                shuffle();
            }
        }, 2000);

        pauseSprite.setPosition(scene.getWidth() / 2, scene.getHeight() / 2);
        grayBgSprite.setPosition(scene.getWidth() / 2, scene.getHeight() / 2);

        tipSprite.setPosition(scene.getWidth() / 2, scene.getHeight());
        tipSprite.setVisible(false);

        picSprite = new Sprite(bmp.copy(Bitmap.Config.ARGB_8888, true));
        picSprite.setAnchor(0.5f, 0.5f);
        picSprite.setVisible(false);
        picSprite.setScale(0.9f);
        picSprite.setPosition(scene.getWidth() / 2, -picSprite.getBitmap().getHeight() / 2);
        sprites.put(108, picSprite);
    }

    public void prepareFragments()
    {
        int fragmentWidth = bmp.getWidth() / fragmentCount;
        int fragmentHeight = bmp.getHeight() / fragmentCount;
        float factor = (float)getWindowWidth() / bmp.getWidth();
        for(int i = 0; i < fragmentCount; ++ i) {
            for(int j = 0; j < fragmentCount; ++ j) {
                int top = j * fragmentHeight;
                int left = i * fragmentWidth;
                int right = left + fragmentWidth;
                int bottom = top + fragmentHeight;
                right = right >  bmp.getWidth() ? bmp.getWidth() : right;
                bottom = bottom > bmp.getHeight() ? bmp.getHeight() : bottom;
                Bitmap fragment = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
                Sprite sprite = new Sprite(fragment);
                sprite.setAnchor(0.5f, 0.5f);
                Point pos = calcPointByIndex(i + j * fragmentCount);
                sprite.setPosition(pos.x, pos.y);
                sprites.put(j * fragmentCount + i, sprite);
            }
        }

        indexes = new int[fragmentCount * fragmentCount];
        for(int i = 0; i < indexes.length; ++ i) {
            indexes[i] = i;
        }
    }

    public void swap2Sprite(int index1, int index2)
    {
        if(index1 == -1 || index2 == -1 || index1 == index2) {
            return;
        }

        Sprite firstSprite = sprites.get(indexes[index1]);
        Sprite secondSprite = sprites.get(indexes[index2]);
        if(firstSprite.isMoving() || secondSprite.isMoving()) {
            return;
        }

        int x0 = firstSprite.getX();
        int y0 = firstSprite.getY();
        int x1 = secondSprite.getX();
        int y1 = secondSprite.getY();
        firstSprite.moveTo(x1, y1, 300, null);
        secondSprite.moveTo(x0, y0, 300, null);
        int tmp = indexes[index1];
        indexes[index1] = indexes[index2];
        indexes[index2] = tmp;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(checkWin()) {
                    stopCountTime();
                    gameStatus = GAME_STATUS.WIN;
                    winSprite.setPosition(-winSprite.getContentWidth() / 2, scene.getHeight() / 2);
                    winSprite.setVisible(true);
                    winSprite.moveTo(scene.getWidth() / 2, scene.getHeight() / 2, 500, null);
                } else {
                    gameStatus = GAME_STATUS.GAMING;
                }
            }
        }, 400);
        ++ steps;
        stepSprite.setString(String.format("Steps:%02d", steps));
    }

    public void start()
    {
        if(drawThread == null) {
            stopped = false;
            drawThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!stopped) {
                        long start = System.currentTimeMillis();
                        draw();
                        long end = System.currentTimeMillis();
                        if(end - start < frameDuration) {
                            try {
                                Thread.sleep(frameDuration - end + start);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    stopped = false;
                }
            });
            drawThread.start();
        }
    }

    public void stop()
    {
        stopped = true;
        if(drawThread != null) {
            try {
                drawThread.join();
                drawThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getWindowWidth()
    {
        DisplayMetrics dm = Global.context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public int getWindowHeight()
    {
        DisplayMetrics dm = Global.context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    private void draw()
    {
        if(sfh == null || bmp == null) {
            return;
        }

        Canvas canvas = null;
        synchronized (sfh) {
            try {
                canvas = sfh.lockCanvas();
                if (canvas != null) {
                    sceneCanvas.drawColor(Color.BLUE);
                    for(Integer id : sprites.keySet()) {
                        sprites.get(id).draw(sceneCanvas, frames);
                    }
                }
                canvas.save();
                canvas.setMatrix(sceneMatrix);
                canvas.drawBitmap(scene, 0, 0, null);
                canvas.restore();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    sfh.unlockCanvasAndPost(canvas);
                }
            }

            ++ frames;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(gameStatus) {
            case PREPARE:
                inPrepareTouchEvent(event);
                break;
            case GAMING:
                inGamingTouchEvent(event);
                break;
            case SHOW_TIP:
                inShowTipTouchEvent(event);
                break;
            case PAUSE:
                inPauseTouchEvent(event);
                break;
            case WIN:
                inWinEvent(event);
                break;
            default:
                break;
        }
        return true;
    }

    private void inPrepareTouchEvent(MotionEvent event)
    {
        return;
    }

    private void inGamingTouchEvent(MotionEvent event)
    {
        int sx = (int)(event.getX() / scaleFactor);
        int sy = (int)(event.getY() / scaleFactor);
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            selectedIndex = calcIndexByPoint(sx, sy);
            if(selectedIndex == -1) {
                return;
            }

            selectedSprite = new Sprite(Bitmap.createBitmap(sprites.get(indexes[selectedIndex]).getBitmap()));
            Paint paint = new Paint();
            paint.setAlpha(200);
            selectedSprite.setPicPaint(paint);
            selectedSprite.setAnchor(0.5f, 0.5f);
            selectedSprite.setScale(1.2f);
            selectedSprite.setPosition(sx, sy);
            sprites.put(101, selectedSprite);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            if(selectedSprite != null) {
                selectedSprite.setPosition(sx, sy);
            }
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            if(selectedSprite != null) {
                sprites.remove(101);
                selectedSprite = null;
                swap2Sprite(selectedIndex, calcIndexByPoint(sx, sy));
            } else {
                if(tipSprite.containPoint(sx, sy)) {
                    gameStatus = GAME_STATUS.TOUCH_SWALLOW;
                    picSprite.setVisible(true);
                    picSprite.setPosition(scene.getWidth() / 2, -picSprite.getContentHeight() / 2);
                    grayBgSprite.setVisible(true);
                    picSprite.moveTo(scene.getWidth() / 2, scene.getHeight() / 2, 1000, new Runnable() {
                        @Override
                        public void run() {
                            gameStatus = GAME_STATUS.SHOW_TIP;
                        }
                    });
                }
            }
        }
    }

    private void inShowTipTouchEvent(MotionEvent event)
    {
        int sx = (int)(event.getX() / scaleFactor);
        int sy = (int)(event.getY() / scaleFactor);
        if(event.getAction() == MotionEvent.ACTION_UP && picSprite.containPoint(sx, sy)) {
            gameStatus = GAME_STATUS.TOUCH_SWALLOW;
            picSprite.moveTo(scene.getWidth() / 2, scene.getHeight() + picSprite.getBitmap().getHeight() / 2, 1000, new Runnable() {

                @Override
                public void run() {
                    gameStatus = GAME_STATUS.GAMING;
                    picSprite.setVisible(false);
                    grayBgSprite.setVisible(false);
                }
            });
        }
    }

    private void inPauseTouchEvent(MotionEvent event)
    {
        gameStatus = GAME_STATUS.TOUCH_SWALLOW;
        pauseSprite.moveTo(scene.getWidth() + pauseSprite.getContentWidth() / 2, scene.getHeight() / 2, 500, new Runnable() {
            @Override
            public void run() {
                gameStatus = GAME_STATUS.GAMING;
                pauseSprite.setVisible(false);
                grayBgSprite.setVisible(false);
                startCountTime();
            }
        });
    }

    private void inWinEvent(MotionEvent event)
    {
        pauseSprite.setAcceptTouch(true);
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            Sprite sprite = pauseSprite.getContainPointSprite((int)event.getX(), (int)event.getY());
            Log.d("Sprite", "sprite = " + sprite);
        }
    }

    private int calcIndexByPoint(int x, int y)
    {
        int top = (scene.getHeight() - bmp.getWidth()) / 2;
        int bottom = top + bmp.getHeight();
        if(y < top || y > bottom) {
            return -1;
        }

        if(x < 0 || x > bmp.getWidth()) {
            return -1;
        }

        int fragWidth = bmp.getWidth() / fragmentCount;
        int fragHeight = bmp.getHeight() / fragmentCount;

        y -= top;
        int nx = x / fragWidth;
        int ny = y / fragHeight;
        int index = ny * fragmentCount + nx;
        return index >= fragmentCount * fragmentCount ? -1 : index;
    }

    private Point calcPointByIndex(int index)
    {
        int fragWidth = scene.getWidth() / fragmentCount;
        int fragHeight = bmp.getHeight() / fragmentCount;
        int row = index / fragmentCount;
        int col = index % fragmentCount;
        return new Point(col * fragWidth + fragWidth / 2, row * fragHeight + (scene.getHeight() - scene.getWidth()) / 2 + fragHeight / 2);
    }

    private void shuffle()
    {
        int shuffleTimes = fragmentCount * fragmentCount;
        int a, b, c;
        for(int i = 0; i < shuffleTimes; ++ i) {
            a = (int)(Math.random() * 1000) % shuffleTimes;
            b = (int)(Math.random() * 1000) % shuffleTimes;
            c = indexes[a];
            indexes[a] = indexes[b];
            indexes[b] = c;
        }

        for(int i = 0; i < indexes.length; ++ i) {
            Sprite sprite = sprites.get(indexes[i]);
            Point targetPos = calcPointByIndex(i);
            sprite.moveTo(targetPos.x, targetPos.y, 300, null);
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                gameStatus = GAME_STATUS.GAMING;
                tipSprite.setVisible(true);
                startCountTime();
            }
        }, 500);
    }

    private boolean checkWin()
    {
        for(int i = 0; i < indexes.length; ++ i) {
            if(indexes[i] != i) {
                return false;
            }
        }
        return true;
    }

    private void startCountTime()
    {
        if(countThread == null) {
            countStopped = false;
            countThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!countStopped) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ++ timeElapsed;
                        timeSprite.setString(String.format("Time:%02d(S)", timeElapsed));
                    }
                }
            });
            countThread.start();
        }
    }

    private void stopCountTime()
    {
        countStopped = true;
        countThread = null;
    }
}
