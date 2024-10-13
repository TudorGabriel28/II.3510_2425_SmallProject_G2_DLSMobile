package com.example.recipeapp.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.databinding.FragmentRecipeDetailsBinding;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.example.recipeapp.ui.search.RecipeListItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RecipeDetailsFragment extends Fragment {

    private FragmentRecipeDetailsBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
                    Recipe recipe = new Recipe(recipeDetails.get(0).toString(),
                            recipeDetails.get(1).toString(),
                            author,
                            recipeDetails.get(2).toString(),
                            recipeDetails.get(3).toString(),
                            recipeDetails.get(4).toString(),
                            recipeDetails.get(5).toString(),
                            (List<String>) recipeDetails.get(6),
                            (List<String>) recipeDetails.get(7),
                            (List<String>) recipeDetails.get(8));

                    // Populate the views with the recipe details
                    binding.recipeDescriptionTextView.setText(recipe.getDescription());
                    binding.recipePreparationTimeTextView.setText(recipe.getPreparationTime());
                    binding.recipeCookingTimeTextView.setText(recipe.getCookingTime());
                    binding.recipeSkillLevelTextView.setText(recipe.getSkillLevel());
                    binding.recipeIngredientsTextView.setText(recipe.getIngredients().toString());
                    binding.recipeCollectionsTextView.setText(recipe.getCollections().toString());
                    binding.recipeDietTypesTextView.setText(recipe.getDietTypes().toString());
                } else {
                    Toast.makeText(getContext(), "No recipe found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
