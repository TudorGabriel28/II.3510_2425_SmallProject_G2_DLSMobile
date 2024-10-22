package com.example.recipeapp.dtos;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CollectionCarouselDto {
    private String title;
    private ArrayList<CarouselItem> carouselItems;
}
