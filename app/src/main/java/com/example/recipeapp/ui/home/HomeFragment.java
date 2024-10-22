package com.example.recipeapp.ui.home;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.MainActivity;
import com.example.recipeapp.R;
import com.example.recipeapp.dtos.CarouselItem;
import com.example.recipeapp.dtos.CollectionCarouselDto;
import com.example.recipeapp.dtos.CypherQuery;
import com.example.recipeapp.dtos.Neo4jResponse;
import com.example.recipeapp.services.Neo4jApiService;
import com.example.recipeapp.services.Neo4jCallback;
import com.example.recipeapp.services.Neo4jService;
import com.example.recipeapp.ui.search.RecipeListItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private LinearLayout carouselContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        carouselContainer = root.findViewById(R.id.carousel_container);

        fetchCollections();

        return root;
    }

    private void fetchCollections() {
        Neo4jApiService service = Neo4jService.getInstance();
        String query = "MATCH (c:Collection)" +
                "RETURN c.name AS collectionName, COLLECT {MATCH (r:Recipe)-[:COLLECTION]->(c) RETURN r.name LIMIT 5 } LIMIT 10";

        CypherQuery cypherQuery = new CypherQuery(query);

        service.runCypherQuery(cypherQuery).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Neo4jResponse> call, Response<Neo4jResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<List<Object>> values = response.body().getData().getValues();
                    if (!values.isEmpty()) {
                        List<CollectionCarouselDto> collectionCarousels = new ArrayList<>();

                        mapNeo4jResponse(values, collectionCarousels);

                        showCarousels(collectionCarousels);

                    } else {
                        Toast.makeText(getContext(), "No collection found", Toast.LENGTH_SHORT).show();
                        showNoCollectionsText();
                    }
                } else {
                    Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    showNoCollectionsText();
                }

            }

            @Override
            public void onFailure(Call<Neo4jResponse> call, Throwable t) {
                Log.e("Neo4jCallback", "Network call failed", t);
                Toast.makeText(getContext(), "Failed to connect: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showNoCollectionsText();
            }

        });
    }

    private void showNoCollectionsText() {
        TextView noCollectionsText = new TextView(getContext());
        noCollectionsText.setText("No collections found");
        noCollectionsText.setTextSize(18);
        noCollectionsText.setPadding(16, 16, 16, 16);
        carouselContainer.addView(noCollectionsText);
    }

    private static void mapNeo4jResponse(List<List<Object>> values, List<CollectionCarouselDto> collectionCarousels) {
        for (List<Object> value : values) {
            String collectionName = value.get(0).toString();
            List<String> recipeNames = (List<String>) value.get(1);
            ArrayList<CarouselItem> carouselItems = new ArrayList<>();
            for (String recipeName : recipeNames) {
                carouselItems.add(new CarouselItem(recipeName));
            }
            collectionCarousels.add(new CollectionCarouselDto(collectionName, carouselItems));
        }
    }

    private void showCarousels(List<CollectionCarouselDto> collectionCarousels) {
        for (CollectionCarouselDto collection : collectionCarousels) {

            TextView titleView = new TextView(getContext());
            titleView.setText(collection.getTitle());
            titleView.setTextSize(22);
            titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
            titleView.setPadding(16, 45, 16, 16);

            RecyclerView recyclerView = new RecyclerView(getContext());
            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    500));
            recyclerView.setLayoutManager(new com.google.android.material.carousel.CarouselLayoutManager());

            ImageAdapter adapter = new ImageAdapter(getContext(), collection.getCarouselItems());
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener((imageView, path) -> startActivity(new Intent(getContext(), ImageViewActivity.class).putExtra("image", path), ActivityOptions.makeSceneTransitionAnimation(getActivity(), imageView, "image").toBundle()));

            carouselContainer.addView(titleView);
            carouselContainer.addView(recyclerView);

            adapter.setOnItemClickListener((imageView, title) -> {
                
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                Bundle bundle = new Bundle();
                bundle.putString("recipeName", title);

                navController.navigate(R.id.action_navigation_home_to_navigation_recipe_detail, bundle);
            });
        }
    }

    private CollectionCarouselDto populateDummyData(String title) {
        ArrayList<CarouselItem> carouselItems = new ArrayList<>();
        carouselItems.add(new CarouselItem("Recipe test"));
        carouselItems.add(new CarouselItem("Recipe test"));
        return new CollectionCarouselDto(title, carouselItems);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}