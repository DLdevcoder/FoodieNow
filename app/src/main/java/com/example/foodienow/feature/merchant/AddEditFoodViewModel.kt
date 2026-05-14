package com.example.foodienow.feature.merchant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    var imageBytes by mutableStateOf<ByteArray?>(null)
    var isSaving by mutableStateOf(false)
    var uploadSuccess by mutableStateOf(false)

    val foodId: String? = savedStateHandle.get<String>("foodId")?.takeIf { it != "new" }
    var storeId: String = ""
    var imageUrl by mutableStateOf<String?>(null)

    init {
        foodId?.let { id ->
            viewModelScope.launch {
                val food = foodRepository.getFoodById(id)
                name = food.name
                price = food.price.toString()
                description = food.description ?: ""
                storeId = food.storeId
                imageUrl = food.imageUrl
            }
        }
    }

    var errorMessage by mutableStateOf<String?>(null)

    fun onSave() {
        if (name.isBlank() || price.isBlank() || storeId.isBlank()) return

        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                val food = Food(
                    id = foodId ?: "",
                    name = name,
                    price = price.toLongOrNull() ?: 0L,
                    description = description,
                    storeId = storeId,
                    isAvailable = true
                )

                if (foodId == null) {
                    foodRepository.addFood(food, imageBytes)
                } else {
                    merchantRepository.updateFood(food)
                }
                uploadSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message ?: "Có lỗi xảy ra khi lưu món ăn"
            } finally {
                isSaving = false
            }
        }
    }
}