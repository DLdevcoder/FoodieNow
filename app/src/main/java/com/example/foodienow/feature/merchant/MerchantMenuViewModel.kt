package com.example.foodienow.feature.merchant

import androidx.lifecycle.ViewModel
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MerchantMenuViewModel @Inject constructor(
    private val merchantRepo: MerchantRepository
) : ViewModel() {

    //
    // Lấy ID của quán trên db
    fun loadMenu() { }

    // thêm, cập nhật món
    fun addFood(food: Food) { }

    // Tắt/Mở món ăn khi hết
    fun toggleFoodAvailability(foodId: String, isAvailable: Boolean) { }
}