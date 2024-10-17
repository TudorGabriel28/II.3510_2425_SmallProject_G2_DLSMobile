package com.example.recipeapp.data_access;

import android.content.Context;
import androidx.room.Room;

import lombok.Getter;

public class DatabaseClient {

    private Context context;
    private static DatabaseClient instance;

    @Getter
    private RecipeDatabase recipeDatabase;

    private DatabaseClient(Context context) {
        this.context = context;

        // Creating the RecipeDatabase with Room
        recipeDatabase = Room.databaseBuilder(context, RecipeDatabase.class, "RecipeDB").build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

}
