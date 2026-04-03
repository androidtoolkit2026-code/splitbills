package com.splitmate.utils

import android.content.Context
import android.net.Uri
import com.splitmate.domain.model.Expense
import com.splitmate.domain.model.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor() {

    suspend fun exportGroupToCsv(
        context: Context,
        uri: Uri,
        group: Group,
        expenses: List<Expense>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sb = StringBuilder()
            sb.appendLine("Title,Amount,Paid By,Split Type,Date,Notes")

            for (expense in expenses) {
                val payerName = group.members.find { it.id == expense.paidById }?.name ?: "Unknown"
                sb.appendLine(
                    "\"${expense.title}\",${expense.amount},\"$payerName\",${expense.splitType},${FormatUtils.formatDate(expense.date)},\"${expense.notes}\""
                )
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
