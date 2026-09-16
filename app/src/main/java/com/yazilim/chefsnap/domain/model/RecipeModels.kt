package com.yazilim.chefsnap.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    @SerialName("detected_ingredients")
    val detectedIngredients: List<String> = emptyList(),
    @SerialName("recipes")
    val recipes: List<Recipe> = emptyList()
)

@Serializable
data class Recipe(
    @SerialName("title")
    val title: String = "",
    @SerialName("prep_time_minutes")
    val prepTimeMinutes: Int = 0,
    @SerialName("difficulty")
    val difficulty: String = "Kolay",
    @SerialName("calories")
    val calories: Int = 0,
    @SerialName("protein_g")
    val proteinG: Int = 0,
    @SerialName("carbs_g")
    val carbsG: Int = 0,
    @SerialName("fat_g")
    val fatG: Int = 0,
    @SerialName("used_ingredients")
    val usedIngredients: List<String> = emptyList(),
    @SerialName("pantry_ingredients")
    val pantryIngredients: List<String> = emptyList(),
    @SerialName("missing_optional_ingredients")
    val missingOptionalIngredients: List<String> = emptyList(),
    @SerialName("instructions")
    val instructions: List<String> = emptyList()
)