package com.frontier.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by frontier on 9/29/15.
 */
public class ResourcesManager {
    private static List<Category> categories = new ArrayList<Category>();

    public static void init()
    {
        if(!categories.isEmpty()) {
            categories.clear();
        }
        addKeys("pictures/autumn", "秋");
        addKeys("pictures/babies", "宝贝");
        addKeys("pictures/baby_animals", "动物");
        addKeys("pictures/beach_fun", "海滩");
        addKeys("pictures/china", "中国");
        addKeys("pictures/colorful_food", "美食");
        addKeys("pictures/dogs", "小狗");
        addKeys("pictures/domestic_cats", "小猫");
        addKeys("pictures/flowers", "花");
        addKeys("pictures/funny_animals", "可爱的动物");
        addKeys("pictures/graffiti", "涂鸦");
        addKeys("pictures/mountains", "高山");
        addKeys("pictures/spring", "春");
        addKeys("pictures/sunsets", "日落");
        addKeys("pictures/trees", "树");
        addKeys("pictures/winter", "冬");
        addKeys("pictures/horses", "马");
        addKeys("pictures/paris", "巴黎");
        addKeys("pictures/parks", "公园");
        addKeys("pictures/world_of_color", "色彩");
        addKeys("pictures/butterflies", "蝴蝶");
        addKeys("pictures/flowers", "鲜花");
    }

    private static void addKeys(String path, String desc)
    {
        Category category = new Category(path, desc);
        categories.add(category);
    }

    public static List<Category> getCategories()
    {
        return categories;
    }
}
