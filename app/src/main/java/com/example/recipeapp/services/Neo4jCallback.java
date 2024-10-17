package com.example.recipeapp.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class Neo4jCallback<T> implements Callback<T> {
    private Context context;

    public Neo4jCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Log.e("Neo4jCallback", "Network call failed", t);
        Toast.makeText(context, "Failed to connect: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        throw new RuntimeException(t);
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful() && response.body() != null) {
            handleSuccess(response.body());
        } else {
            Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show();
            throw new RuntimeException("Error fetching data");
        }
    }

    public abstract void handleSuccess(T result);
}
