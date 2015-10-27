package com.frontier.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by frontier on 9/29/15.
 */
public class Sprite {
    protected Bitmap bmp;
    protected Bitmap[] bmps = null;
    protected long duration = 0;
    protected int index = 0;
    protected int x, y;
    protected int tx, ty;
    protected float anchorX, anchorY;
    protected float factor = 1;
    protected float focusInFactor = 1;
    protected Paint picPaint = null;
    protected Paint textPaint = null;
    protected Matrix matrix = null;
    protected Canvas selfCanvas = null;
    protected boolean visible = true;
    protected boolean hasFocus = false;

    protected boolean acceptTouch = false;

    protected boolean isMoving = false;

    protected static Sprite cursorDownSprite = null;
    protected String text = null;

    protected Map<Integer, Sprite> childs = new TreeMap<Integer, Sprite>(new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return lhs.compareTo(rhs);
        }
    });

    public Sprite(String text)
    {
        this.text = text;
    }

    public Sprite(Bitmap bmp)
    {
        this.bmp = bmp;
        matrix = new Matrix();
        matrix.setScale(1, 1);
        selfCanvas = null;

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
    }

    public Sprite(Bitmap bmp, int row, int col, long duration)
    {
        this.duration = duration;
        bmps = new Bitmap[row * col];
        int fragWidth = bmp.getWidth() / col;
        int fragHeight = bmp.getHeight() / row;
        for(int i = 0; i < row; ++ i) {
            for(int j = 0; j < col; ++ j) {
                bmps[i * row + j] = Bitmap.createBitmap(bmp, j * fragWidth, i * fragHeight, fragWidth, fragHeight);
            }
        }

        this.bmp = bmps[0];
        index = 0;
    }

    public Sprite(Bitmap bmp, String text)
    {
        this(bmp);
        this.text = text;
    }

    public void setTextPaint(Paint src)
    {
        this.textPaint = src;
    }

    public boolean isAcceptTouch() {
        return acceptTouch;
    }

    public void setAcceptTouch(boolean acceptTouch)
    {
        this.acceptTouch = acceptTouch;
    }

    public void setString(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }

    public void addChild(int id, Sprite sprite)
    {
        childs.put(id, sprite);
    }

    public void removeChild(int id)
    {
        childs.remove(id);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPicPaint(Paint paint)
    {
        picPaint = paint;
    }

    public Bitmap getBitmap()
    {
        return bmp;
    }

    public int getContentWidth()
    {
        return bmp.getWidth();
    }

    public  int getContentHeight()
    {
        return bmp.getHeight();
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getTargetX()
    {
        return tx;
    }

    public int getTargetY()
    {
        return ty;
    }

    public void setAnchorX(float anchorX)
    {
        this.anchorX = anchorX;
    }

    public void setAnchorY(float anchorY)
    {
        this.anchorY = anchorY;
    }

    public void setAnchor(float anchorX, float anchorY)
    {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public void setScale(float factor)
    {
        this.factor = factor;
        matrix = new Matrix();
        matrix.setScale(factor, factor);
    }

    public float getScale()
    {
        return factor;
    }

    public void moveTo(final int tx, final int ty, final long duration, final Runnable callback)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startX = x;
                long startY = y;
                long endX = tx;
                long endY = ty;
                long time = duration;

                long steps = time / 10;
                float dx = (endX - startX) / (float)steps;
                float dy = (endY - startY) / (float)steps;
                float totalDX = 0;
                float totalDY = 0;
                while(steps >= 0) {
                    totalDX += dx;
                    totalDY += dy;
                    Sprite.this.x = (int)(startX + totalDX);
                    Sprite.this.y = (int)(startY + totalDY);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    -- steps;
                }
                Sprite.this.x = (int)endX;
                Sprite.this.y = (int)endY;
                if(callback != null) {
                    callback.run();
                }
                isMoving = false;
            }
        });
        isMoving = true;
        thread.start();
    }

    public void draw(Canvas canvas, long frame)
    {
        if(!visible) {
            return;
        }

        if(selfCanvas == null) {
            selfCanvas = new Canvas(bmp);
        }

        for(Integer id : childs.keySet()) {
            childs.get(id).draw(selfCanvas, frame);
        }

        if(text != null) {
            int textWidth = (int) textPaint.measureText(text);
            int top = (int) (bmp.getHeight() - textPaint.getTextSize()) / 2;
            int left = (int) (bmp.getWidth() - textWidth) / 2;
            selfCanvas.drawText(text, left, top, textPaint);
        }

        if(bmps != null) {
            long nFrames = duration / Global.gameLogic.getFPS();
            if(frame % nFrames == 0) {
                ++ index;
                index = index % bmps.length;
                bmp = bmps[index];
            }
        }

        canvas.save();
        canvas.setMatrix(matrix);

        float totalFocus = factor;
        if(hasFocus) {
            totalFocus = factor * focusInFactor;
        } else {
            totalFocus = factor;
        }
        canvas.drawBitmap(bmp, (x - bmp.getWidth() * anchorX * totalFocus) / totalFocus, (y - bmp.getHeight() * anchorY * totalFocus) / totalFocus, picPaint);
        canvas.restore();
    }

    public void focus()
    {
        focusInFactor = 1.2f;
        hasFocus = true;
    }

    public void loseFocus()
    {
        focusInFactor = 1;
        hasFocus = false;
    }

    public Sprite getContainPointSprite(int x, int y)
    {
        int nx = x - (int)((this.x - bmp.getWidth() * anchorX * factor) / factor);
        int ny = y - (int)((this.y - bmp.getHeight() * anchorY * factor) / factor);
        Object[] keys = childs.keySet().toArray();
        for(int i = keys.length - 1; i >= 0; -- i) {
            Integer key = (Integer)keys[i];
            Sprite sprite = childs.get(key);
            Sprite foundSprite = sprite.getContainPointSprite(nx, ny);
            if(foundSprite != null) {
                return foundSprite;
            }
        }
        return acceptTouch && containPoint(x, y) ? this : null;
    }

    public boolean containPoint(int x, int y)
    {
        float totalFocus = factor;
        if(hasFocus) {
            totalFocus = factor * focusInFactor;
        } else {
            totalFocus = factor;
        }

        int left = (int)((this.x - bmp.getWidth() * anchorX * totalFocus) / totalFocus);
        int top = (int)((this.y - bmp.getHeight() * anchorY * totalFocus) / totalFocus);
        int right = (int)(left + bmp.getWidth() * totalFocus);
        int bottom = (int)(top + bmp.getHeight() * totalFocus);
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    public interface OnClickListener {
        public void onClick(Sprite sprite);
    }
}
