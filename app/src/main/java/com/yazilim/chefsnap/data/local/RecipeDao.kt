package com.yazilim.chefsnap.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Query("SELECT * FROM favorite_recipes ORDER BY savedAt DESC")
    fun getAllFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE title = :title LIMIT 1)")
    fun isRecipeFavorite(title: String): Flow<Boolean>

    @Query("DELETE FROM favorite_recipes WHERE title = :title")
    suspend fun deleteRecipeByTitle(title: String)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}