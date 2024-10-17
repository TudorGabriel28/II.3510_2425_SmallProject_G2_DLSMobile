package com.example.recipeapp.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipeapp.R;
import com.example.recipeapp.databinding.FragmentSearchBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchView searchView;
    private ListView recipeListView;
    private RecipeListAdapter recipeListAdapter;
//    private List<String> selectedIngredients = new ArrayList<>();
//    private ChipGroup ingredientChipGroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d("SearchFragment", "onCreateView");

        binding = FragmentSearchBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        // Initialize UI components
        searchView = binding.searchView;
        recipeListView = binding.recipeListView;
        updateEmptySearchLayoutVisibility(true);

        // Set search query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SearchFragment", "onQueryTextSubmit: " + query);
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
            // Get the selected recipe
            RecipeListItem recipeListItem = (RecipeListItem) parent.getItemAtPosition(position);

            // Use NavController to navigate to RecipeDetailFragment
            Bundle bundle = new Bundle();
            bundle.putString("recipeName", recipeListItem.getName());
            bundle.putString("recipeAuthor", recipeListItem.getAuthor());

            // Navigate to the detail fragment
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_navigation_home_to_navigation_recipe_detail, bundle);

        });



        return root;
    }

    @Override
    public void onResume() {
        Log.d("SearchFragment", "onResume");
        super.onResume();
        // Reinitialize the search view and list view
        if (searchView != null && searchView.getQuery().length() > 0) {
            searchRecipeByName(searchView.getQuery().toString());
        }
    }


    private void updateEmptySearchLayoutVisibility(boolean isEmpty) {
        if (isEmpty) {
            binding.emptySearchLayout.setVisibility(View.VISIBLE);
            binding.recipeListView.setVisibility(View.GONE);
        } else {
            binding.emptySearchLayout.setVisibility(View.GONE);
            binding.recipeListView.setVisibility(View.VISIBLE);
        }
    }


    private void searchRecipeByName(String recipeName) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe)<-[:WROTE]-(a:Author) WHERE r.name CONTAINS $name RETURN r.name AS name, a.name AS author LIMIT 100";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        service.runCypherQuery(cypherQuery).enqueue(new Neo4jCallback<>(getContext()) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
                List<List<Object>> values = result.getData().getValues();
                if (!values.isEmpty()) {
                    List<RecipeListItem> recipeListItems = new ArrayList<>();
                    for (List<Object> value : values) {
                        recipeListItems.add(new RecipeListItem(value.get(0).toString(), value.get(1).toString()));
                    }

                    updateEmptySearchLayoutVisibility(recipeListItems.isEmpty());
                    updateSearchList(recipeListItems);
                } else {
                    Toast.makeText(getContext(), "No recipe found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSearchList(List<RecipeListItem> recipeListItems) {
        recipeListAdapter = new RecipeListAdapter(getContext(), recipeListItems);
        recipeListView.setAdapter(recipeListAdapter);
    }

    @Override
    public void onDestroyView() {
        Log.d("SearchFragment", "onDestroyView");
        super.onDestroyView();
        binding = null;
    }
}