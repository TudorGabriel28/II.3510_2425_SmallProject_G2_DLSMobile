package com.example.recipeapp.helpers;


import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

public class FragmentExtensions {

    public static void onBackButtonPressed(Fragment fragment, Callback callback) {
        fragment.requireActivity().getOnBackPressedDispatcher().addCallback(fragment.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!callback.handle()) {
                    remove();
                    performBackPress(fragment);
                }
            }
        });
    }

    public static void performBackPress(Fragment fragment) {
        fragment.requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    public interface Callback {
        boolean handle();
    }
}