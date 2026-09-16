package com.yazilim.chefsnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yazilim.chefsnap.domain.model.Recipe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "favorite_recipes")
@TypeConverters(RecipeConverters::class)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prepTimeMinutes: Int,
    val difficulty: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val usedIngredients: List<String>,
    val pantryIngredients: List<String>,
    val missingOptionalIngredients: List<String>,
    val instructions: List<String>,
    val savedAt: Long = System.currentTimeMillis()
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    title = this.title,
    prepTimeMinutes = this.prepTimeMinutes,
    difficulty = this.difficulty,
    calories = this.calories,
    proteinG = this.proteinG,
    carbsG = this.carbsG,
    fatG = this.fatG,
    usedIngredients = this.usedIngredients,
    pantryIngredients = this.pantryIngredients,
    missingOptionalIngredients = this.missingOptionalIngredients,
    instructions = this.instructions
)

fun RecipeEntity.toDomain(): Recipe = Recipe(
    title = this.title,
    prepTimeMinutes = this.prepTimeMinutes,
    difficulty = this.difficulty,
    calories = this.calories,
    proteinG = this.proteinG,
    carbsG = this.carbsG,
    fatG = this.fatG,
    usedIngredients = this.usedIngredients,
    pantryIngredients = this.pantryIngredients,
    missingOptionalIngredients = this.missingOptionalIngredients,
    instructions = this.instructions
)

class RecipeConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}