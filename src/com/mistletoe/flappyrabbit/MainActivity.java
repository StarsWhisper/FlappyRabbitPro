package com.mistletoe.flappyrabbit;

import com.mistletoe.flappyrabbit.view.GameView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;




public class MainActivity extends Activity {
    private GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mGameView = new GameView(this, null);
        setContentView(mGameView);
    }
}
