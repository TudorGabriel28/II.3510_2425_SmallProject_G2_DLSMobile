package com.example.recipeapp.ui.recipe;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.data_access.DatabaseClient;
import com.example.recipeapp.data_access.RecipeDatabase;
import com.example.recipeapp.databinding.FragmentRecipeDetailsBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.entities.Recipe;
import com.example.recipeapp.helpers.Converters;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecipeDetailsFragment extends Fragment {

    private FragmentRecipeDetailsBinding binding;
    private Boolean isFavorite = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RecipeDatabase localDb = DatabaseClient.getInstance(requireContext()).getRecipeDatabase();
        // Inflate the layout using ViewBinding
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() == null) {
            Log.e("RecipeDetailsFragment", "No arguments passed");
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }

        String recipeName = getArguments().getString("recipeName");
        String recipeAuthor = getArguments().getString("recipeAuthor");

        binding.recipeTitleTextView.setText(recipeName);
        binding.recipeAuthorTextView.setText(recipeAuthor);


        CompletableFuture<Void> recipeFuture = fetchRecipeDetails(recipeName, recipeAuthor).thenAccept(recipe -> {
            displayData(recipe);
            handleFavoriteBtn(localDb, recipe);
        }).exceptionally(throwable -> {
//            Toast.makeText(getContext(), "Error fetching recipe details", Toast.LENGTH_SHORT).show();
//            returnToPreviousFragment();
            return null;
        });



        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void returnToPreviousFragment() {
        requireActivity().runOnUiThread(() -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void handleFavoriteBtn(RecipeDatabase localDb, Recipe recipe) {
        CompletableFuture.supplyAsync(() -> localDb.recipeDao().getRecipeById(recipe.getId()))
        .thenAccept(response -> {
            if (response != null) {
                isFavorite = true;
                binding.favoritesButton.setText("Remove from favorites");
            } else {
                isFavorite = false;
                binding.favoritesButton.setText("Add to favorites");
            }
        });

        binding.favoritesButton.setOnClickListener(v -> {
            new Thread(() -> {
                if (isFavorite) {
                    localDb.recipeDao().deleteRecipe(recipe.getId());
                    isFavorite = false;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Recipe removed from favorites", Toast.LENGTH_SHORT).show();
                        binding.favoritesButton.setText("Add to favorites");
                    });
                } else {
                    localDb.recipeDao().insertRecipe(recipe);
                    isFavorite = true;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Recipe added to favorites", Toast.LENGTH_SHORT).show();
                        binding.favoritesButton.setText("Remove from favorites");
                    });
                }
            }).start();
        });
    }

    // Method to add the chip to the ChipGroup
    private void addChip(String ingredient) {
        Chip chip = new Chip(getContext());
        chip.setText(ingredient);
        chip.setOnCloseIconClickListener(v -> {
            binding.selectedIngredientsChipGroup.removeView(chip);
        });

        binding.selectedIngredientsChipGroup.addView(chip);
    }

    private CompletableFuture<Recipe> fetchRecipeDetails(String recipeName, String author) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe {name: $name})-[:CONTAINS_INGREDIENT]->(i: Ingredient)  \n" +
"                          OPTIONAL MATCH (r)-[:COLLECTION]->(c: Collection)\n" +
"                          OPTIONAL MATCH (r)-[:DIET_TYPE]->(d: DietType)\n" +
"                          RETURN r.id AS Id, r.name AS Name, r.description AS Description, r.preparationTime AS PreparationTime, \n" +
"                                        r.cookingTime AS CookingTime, r.skillLevel AS SkillLevel, collect(DISTINCT i.name) AS Ingredients,\n" +
"                                        collect(DISTINCT c.name) AS Collections, collect(DISTINCT d.name) AS DietTypes";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        CompletableFuture<Recipe> futureRecipe = new CompletableFuture<>();

        service.runCypherQuery(cypherQuery).enqueue(new Neo4jCallback<>(getContext()) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
                List<List<Object>> values = result.getData().getValues();
                if (!values.isEmpty()) {
                    Recipe recipe = mapResponseToRecipe(values, author);
                    futureRecipe.complete(recipe);
                } else {
                    Toast.makeText(getContext(), "No recipe found", Toast.LENGTH_SHORT).show();
                    returnToPreviousFragment();
                }
            }
        });

        return futureRecipe;
    }

    @NonNull
    private static Recipe mapResponseToRecipe(List<List<Object>> values, String author) {
        List<Object> recipeDetails = values.get(0);

//                    Convert time from seconds to minutes and add "minutes" to the string
        int preparationTimeInMin = (int) (Double.parseDouble(recipeDetails.get(3).toString()) / 60);
        int cookingTimeInMin = (int) (Double.parseDouble(recipeDetails.get(4).toString()) / 60);

        String ingredientStr = recipeDetails.get(6).toString().substring(1, recipeDetails.get(6).toString().length() - 1);
        String collectionStr = recipeDetails.get(7).toString().substring(1, recipeDetails.get(7).toString().length() - 1);
        String dietTypeStr = recipeDetails.get(8).toString().substring(1, recipeDetails.get(8).toString().length() - 1);

        return new Recipe(Integer.parseInt(recipeDetails.get(0).toString()),
                recipeDetails.get(1).toString(),
                author,
                recipeDetails.get(2).toString(),
                preparationTimeInMin + " min",
                cookingTimeInMin + " min",
                recipeDetails.get(5).toString(),
                ingredientStr,
                collectionStr,
                dietTypeStr);
    }

    private void displayData(Recipe recipe) {
        // Populate the views with the recipe details
        binding.recipeDescriptionTextView.setText(recipe.getDescription());
        binding.recipePreparationTimeTextView.setText(recipe.getPreparationTime());
        binding.recipeCookingTimeTextView.setText(recipe.getCookingTime());
        binding.recipeSkillLevelTextView.setText(recipe.getSkillLevel());
        for (String ingredient : Converters.fromString(recipe.getIngredients())) {
            addChip(ingredient);
        }
        binding.recipeDietTypesTextView.setText(recipe.getDietTypes());
        binding.recipeCollectionsTextView.setText(recipe.getCollections());
    }
}
