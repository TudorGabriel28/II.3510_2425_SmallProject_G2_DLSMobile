package com.example.recipeapp.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipeapp.R;
import com.example.recipeapp.dtos.CarouselItem;

import java.util.ArrayList;

import lombok.Setter;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    Context context;
    ArrayList<CarouselItem> arrayList;
    @Setter
    OnItemClickListener onItemClickListener;

    public ImageAdapter(Context context, ArrayList<CarouselItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarouselItem item = arrayList.get(position);
        Glide.with(context).load(item.getImage()).into(holder.imageView);
        holder.textView.setText(item.getTitle());
        holder.itemView.setOnClickListener(view -> onItemClickListener.onClick(holder.imageView, item.getTitle()));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
            textView = itemView.findViewById(R.id.list_item_text);
        }
    }

    public interface OnItemClickListener {
        void onClick(ImageView imageView, String title);
    }
}