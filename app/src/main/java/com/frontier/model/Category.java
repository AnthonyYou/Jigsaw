package com.frontier.model;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frontier on 9/29/15.
 */
public class Category {
    private String path = null;
    private String desc = null;
    private List<JigsawPic> pics = new ArrayList<JigsawPic>();
    public Category(String path, String desc)
    {
        this.path = path;
        this.desc = desc;
    }

    public String getPath()
    {
        return path;
    }

    public String getDesc()
    {
        return desc;
    }

    public List<JigsawPic> getAllPics()
    {
        if(pics.isEmpty()) {
            try {
                String[] files = Global.am.list(path);
                for(String file : files) {
                    JigsawPic pic = new JigsawPic(path + "/" + file);
                    pics.add(pic);
                }
                return pics;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(pics.size() % 2 == 1) {
            pics.add(new JigsawPic(null));
        }
        return pics;
    }

    public Bitmap getPreviewPic()
    {
        List<JigsawPic> pics = getAllPics();
        return pics.get(0).getPreview();
    }
}
