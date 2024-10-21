package com.example.recipeapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.recipeapp.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Observer pour le nom d'utilisateur
        profileViewModel.getUserName().observe(getViewLifecycleOwner(), binding.textUserName::setText);

        // Observer pour l'email d'utilisateur
        profileViewModel.getUserEmail().observe(getViewLifecycleOwner(), binding.textUserEmail::setText);

        // Observer pour les allergies de l'utilisateur
        profileViewModel.getUserAllergies().observe(getViewLifecycleOwner(), binding.textUserAllergies::setText);

        profileViewModel.loadUserData("email@example.com"); // Remplace par l'email de l'utilisateur


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}