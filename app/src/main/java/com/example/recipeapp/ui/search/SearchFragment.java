package com.example.recipeapp.ui.search;

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
import com.example.recipeapp.databinding.FragmentSearchBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.example.recipeapp.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchView searchView;
    private ListView recipeListView;
    private SearchRecipeAdapter searchRecipeAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        // Initialize UI components
        searchView = binding.searchView;
        recipeListView = binding.recipeListView;

        // Set search query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRecipeByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

//        Set on item click listener
        recipeListView.setOnItemClickListener((parent, view, position, id) -> {
            RecipeListItem recipeListItem = (RecipeListItem) parent.getItemAtPosition(position);
//            Toast.makeText(getContext(), recipeListItem.getName(), Toast.LENGTH_SHORT).show();

            // Create an Intent to start RecipeDetailActivity
//            Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
//
//            // Pass recipe details to the RecipeDetailActivity
//            intent.putExtra("recipeName", selectedRecipe.getName());
//            intent.putExtra("recipeCategory", selectedRecipe.getCategory());
//            startActivity(intent);
        });

        return root;
    }

    private void searchRecipeByName(String recipeName) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe)<-[:WROTE]-(a:Author) WHERE r.name CONTAINS $name RETURN r.name AS name, a.name AS author LIMIT 10";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        service.runCypherQuery(cypherQuery).enqueue(new Neo4jCallback<>(getContext()) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
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