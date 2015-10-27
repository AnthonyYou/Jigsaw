package com.frontier.jigsaw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;

import com.frontier.logic.GameLogic;
import com.frontier.model.Category;
import com.frontier.model.Global;
import com.frontier.model.JigsawPic;
import com.frontier.model.ResourcesManager;

/**
 * Created by frontier on 9/29/15.
 */
public class GameActivity extends Activity{
    private SurfaceView sv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sv = new SurfaceView(this);
        if(Global.gameLogic == null) {
            Global.gameLogic = new GameLogic();
        }
        sv.getHolder().addCallback(Global.gameLogic);
        sv.setOnTouchListener(Global.gameLogic);

        Intent intent = getIntent();
        int categoryId = intent.getIntExtra("categoryId", 0);
        int picId = intent.getIntExtra("picId", 0);
        Category category = ResourcesManager.getCategories().get(categoryId);
        JigsawPic jigsawPic = category.getAllPics().get(picId);
        Global.gameLogic.setBitmap(jigsawPic.getPic());
        Global.gameLogic.start();

        setContentView(sv);
    }

    @Override
    protected void onPause() {
//        Global.gameLogic.enterPauseState();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Global.gameLogic.stop();
        super.onDestroy();
    }
}
