package com.yazilim.chefsnap.presentation.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yazilim.chefsnap.data.repository.RecipeRepositoryImpl
import com.yazilim.chefsnap.domain.repository.RecipeRepository
import com.yazilim.chefsnap.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RecipeRepository = RecipeRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentImageUri: Uri? = null

    // 1. Aşama: Görsel işlenir ve sadece malzemeler tespit edilir
    fun analyzeImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            currentImageUri = imageUri
            _uiState.value = HomeUiState.Loading("Fotoğraf optimize ediliyor...")

            val base64Image = ImageUtils.uriToOptimizedBase64(context, imageUri)
            if (base64Image.isNullOrBlank()) {
                _uiState.value = HomeUiState.Error("Görsel işlenirken bir hata oluştu.")
                return@launch
            }

            _uiState.value = HomeUiState.Loading("Malzemeler tespit ediliyor...")

            val result = repository.detectIngredientsFromImage(base64Image)
            result.onSuccess { ingredients ->
                if (ingredients.isEmpty()) {
                    _uiState.value = HomeUiState.Error("Fotoğrafta herhangi bir yiyecek malzemesi tespit edilemedi.")
                } else {
                    _uiState.value = HomeUiState.IngredientsVerification(
                        selectedImageUri = currentImageUri,
                        detectedIngredients = ingredients
                    )
                }
            }.onFailure { throwable ->
                _uiState.value = HomeUiState.Error(
                    throwable.localizedMessage ?: "Malzemeler taranırken bir hata meydana geldi."
                )
            }
        }
    }

    // 2. Aşama: Kullanıcının onayladığı/düzelttiği nihai listeyle tarifler üretilir
    fun generateRecipesWithConfirmedIngredients(confirmedIngredients: List<String>) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading("Şef tarifleri hazırlıyor...")

            val result = repository.getRecipesFromIngredients(confirmedIngredients)
            result.onSuccess { response ->
                _uiState.value = HomeUiState.Success(
                    selectedImageUri = currentImageUri,
                    detectedIngredients = confirmedIngredients,
                    recipes = response.recipes
                )
            }.onFailure { throwable ->
                _uiState.value = HomeUiState.Error(
                    throwable.localizedMessage ?: "Tarifler üretilirken bir hata oluştu."
                )
            }
        }
    }

    fun resetState() {
        currentImageUri = null
        _uiState.value = HomeUiState.Idle
    }
}