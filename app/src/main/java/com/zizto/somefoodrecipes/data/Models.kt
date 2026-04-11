package com.zizto.somefoodrecipes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class MealResponse(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class MealDto(
    @SerializedName("idMeal") val idMeal: String,
    @SerializedName("strMeal") val strMeal: String,
    @SerializedName("strMealThumb") val strMealThumb: String,
    @SerializedName("strInstructions") val strInstructions: String?
)

@Entity(tableName = "favorite_recipes")
data class FavoriteMeal(
    @PrimaryKey val idMeal: String,
    val name: String,
    val imageUrl: String,
    val instructions: String
)