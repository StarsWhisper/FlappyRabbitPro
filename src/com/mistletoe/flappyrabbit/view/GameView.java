package com.mistletoe.flappyrabbit.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mistletoe.flappyrabbit.element.Bird;
import com.mistletoe.flappyrabbit.element.BirdWorld;

/**
 * Created by Administrator on 2015/4/11 0011.
 */
public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Bird mBird;
    private BirdWorld mBirdWorld;
    private List<Bitmap[]> mListBirdsSkin;
    private List<Bitmap[]> mListPipesSkin;
    private List<Bitmap> mListSkySkin;
    private Bitmap mGroundSkin;
    private Matrix mMatrix;
    private boolean mIsRunning;

    private SoundPool mSoundPool;
    private Map<String, Integer> mSoundMap;

    private GestureDetector mGestureDetector;

    //    private boolean mIsPlay;
    private Paint mPaint;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStrokeWidth(6);

        mMatrix = new Matrix();
        getHolder().addCallback(this);
        mGestureDetector = new GestureDetector(getContext(), new GameGestureDetector());
        loadSoundPool();
    }

    private Rect calcBirdShotBound() {
        Bitmap birdSkin = mListBirdsSkin.get(0)[0];
        Rect bound = new Rect();
        bound.set(getWidth() / 3 - birdSkin.getWidth() / 2, getHeight() / 2 - birdSkin.getHeight() / 2,
                getWidth() / 3 + birdSkin.getWidth() / 2, getHeight() / 2 + birdSkin.getHeight() / 2);
        return bound;
    }

    private Rect calcBirdInitBound() {
        Bitmap birdSkin = mListBirdsSkin.get(0)[0];
        Rect bound = new Rect();
        bound.set(getWidth() / 2 - birdSkin.getWidth() / 2, getHeight() * 2 / 5 - birdSkin.getHeight() / 2,
                getWidth() / 2 + birdSkin.getWidth() / 2, getHeight() * 2 / 5 + birdSkin.getHeight() / 2);
        return bound;
    }

    private void loadSoundPool() {
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        AssetManager am = getContext().getAssets();
        mSoundMap = new HashMap<>();
        try {
            mSoundMap.put("Die", mSoundPool.load(am.openFd("sound/Die.wav"), 1));
            mSoundMap.put("Hit", mSoundPool.load(am.openFd("sound/Hit.wav"), 1));
            mSoundMap.put("Point", mSoundPool.load(am.openFd("sound/Point.wav"), 1));
            mSoundMap.put("Swooshing", mSoundPool.load(am.openFd("sound/Swooshing.wav"), 1));
            mSoundMap.put("Wing", mSoundPool.load(am.openFd("sound/Wing.wav"), 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBackgroundSkin() {
        mListSkySkin = new ArrayList<>();
        Bitmap skyOrigin, skyScale, sky;
        skyOrigin = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_day);
        skyScale = Bitmap.createScaledBitmap(skyOrigin, getWidth(), getHeight(), false);
        sky = Bitmap.createBitmap(skyScale, 0, 0, skyScale.getWidth(), getHeight() * 4 / 5);
        mListSkySkin.add(sky);
        skyOrigin.recycle();
        skyScale.recycle();

        skyOrigin = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_night);
        skyScale = Bitmap.createScaledBitmap(skyOrigin, getWidth(), getHeight(), false);
        sky = Bitmap.createBitmap(skyScale, 0, 0, skyScale.getWidth(), getHeight() * 4 / 5);
        mListSkySkin.add(sky);
        skyOrigin.recycle();
        skyScale.recycle();

        Bitmap groundOrigin;
        groundOrigin = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.land);
        mGroundSkin = Bitmap.createScaledBitmap(groundOrigin, getWidth(),
                getHeight() * 1 / 5, false);
        groundOrigin.recycle();
    }

    private void loadBirdsSkin() {
        Bitmap[] birds = null;
        Bitmap bitmap = null;
        int width = getWidth() / 6;
        int height = getHeight() * 3 / 32;

        mListBirdsSkin = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            birds = new Bitmap[3];
            for (int j = 0; j < 3; j++) {
                bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.bird0_0 + i * 3 + j);
                birds[j] = Bitmap.createScaledBitmap(bitmap, width, height, false);
                bitmap.recycle();
            }
            mListBirdsSkin.add(birds);
        }
    }

    private void loadPipesSkin() {
        Bitmap[] pipes = null;
        Bitmap bitmap = null;
        int width = getWidth() * 13 / 72;
        int height = getHeight() * 5 / 8;

        mListPipesSkin = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            pipes = new Bitmap[2];
            for (int j = 0; j < 2; j++) {
                bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.pipe2_down + i * 2 + j);
                pipes[j] = Bitmap.createScaledBitmap(bitmap, width, height, false);
                bitmap.recycle();
            }
            mListPipesSkin.add(pipes);
        }
    }

    @Override
    public void draw(Canvas canvas) {
//        canvas.drawColor(0xFF000000);
        mBirdWorld.draw(canvas);
//        canvas.drawLine(270, 0, 270, 1920, mPaint);
//        canvas.drawLine(450, 0, 450, 1920, mPaint);
        mBird.draw(canvas);
    }

    @Override
    public void run() {
        while (mIsRunning) {
            Canvas canvas = getHolder().lockCanvas();
            draw(canvas);
            getHolder().unlockCanvasAndPost(canvas);
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        loadBirdsSkin();
        loadPipesSkin();
        loadBackgroundSkin();

        Rect bound = calcBirdInitBound();
        mBird = new Bird().setBound(bound).setMatrix(mMatrix)
                .setBirdsSkin(mListBirdsSkin.get(0));
        mBird.makeStandby();

        mBirdWorld = new BirdWorld().setBound(new Rect(0, 0, getWidth(), getHeight()))
                .setSkySkin(mListSkySkin.get(0)).setGroundSkin(mGroundSkin)
                .setPipesSkin(mListPipesSkin.get(1));
        mBirdWorld.makeStandby();

        mIsRunning = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRunning = false;
    }

    private class GameGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mIsRunning) {
                if (mBirdWorld.IsStandBy()) {
                    mBirdWorld.roll();
                    Rect bound = calcBirdShotBound();
                    synchronized (mBird) {
                        mBird.setBound(bound).shot();
                    }
                } else {
                    mBird.shot();
                    int id = mSoundMap.get("Wing");
                    mSoundPool.play(id, 1f, 1f, 1, 0, 1f);
                }
            }
            return true;
        }
    }
}
