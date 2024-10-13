package com.example.recipeapp.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Neo4jService {
    private static final String BASE_URL = "https://d8cd79db.databases.neo4j.io";

    private static Neo4jApiService apiService;

    public static Neo4jApiService getInstance() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(Neo4jApiService.class);
        }
        return apiService;
    }
}

