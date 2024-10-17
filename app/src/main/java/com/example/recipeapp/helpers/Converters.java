package com.example.recipeapp.helpers;

import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {

    @TypeConverter
    public String fromList(List<String> list) {
        return list != null ? String.join(",", list) : null;
    }

    @TypeConverter
    public static List<String> fromString(String value) {
        return value != null ? Arrays.asList(value.split(",")) : null;
    }
}
