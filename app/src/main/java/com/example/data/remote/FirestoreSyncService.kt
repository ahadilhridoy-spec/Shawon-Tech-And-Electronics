package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.TransactionEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSyncService(context: Context) {
    private val databaseId = context.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)

    companion object {
        private const val TAG = "FirestoreSyncService"
    }

    suspend fun uploadTransaction(userId: String, transaction: TransactionEntity) {
        if (userId.isBlank()) return
        try {
            val docRef = firestore.collection("users").document(userId)
                .collection("transactions").document(transaction.id.toString())

            val map = hashMapOf(
                "id" to transaction.id.toString(),
                "dateString" to transaction.dateString,
                "dateMillis" to transaction.dateMillis,
                "type" to transaction.type,
                "method" to transaction.method,
                "flowDirection" to transaction.flowDirection,
                "amount" to transaction.amount,
                "senderAccount" to transaction.senderAccount,
                "receiverAccount" to transaction.receiverAccount,
                "personName" to transaction.personName,
                "description" to transaction.description,
                "rawCode" to transaction.rawCode,
                "totalBillAmount" to transaction.totalBillAmount,
                "paidAmount" to transaction.paidAmount,
                "dueAmount" to transaction.dueAmount,
                "quantity" to transaction.quantity,
                "unitPrice" to transaction.unitPrice,
                "dueDate" to transaction.dueDate,
                "customerPhone" to transaction.customerPhone,
                "createdAt" to transaction.createdAt,
                "userId" to userId
            )
            docRef.set(map, SetOptions.merge()).await()
            Log.d(TAG, "Uploaded transaction #${transaction.id} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload transaction #${transaction.id}", e)
        }
    }

    suspend fun uploadPaymentAccount(userId: String, account: PaymentAccountEntity) {
        if (userId.isBlank()) return
        try {
            val docRef = firestore.collection("users").document(userId)
                .collection("payment_accounts").document(account.id.toString())

            val map = hashMapOf(
                "id" to account.id.toString(),
                "accountNumber" to account.accountNumber,
                "accountCode" to account.accountCode,
                "method" to account.method,
                "scope" to account.scope,
                "note" to account.note,
                "isCustom" to account.isCustom,
                "sortOrder" to account.sortOrder,
                "userId" to userId
            )
            docRef.set(map, SetOptions.merge()).await()
            Log.d(TAG, "Uploaded payment account #${account.id} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload payment account #${account.id}", e)
        }
    }

    suspend fun deleteTransaction(userId: String, transactionId: Long) {
        if (userId.isBlank()) return
        try {
            firestore.collection("users").document(userId)
                .collection("transactions").document(transactionId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete transaction #$transactionId from Firestore", e)
        }
    }

    suspend fun deletePaymentAccount(userId: String, accountId: Long) {
        if (userId.isBlank()) return
        try {
            firestore.collection("users").document(userId)
                .collection("payment_accounts").document(accountId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete payment account #$accountId from Firestore", e)
        }
    }

    suspend fun syncAllTransactions(userId: String, transactions: List<TransactionEntity>) {
        if (userId.isBlank()) return
        transactions.forEach { uploadTransaction(userId, it) }
    }

    suspend fun syncAllAccounts(userId: String, accounts: List<PaymentAccountEntity>) {
        if (userId.isBlank()) return
        accounts.forEach { uploadPaymentAccount(userId, it) }
    }

    fun observeDashboardSummary(userId: String): Flow<FirestoreDashboardSummaryData> =
        callbackFlow {
            if (userId.isBlank()) {
                trySend(
                    FirestoreDashboardSummaryData(
                        isLoading = false,
                        isFromCloud = false,
                        error = "ক্লাউড সিঙ্ক করতে গুগল সাইন-ইন প্রয়োজন"
                    )
                )
                awaitClose { }
                return@callbackFlow
            }

            trySend(FirestoreDashboardSummaryData(isLoading = true, isFromCloud = true))

            val listenerRegistration = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to Firestore transactions", error)
                        trySend(
                            FirestoreDashboardSummaryData(
                                isLoading = false,
                                isFromCloud = false,
                                error = error.localizedMessage ?: "ফায়ারবেস থেকে ডেটা লোড ব্যর্থ হয়েছে"
                            )
                        )
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        var totalSales = 0.0
                        var totalExpenses = 0.0
                        val count = snapshot.size()

                        for (doc in snapshot.documents) {
                            val type = doc.getString("type") ?: ""
                            val flow = doc.getString("flowDirection") ?: ""
                            val amount = doc.getDouble("amount") ?: 0.0
                            val totalBill = doc.getDouble("totalBillAmount") ?: amount

                            val isSale = type.equals("SELL", ignoreCase = true) ||
                                    type.contains("বিক্র", ignoreCase = true) ||
                                    flow.equals("INCOMING", ignoreCase = true)

                            val isExpense = type.equals("EXPENSE", ignoreCase = true) ||
                                    type.equals("BUY", ignoreCase = true) ||
                                    type.equals("PERSONAL_EXPENSE", ignoreCase = true) ||
                                    type.contains("খরচ", ignoreCase = true) ||
                                    type.contains("ক্রয়", ignoreCase = true) ||
                                    flow.equals("OUTGOING", ignoreCase = true)

                            if (isSale) {
                                totalSales += if (totalBill > 0) totalBill else amount
                            } else if (isExpense) {
                                totalExpenses += amount
                            } else {
                                if (flow.equals("INCOMING", ignoreCase = true)) {
                                    totalSales += amount
                                } else {
                                    totalExpenses += amount
                                }
                            }
                        }

                        val netBalance = totalSales - totalExpenses

                        trySend(
                            FirestoreDashboardSummaryData(
                                totalSales = totalSales,
                                totalExpenses = totalExpenses,
                                netBalance = netBalance,
                                transactionCount = count,
                                lastUpdatedMillis = System.currentTimeMillis(),
                                isFromCloud = true,
                                isLoading = false,
                                error = null
                            )
                        )
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    suspend fun fetchDashboardSummaryOnce(userId: String): FirestoreDashboardSummaryData {
        if (userId.isBlank()) {
            return FirestoreDashboardSummaryData(isLoading = false, isFromCloud = false, error = "গুগল সাইন-ইন প্রয়োজন")
        }
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()

            var totalSales = 0.0
            var totalExpenses = 0.0
            val count = snapshot.size()

            for (doc in snapshot.documents) {
                val type = doc.getString("type") ?: ""
                val flow = doc.getString("flowDirection") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val totalBill = doc.getDouble("totalBillAmount") ?: amount

                val isSale = type.equals("SELL", ignoreCase = true) ||
                        type.contains("বিক্র", ignoreCase = true) ||
                        flow.equals("INCOMING", ignoreCase = true)

                val isExpense = type.equals("EXPENSE", ignoreCase = true) ||
                        type.equals("BUY", ignoreCase = true) ||
                        type.equals("PERSONAL_EXPENSE", ignoreCase = true) ||
                        type.contains("খরচ", ignoreCase = true) ||
                        type.contains("ক্রয়", ignoreCase = true) ||
                        flow.equals("OUTGOING", ignoreCase = true)

                if (isSale) {
                    totalSales += if (totalBill > 0) totalBill else amount
                } else if (isExpense) {
                    totalExpenses += amount
                } else {
                    if (flow.equals("INCOMING", ignoreCase = true)) {
                        totalSales += amount
                    } else {
                        totalExpenses += amount
                    }
                }
            }

            val netBalance = totalSales - totalExpenses

            FirestoreDashboardSummaryData(
                totalSales = totalSales,
                totalExpenses = totalExpenses,
                netBalance = netBalance,
                transactionCount = count,
                lastUpdatedMillis = System.currentTimeMillis(),
                isFromCloud = true,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch dashboard summary from Firestore", e)
            FirestoreDashboardSummaryData(
                isLoading = false,
                isFromCloud = false,
                error = e.localizedMessage ?: "ফায়ারবেস ত্রুটি"
            )
        }
    }
}

data class FirestoreDashboardSummaryData(
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val transactionCount: Int = 0,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isFromCloud: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
