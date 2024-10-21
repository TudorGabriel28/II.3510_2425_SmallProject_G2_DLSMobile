package com.example.recipeapp.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // Add this line to generate setters
@NoArgsConstructor // Add this line to create a no-argument constructor
@AllArgsConstructor
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = false)
    private int id;
    private String name;
    private String author;
    private String description;
    private String preparationTime;
    private String cookingTime;
    private String skillLevel;
    private String ingredients;
    private String collections;
    private String dietTypes;
    private float rating; // Store the recipe rating

    // You might want to create setters if you need to update fields
    public void setRating(float rating) {
        this.rating = rating;
    }
}