package com.frontier.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import com.frontier.jigsaw.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Created by frontier on 9/29/15.
 */
public class JigsawPic {
    private SoftReference<Bitmap> preview = null;
    private SoftReference<Bitmap> pic = null;
    private String path = null;

    public static int PREVIEW_WIDTH = 120;
    public static int PREVIEW_HEIGHT = 120;

    public JigsawPic(String path)
    {
        this.path = path;
    }

    public Bitmap getPreview()
    {
        if(preview == null || preview.get() == null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bmp = readBitmap(PREVIEW_WIDTH, PREVIEW_HEIGHT);
            preview = new SoftReference<Bitmap>(bmp);
            return bmp;
        } else {
            return preview.get();
        }
    }

    public Bitmap getPic()
    {
        if(pic == null || pic.get() == null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            DisplayMetrics dm = Global.context.getResources().getDisplayMetrics();
            Bitmap bmp = readBitmap(dm.widthPixels, dm.widthPixels);
            pic = new SoftReference<Bitmap>(bmp);
            return bmp;
        } else {
            return pic.get();
        }
    }

    private Bitmap readBitmap(int tWidth, int tHeight)
    {
        try {
            InputStream is = null;
            if(path != null) {
                is = Global.am.open(path);
            } else {
                is = Global.context.getResources().openRawResource(R.raw.default_pic);
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
            opts.inSampleSize = opts.outWidth / tWidth;
            opts.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeStream(is, null, opts);
            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
