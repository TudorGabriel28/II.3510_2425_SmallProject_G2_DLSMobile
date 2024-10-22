package com.example.recipeapp.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipeapp.R;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private SearchView searchView;
    private ListView recipeListView;
    private RecipeListAdapter recipeListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        
        searchView = root.findViewById(R.id.searchView);
        recipeListView = root.findViewById(R.id.recipeListView);
        updateEmptySearchLayoutVisibility(root, true);

        
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

        
        recipeListView.setOnItemClickListener((parent, view, position, id) -> {
            
            RecipeListItem recipeListItem = (RecipeListItem) parent.getItemAtPosition(position);

            
            Bundle bundle = new Bundle();
            bundle.putString("recipeName", recipeListItem.getName());
            bundle.putString("recipeAuthor", recipeListItem.getAuthor());

            
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_navigation_home_to_navigation_recipe_detail, bundle);
        });

        return root;
    }

    @Override
    public void onResume() {
        Log.d("SearchFragment", "onResume");
        super.onResume();
        
        updateEmptySearchLayoutVisibility(getView(), true);
        recipeListView.setAdapter(null);
    }

    private void updateEmptySearchLayoutVisibility(View root, boolean isEmpty) {
        View emptySearchLayout = root.findViewById(R.id.emptySearchLayout);
        ListView recipeListView = root.findViewById(R.id.recipeListView);
        if (isEmpty) {
            emptySearchLayout.setVisibility(View.VISIBLE);
            recipeListView.setVisibility(View.GONE);
        } else {
            emptySearchLayout.setVisibility(View.GONE);
            recipeListView.setVisibility(View.VISIBLE);
        }
    }

    private void searchRecipeByName(String recipeName) {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (r:Recipe)<-[:WROTE]-(a:Author) WHERE r.name CONTAINS $name RETURN r.name AS name, a.name AS author LIMIT 100";

        CypherQuery cypherQuery = new CypherQuery(query);
        cypherQuery.addParameter("name", recipeName);

        service.runCypherQuery(cypherQuery).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Neo4jResponse> call, Response<Neo4jResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<List<Object>> values = response.body().getData().getValues();
                    if (!values.isEmpty()) {
                        List<RecipeListItem> recipeListItems = new ArrayList<>();
                        for (List<Object> value : values) {
                            recipeListItems.add(new RecipeListItem(value.get(0).toString(), value.get(1).toString()));
                        }

                        updateEmptySearchLayoutVisibility(getView(), recipeListItems.isEmpty());
                        updateSearchList(recipeListItems);
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                        showNoResultsText();
                    }
                } else {
                    Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    showNoResultsText();
                }
            }

            @Override
            public void onFailure(Call<Neo4jResponse> call, Throwable t) {
                Log.e("Neo4jCallback", "Network call failed", t);
                Toast.makeText(getContext(), "Failed to connect: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showNoResultsText();
            }
        });
    }

    private void showNoResultsText() {
        View root = getView();
        if (root != null) {
            updateEmptySearchLayoutVisibility(root, true);
            TextView emptySearchText = root.findViewById(R.id.emptySearchText);
            emptySearchText.setText("No results found");
        }
    }

    private void updateSearchList(List<RecipeListItem> recipeListItems) {
        recipeListAdapter = new RecipeListAdapter(getContext(), recipeListItems);
        recipeListView.setAdapter(recipeListAdapter);
    }

    @Override
    public void onDestroyView() {
        Log.d("SearchFragment", "onDestroyView");
        super.onDestroyView();
    }
}