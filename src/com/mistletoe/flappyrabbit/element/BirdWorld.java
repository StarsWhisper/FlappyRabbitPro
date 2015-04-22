package com.mistletoe.flappyrabbit.element;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Administrator on 2015/4/11 0011.
 */
public class BirdWorld {
    public static final int DEFAULT_ROLLING_SPEED = 30;

    private static final int SPEED_SCALE = 8;

    private Rect mBound;
    private int mGroundTop;

    private Bitmap mSkySkin;
    private Bitmap mGroundSkin;
    private Bitmap[] mPipesSkin;

    private List<PipePair> mTemplatePipeList;
    private Queue<PipePair> mPipePairQueue;
    private int mNextPipeFrameCount;

    private int mRollingSpeed;
    private boolean mIsStandby;
    private int mFrameCount;

    public BirdWorld() {
        mRollingSpeed = DEFAULT_ROLLING_SPEED;
        mTemplatePipeList = new ArrayList<>();
        mPipePairQueue = new LinkedList<>();
    }

    public BirdWorld setBound(Rect bound) {
        mBound = bound;
        mGroundTop = mBound.top + mBound.height() * 4 / 5;
        return this;
    }

    public BirdWorld setSkySkin(Bitmap skySkin) {
        mSkySkin = skySkin;
        return this;
    }

    public BirdWorld setGroundSkin(Bitmap groundSkin) {
        mGroundSkin = groundSkin;
        return this;
    }

    public BirdWorld setPipesSkin(Bitmap[] skins) {
        mPipesSkin = skins;
        return this;
    }

    public BirdWorld setRollingSpeed(int rollingSpeed) {
        mRollingSpeed = rollingSpeed;
        return this;
    }

    private void genTemplatePipeList() {
        int top = mBound.top + mBound.height() / 10;
        int space = mBound.height() * 3 / 10;
        int step = mBound.height() / 10;
        mTemplatePipeList.clear();
        while (top + space < mGroundTop) {
            PipePair pp = new PipePair().setDownBottom(top).setUpTop(top + space);
            mTemplatePipeList.add(pp);
            top += step;
        }
    }

    private void genPipePair() {
        PipePair pp = null;
        if (mTemplatePipeList.isEmpty()) {
            genTemplatePipeList();
        }
        PipePair temp = mTemplatePipeList.get((int) (Math.random() * mTemplatePipeList.size()));
        if (!mPipePairQueue.isEmpty()) {
            PipePair tmp = mPipePairQueue.peek();
            if (tmp.bound.right < 0) {
                pp = mPipePairQueue.poll();
                pp.setDownBottom(temp.downBottom).setUpTop(temp.upTop);
                pp.bound.set(temp.bound);
            }
        }
        if (pp == null) {
            pp = (PipePair) temp.clone();
        }
        mPipePairQueue.offer(pp);
    }

    public void makeStandby() {
        mIsStandby = true;
        mFrameCount = 0;
    }

    public boolean IsStandBy() {
        return mIsStandby;
    }

    public void roll() {
        mIsStandby = false;
        mNextPipeFrameCount = -1;
    }

    public void draw(Canvas canvas) {
        int skyLeft = mBound.left;
        int groundLeft = mBound.left;
        if (!mIsStandby) {
            int recycleFrameCount = mBound.width() / mRollingSpeed;
            int groundFrameCount = mFrameCount % recycleFrameCount;
            skyLeft -= mFrameCount * mRollingSpeed / SPEED_SCALE;
            groundLeft -= groundFrameCount * mRollingSpeed;
            canvas.drawBitmap(mSkySkin, skyLeft + mBound.width(), mBound.top, null);
            canvas.drawBitmap(mGroundSkin, groundLeft + mBound.width(), mGroundTop, null);

            canvas.drawBitmap(mSkySkin, skyLeft, mBound.top, null);
            canvas.drawBitmap(mGroundSkin, groundLeft, mGroundTop, null);

            for (PipePair pp : mPipePairQueue) {
                pp.draw(canvas);
            }

            Log.d("mytag", "mNextPipeFrameCount = " + mNextPipeFrameCount);
            if (mNextPipeFrameCount == -1) {
                mNextPipeFrameCount = recycleFrameCount;
            }

            if (mFrameCount == mNextPipeFrameCount) {
                genPipePair();
                mNextPipeFrameCount += recycleFrameCount / 2;
                if (mNextPipeFrameCount >= (SPEED_SCALE * recycleFrameCount)) {
                    mNextPipeFrameCount -= (SPEED_SCALE * recycleFrameCount);
                }
            }

            mFrameCount++;
            for (PipePair pp : mPipePairQueue) {
                pp.roll();
            }

            if (mFrameCount == (SPEED_SCALE * recycleFrameCount)) {
                mFrameCount = 0;
            }
        } else {
            canvas.drawBitmap(mSkySkin, skyLeft, mBound.top, null);
            canvas.drawBitmap(mGroundSkin, groundLeft, mGroundTop, null);
        }
    }

    private class PipePair implements Cloneable {
        Rect bound;
        private int downBottom;
        private int upTop;

        PipePair() {
            bound = new Rect(mBound.right, mBound.top,
                    mBound.right + mPipesSkin[0].getWidth(),
                    mGroundTop);
        }

        @Override
        protected Object clone() {
            PipePair pp = null;
            try {
                pp = (PipePair) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            pp.bound = new Rect(bound);
            return pp;
        }

        PipePair setDownBottom(int downBottom) {
            this.downBottom = downBottom;
            return this;
        }

        PipePair setUpTop(int upTop) {
            this.upTop = upTop;
            return this;
        }

        void roll() {
            bound.offset(-mRollingSpeed, 0);
        }

        void draw(Canvas canvas) {
            canvas.save();
            canvas.clipRect(bound);
            canvas.drawBitmap(mPipesSkin[0], bound.left,
                    downBottom - mPipesSkin[0].getHeight(), null);

            Log.d("mytag", "bound.left = " + bound.left);
            canvas.drawBitmap(mPipesSkin[1], bound.left, upTop, null);
            canvas.restore();
        }
    }
}
