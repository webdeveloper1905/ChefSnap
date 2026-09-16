package com.yazilim.chefsnap.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yazilim.chefsnap.data.billing.SubscriptionManager
import com.yazilim.chefsnap.data.preference.UsageLimitManager
import com.yazilim.chefsnap.presentation.components.LoadingView
import com.yazilim.chefsnap.presentation.components.RecipeCard
import com.yazilim.chefsnap.presentation.paywall.PaywallScreen
import com.yazilim.chefsnap.presentation.profile.ProfileScreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isPro by SubscriptionManager.isProUser.collectAsState()

    val usageManager = remember { UsageLimitManager(context) }
    var remainingScans by remember { mutableStateOf(usageManager.getRemainingScans()) }
    var showPaywall by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            usageManager.incrementScanCount()
            remainingScans = usageManager.getRemainingScans()
            viewModel.analyzeImage(context, it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let {
                usageManager.incrementScanCount()
                remainingScans = usageManager.getRemainingScans()
                viewModel.analyzeImage(context, it)
            }
        }
    }

    fun launchCameraIntent() {
        try {
            val imagesDir = File(context.cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val tempFile = File.createTempFile("chef_snap_", ".jpg", imagesDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Kamera başlatılamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraIntent()
        } else {
            Toast.makeText(context, "Fotoğraf çekmek için kamera izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleScanAction(action: () -> Unit) {
        if (usageManager.canPerformScan()) {
            action()
        } else {
            showPaywall = true
        }
    }

    fun checkAndLaunchCamera() {
        handleScanAction {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun checkAndLaunchGallery() {
        handleScanAction {
            galleryLauncher.launch("image/*")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ChefSnap",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Profil ve Üyelik Butonu
                    IconButton(onClick = { showProfile = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profil & Üyelik",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // PRO Rozeti veya Satın Al Butonu
                    if (isPro) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { showProfile = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showPaywall = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "PRO",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRO'ya Geç",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (uiState is HomeUiState.Success || uiState is HomeUiState.Error || uiState is HomeUiState.IngredientsVerification) {
                        IconButton(onClick = { viewModel.resetState() }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Yeniden Başlat"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Dolabındaki Malzemeleri Tara",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Buzdolabının veya tabağının fotoğrafını çek; yapay zeka makroları hesaplayıp yüksek proteinli tarifler sunsun.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Günlük Kalan Hak Rozeti
                        if (!isPro) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Bugünkü Kalan Ücretsiz Hak: $remainingScans / ${UsageLimitManager.FREE_DAILY_LIMIT}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (remainingScans > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { checkAndLaunchCamera() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fotoğraf Çek", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { checkAndLaunchGallery() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galeriden Seç", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is HomeUiState.Loading -> {
                    LoadingView(
                        message = state.message,
                        modifier = Modifier
                    )
                }

                // 2. AŞAMA: MALZEME ONAY VE DÜZELTME DİYALOĞU
                is HomeUiState.IngredientsVerification -> {
                    IngredientVerificationDialog(
                        initialIngredients = state.detectedIngredients,
                        onConfirm = { confirmedList ->
                            viewModel.generateRecipesWithConfirmedIngredients(confirmedList)
                        },
                        onCancel = {
                            viewModel.resetState()
                        }
                    )
                }

                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.selectedImageUri != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    AsyncImage(
                                        model = state.selectedImageUri,
                                        contentDescription = "Taranan Fotoğraf",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Text(
                                text = "Onaylanan Malzemeler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                state.detectedIngredients.forEach { item ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(item) },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Önerilen Fitness Tarifleri (${state.recipes.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(state.recipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Bir Hata Oluştu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.resetState() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            }
        }
    }

    // Paywall Modal Katmanı
    if (showPaywall) {
        PaywallScreen(
            onDismiss = { showPaywall = false },
            onPurchaseSuccess = {
                showPaywall = false
                remainingScans = usageManager.getRemainingScans()
            }
        )
    }

    // Profil ve Üyelik Yönetimi Modal Katmanı
    if (showProfile) {
        ProfileScreen(
            onDismiss = { showProfile = false },
            onOpenPaywall = {
                showProfile = false
                showPaywall = true
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientVerificationDialog(
    initialIngredients: List<String>,
    onConfirm: (List<String>) -> Unit,
    onCancel: () -> Unit
) {
    val ingredients = remember { mutableStateListOf(*initialIngredients.toTypedArray()) }
    var newIngredientText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Malzemeleri Onayla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AI tarafından tespit edilen malzemeler. Yanlış olanları (X) ile kaldırabilir, eksikleri ekleyebilirsin:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tespit Edilen Malzemeler (Chip Listesi)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ingredients.forEach { item ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(item) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Sil",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { ingredients.remove(item) }
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Eksik/Düzeltilmiş Malzeme Ekleme Satırı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newIngredientText,
                        onValueChange = { newIngredientText = it },
                        placeholder = { Text("Örn: Sucuk, Zeytin...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newIngredientText.isNotBlank()) {
                                ingredients.add(newIngredientText.trim())
                                newIngredientText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Ekle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ingredients.isNotEmpty()) {
                        onConfirm(ingredients.toList())
                    }
                },
                enabled = ingredients.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tarifi Hazırla", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}