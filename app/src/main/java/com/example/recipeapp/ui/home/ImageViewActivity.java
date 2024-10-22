package com.example.recipeapp.ui.home;

import static android.content.Intent.getIntent;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.recipeapp.R;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView imageView = findViewById(R.id.imageView);
        Uri uri = Uri.parse(getIntent().getStringExtra("image")).buildUpon().scheme("https").build();
        Glide.with(ImageViewActivity.this).load(uri).into(imageView);
    }
}