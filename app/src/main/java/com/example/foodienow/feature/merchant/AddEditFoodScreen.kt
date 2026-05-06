package com.example.foodienow.feature.merchant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.theme.ColorPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    storeId: String,
    onBack: () -> Unit,
    viewModel: AddEditFoodViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Gán storeId từ MerchantHome sang ViewModel
    LaunchedEffect(Unit) { viewModel.storeId = storeId }

    // Xử lý khi lưu thành công
    if (viewModel.uploadSuccess) {
        LaunchedEffect(Unit) { onBack() }
    }

    // Bộ chọn ảnh
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
                title = { Text(if (viewModel.foodId == null) "Thêm món ăn" else "Chỉnh sửa món", color = Color.White) },
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

            // Hiển thị lỗi nếu có
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
                    .height(200.dp)
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
                        contentDescription = "Ảnh món ăn",
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
                        Text("Bấm để chọn ảnh món ăn", color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Tên món ăn") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.price,
                onValueChange = { viewModel.price = it },
                label = { Text("Giá bán (VNĐ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Mô tả món ăn") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isSaving) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.onSave() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Text("LƯU MÓN ĂN", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}