package com.yazilim.chefsnap.presentation.home

import android.net.Uri
import com.yazilim.chefsnap.domain.model.Recipe

sealed interface HomeUiState {
    data object Idle : HomeUiState

    // Yükleme mesajını dinamik yönetmek için tek Loading kalabilir
    data class Loading(val message: String = "Malzemeler analiz ediliyor...") : HomeUiState

    // AI malzemeleri bulduktan sonra kullanıcının önüne çıkan onay aşaması
    data class IngredientsVerification(
        val selectedImageUri: Uri?,
        val detectedIngredients: List<String>
    ) : HomeUiState


    data class Success(
        val selectedImageUri: Uri?,
        val detectedIngredients: List<String>,
        val recipes: List<Recipe>
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}