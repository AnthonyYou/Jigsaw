package com.frontier.jigsaw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.frontier.model.Category;
import com.frontier.model.JigsawPic;
import com.frontier.model.ResourcesManager;

import java.util.ArrayList;
import java.util.List;


public class SingleCategoryActivity extends Activity {

    private Category category = null;
    private List<JigsawPic> pics = null;
    private CategoryAdapter adapter = null;
    private int categoryId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_category);

        Intent intent = getIntent();
        categoryId = intent.getIntExtra("selectedId", 0);
        category = ResourcesManager.getCategories().get(categoryId);
        pics = category.getAllPics();

        ListView lv = (ListView) findViewById(R.id.single_category_list);
        adapter = new CategoryAdapter();
        lv.setAdapter(adapter);

        TextView title = (TextView) findViewById(R.id.single_category_title);
        title.setText(category.getDesc());
    }

    private void itemSelected(int id)
    {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("picId", id);
        startActivity(intent);
    }

    private class CategoryAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return pics.size() / 2;
        }

        @Override
        public Object getItem(int position) {
            List<JigsawPic> twoJigsawPics = new ArrayList<JigsawPic>();
            twoJigsawPics.add(pics.get(position * 2));
            twoJigsawPics.add(pics.get(position * 2 + 1));
            return twoJigsawPics;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(SingleCategoryActivity.this);
            View v = convertView == null ? inflater.inflate(R.layout.category_item, null) : convertView;

            ImageView image1 = (ImageView) v.findViewById(R.id.image1);
            TextView name1 = (TextView) v.findViewById(R.id.name1);
            ImageView image2 = (ImageView) v.findViewById(R.id.image2);
            TextView name2 = (TextView) v.findViewById(R.id.name2);

            List<JigsawPic> twoJigsawPics = (List<JigsawPic>) getItem(position);
            image1.setImageBitmap(twoJigsawPics.get(0).getPreview());
            image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemSelected(position * 2);
                }
            });
            name1.setText(String.valueOf(position * 2));

            image2.setImageBitmap(twoJigsawPics.get(1).getPreview());
            image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemSelected(position * 2 + 1);
                }
            });
            name2.setText(String.valueOf(position * 2 + 1));

            return v;
        }
    }
}
