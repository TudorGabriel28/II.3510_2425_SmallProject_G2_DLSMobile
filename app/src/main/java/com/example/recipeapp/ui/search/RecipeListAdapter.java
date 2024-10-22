package com.example.recipeapp.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.recipeapp.R;

import java.util.List;

public class RecipeListAdapter extends ArrayAdapter<RecipeListItem> {

    public RecipeListAdapter(Context context, List<RecipeListItem> recipes) {
        super(context, 0, recipes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        RecipeListItem recipe = getItem(position);

        
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_list_item, parent, false);
        }

        
        TextView primaryText = convertView.findViewById(R.id.primaryText);
        TextView subText = convertView.findViewById(R.id.subText);

        
        primaryText.setText(recipe.getName());  
        subText.setText(recipe.getAuthor());  

        
        return convertView;
    }
}
