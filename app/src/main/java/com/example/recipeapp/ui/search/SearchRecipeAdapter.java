package com.example.recipeapp.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.recipeapp.R;

import java.util.List;

public class SearchRecipeAdapter extends ArrayAdapter<RecipeListItem> {

    public SearchRecipeAdapter(Context context, List<RecipeListItem> recipes) {
        super(context, 0, recipes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RecipeListItem recipe = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_list_item, parent, false);
        }

        // Lookup view for data population
        TextView primaryText = convertView.findViewById(R.id.primaryText);
        TextView subText = convertView.findViewById(R.id.subText);

        // Populate the data into the template view using the data object
        primaryText.setText(recipe.getName());  // Primary text
        subText.setText(recipe.getAuthor());  // Sub-item text

        // Return the completed view to render on screen
        return convertView;
    }
}
