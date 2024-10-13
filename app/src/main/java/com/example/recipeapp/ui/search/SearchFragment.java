package com.example.recipeapp.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipeapp.R;
import com.example.recipeapp.databinding.FragmentHomeBinding;
import com.example.recipeapp.databinding.FragmentSearchBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.example.recipeapp.ui.home.HomeViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchView searchView;
    private ListView recipeListView;
    private SearchRecipeAdapter searchRecipeAdapter;
//    private List<String> selectedIngredients = new ArrayList<>();
//    private ChipGroup ingredientChipGroup;

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
        recipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected recipe
                RecipeListItem recipeListItem = (RecipeListItem) parent.getItemAtPosition(position);

                // Use NavController to navigate to RecipeDetailFragment
                Bundle bundle = new Bundle();
                bundle.putString("recipeName", recipeListItem.getName());
                bundle.putString("recipeAuthor", recipeListItem.getAuthor());

                // Navigate to the detail fragment
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_navigation_home_to_navigation_recipe_detail, bundle);
            }
        });


//        recipeListView = binding.recipeListView;
//        ingredientChipGroup = binding.ingredientChipGroup;
//
//        List<String> ingredients = getIngredientsList();  // Load ingredients from your data source
//        addIngredientsToChipGroup(ingredients);

        return root;
    }

//    private List<String> getIngredientsList() {
//        // Mock data or load from your Neo4j or local database
//        return Arrays.asList("Tomato", "Cheese", "Garlic", "Onion", "Chicken", "Basil", "Pepper", "Salt");
//    }

//    private void addIngredientsToChipGroup(List<String> ingredients) {
//        for (String ingredient : ingredients) {
//            Chip chip = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.chip_item, ingredientChipGroup, false);
//            chip.setText(ingredient);
//            chip.setCheckable(true);
//
//            // Handle chip selection
//            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                if (isChecked) {
//                    selectedIngredients.add(ingredient);
//                } else {
//                    selectedIngredients.remove(ingredient);
//                }
//            });
//
//            ingredientChipGroup.addView(chip);
//        }
//    }

    private void searchRecipeByName(String recipeName) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe)<-[:WROTE]-(a:Author) WHERE r.name CONTAINS $name RETURN r.name AS name, a.name AS author LIMIT 10";

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