package com.example.recipeapp.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.R;
import com.example.recipeapp.databinding.FragmentRecipeDetailsBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.example.recipeapp.ui.search.RecipeListItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RecipeDetailsFragment extends Fragment {

    private FragmentRecipeDetailsBinding binding;
    private ChipGroup selectedIngredientsChipGroup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        selectedIngredientsChipGroup = root.findViewById(R.id.selectedIngredientsChipGroup);

        // Get the arguments passed from the previous fragment
        if (getArguments() != null) {
            String recipeName = getArguments().getString("recipeName");
            String recipeAuthor = getArguments().getString("recipeAuthor");

            // Populate the views with the recipe data
            binding.recipeTitleTextView.setText(recipeName);
            binding.recipeAuthorTextView.setText(recipeAuthor);

//            Get recipe details from the database
            getRecipeDetails(recipeName, recipeAuthor);


        } else {
            // If no arguments are passed, display a message
            binding.recipeTitleTextView.setText("No recipe selected");
            binding.recipeAuthorTextView.setText("Please select a recipe from the list");
        }

        return root;
    }

    // Method to add the chip to the ChipGroup
    private void addChip(String ingredient) {
        Chip chip = new Chip(getContext());
        chip.setText(ingredient);
        chip.setOnCloseIconClickListener(v -> {
            selectedIngredientsChipGroup.removeView(chip);
        });

        selectedIngredientsChipGroup.addView(chip);
    }

    private void getRecipeDetails(String recipeName, String author) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe {name: $name})-[:CONTAINS_INGREDIENT]->(i: Ingredient)  \n" +
"                          OPTIONAL MATCH (r)-[:COLLECTION]->(c: Collection)\n" +
"                          OPTIONAL MATCH (r)-[:DIET_TYPE]->(d: DietType)\n" +
"                          RETURN r.id AS Id, r.name AS Name, r.description AS Description, r.preparationTime AS PreparationTime, \n" +
"                                        r.cookingTime AS CookingTime, r.skillLevel AS SkillLevel, collect(DISTINCT i.name) AS Ingredients,\n" +
"                                        collect(DISTINCT c.name) AS Collections, collect(DISTINCT d) AS DietTypes";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        service.runCypherQuery(cypherQuery).enqueue(new Neo4jCallback<>(getContext()) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
                List<List<Object>> values = result.getData().getValues();
                if (!values.isEmpty()) {
                    List<Object> recipeDetails = values.get(0);

//                    Convert time from seconds to minutes and add "minutes" to the string
                    int preparationTimeInMin = (int) (Double.parseDouble(recipeDetails.get(3).toString()) / 60);
                    int cookingTimeInMin = (int) (Double.parseDouble(recipeDetails.get(4).toString()) / 60);

                    Recipe recipe = new Recipe(recipeDetails.get(0).toString(),
                            recipeDetails.get(1).toString(),
                            author,
                            recipeDetails.get(2).toString(),
                            preparationTimeInMin + " min",
                            cookingTimeInMin + " min",
                            recipeDetails.get(5).toString(),
                            (List<String>) recipeDetails.get(6),
                            (List<String>) recipeDetails.get(7),
                            (List<String>) recipeDetails.get(8));

                    // Populate the views with the recipe details
                    binding.recipeDescriptionTextView.setText(recipe.getDescription());
                    binding.recipePreparationTimeTextView.setText(recipe.getPreparationTime());
                    binding.recipeCookingTimeTextView.setText(recipe.getCookingTime());
                    binding.recipeSkillLevelTextView.setText(recipe.getSkillLevel());
                    for (String ingredient : recipe.getIngredients()) {
                        addChip(ingredient);
                    }

                    if (recipe.getCollections() != null) {
                        String collections = recipe.getCollections().toString();
                        collections = collections.substring(1, collections.length() - 1);
                        binding.recipeCollectionsTextView.setText(collections);
                    } else {
                        binding.recipeCollectionsTextView.setText(R.string.no_collections);
                    }


                    if (recipe.getDietTypes() == null || recipe.getDietTypes().size() == 0) {
                        binding.recipeDietTypesTextView.setText(R.string.no_diet_types);
                    } else {
                        String dietTypes = recipe.getDietTypes().toString();
                        dietTypes = dietTypes.substring(1, dietTypes.length() - 1);
                        binding.recipeDietTypesTextView.setText(dietTypes);
                    }
                } else {
                    Toast.makeText(getContext(), "No recipe found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
