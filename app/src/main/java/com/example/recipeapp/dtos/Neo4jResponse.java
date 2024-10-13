package com.example.recipeapp.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Neo4jResponse {
    private Data data;

    @Getter
    public static class Data {
        private List<String> fields;
        private List<List<String>> values;

    }
}