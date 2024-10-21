package com.example.recipeapp.data_access;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.recipeapp.entities.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    void insertRecipe(Recipe recipe);

    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    Recipe getRecipeById(int recipeId);

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    void deleteRecipe(int recipeId);

    @Query("UPDATE recipes SET rating = :rating WHERE id = :recipeId")
    void updateRecipeRating(int recipeId, float rating);

    @Query("SELECT rating FROM recipes WHERE id = :recipeId")
    float getRecipeRating(int recipeId);
}