package com.example.recipeapp.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class CypherQuery {
    private String statement;
    private Map<String, Object> parameters = new HashMap<>();

    public CypherQuery(String statement) {
        this.statement = statement;
    }

    public void addParameter(String key, Object value) {
        parameters.put(key, value);
    }
}

