package com.example.recipeapp.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.R;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IngredientSearchFragment extends Fragment {
    private AutoCompleteTextView ingredientSearchInput;
    private ChipGroup selectedIngredientsChipGroup;
    private ListView recipeListView;
    private Button searchButton;
    private SearchRecipeAdapter searchRecipeAdapter;

    private List<String> ingredientsList = new ArrayList<>(); // Your list of ingredients
    private List<String> selectedIngredients = new ArrayList<>(); // List of selected ingredients

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ingredient_search, container, false);

        ingredientSearchInput = root.findViewById(R.id.ingredientSearchInput);
        selectedIngredientsChipGroup = root.findViewById(R.id.selectedIngredientsChipGroup);
        recipeListView = root.findViewById(R.id.recipeListView);
        searchButton = root.findViewById(R.id.searchButton);

        // Assuming you have a list of ingredients fetched from the database
        ingredientsList = fetchAllIngredients();

        // Setup the AutoCompleteTextView adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, ingredientsList);
        ingredientSearchInput.setAdapter(adapter);

        // Handle ingredient selection
        ingredientSearchInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedIngredient = (String) parent.getItemAtPosition(position);

            if (!selectedIngredients.contains(selectedIngredient)) {
                selectedIngredients.add(selectedIngredient);
                addChip(selectedIngredient); // Create chip for selected ingredient
            }

            // Clear input after selection
            ingredientSearchInput.setText("");
        });

        // Search button click event
        searchButton.setOnClickListener(v -> {
            if (selectedIngredients.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one ingredient", Toast.LENGTH_SHORT).show();
            } else {
                searchRecipesByIngredients(selectedIngredients);
            }
        });

        return root;
    }

    // Method to add the chip to the ChipGroup
    private void addChip(String ingredient) {
        Chip chip = new Chip(getContext());
        chip.setText(ingredient);
        chip.setCloseIconVisible(true); // To allow removal of chip
        chip.setOnCloseIconClickListener(v -> {
            selectedIngredientsChipGroup.removeView(chip);
            selectedIngredients.remove(ingredient);
        });

        selectedIngredientsChipGroup.addView(chip);
    }

    // Method to fetch ingredients (this can be an API call to get the ingredients from the database)
    private List<String> fetchAllIngredients() {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (i:Ingredient) RETURN i.name AS name";

        CypherQuery cypherQuery = new CypherQuery(query);
        List<String> ingredients = new ArrayList<>();

        service.runCypherQuery(cypherQuery).enqueue(new Neo4jCallback<>(getContext()) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
                List<List<Object>> values = result.getData().getValues();
                if (!values.isEmpty()) {
                    for (List<Object> value : values) {
                        ingredients.add(value.get(0).toString());
                    }
                } else {
                    Toast.makeText(getContext(), "No recipes found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return ingredients;
    }

    // Method to search for recipes by selected ingredients
    private void searchRecipesByIngredients(List<String> ingredients) {
        // Construct your Neo4j query or any API call
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (a:Author)-[:WROTE]->(r:Recipe)-[:CONTAINS_INGREDIENT]->(i:Ingredient) " +
                "WHERE i.name IN $ingredients " +
                "RETURN r.name AS name, a.name AS author LIMIT 10";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("ingredients", ingredients);

        service.runCypherQuery(cypherQuery).enqueue(new Neo4jCallback<>(getContext()) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
                List<List<Object>> values = result.getData().getValues();
                if (!values.isEmpty()) {
                    List<RecipeListItem> recipeListItems = new ArrayList<>();
                    for (List<Object> value : values) {
                        recipeListItems.add(new RecipeListItem(value.get(0).toString(), value.get(1).toString()));
                    }
                    updateSearchList(recipeListItems); // Update the ListView with found recipes
                } else {
                    Toast.makeText(getContext(), "No recipes found", Toast.LENGTH_SHORT).show();
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
}

