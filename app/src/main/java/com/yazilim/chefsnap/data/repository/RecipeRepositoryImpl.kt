package com.yazilim.chefsnap.data.repository

import android.util.Log
import com.yazilim.chefsnap.data.remote.GeminiApiClient
import com.yazilim.chefsnap.data.remote.dto.Content
import com.yazilim.chefsnap.data.remote.dto.GeminiRequest
import com.yazilim.chefsnap.data.remote.dto.GenerationConfig
import com.yazilim.chefsnap.data.remote.dto.InlineData
import com.yazilim.chefsnap.data.remote.dto.Part
import com.yazilim.chefsnap.domain.model.RecipeResponse
import com.yazilim.chefsnap.domain.repository.RecipeRepository
import com.yazilim.chefsnap.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RecipeRepositoryImpl : RecipeRepository {

    // 1. Aşama: Sadece fotoğraftaki malzemeleri liste olarak çeker
    override suspend fun detectIngredientsFromImage(imageBase64: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val promptText = """
                    Fotoğraftaki tüm yiyecek ve yemek malzemelerini net bir şekilde tespit et.
                    Sadece geçerli bir JSON formatında döndür.
                    JSON Şeması:
                    {
                      "ingredients": ["Yumurta", "Sucuk", "Kaşar Peyniri"]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = promptText),
                                Part(
                                    inlineData = InlineData(
                                        mimeType = "image/jpeg",
                                        data = imageBase64
                                    )
                                )
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    )
                )

                val response = GeminiApiClient.apiService.generateRecipe(
                    model = Constants.GEMINI_MODEL,
                    apiKey = Constants.GEMINI_API_KEY,
                    request = request
                )

                val rawJson = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                if (rawJson.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Malzemeler tespit edilemedi."))
                }

                val cleanedJson = rawJson
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val jsonElement = Json.parseToJsonElement(cleanedJson)
                val ingredientsList = jsonElement.jsonObject["ingredients"]
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    ?: emptyList()

                Result.success(ingredientsList)
            } catch (e: Exception) {
                Log.e("ChefSnap_API", "Malzeme Tespit Hatası: ${e.message}", e)
                Result.failure(e)
            }
        }

    // 2. Aşama: Kullanıcının onayladığı/düzelttiği malzemelerle tarif üretir
    override suspend fun getRecipesFromIngredients(ingredients: List<String>): Result<RecipeResponse> =
        withContext(Dispatchers.IO) {
            try {
                val ingredientsText = ingredients.joinToString(", ")
                val promptText = """
                    Sen profesyonel bir sporcu beslenme uzmanı ve fitness şefisin.
                    Elimdeki doğrulanmış malzemeler: $ingredientsText.
                    
                    Temel kiler malzemelerinin (tuz, karabiber, zeytinyağı, un, su) evde bulunduğunu varsayarak, bu malzemelerle yapılabilecek 2-3 adet yüksek proteinli, dengeli ve pratik fitness tarifi üret.
                    
                    Her tarif için porsiyon bazlı yaklaşık makro besin değerlerini (kalori, protein, karbonhidrat, yağ) gram cinsinden hesapla.

                    Yanıtı SADECE geçerli bir JSON olarak döndür.
                    
                    JSON Şeması:
                    {
                      "detected_ingredients": [$ingredientsText],
                      "recipes": [
                        {
                          "title": "Yüksek Proteinli Fit Öğün",
                          "prep_time_minutes": 15,
                          "difficulty": "Kolay",
                          "calories": 420,
                          "protein_g": 28,
                          "carbs_g": 45,
                          "fat_g": 12,
                          "used_ingredients": ["$ingredientsText"],
                          "pantry_ingredients": ["Zeytinyağı", "Tuz"],
                          "missing_optional_ingredients": [],
                          "instructions": [
                            "Adım 1...",
                            "Adım 2..."
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = promptText)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    )
                )

                val response = GeminiApiClient.apiService.generateRecipe(
                    model = Constants.GEMINI_MODEL,
                    apiKey = Constants.GEMINI_API_KEY,
                    request = request
                )

                val rawJson = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                if (rawJson.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Tarifler üretilemedi."))
                }

                val cleanedJson = rawJson
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val parsedResponse = GeminiApiClient.json.decodeFromString<RecipeResponse>(cleanedJson)
                Result.success(parsedResponse)
            } catch (e: Exception) {
                Log.e("ChefSnap_API", "Tarif Üretim Hatası: ${e.message}", e)
                Result.failure(e)
            }
        }

    // Eski doğrudan görselden üreten metot (Geriye dönük uyumluluk için)
    override suspend fun getRecipesFromImage(imageBase64: String): Result<RecipeResponse> =
        withContext(Dispatchers.IO) {
            try {
                val promptText = """
                    Sen profesyonel bir sporcu beslenme uzmanı ve fitness şefisin.
                    Fotoğraftaki yiyecek malzemelerini net bir şekilde tespit et.
                    Temel kiler malzemelerinin evde bulunduğunu varsayarak 2-3 adet yüksek proteinli fitness tarifi üret.
                    Yanıtı SADECE geçerli bir JSON olarak döndür.
                    JSON Şeması:
                    {
                      "detected_ingredients": ["Yumurta", "Yulaf", "Süt"],
                      "recipes": [
                        {
                          "title": "Yüksek Proteinli Yulaf Pankeki",
                          "prep_time_minutes": 15,
                          "difficulty": "Kolay",
                          "calories": 420,
                          "protein_g": 28,
                          "carbs_g": 45,
                          "fat_g": 12,
                          "used_ingredients": ["Yumurta", "Yulaf", "Süt"],
                          "pantry_ingredients": ["Zeytinyağı", "Tuz"],
                          "missing_optional_ingredients": ["Muz"],
                          "instructions": ["Pişirin."]
                        }
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = promptText),
                                Part(inlineData = InlineData(mimeType = "image/jpeg", data = imageBase64))
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
                )

                val response = GeminiApiClient.apiService.generateRecipe(
                    model = Constants.GEMINI_MODEL,
                    apiKey = Constants.GEMINI_API_KEY,
                    request = request
                )

                val rawJson = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawJson.isNullOrBlank()) return@withContext Result.failure(Exception("Yapay zekadan yanıt alınamadı."))

                val cleanedJson = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                Result.success(GeminiApiClient.json.decodeFromString<RecipeResponse>(cleanedJson))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}