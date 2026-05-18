package com.example.foodienow.feature.merchant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Category
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.CustomerFoodRepository
import com.example.foodienow.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditFoodViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val merchantRepository: MerchantRepository,
    private val foodRepository: CustomerFoodRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var price by mutableStateOf("")
    var description by mutableStateOf("")
    val predefinedCategories = listOf("Cơm", "Mì", "Đồ uống", "Bánh mì", "Đồ ăn vặt", "Fast food", "Ăn chay", "Món cuốn", "Khác")
    var selectedCategory by mutableStateOf("")
    var isOtherCategory by mutableStateOf(false)
    var customCategoryName by mutableStateOf("")

    // Từ điển ẩn tải từ DB để dò ID
    private var dbCategories by mutableStateOf<List<Category>>(emptyList())

    var imageBytes by mutableStateOf<ByteArray?>(null)
    var isSaving by mutableStateOf(false)
    var uploadSuccess by mutableStateOf(false)

    val foodId: String? = savedStateHandle.get<String>("foodId")?.takeIf { it != "new" }
    var storeId: String = ""
    var imageUrl by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // 1. Tải ngầm danh sách Categories từ DB làm từ điển quy đổi
                dbCategories = merchantRepository.getCategories()

                // 2. Tải thông tin món ăn (nếu đang Edit)
                if (foodId != null) {
                    val food = foodRepository.getFoodById(foodId)
                    name = food.name
                    price = food.price.toString()
                    description = food.description ?: ""
                    storeId = food.storeId
                    imageUrl = food.imageUrl

                    // Lấy ID từ món ăn, dò ngược lại ra tên Category
                    val existingCategory = dbCategories.find { it.id == food.categoryId }
                    val categoryName = existingCategory?.name ?: ""

                    if (categoryName in predefinedCategories) {
                        selectedCategory = categoryName
                        isOtherCategory = false
                    } else if (categoryName.isNotBlank()) {
                        selectedCategory = ""
                        customCategoryName = categoryName
                        isOtherCategory = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatCategoryText(input: String): String {
        if (input.isBlank()) return ""
        return input.trim().lowercase().replaceFirstChar { it.uppercase() }
    }

    fun onSave(errorEmptyFields: String, errorEmptyCategory: String, errorSaveFailed: String) {
        if (name.isBlank() || price.isBlank() || storeId.isBlank()) {
            errorMessage = errorEmptyFields
            return
        }

        val rawCategoryName = if (isOtherCategory) customCategoryName else selectedCategory
        if (rawCategoryName.isBlank()) {
            errorMessage = errorEmptyCategory
            return
        }

        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                val formattedName = formatCategoryText(rawCategoryName)

                // Quy đổi Tên -> ID
                var finalCategoryId = dbCategories.find { it.name.equals(formattedName, ignoreCase = true) }?.id

                // Nếu DB chưa có Category này (Khách nhập 'Khác' tên mới toanh), tạo trên DB để lấy ID
                if (finalCategoryId == null) {
                    val newCategory = merchantRepository.createCategory(formattedName)
                    finalCategoryId = newCategory.id
                    // Cập nhật lại từ điển ngầm
                    dbCategories = dbCategories + newCategory
                }

                val food = Food(
                    id = foodId ?: "",
                    name = name,
                    price = price.toLongOrNull() ?: 0L,
                    description = description,
                    storeId = storeId,
                    isAvailable = true,
                    categoryId = finalCategoryId
                )

                if (foodId == null) {
                    foodRepository.addFood(food, imageBytes)
                } else {
                    merchantRepository.updateFood(food)
                }
                uploadSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message ?: errorSaveFailed
            } finally {
                isSaving = false
            }
        }
    }
}