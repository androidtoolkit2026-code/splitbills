package com.splitmate.domain.model

data class Currency(
    val code: String,
    val symbol: String,
    val name: String
) {
    companion object {
        val DEFAULT = Currency("INR", "₹", "Indian Rupee")

        val SUPPORTED = listOf(
            Currency("INR", "₹", "Indian Rupee"),
            Currency("USD", "$", "US Dollar"),
            Currency("EUR", "€", "Euro"),
            Currency("GBP", "£", "British Pound"),
            Currency("JPY", "¥", "Japanese Yen"),
            Currency("AUD", "A$", "Australian Dollar"),
            Currency("CAD", "C$", "Canadian Dollar"),
            Currency("SGD", "S$", "Singapore Dollar"),
            Currency("AED", "د.إ", "UAE Dirham"),
            Currency("THB", "฿", "Thai Baht")
        )

        fun fromCode(code: String): Currency {
            return SUPPORTED.find { it.code == code } ?: DEFAULT
        }
    }
}
