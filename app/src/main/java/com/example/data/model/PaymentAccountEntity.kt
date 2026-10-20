package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountScope(val banglaLabel: String, val shortCode: String = "") {
    SALE_RECEIVER("বিক্রয়ের জমা / প্রাপক (+)", "SALE_IN"),
    BUY_SENDER("ক্রয়ের খরচ / প্রেরক (-)", "BUY_OUT"),
    BUY_RECEIVER("ক্রয়ের প্রাপক (মহাজন / সাপ্লায়ার)", "BUY_IN"),
    ALL("সবক্ষেত্রে প্রযোজ্য (উভয়)", "ALL")
}

@Entity(tableName = "payment_accounts")
data class PaymentAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountNumber: String, // e.g. "2774", "2689", "5822", "8960", "3915", "8787", "4010", "2050", "Cash"
    val accountCode: String,   // e.g. "B/S", "B/A", "B/P", "N/S", "N/A", "N/P", "CASH"
    val method: String,        // PaymentMethod.BKASH.name, NAGAD, CASH, BANK
    val scope: String = AccountScope.ALL.name, // AccountScope name
    val note: String = "",     // e.g. "দোকান বিকাশ", "এজেন্ট", "পার্সোনাল", "ক্যাশ ড্রয়ার"
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
) {
    val displayLabel: String
        get() = if (accountCode.isNotBlank()) "$accountNumber ☞ $accountCode" else accountNumber

    val fullDescription: String
        get() {
            val methodLabel = when (method) {
                PaymentMethod.BKASH.name -> "বিকাশ"
                PaymentMethod.NAGAD.name -> "নগদ"
                PaymentMethod.CASH.name -> "ক্যাশ"
                else -> method
            }
            return if (note.isNotBlank()) "$displayLabel ($note)" else "$displayLabel ($methodLabel)"
        }
}
