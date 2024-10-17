package com.example.recipeapp.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
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

}
