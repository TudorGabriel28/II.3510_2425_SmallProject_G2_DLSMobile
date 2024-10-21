package com.example.recipeapp.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<String> userAllergies = new MutableLiveData<>();

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public LiveData<String> getUserAllergies() {
        return userAllergies;
    }

    // Methode pour charger les données utilisateur depuis Neo4j
    public void loadUserData(String userEmail) {
        Neo4jApiService apiService = Neo4jService.getInstance();

        // Création de la requête Cypher
        String query = "MATCH (u:User {email: '" + userEmail + "'}) RETURN u.name AS name, u.email AS email, u.allergies AS allergies";
        CypherQuery cypherQuery = new CypherQuery(query);

        // Envoi de la requête via Retrofit
        Call<Neo4jResponse> call = apiService.runCypherQuery(cypherQuery);
        call.enqueue(new Neo4jCallback<Neo4jResponse>(/* contexte ici */) {
            @Override
            public void handleSuccess(Neo4jResponse result) {
                // Traitement des résultats et mise à jour des LiveData
                List<Map<String, Object>> data = result.getResults();
                if (!data.isEmpty()) {
                    Map<String, Object> user = data.get(0);
                    userName.postValue((String) user.get("name"));
                    userEmail.postValue((String) user.get("email"));
                    userAllergies.postValue((String) user.get("allergies"));
                }
            }
        });
    }
}
