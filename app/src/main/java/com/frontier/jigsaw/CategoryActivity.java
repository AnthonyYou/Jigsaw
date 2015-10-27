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
import com.frontier.model.Global;
import com.frontier.model.ResourcesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CategoryActivity extends Activity {

    private List<Category> categories = null;
    private CategoryAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Global.context = this;
        categories = ResourcesManager.getCategories();

        ListView lv = (ListView) findViewById(R.id.category_list);
        adapter = new CategoryAdapter();
        lv.setAdapter(adapter);
    }

    private void itemSelected(int id)
    {
        Intent intent = new Intent(this, SingleCategoryActivity.class);
        intent.putExtra("selectedId", id);
        startActivity(intent);
    }

    private class CategoryAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return categories.size() / 2;
        }

        @Override
        public Object getItem(int position) {
            List<Category> twoCategories = new ArrayList<Category>();
            twoCategories.add(categories.get(position * 2));
            twoCategories.add(categories.get(position * 2 + 1));
            return twoCategories;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(CategoryActivity.this);
            View v = convertView == null ? inflater.inflate(R.layout.category_item, null) : convertView;
            ImageView image1 = (ImageView) v.findViewById(R.id.image1);
            TextView name1 = (TextView) v.findViewById(R.id.name1);
            ImageView image2 = (ImageView) v.findViewById(R.id.image2);
            TextView name2 = (TextView) v.findViewById(R.id.name2);

            List<Category> twoCategories = (List<Category>) getItem(position);
            image1.setImageBitmap(twoCategories.get(0).getPreviewPic());
            image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemSelected(position * 2);
                }
            });
            name1.setText(twoCategories.get(0).getDesc());

            image2.setImageBitmap(twoCategories.get(1).getPreviewPic());
            image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemSelected(position * 2 + 1);
                }
            });
            name2.setText(twoCategories.get(1).getDesc());

            return v;
        }
    }
}
