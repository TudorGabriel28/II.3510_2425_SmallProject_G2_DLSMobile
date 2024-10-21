package com.example.recipeapp.services;

import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Neo4jApiService {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Basic " + "bmVvNGo6c0pXSjBoeFgzMUc2eUpjUldhcHNIUlhiN3lHWGhpay1lQ1Q1aTVMbUFMdw=="
    })
    @POST("db/neo4j/query/v2")
    Call<Neo4jResponse> runCypherQuery(@Body CypherQuery query);

    // New method for fetching similar recipes
    @POST("db/neo4j/query/v2")
    Call<Neo4jResponse> fetchSimilarRecipes(@Body CypherQuery query);

    @POST("db/neo4j/query/v2")
    Call<Neo4jResponse> fetchCollections(@Body CypherQuery query);

}
