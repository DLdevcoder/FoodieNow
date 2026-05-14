package com.example.foodienow.feature.customer_home.components
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Long.formatPrice(): String {
    val symbols = DecimalFormatSymbols(Locale("vi", "VN"))
    symbols.groupingSeparator = '.'
    val decimalFormat = DecimalFormat("###,###", symbols)
    return "${decimalFormat.format(this)} VNĐ"
}