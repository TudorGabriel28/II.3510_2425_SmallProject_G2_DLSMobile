package com.example.recipeapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.recipeapp.databinding.FragmentHomeBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SearchView searchView;
    private ListView recipeListView;
    private SearchRecipeAdapter searchRecipeAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        // Initialize UI components
        searchView = binding.searchView;
        recipeListView = binding.recipeListView;


        // Set search query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Search for the recipe by name
                System.out.println("Query: " + query);
                searchRecipeByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return root;
    }

    private void searchRecipeByName(String recipeName) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = String.format("MATCH (r:Recipe)<-[:WROTE]-(a:Author) WHERE r.name CONTAINS $name RETURN r.name AS name, a.name AS author LIMIT 10");

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        service.runCypherQuery(cypherQuery).enqueue(new Callback<Neo4jResponse>() {
            @Override
            public void onResponse(Call<Neo4jResponse> call, Response<Neo4jResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Neo4jResponse result = response.body();
                    List<List<String>> values = result.getData().getValues();

                    if (!values.isEmpty()) {
                        List<RecipeListItem> recipeListItems = new ArrayList<>();
                        for (List<String> value : values) {
                            recipeListItems.add(new RecipeListItem(value.get(0), value.get(1)));
                        }
                        updateSearchList(recipeListItems);
                    } else {
                        Toast.makeText(getContext(), "No recipe found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error fetching recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Neo4jResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSearchList(List<RecipeListItem> recipeListItems) {
        if (searchRecipeAdapter == null) {
            searchRecipeAdapter = new SearchRecipeAdapter(getContext(), recipeListItems);
            recipeListView.setAdapter(searchRecipeAdapter);
        } else {
            searchRecipeAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}