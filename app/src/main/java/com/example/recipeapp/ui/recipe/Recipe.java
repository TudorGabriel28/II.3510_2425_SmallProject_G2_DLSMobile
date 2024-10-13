package com.example.recipeapp.ui.recipe;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class Recipe {
    private String id;
    private String name;
    private String author;
    private String description;
    private String preparationTime;
    private String cookingTime;
    private String skillLevel;
    private List<String> ingredients;
    private List<String> collections;
    private List<String> dietTypes;

}
