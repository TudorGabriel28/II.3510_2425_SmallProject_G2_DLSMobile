package com.example.recipeapp.ui.recipe;

import android.content.Intent; 
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; 
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipeapp.R;
import com.example.recipeapp.data_access.DatabaseClient;
import com.example.recipeapp.data_access.RecipeDatabase;
import com.example.recipeapp.dtos.CollectionCarouselDto;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.entities.Recipe;
import com.example.recipeapp.helpers.Converters;
import com.example.recipeapp.helpers.FragmentExtensions;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsFragment extends Fragment {

    private Boolean isFavorite = false;
    private RecipeDatabase localDb;
    private RatingBar ratingBar;
    private Button shareRecipeButton; 
    private Button favoritesButton;
    private TextView recipeTitleTextView;
    private TextView recipeDescriptionTextView;
    private TextView recipeAuthorTextView;
    private TextView recipePreparationTimeTextView;
    private TextView recipeCookingTimeTextView;
    private TextView recipeSkillLevelTextView;
    private TextView recipeDietTypesTextView;
    private TextView recipeCollectionsTextView;
    private ChipGroup selectedIngredientsChipGroup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localDb = DatabaseClient.getInstance(requireContext()).getRecipeDatabase();
        
        View root = inflater.inflate(R.layout.fragment_recipe_details, container, false);

        
        shareRecipeButton = root.findViewById(R.id.shareRecipeButton);
        favoritesButton = root.findViewById(R.id.favorites_button);
        recipeTitleTextView = root.findViewById(R.id.recipeTitleTextView);
        recipeDescriptionTextView = root.findViewById(R.id.recipeDescriptionTextView);
        recipeAuthorTextView = root.findViewById(R.id.recipeAuthorTextView);
        recipePreparationTimeTextView = root.findViewById(R.id.recipePreparationTimeTextView);
        recipeCookingTimeTextView = root.findViewById(R.id.recipeCookingTimeTextView);
        recipeSkillLevelTextView = root.findViewById(R.id.recipeSkillLevelTextView);
        recipeDietTypesTextView = root.findViewById(R.id.recipeDietTypesTextView);
        recipeCollectionsTextView = root.findViewById(R.id.recipeCollectionsTextView);
        ratingBar = root.findViewById(R.id.ratingBar);
        selectedIngredientsChipGroup = root.findViewById(R.id.selectedIngredientsChipGroup);

        if (getArguments() == null) {
            Log.e("RecipeDetailsFragment", "No arguments passed");
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }

        String recipeName = getArguments().getString("recipeName");

        recipeTitleTextView.setText(recipeName);

        CompletableFuture<Void> recipeFuture = fetchRecipeDetails(recipeName).thenAccept(recipe -> {
            displayData(recipe);
            handleFavoriteBtn(localDb, recipe);
            loadRating(recipe.getId()); 

            
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
    }

    private void returnToPreviousFragment() {
        requireActivity().runOnUiThread(() -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void handleFavoriteBtn(RecipeDatabase localDb, Recipe recipe) {
        CompletableFuture.supplyAsync(() -> localDb.recipeDao().getRecipeById(recipe.getId()))
                .thenAccept(response -> {
                    if (response != null) {
                        isFavorite = true;
                        favoritesButton.setText("Remove from favorites");
                    } else {
                        isFavorite = false;
                        favoritesButton.setText("Add to favorites");
                    }
                });

        favoritesButton.setOnClickListener(v -> {
            new Thread(() -> {
                if (isFavorite) {
                    localDb.recipeDao().deleteRecipe(recipe.getId());
                    isFavorite = false;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Recipe removed from favorites", Toast.LENGTH_SHORT).show();
                        favoritesButton.setText("Add to favorites");
                    });
                } else {
                    localDb.recipeDao().insertRecipe(recipe);
                    isFavorite = true;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Recipe added to favorites", Toast.LENGTH_SHORT).show();
                        favoritesButton.setText("Remove from favorites");
                    });
                }
            }).start();
        });
    }

    
    private void addChip(String ingredient) {
        Chip chip = new Chip(getContext());
        chip.setText(ingredient);
        chip.setOnCloseIconClickListener(v -> {
            selectedIngredientsChipGroup.removeView(chip);
        });

        selectedIngredientsChipGroup.addView(chip);
    }

    private CompletableFuture<Recipe> fetchRecipeDetails(String recipeName) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe {name: $name})-[:CONTAINS_INGREDIENT]->(i: Ingredient)  \n" +
                "                          OPTIONAL MATCH (r)-[:COLLECTION]->(c: Collection)\n" +
                "                          OPTIONAL MATCH (r)-[:DIET_TYPE]->(d: DietType)\n" +
                "                          OPTIONAL MATCH (r)<-[:WROTE]-(a: Author)\n" +
                "                          RETURN r.id AS Id, r.name AS Name, r.description AS Description, r.preparationTime AS PreparationTime, \n" +
                "                                        r.cookingTime AS CookingTime, r.skillLevel AS SkillLevel, collect(DISTINCT i.name) AS Ingredients,\n" +
                "                                        collect(DISTINCT c.name) AS Collections, collect(DISTINCT d.name) AS DietTypes, a.name AS author";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        CompletableFuture<Recipe> futureRecipe = new CompletableFuture<>();

        service.runCypherQuery(cypherQuery).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Neo4jResponse> call, Response<Neo4jResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<List<Object>> values = response.body().getData().getValues();
                    if (!values.isEmpty()) {
                        Recipe recipe = mapResponseToRecipe(values);
                        futureRecipe.complete(recipe);

                    } else {
                        Toast.makeText(getContext(), "No recipe found", Toast.LENGTH_SHORT).show();
                        returnToPreviousFragment();
                    }
                } else {
                    Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    returnToPreviousFragment();
                }

            }

            @Override
            public void onFailure(Call<Neo4jResponse> call, Throwable t) {
                Log.e("Neo4jCallback", "Network call failed", t);
                Toast.makeText(getContext(), "Failed to connect: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                returnToPreviousFragment();
            }

        });

        return futureRecipe;
    }

    @NonNull
    private static Recipe mapResponseToRecipe(List<List<Object>> values) {
        List<Object> recipeDetails = values.get(0);

        
        int preparationTimeInMin = (int) (Double.parseDouble(recipeDetails.get(3).toString()) / 60);
        int cookingTimeInMin = (int) (Double.parseDouble(recipeDetails.get(4).toString()) / 60);

        String ingredientStr = recipeDetails.get(6).toString().substring(1, recipeDetails.get(6).toString().length() - 1);
        String collectionStr = recipeDetails.get(7).toString().substring(1, recipeDetails.get(7).toString().length() - 1);
        String dietTypeStr = recipeDetails.get(8).toString().substring(1, recipeDetails.get(8).toString().length() - 1);

        float defaultRating = 0.0f; 

        return new Recipe(Integer.parseInt(recipeDetails.get(0).toString()),
                recipeDetails.get(1).toString(),
                recipeDetails.get(9).toString(),
                recipeDetails.get(2).toString(),
                preparationTimeInMin + " min",
                cookingTimeInMin + " min",
                recipeDetails.get(5).toString(),
                ingredientStr,
                collectionStr,
                dietTypeStr,
                defaultRating); 
    }

    private void displayData(Recipe recipe) {
        
        recipeDescriptionTextView.setText(recipe.getDescription());
        recipeAuthorTextView.setText(recipe.getAuthor());
        recipePreparationTimeTextView.setText(recipe.getPreparationTime());
        recipeCookingTimeTextView.setText(recipe.getCookingTime());
        recipeSkillLevelTextView.setText(recipe.getSkillLevel());

        for (String ingredient : Converters.fromString(recipe.getIngredients())) {
            addChip(ingredient);
        }

        recipeDietTypesTextView.setText(recipe.getDietTypes());
        recipeCollectionsTextView.setText(recipe.getCollections());

        
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

    
    private void shareRecipe(Recipe recipe) {
        String recipeTitle = recipe.getName();
        String recipeDescription = recipe.getDescription();
        String ingredients = recipe.getIngredients();
        String preparationTime = recipe.getPreparationTime();
        String cookingTime = recipe.getCookingTime();
        String skillLevel = recipe.getSkillLevel();

        
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