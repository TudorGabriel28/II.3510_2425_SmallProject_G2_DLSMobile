package com.example.recipeapp.ui.recipe;

import android.content.Intent; // Import for sharing
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import for Button
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private RecipeDatabase localDb;
    private RatingBar ratingBar;
    private Button shareRecipeButton; // Declare the share button

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        localDb = DatabaseClient.getInstance(requireContext()).getRecipeDatabase();
        // Inflate the layout using ViewBinding
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the share button
        shareRecipeButton = binding.shareRecipeButton;

        if (getArguments() == null) {
            Log.e("RecipeDetailsFragment", "No arguments passed");
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }

        String recipeName = getArguments().getString("recipeName");
        String recipeAuthor = getArguments().getString("recipeAuthor");

        binding.recipeTitleTextView.setText(recipeName);
        binding.recipeAuthorTextView.setText(recipeAuthor);

        // Initialize RatingBar
        ratingBar = binding.ratingBar;

        CompletableFuture<Void> recipeFuture = fetchRecipeDetails(recipeName, recipeAuthor).thenAccept(recipe -> {
            displayData(recipe);
            handleFavoriteBtn(localDb, recipe);
            loadRating(recipe.getId()); // Load existing rating

            // Set up share button click listener
            shareRecipeButton.setOnClickListener(v -> shareRecipe(recipe));
        }).exceptionally(throwable -> {
            Toast.makeText(getContext(), "Error fetching recipe details", Toast.LENGTH_SHORT).show();
            returnToPreviousFragment();
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
                "                                        collect(DISTINCT c.name) AS Collections, collect(DISTINCT d.name) AS DietTypes, r.rating AS Rating";

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

            // Remove the handleFailure method completely
        });

        return futureRecipe;
    }

    @NonNull
    private static Recipe mapResponseToRecipe(List<List<Object>> values, String author) {
        List<Object> recipeDetails = values.get(0);

        // Convert time from seconds to minutes and add "minutes" to the string
        int preparationTimeInMin = (int) (Double.parseDouble(recipeDetails.get(3).toString()) / 60);
        int cookingTimeInMin = (int) (Double.parseDouble(recipeDetails.get(4).toString()) / 60);

        String ingredientStr = recipeDetails.get(6).toString().substring(1, recipeDetails.get(6).toString().length() - 1);
        String collectionStr = recipeDetails.get(7).toString().substring(1, recipeDetails.get(7).toString().length() - 1);
        String dietTypeStr = recipeDetails.get(8).toString().substring(1, recipeDetails.get(8).toString().length() - 1);

        float defaultRating = 0.0f; // Default rating value

        return new Recipe(Integer.parseInt(recipeDetails.get(0).toString()),
                recipeDetails.get(1).toString(),
                author,
                recipeDetails.get(2).toString(),
                preparationTimeInMin + " min",
                cookingTimeInMin + " min",
                recipeDetails.get(5).toString(),
                ingredientStr,
                collectionStr,
                dietTypeStr,
                defaultRating); // Include rating
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

        // Set up RatingBar listener
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                saveRating(recipe.getId(), rating);
            }
        });
    }

    private void saveRating(int recipeId, float rating) {
        new Thread(() -> {
            localDb.recipeDao().updateRecipeRating(recipeId, rating);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Rating saved", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void loadRating(int recipeId) {
        new Thread(() -> {
            float rating = localDb.recipeDao().getRecipeRating(recipeId);
            requireActivity().runOnUiThread(() -> {
                ratingBar.setRating(rating);
            });
        }).start();
    }

    // Method to share the recipe
    private void shareRecipe(Recipe recipe) {
        String recipeTitle = recipe.getName();
        String recipeDescription = recipe.getDescription();
        String ingredients = recipe.getIngredients();
        String preparationTime = recipe.getPreparationTime();
        String cookingTime = recipe.getCookingTime();
        String skillLevel = recipe.getSkillLevel();

        // Create the share text including all relevant information
        String shareText = "Check out this recipe!\n\n" +
                "Title: " + recipeTitle + "\n" +
                "Description: " + recipeDescription + "\n" +
                "Preparation Time: " + preparationTime + "\n" +
                "Cooking Time: " + cookingTime + "\n" +
                "Skill Level: " + skillLevel + "\n\n" +
                "Ingredients:\n" + ingredients;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share Recipe via"));
    }
}