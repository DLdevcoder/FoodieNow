package com.example.foodienow.feature.merchant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.theme.ColorPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditFoodScreen(
    storeId: String,
    onBack: () -> Unit,
    viewModel: AddEditFoodViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCategoryBottomSheet by remember { mutableStateOf(false) }

    val errorEmptyFields = stringResource(R.string.error_empty_food_fields)
    val errorEmptyCategory = stringResource(R.string.error_empty_category)
    val errorSaveFailed = stringResource(R.string.error_save_food_failed)
    val categoryOther = stringResource(R.string.category_other)

    LaunchedEffect(Unit) {
        viewModel.storeId = storeId
    }

    if (viewModel.uploadSuccess) {
        LaunchedEffect(Unit) { onBack() }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            viewModel.imageBytes = inputStream?.readBytes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.foodId == null) stringResource(R.string.merchant_food_title_add)
                        else stringResource(R.string.merchant_food_title_edit),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || viewModel.imageUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = selectedImageUri ?: viewModel.imageUrl,
                        contentDescription = stringResource(R.string.merchant_food_image_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.merchant_food_select_image), color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text(stringResource(R.string.merchant_food_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.price,
                onValueChange = { viewModel.price = it },
                label = { Text(stringResource(R.string.merchant_food_price_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = if (viewModel.isOtherCategory) categoryOther else viewModel.selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.merchant_food_category_label)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { showCategoryBottomSheet = true }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showCategoryBottomSheet = true },
                shape = RoundedCornerShape(12.dp)
            )

            if (showCategoryBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCategoryBottomSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.merchant_food_category_label),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            viewModel.predefinedCategories.forEach { category ->
                                CategoryChip(
                                    name = category,
                                    isSelected = viewModel.selectedCategory == category && !viewModel.isOtherCategory,
                                    onClick = {
                                        viewModel.selectedCategory = category
                                        viewModel.isOtherCategory = false
                                        showCategoryBottomSheet = false
                                    }
                                )
                            }
                            CategoryChip(
                                name = categoryOther,
                                isSelected = viewModel.isOtherCategory,
                                onClick = {
                                    viewModel.selectedCategory = ""
                                    viewModel.isOtherCategory = true
                                    showCategoryBottomSheet = false
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            if (viewModel.isOtherCategory) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.customCategoryName,
                    onValueChange = { viewModel.customCategoryName = it },
                    label = { Text(stringResource(R.string.merchant_food_custom_category_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text(stringResource(R.string.merchant_food_description_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isSaving) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.onSave(errorEmptyFields, errorEmptyCategory, errorSaveFailed)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(stringResource(R.string.merchant_food_save_button), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) ColorPrimary.copy(alpha = 0.1f) else Color.White,
        contentColor = if (isSelected) ColorPrimary else Color.Gray,
        border = if (isSelected) BorderStroke(1.dp, ColorPrimary) else BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}