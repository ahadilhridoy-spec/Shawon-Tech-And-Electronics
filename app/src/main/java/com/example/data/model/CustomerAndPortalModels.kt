package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val creditLimit: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_requests")
data class PaymentRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val amount: Double,
    val paymentMethod: String, // BKASH, NAGAD, CASH
    val senderCode: String, // সেন্ডার কোড বা নম্বর (যেমন: শেষ ৪ ডিজিট বা অ্যাকাউন্ট)
    val receiverCode: String, // প্রাপক কোড বা নম্বর (যেমন: 2774 B/S, 2689 N/S)
    val trxId: String = "", // ট্রানজেকশন আইডি
    val note: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val submissionDateMillis: Long = System.currentTimeMillis(),
    val submissionDateString: String = "",
    val reviewedDateMillis: Long? = null,
    val rejectionReason: String = ""
)

@Entity(tableName = "products")
data class ProductItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "ইলেকট্রনিক্স",
    val buyPrice: Double = 190.0,
    val sellPrice: Double = 230.0,
    val stockQuantity: Int = 10,
    val isAvailable: Boolean = true,
    val unit: String = "পিস"
)
