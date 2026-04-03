package com.splitmate.utils

import com.splitmate.domain.model.Currency
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun formatCurrency(amount: Double, currencyCode: String = "INR"): String {
        val currency = Currency.fromCode(currencyCode)
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${currency.symbol}${formatter.format(kotlin.math.abs(amount))}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 30 -> formatDate(timestamp)
            days > 1 -> "${days.toInt()} days ago"
            days == 1L -> "Yesterday"
            hours > 1 -> "${hours.toInt()} hours ago"
            hours == 1L -> "1 hour ago"
            minutes > 1 -> "${minutes.toInt()} minutes ago"
            else -> "Just now"
        }
    }

    fun getInitials(name: String): String {
        return name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "?" }
    }
}
