package com.yazilim.chefsnap.domain.repository

import com.yazilim.chefsnap.domain.model.RecipeResponse

interface RecipeRepository {

    suspend fun detectIngredientsFromImage(imageBase64: String): Result<List<String>>


    suspend fun getRecipesFromIngredients(ingredients: List<String>): Result<RecipeResponse>


    suspend fun getRecipesFromImage(imageBase64: String): Result<RecipeResponse>
}