package com.example.recipeapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.recipeapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MenuItem searchByIngredientsButton;
    private Neo4jService neo4jService; // Instance pour interagir avec Neo4j

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        neo4jService = new Neo4jService(); // Initialisation de Neo4jService pour charger les données

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_favorites, R.id.navigation_profile, R.id.navigation_search)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        showMenuBtnOnlyOnSearchFragment(navController);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        loadHomeData(); // Charger les données pour la page d'accueil

    }

    private void loadHomeData(){
        neo4jService.getCategories(new Neo4jCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> categories) {

            }

        }

    private void showMenuBtnOnlyOnSearchFragment(NavController navController) {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (searchByIngredientsButton != null) {
                if (destination.getId() == R.id.navigation_search) {
                    searchByIngredientsButton.setVisible(true);
                } else {
                    searchByIngredientsButton.setVisible(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar_menu, menu);
        searchByIngredientsButton = menu.findItem(R.id.search_button);
        searchByIngredientsButton.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_button) {
            // Handle search button click
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_ingredient_search);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }
    }

}