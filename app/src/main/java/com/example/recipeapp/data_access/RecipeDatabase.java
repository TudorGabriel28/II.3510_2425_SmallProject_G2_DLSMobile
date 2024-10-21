package com.example.recipeapp.data_access;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.recipeapp.entities.Recipe;

@Database(entities = {Recipe.class}, version = 1, exportSchema = true) // or false
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract RecipeDao recipeDao();
}