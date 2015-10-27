package com.frontier.jigsaw;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.frontier.model.Global;
import com.frontier.model.ResourcesManager;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends Activity {

    private TextView loadingLabel = null;
    private int progress = 0;
    private Handler handler = new Handler();
    private String[] progressText = {
        "Loading   ",
        "Loading.  ",
        "Loading.. ",
        "Loading..."
    };

    private Runnable progressIndicator = new Runnable() {
        @Override
        public void run() {
            loadingLabel.setText(progressText[progress % progressText.length]);
            ++ progress;
            handler.postDelayed(progressIndicator, 500);
        }
    };

    private boolean splashFinished = false;
    private boolean loadingFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        loadingLabel = (TextView) findViewById(R.id.loading);
        startIndicator();

        Global.am = getAssets();
        Global.context = getApplicationContext();

        loadResources();
        startSplashCountDown();
    }

    private void startIndicator()
    {
        handler.postDelayed(progressIndicator, 500);
    }

    private void startSplashCountDown()
    {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                splashFinished = true;
                enterGame();
            }
        }, 2000);
    }

    private void loadResources()
    {
        LoadingTask loadingTask = new LoadingTask();
        loadingTask.execute();
    }

    private void enterGame()
    {
        if(splashFinished && loadingFinished)
        {
            handler.removeCallbacks(progressIndicator);
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private class LoadingTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            loadingLabel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ResourcesManager.init();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            loadingFinished = true;
            enterGame();
        }
    }
}
