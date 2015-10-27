package com.frontier.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by frontier on 10/10/15.
 */
public class TextSprite extends Sprite {
    private float textSize = 30f;
    private int textColor = Color.BLACK;
    private Paint textPaint = null;
    public TextSprite(String text) {
        super(text);
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
    }

    public void setTextSize(float textSize)
    {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
    }

    public float getTextSize()
    {
        return textSize;
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
        textPaint.setColor(textColor);
    }

    public int getTextColor()
    {
        return textColor;
    }

    @Override
    public void draw(Canvas canvas, long frame) {
        canvas.drawText(text, x, y, textPaint);
    }
}
