package com.frontier.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by frontier on 10/3/15.
 */
public class Button extends Sprite {

    private String text;
    private int textSize = 20;
    public Button(Bitmap bmp, String text, int textSize) {
        super(bmp);
        this.text = text;
        this.textSize = textSize;

        acceptTouch = true;
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
    }

    @Override
    public void draw(Canvas canvas, long frame) {
        super.draw(canvas, frame);
        if(isVisible()) {

        }
    }
}
