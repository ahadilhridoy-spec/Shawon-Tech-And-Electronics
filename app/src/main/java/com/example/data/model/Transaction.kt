package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val banglaLabel: String, val shortCode: String) {
    SELL("পণ্য বিক্রয়", "বিক্রি"),
    BUY("পণ্য ক্রয়", "ক্রয়"),
    EXPENSE("সাধারণ খরচ", "খরচ"),
    PERSONAL_EXPENSE("ব্যক্তিভিত্তিক খরচ", "ব্যক্তি");

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SELL
        }
    }
}

enum class PaymentMethod(val banglaLabel: String, val codePrefix: String) {
    BKASH("বিকাশ", "B"),
    NAGAD("নগদ", "N"),
    CASH("ক্যাশ", "C"),
    BANK("ব্যাংক", "Bank");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BKASH
        }
    }
}

enum class FlowDirection(val banglaLabel: String, val isIncoming: Boolean) {
    INCOMING("জমা / ঢুকেছে (+)", true),
    OUTGOING("খরচ / গেছে (-)", false);

    companion object {
        fun fromString(value: String): FlowDirection {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: INCOMING
        }
    }
}

enum class PaymentStatus(val banglaLabel: String) {
    PAID("পরিশোধিত"),
    PARTIAL("আংশিক নগদ ও বাকি"),
    DUE("বাকি")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // e.g., "১০ সেপ্টেম্বর" or "2026-09-10"
    val dateMillis: Long,
    val type: String, // TransactionType name
    val method: String, // PaymentMethod name
    val flowDirection: String, // FlowDirection name
    val amount: Double, // Money that actually changed hands (paid amount)
    val senderAccount: String = "", // e.g. "2050", "4010", "965", "017..."
    val receiverAccount: String = "", // e.g. "5822", "2689", "2774", "018..."
    val personName: String = "", // e.g. "রানা ভাই", "মিরাজ ভাই", কাস্টমার/সাপ্লায়ার
    val description: String = "", // e.g. "Carry Cost", "20k + 4k + 8k + 5k", "মোবাইল ডিসপ্লে"
    val rawCode: String = "", // e.g. "30K (B 915/689)"
    val totalBillAmount: Double = amount, // Total bill amount (মোট বিল)
    val paidAmount: Double = amount, // Partial or full paid amount (আংশিক নগদ পরিশোধ)
    val dueAmount: Double = 0.0, // Partial or full due amount (বাকি টাকা)
    val quantity: Int = 0, // পিস বা সংখ্যার হিসাব
    val unitPrice: Double = 0.0, // প্রতি পিসের মূল্য (ডিফল্ট: ক্রয় ১৯০৳, বিক্রয় ২৩০৳)
    val dueDate: String = "", // বাকি পরিশোধের তারিখ
    val customerPhone: String = "", // ফোন নম্বর
    val createdAt: Long = System.currentTimeMillis()
) {
    val paymentStatus: PaymentStatus
        get() = when {
            dueAmount <= 0.0 -> PaymentStatus.PAID
            paidAmount <= 0.0 -> PaymentStatus.DUE
            else -> PaymentStatus.PARTIAL
        }
}
