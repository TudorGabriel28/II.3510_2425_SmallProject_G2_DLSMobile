package com.example.recipeapp.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipeapp.R;
import com.example.recipeapp.data_access.DatabaseClient;
import com.example.recipeapp.databinding.FragmentFavoritesBinding;
import com.example.recipeapp.entities.Recipe;
import com.example.recipeapp.ui.search.RecipeListItem;
import com.example.recipeapp.ui.search.RecipeListAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private RecipeListAdapter recipeListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FavoritesViewModel favoritesViewModel =
                new ViewModelProvider(this).get(FavoritesViewModel.class);

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        new Thread(() -> {
            List<Recipe> recipes = DatabaseClient.getInstance(getContext()).getRecipeDatabase().recipeDao().getAllRecipes();
            List<RecipeListItem> recipeListItems = new ArrayList<>();
            for (Recipe recipe : recipes) {
                recipeListItems.add(new RecipeListItem(recipe.getName(), recipe.getAuthor()));
            }
            // Update the UI on the main thread
            getActivity().runOnUiThread(() -> {
                updateFavoritesListView(recipeListItems);
            });
        }).start();

        binding.recipeListView.setOnItemClickListener((parent, view, position, id) -> {
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

    private void updateFavoritesListView(List<RecipeListItem> recipeListItems) {
        recipeListAdapter = new RecipeListAdapter(getContext(), recipeListItems);
        binding.recipeListView.setAdapter(recipeListAdapter);
        updateEmptyFavoritesLayoutVisibility(recipeListItems.isEmpty());
    }

    private void updateEmptyFavoritesLayoutVisibility(boolean isEmpty) {
        if (isEmpty) {
            binding.emptyFavoritesLayout.setVisibility(View.VISIBLE);
            binding.recipeListView.setVisibility(View.GONE);
        } else {
            binding.emptyFavoritesLayout.setVisibility(View.GONE);
            binding.recipeListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}