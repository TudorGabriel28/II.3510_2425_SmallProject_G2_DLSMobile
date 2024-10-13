package com.example.recipeapp.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder> {

    private List<String> ingredientsList;

    public IngredientsAdapter(List<String> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new IngredientsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder, int position) {
        String ingredient = ingredientsList.get(position);
        holder.ingredientTextView.setText(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredientsList.size();
    }

    static class IngredientsViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientTextView;

        public IngredientsViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
