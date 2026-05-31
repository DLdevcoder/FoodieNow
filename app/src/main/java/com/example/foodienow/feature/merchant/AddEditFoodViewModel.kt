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

    var availableCategories by mutableStateOf<List<Category>>(emptyList())
    var selectedCategory by mutableStateOf<Category?>(null)

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
                availableCategories = merchantRepository.getCategories()

                if (foodId != null) {
                    val food = foodRepository.getFoodById(foodId)
                    name = food.name
                    price = food.price.toString()
                    description = food.description ?: ""
                    storeId = food.storeId
                    imageUrl = food.imageUrl

                    selectedCategory = availableCategories.find { it.id == food.categoryId }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onSave(errorEmptyFields: String, errorEmptyCategory: String, errorSaveFailed: String) {
        if (name.isBlank() || price.isBlank() || storeId.isBlank()) {
            errorMessage = errorEmptyFields
            return
        }
        if (selectedCategory == null) {
            errorMessage = errorEmptyCategory
            return
        }

        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                val finalCategoryId = selectedCategory?.id

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
                    merchantRepository.updateFood(food, imageBytes)
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