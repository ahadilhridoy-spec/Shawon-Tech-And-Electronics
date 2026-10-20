package com.example.data.repository

import com.example.data.local.CustomerDao
import com.example.data.local.PaymentAccountDao
import com.example.data.local.PaymentRequestDao
import com.example.data.local.ProductDao
import com.example.data.local.TransactionDao
import com.example.data.model.CustomerEntity
import com.example.data.model.DefaultPaymentAccounts
import com.example.data.model.FlowDirection
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.PaymentRequestEntity
import com.example.data.model.ProductItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.BanglaUtils
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val paymentAccountDao: PaymentAccountDao,
    private val customerDao: CustomerDao,
    private val paymentRequestDao: PaymentRequestDao,
    private val productDao: ProductDao
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<PaymentAccountEntity>> = paymentAccountDao.getAllAccounts()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allPaymentRequests: Flow<List<PaymentRequestEntity>> = paymentRequestDao.getAllRequests()
    val pendingPaymentRequests: Flow<List<PaymentRequestEntity>> = paymentRequestDao.getPendingRequests()
    val allProducts: Flow<List<ProductItemEntity>> = productDao.getAllProducts()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun insertAll(transactions: List<TransactionEntity>) {
        transactionDao.insertAll(transactions)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }

    // Account methods
    fun getAccountsByScope(scope: String): Flow<List<PaymentAccountEntity>> {
        return paymentAccountDao.getAccountsByScope(scope)
    }

    fun getAccountsByScopeAndMethod(scope: String, method: String): Flow<List<PaymentAccountEntity>> {
        return paymentAccountDao.getAccountsByScopeAndMethod(scope, method)
    }

    suspend fun insertAccount(account: PaymentAccountEntity): Long {
        return paymentAccountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: PaymentAccountEntity) {
        paymentAccountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: PaymentAccountEntity) {
        paymentAccountDao.deleteAccount(account)
    }

    suspend fun deleteAccountById(id: Long) {
        paymentAccountDao.deleteById(id)
    }

    suspend fun ensureDefaultAccountsSeeded() {
        val count = paymentAccountDao.getAccountCount()
        if (count == 0) {
            paymentAccountDao.insertAll(DefaultPaymentAccounts.defaultList)
        }
    }

    suspend fun restoreDefaultAccounts() {
        paymentAccountDao.clearAll()
        paymentAccountDao.insertAll(DefaultPaymentAccounts.defaultList)
    }

    // Customer methods
    suspend fun insertCustomer(customer: CustomerEntity): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun deleteCustomerById(id: Long) {
        customerDao.deleteById(id)
    }

    suspend fun ensureDefaultCustomersAndProductsSeeded() {
        if (customerDao.getCustomerCount() == 0) {
            val defaultCustomers = listOf(
                CustomerEntity(name = "রানা ভাই", phone = "01711223344", address = "মতিঝিল, ঢাকা", note = "নিয়মিত পাইকারি ক্রেতা"),
                CustomerEntity(name = "মিরাজ ভাই", phone = "01819887766", address = "মিরপুর-১০, ঢাকা", note = "ডিসপ্লে ও এক্সেসরিজ গ্রাহক"),
                CustomerEntity(name = "আকাশ টেলিকম", phone = "01912334455", address = "উত্তরা সেক্টর ৭", note = "মোবাইল চার্জার ও পার্টস"),
                CustomerEntity(name = "সুমন ইলেকট্রনিক্স", phone = "01622334455", address = "যাত্রাবাড়ী, ঢাকা", note = "টিভি পার্টস ও এলইডি"),
                CustomerEntity(name = "আলমগীর স্টোর", phone = "01555667788", address = "টঙ্গী বাজার", note = "ক্যাশ ও বিকাশ নিয়মিত লেনদেন")
            )
            customerDao.insertAll(defaultCustomers)
        }

        if (productDao.getProductCount() == 0) {
            val defaultProducts = listOf(
                ProductItemEntity(name = "স্মার্ট এলইডি ডিসপ্লে (HD Panel)", category = "ডিসপ্লে", buyPrice = 190.0, sellPrice = 230.0, stockQuantity = 35, isAvailable = true),
                ProductItemEntity(name = "ফাস্ট চার্জার ২০W অ্যাডাপ্টার", category = "এক্সেসরিজ", buyPrice = 190.0, sellPrice = 230.0, stockQuantity = 50, isAvailable = true),
                ProductItemEntity(name = "ইউএসবি ক্যাবল ৩.০ ব্রেইডেড", category = "ক্যাবল", buyPrice = 120.0, sellPrice = 160.0, stockQuantity = 0, isAvailable = false),
                ProductItemEntity(name = "টিভি মাদারবোর্ড ইউনিভার্সাল", category = "সার্কিট", buyPrice = 450.0, sellPrice = 580.0, stockQuantity = 12, isAvailable = true),
                ProductItemEntity(name = "লিথিয়াম ব্যাটারি ৩০০০mAh", category = "ব্যাটারি", buyPrice = 190.0, sellPrice = 230.0, stockQuantity = 25, isAvailable = true),
                ProductItemEntity(name = "ব্লুটুথ হেডফোন প্রো", category = "অডিও", buyPrice = 310.0, sellPrice = 420.0, stockQuantity = 0, isAvailable = false)
            )
            productDao.insertAll(defaultProducts)
        }
    }

    // Payment Request methods
    suspend fun submitPaymentRequest(request: PaymentRequestEntity): Long {
        return paymentRequestDao.insertRequest(request)
    }

    suspend fun approvePaymentRequest(request: PaymentRequestEntity, note: String = ""): TransactionEntity {
        // 1. Update request status
        val updatedRequest = request.copy(
            status = "APPROVED",
            reviewedDateMillis = System.currentTimeMillis()
        )
        paymentRequestDao.updateRequest(updatedRequest)

        // 2. Automatically create transaction in ledger
        val dateString = BanglaUtils.formatDateToBangla(System.currentTimeMillis())
        val newTx = TransactionEntity(
            dateString = dateString,
            dateMillis = System.currentTimeMillis(),
            type = TransactionType.SELL.name,
            method = request.paymentMethod,
            flowDirection = FlowDirection.INCOMING.name,
            amount = request.amount,
            paidAmount = request.amount,
            totalBillAmount = request.amount,
            dueAmount = 0.0,
            senderAccount = request.senderCode,
            receiverAccount = request.receiverCode,
            personName = request.customerName,
            customerPhone = request.customerPhone,
            description = "কাস্টমার পেমেন্ট রিকোয়েস্ট অনুমোদিত (TrxID: ${request.trxId.ifEmpty { "N/A" }})${if (note.isNotBlank()) " - $note" else ""}",
            rawCode = "${request.paymentMethod.take(1)} ${request.senderCode}/${request.receiverCode}"
        )
        transactionDao.insertTransaction(newTx)
        return newTx
    }

    suspend fun rejectPaymentRequest(request: PaymentRequestEntity, reason: String) {
        val updatedRequest = request.copy(
            status = "REJECTED",
            reviewedDateMillis = System.currentTimeMillis(),
            rejectionReason = reason
        )
        paymentRequestDao.updateRequest(updatedRequest)
    }

    // Product methods
    suspend fun insertProduct(product: ProductItemEntity): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductItemEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductItemEntity) = productDao.deleteProduct(product)
}
