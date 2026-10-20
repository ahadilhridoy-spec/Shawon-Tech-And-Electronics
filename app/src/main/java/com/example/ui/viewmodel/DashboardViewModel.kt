package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountScope
import com.example.data.model.FlowDirection
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.remote.FirebaseAuthManager
import com.example.data.remote.FirestoreSyncService
import com.example.data.repository.TransactionRepository
import com.example.data.sample.PdfSampleData
import com.example.util.BanglaUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.remote.FirestoreDashboardSummaryData
import java.util.Calendar

data class AccountSummary(
    val accountCode: String,
    val method: String, // bKash, Nagad, or Mixed
    val totalReceived: Double, // কত ঢুকলো
    val totalSent: Double, // কত গেলো
    val netBalance: Double,
    val transactionCount: Int
)

data class PersonExpenseSummary(
    val personName: String,
    val totalAmount: Double,
    val items: List<TransactionEntity>
)

data class DateGroupSummary(
    val dateString: String,
    val dailyTotal: Double,
    val items: List<TransactionEntity>
)

data class MonthlyFinancialData(
    val periodKey: String,
    val periodLabel: String,
    val sales: Double,
    val expenses: Double,
    val netMargin: Double,
    val transactionCount: Int
)

data class DashboardUiState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val grandTotal: Double = 0.0,
    val dateWiseTotal: Double = 0.0,
    val personalExpensesTotal: Double = 0.0,
    val totalSell: Double = 0.0,
    val totalBuy: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalIncoming: Double = 0.0,
    val totalOutgoing: Double = 0.0,
    val bkashInflow: Double = 0.0,
    val bkashOutflow: Double = 0.0,
    val bkashBalance: Double = 0.0,
    val nagadInflow: Double = 0.0,
    val nagadOutflow: Double = 0.0,
    val nagadBalance: Double = 0.0,
    val cashInflow: Double = 0.0,
    val cashOutflow: Double = 0.0,
    val cashBalance: Double = 0.0,
    val accounts: List<AccountSummary> = emptyList(),
    val personExpenses: List<PersonExpenseSummary> = emptyList(),
    val dateGroups: List<DateGroupSummary> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val monthlyComparison: List<MonthlyFinancialData> = emptyList(),
    val dailyComparison: List<MonthlyFinancialData> = emptyList(),
    val totalDueReceivable: Double = 0.0, // বিক্রির মোট বাকি (কাস্টমারদের কাছে পাওনা)
    val totalDuePayable: Double = 0.0 // ক্রয়ের মোট বাকি (মহাজন/সাপ্লায়ারদের দেনা)
)

data class TransactionFormState(
    val dateString: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val selectedType: TransactionType = TransactionType.SELL,
    val selectedMethod: PaymentMethod = PaymentMethod.BKASH,
    val selectedFlow: FlowDirection = FlowDirection.INCOMING,
    val quantityInput: String = "", // পিস সংখ্যা
    val unitPriceInput: String = "230", // প্রতি পিসের মূল্য (বিক্রয় ডিফল্ট ২৩০, ক্রয় ডিফল্ট ১৯০)
    val amountInput: String = "", // নগদ/বিকাশ পরিশোধিত টাকা
    val totalBillInput: String = "", // মোট পণ্যের মূল্য / মোট বিল
    val dueAmountInput: String = "", // আংশিক বাকি টাকা
    val hasDue: Boolean = false, // আংশিক বাকি আছে কিনা
    val dueDateInput: String = "", // বাকি পরিশোধের সম্ভাব্য তারিখ
    val customerPhoneInput: String = "", // কাস্টমার/সাপ্লায়ার ফোন
    val senderInput: String = "",
    val receiverInput: String = "",
    val personInput: String = "",
    val descriptionInput: String = "",
    val quickCodeInput: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    val authManager = FirebaseAuthManager(application)
    val syncService = FirestoreSyncService(application)
    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser
    val isSigningIn: StateFlow<Boolean> = authManager.isSigningIn
    val authErrorMessage: StateFlow<String?> = authManager.errorMessage

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(
            database.transactionDao(),
            database.paymentAccountDao(),
            database.customerDao(),
            database.paymentRequestDao(),
            database.productDao()
        )
        viewModelScope.launch {
            repository.ensureDefaultAccountsSeeded()
            repository.ensureDefaultCustomersAndProductsSeeded()
        }
    }

    val paymentAccounts: StateFlow<List<PaymentAccountEntity>> = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<com.example.data.model.CustomerEntity>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPaymentRequests: StateFlow<List<com.example.data.model.PaymentRequestEntity>> = repository.allPaymentRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingPaymentRequests: StateFlow<List<com.example.data.model.PaymentRequestEntity>> = repository.pendingPaymentRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts: StateFlow<List<com.example.data.model.ProductItemEntity>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val firestoreSummary: StateFlow<FirestoreDashboardSummaryData> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                syncService.observeDashboardSummary(user.uid)
            } else {
                flowOf(
                    FirestoreDashboardSummaryData(
                        isLoading = false,
                        isFromCloud = false,
                        error = "ক্লাউড ডেটা দেখতে গুগল সাইন-ইন করুন"
                    )
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FirestoreDashboardSummaryData(isLoading = false, isFromCloud = false)
        )

    private val _selectedFilterMethod = MutableStateFlow<String?>("ALL")
    val selectedFilterMethod: StateFlow<String?> = _selectedFilterMethod

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Form state - starts completely empty as strictly required
    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allTransactions,
        _selectedFilterMethod,
        _searchQuery
    ) { transactions, filterMethod, query ->
        computeDashboardUiState(transactions, filterMethod, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun computeDashboardUiState(
        transactions: List<TransactionEntity>,
        filterMethod: String?,
        query: String
    ): DashboardUiState {
        var dateWiseTotal = 0.0
        var personalExpensesTotal = 0.0
        var totalSell = 0.0
        var totalBuy = 0.0
        var totalExpense = 0.0
        var totalIncoming = 0.0
        var totalOutgoing = 0.0

        var bkashIn = 0.0
        var bkashOut = 0.0
        var nagadIn = 0.0
        var nagadOut = 0.0
        var cashIn = 0.0
        var cashOut = 0.0
        var dueReceivable = 0.0
        var duePayable = 0.0

        val accountMap = mutableMapOf<String, MutableMap<String, Any>>()

        for (tx in transactions) {
            val amt = tx.amount
            val isPersonal = tx.type == TransactionType.PERSONAL_EXPENSE.name
            val isIncoming = tx.flowDirection == FlowDirection.INCOMING.name

            if (isPersonal) {
                personalExpensesTotal += amt
            } else {
                dateWiseTotal += amt
            }

            val bill = if (tx.totalBillAmount > 0) tx.totalBillAmount else (amt + tx.dueAmount)

            if (tx.dueAmount > 0) {
                if (tx.type == TransactionType.SELL.name || isIncoming) {
                    dueReceivable += tx.dueAmount
                } else {
                    duePayable += tx.dueAmount
                }
            }

            when (tx.type) {
                TransactionType.SELL.name -> totalSell += bill
                TransactionType.BUY.name -> totalBuy += bill
                TransactionType.EXPENSE.name -> totalExpense += amt
                TransactionType.PERSONAL_EXPENSE.name -> totalExpense += amt
            }

            if (isIncoming) {
                totalIncoming += amt
            } else {
                totalOutgoing += amt
            }

            when (tx.method) {
                PaymentMethod.BKASH.name -> {
                    if (isIncoming) bkashIn += amt else bkashOut += amt
                }
                PaymentMethod.NAGAD.name -> {
                    if (isIncoming) nagadIn += amt else nagadOut += amt
                }
                PaymentMethod.CASH.name -> {
                    if (isIncoming) cashIn += amt else cashOut += amt
                }
            }

            // Track account level stats for both sender and receiver
            val sender = tx.senderAccount.trim()
            val receiver = tx.receiverAccount.trim()

            if (receiver.isNotEmpty()) {
                val acc = accountMap.getOrPut(receiver) {
                    mutableMapOf("in" to 0.0, "out" to 0.0, "count" to 0, "method" to tx.method)
                }
                acc["in"] = (acc["in"] as Double) + amt
                acc["count"] = (acc["count"] as Int) + 1
            }

            if (sender.isNotEmpty()) {
                val acc = accountMap.getOrPut(sender) {
                    mutableMapOf("in" to 0.0, "out" to 0.0, "count" to 0, "method" to tx.method)
                }
                acc["out"] = (acc["out"] as Double) + amt
                acc["count"] = (acc["count"] as Int) + 1
            }
        }

        val grandTotal = dateWiseTotal + personalExpensesTotal

        // Build account summaries
        val accountsList = accountMap.map { (code, data) ->
            val received = data["in"] as Double
            val sent = data["out"] as Double
            AccountSummary(
                accountCode = code,
                method = data["method"] as String,
                totalReceived = received,
                totalSent = sent,
                netBalance = received - sent,
                transactionCount = data["count"] as Int
            )
        }.sortedByDescending { it.totalReceived + it.totalSent }

        // Build person expenses
        val personMap = transactions.filter { it.type == TransactionType.PERSONAL_EXPENSE.name }
            .groupBy { it.personName.ifEmpty { "অন্যান্য ব্যক্তি/খরচ" } }
        val personExpenses = personMap.map { (name, list) ->
            PersonExpenseSummary(
                personName = name,
                totalAmount = list.sumOf { it.amount },
                items = list
            )
        }.sortedByDescending { it.totalAmount }

        // Build date groups (for standard/datewise ledger)
        val dateMap = transactions.filter { it.type != TransactionType.PERSONAL_EXPENSE.name }
            .groupBy { it.dateString }
        val dateGroups = dateMap.map { (date, list) ->
            DateGroupSummary(
                dateString = date,
                dailyTotal = list.sumOf { it.amount },
                items = list
            )
        }

        val banglaMonthNames = arrayOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )

        val monthGroupMap = mutableMapOf<String, MutableList<TransactionEntity>>()
        val cal = Calendar.getInstance()

        for (tx in transactions) {
            cal.timeInMillis = tx.dateMillis
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val key = String.format(java.util.Locale.US, "%04d-%02d", y, m)
            monthGroupMap.getOrPut(key) { mutableListOf() }.add(tx)
        }

        val monthlyComparison = monthGroupMap.map { (key, txList) ->
            val parts = key.split("-")
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 8
            val monthName = banglaMonthNames.getOrElse(m) { "মাস ${m + 1}" }
            val label = "$monthName '${BanglaUtils.toBangla(y.toString().takeLast(2))}"

            val sales = txList.filter {
                it.type == TransactionType.SELL.name ||
                (it.flowDirection == FlowDirection.INCOMING.name && it.type != TransactionType.EXPENSE.name && it.type != TransactionType.PERSONAL_EXPENSE.name)
            }.sumOf { it.amount }

            val expenses = txList.filter {
                it.type == TransactionType.EXPENSE.name ||
                it.type == TransactionType.PERSONAL_EXPENSE.name ||
                it.type == TransactionType.BUY.name ||
                it.flowDirection == FlowDirection.OUTGOING.name
            }.sumOf { it.amount }

            MonthlyFinancialData(
                periodKey = key,
                periodLabel = label,
                sales = sales,
                expenses = expenses,
                netMargin = sales - expenses,
                transactionCount = txList.size
            )
        }.sortedBy { it.periodKey }

        val dailyComparison = transactions.groupBy { it.dateString }.map { (date, txList) ->
            val sales = txList.filter {
                it.type == TransactionType.SELL.name || it.flowDirection == FlowDirection.INCOMING.name
            }.sumOf { it.amount }

            val expenses = txList.filter {
                it.type == TransactionType.EXPENSE.name ||
                it.type == TransactionType.PERSONAL_EXPENSE.name ||
                it.type == TransactionType.BUY.name ||
                it.flowDirection == FlowDirection.OUTGOING.name
            }.sumOf { it.amount }

            MonthlyFinancialData(
                periodKey = date,
                periodLabel = date,
                sales = sales,
                expenses = expenses,
                netMargin = sales - expenses,
                transactionCount = txList.size
            )
        }

        return DashboardUiState(
            allTransactions = transactions,
            grandTotal = grandTotal,
            dateWiseTotal = dateWiseTotal,
            personalExpensesTotal = personalExpensesTotal,
            totalSell = totalSell,
            totalBuy = totalBuy,
            totalExpense = totalExpense,
            totalIncoming = totalIncoming,
            totalOutgoing = totalOutgoing,
            bkashInflow = bkashIn,
            bkashOutflow = bkashOut,
            bkashBalance = bkashIn - bkashOut,
            nagadInflow = nagadIn,
            nagadOutflow = nagadOut,
            nagadBalance = nagadIn - nagadOut,
            cashInflow = cashIn,
            cashOutflow = cashOut,
            cashBalance = cashIn - cashOut,
            accounts = accountsList,
            personExpenses = personExpenses,
            dateGroups = dateGroups,
            recentTransactions = transactions.take(15),
            monthlyComparison = monthlyComparison,
            dailyComparison = dailyComparison,
            totalDueReceivable = dueReceivable,
            totalDuePayable = duePayable
        )
    }

    // Form handlers
    fun toggleHasDue(hasDue: Boolean) {
        val total = _formState.value.totalBillInput.toDoubleOrNull() ?: (_formState.value.amountInput.toDoubleOrNull() ?: 0.0)
        val paid = _formState.value.amountInput.toDoubleOrNull() ?: 0.0
        val due = if (total > paid) (total - paid) else 0.0
        _formState.value = _formState.value.copy(
            hasDue = hasDue,
            totalBillInput = if (hasDue && _formState.value.totalBillInput.isBlank()) _formState.value.amountInput else _formState.value.totalBillInput,
            dueAmountInput = if (hasDue && due > 0) (if (due % 1.0 == 0.0) due.toLong().toString() else due.toString()) else _formState.value.dueAmountInput
        )
    }

    fun updateTotalBill(bill: String) {
        val total = bill.toDoubleOrNull() ?: 0.0
        val paid = _formState.value.amountInput.toDoubleOrNull() ?: 0.0
        val due = if (total >= paid) (total - paid) else 0.0
        _formState.value = _formState.value.copy(
            totalBillInput = bill,
            dueAmountInput = if (due > 0) { if (due % 1.0 == 0.0) due.toLong().toString() else due.toString() } else "",
            errorMessage = null
        )
    }

    fun updateDueAmount(due: String) {
        _formState.value = _formState.value.copy(dueAmountInput = due)
    }

    fun updateCustomerPhone(phone: String) {
        _formState.value = _formState.value.copy(customerPhoneInput = phone)
    }

    fun updateDueDate(dueDate: String) {
        _formState.value = _formState.value.copy(dueDateInput = dueDate)
    }

    fun settleDue(transaction: TransactionEntity, amountToSettle: Double) {
        if (amountToSettle <= 0.0) return
        val currentDue = transaction.dueAmount
        val actualSettle = minOf(amountToSettle, currentDue)
        val newDue = currentDue - actualSettle
        val newPaid = transaction.paidAmount + actualSettle

        val updated = transaction.copy(
            paidAmount = newPaid,
            dueAmount = newDue,
            amount = newPaid
        )
        viewModelScope.launch {
            repository.updateTransaction(updated)
        }
    }

    fun updateDate(date: String, millis: Long = System.currentTimeMillis()) {
        _formState.value = _formState.value.copy(dateString = date, dateMillis = millis)
    }

    fun setDateToToday() {
        val today = BanglaUtils.getCurrentDateBangla()
        _formState.value = _formState.value.copy(
            dateString = today,
            dateMillis = System.currentTimeMillis()
        )
    }

    fun updateType(type: TransactionType) {
        val newFlow = if (type == TransactionType.SELL) FlowDirection.INCOMING else FlowDirection.OUTGOING
        val defaultPrice = if (type == TransactionType.SELL) "230" else if (type == TransactionType.BUY) "190" else _formState.value.unitPriceInput
        val qty = _formState.value.quantityInput.toIntOrNull()
        val totalFromQty = if (qty != null && qty > 0) {
            val price = defaultPrice.toDoubleOrNull() ?: 0.0
            val total = qty * price
            if (total % 1.0 == 0.0) total.toLong().toString() else total.toString()
        } else _formState.value.totalBillInput

        val isFullPayment = !_formState.value.hasDue
        _formState.value = _formState.value.copy(
            selectedType = type,
            selectedFlow = newFlow,
            unitPriceInput = defaultPrice,
            totalBillInput = if (qty != null && qty > 0) totalFromQty else _formState.value.totalBillInput,
            amountInput = if (isFullPayment && qty != null && qty > 0) totalFromQty else _formState.value.amountInput
        )
    }

    fun updateQuantity(quantity: String) {
        val qty = quantity.toIntOrNull()
        val price = _formState.value.unitPriceInput.toDoubleOrNull() ?: 0.0
        val total = if (qty != null && qty > 0 && price > 0.0) {
            val calc = qty * price
            if (calc % 1.0 == 0.0) calc.toLong().toString() else calc.toString()
        } else {
            _formState.value.totalBillInput
        }
        val isFullPayment = !_formState.value.hasDue
        _formState.value = _formState.value.copy(
            quantityInput = quantity,
            totalBillInput = if (qty != null && qty > 0 && price > 0.0) total else _formState.value.totalBillInput,
            amountInput = if (isFullPayment && qty != null && qty > 0 && price > 0.0) total else _formState.value.amountInput,
            errorMessage = null
        )
    }

    fun updateUnitPrice(unitPrice: String) {
        val price = unitPrice.toDoubleOrNull() ?: 0.0
        val qty = _formState.value.quantityInput.toIntOrNull()
        val total = if (qty != null && qty > 0 && price > 0.0) {
            val calc = qty * price
            if (calc % 1.0 == 0.0) calc.toLong().toString() else calc.toString()
        } else {
            _formState.value.totalBillInput
        }
        val isFullPayment = !_formState.value.hasDue
        _formState.value = _formState.value.copy(
            unitPriceInput = unitPrice,
            totalBillInput = if (qty != null && qty > 0 && price > 0.0) total else _formState.value.totalBillInput,
            amountInput = if (isFullPayment && qty != null && qty > 0 && price > 0.0) total else _formState.value.amountInput,
            errorMessage = null
        )
    }

    fun updateMethod(method: PaymentMethod) {
        _formState.value = _formState.value.copy(selectedMethod = method)
    }

    fun updateFlow(flow: FlowDirection) {
        _formState.value = _formState.value.copy(selectedFlow = flow)
    }

    fun updateAmount(amount: String) {
        _formState.value = _formState.value.copy(amountInput = amount, errorMessage = null)
    }

    fun addAmount(increment: Double) {
        val current = _formState.value.amountInput.toDoubleOrNull() ?: 0.0
        val next = current + increment
        _formState.value = _formState.value.copy(
            amountInput = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString(),
            errorMessage = null
        )
    }

    fun updateSender(sender: String) {
        _formState.value = _formState.value.copy(senderInput = sender)
    }

    fun updateReceiver(receiver: String) {
        _formState.value = _formState.value.copy(receiverInput = receiver)
    }

    fun updatePerson(person: String) {
        _formState.value = _formState.value.copy(personInput = person)
    }

    fun updateDescription(desc: String) {
        _formState.value = _formState.value.copy(descriptionInput = desc)
    }

    fun updateQuickCodeInput(code: String) {
        _formState.value = _formState.value.copy(quickCodeInput = code)
    }

    fun applyQuickCode(code: String) {
        val parsed = BanglaUtils.parseShorthandCode(code)
        if (parsed != null) {
            val amountStr = parsed.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: _formState.value.amountInput

            _formState.value = _formState.value.copy(
                amountInput = amountStr,
                selectedMethod = parsed.method ?: _formState.value.selectedMethod,
                senderInput = parsed.sender ?: _formState.value.senderInput,
                receiverInput = parsed.receiver ?: _formState.value.receiverInput,
                errorMessage = null
            )
        } else {
            _formState.value = _formState.value.copy(errorMessage = "কোড ফরম্যাট বুঝতে পারা যায়নি। যেমন: 30K (B 915/689)")
        }
    }

    fun saveTransaction(): Boolean {
        val state = _formState.value

        val paidAmt = state.amountInput.toDoubleOrNull() ?: 0.0
        val totalBill = if (state.hasDue) {
            state.totalBillInput.toDoubleOrNull() ?: paidAmt
        } else {
            paidAmt
        }

        val dueAmt = if (state.hasDue) {
            val calcDue = state.dueAmountInput.toDoubleOrNull() ?: (totalBill - paidAmt)
            maxOf(0.0, calcDue)
        } else {
            0.0
        }

        if (totalBill <= 0.0 && paidAmt <= 0.0 && dueAmt <= 0.0) {
            _formState.value = state.copy(errorMessage = "অনুগ্রহ করে সঠিক টাকার পরিমাণ বা মোট বিল লিখুন")
            return false
        }

        val dateStr = state.dateString.ifBlank { BanglaUtils.getCurrentDateBangla() }

        val rawCode = if (state.quickCodeInput.isNotBlank()) {
            state.quickCodeInput.trim()
        } else {
            BanglaUtils.generateTransactionCode(
                amount = if (paidAmt > 0) paidAmt else totalBill,
                method = state.selectedMethod,
                sender = state.senderInput,
                receiver = state.receiverInput
            )
        }

        val entity = TransactionEntity(
            dateString = dateStr,
            dateMillis = state.dateMillis,
            type = state.selectedType.name,
            method = state.selectedMethod.name,
            flowDirection = state.selectedFlow.name,
            amount = paidAmt, // Cashflow amount
            senderAccount = state.senderInput.trim(),
            receiverAccount = state.receiverInput.trim(),
            personName = state.personInput.trim(),
            description = state.descriptionInput.trim(),
            rawCode = rawCode,
            totalBillAmount = if (totalBill > 0) totalBill else paidAmt,
            paidAmount = paidAmt,
            dueAmount = dueAmt,
            quantity = state.quantityInput.toIntOrNull() ?: 0,
            unitPrice = state.unitPriceInput.toDoubleOrNull() ?: (if (state.selectedType == TransactionType.BUY) 190.0 else 230.0),
            dueDate = state.dueDateInput.trim(),
            customerPhone = state.customerPhoneInput.trim()
        )

        viewModelScope.launch {
            val id = repository.insertTransaction(entity)
            val user = currentUser.value
            if (user != null) {
                syncService.uploadTransaction(user.uid, entity.copy(id = id))
            }
        }

        val successMsg = if (dueAmt > 0) {
            "লেনদেন সংরক্ষিত! পরিশোধ: ৳${paidAmt.toLong()}, বাকি: ৳${dueAmt.toLong()}"
        } else {
            "লেনদেন সফলভাবে সংরক্ষিত হয়েছে!"
        }

        // Reset form to blank ready for new entry
        _formState.value = TransactionFormState(
            dateString = state.dateString,
            dateMillis = state.dateMillis,
            successMessage = successMsg
        )
        return true
    }

    fun clearForm() {
        _formState.value = TransactionFormState()
    }

    fun clearMessage() {
        _formState.value = _formState.value.copy(errorMessage = null, successMessage = null)
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            val user = currentUser.value
            if (user != null) {
                syncService.deleteTransaction(user.uid, transaction.id)
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            val user = currentUser.value
            if (user != null) {
                syncService.uploadTransaction(user.uid, transaction)
            }
        }
    }

    fun setFilterMethod(method: String?) {
        _selectedFilterMethod.value = method
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadSamplePdfData() {
        viewModelScope.launch {
            val samples = PdfSampleData.getSampleTransactions()
            repository.insertAll(samples)
            val user = currentUser.value
            if (user != null) {
                syncService.syncAllTransactions(user.uid, samples)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Google Sign-In & Firebase Cloud Sync
    fun signInWithGoogle(activityContext: Context? = null) {
        viewModelScope.launch {
            authManager.signInWithGoogle(activityContext)
            val user = currentUser.value
            if (user != null) {
                try {
                    val txs = repository.allTransactions.first()
                    syncService.syncAllTransactions(user.uid, txs)
                    val accs = repository.allAccounts.first()
                    syncService.syncAllAccounts(user.uid, accs)
                } catch (e: Exception) {
                    android.util.Log.e("DashboardViewModel", "Initial sync error", e)
                }
            }
        }
    }

    fun clearAuthError() {
        authManager.clearErrorMessage()
    }

    fun signOut() {
        authManager.signOut()
    }

    fun syncNow() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                val txs = repository.allTransactions.first()
                syncService.syncAllTransactions(user.uid, txs)
                val accs = repository.allAccounts.first()
                syncService.syncAllAccounts(user.uid, accs)
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Manual sync error", e)
            }
        }
    }

    // Account quick selection
    fun selectReceiverAccount(account: PaymentAccountEntity) {
        val method = PaymentMethod.fromString(account.method)
        _formState.value = _formState.value.copy(
            receiverInput = account.accountNumber,
            selectedMethod = method
        )
    }

    fun selectSenderAccount(account: PaymentAccountEntity) {
        val method = PaymentMethod.fromString(account.method)
        _formState.value = _formState.value.copy(
            senderInput = account.accountNumber,
            selectedMethod = method
        )
    }

    // Dynamic Account Management (ভবিষ্যতে আরও একাউন্ট সেট করার সুবিধা)
    fun addCustomAccount(
        number: String,
        code: String,
        method: PaymentMethod,
        scope: AccountScope,
        note: String
    ) {
        if (number.isBlank()) return
        viewModelScope.launch {
            val entity = PaymentAccountEntity(
                accountNumber = number.trim(),
                accountCode = code.trim(),
                method = method.name,
                scope = scope.name,
                note = note.trim(),
                isCustom = true,
                sortOrder = 99
            )
            val id = repository.insertAccount(entity)
            val user = currentUser.value
            if (user != null) {
                syncService.uploadPaymentAccount(user.uid, entity.copy(id = id))
            }
        }
    }

    fun updateAccount(account: PaymentAccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
            val user = currentUser.value
            if (user != null) {
                syncService.uploadPaymentAccount(user.uid, account)
            }
        }
    }

    fun deleteAccount(account: PaymentAccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            val user = currentUser.value
            if (user != null) {
                syncService.deletePaymentAccount(user.uid, account.id)
            }
        }
    }

    fun restoreDefaultAccounts() {
        viewModelScope.launch {
            repository.restoreDefaultAccounts()
        }
    }

    // Customer Operations
    fun addCustomer(
        name: String,
        phone: String = "",
        address: String = "",
        note: String = ""
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val customer = com.example.data.model.CustomerEntity(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                note = note.trim()
            )
            repository.insertCustomer(customer)
        }
    }

    fun updateCustomer(customer: com.example.data.model.CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: com.example.data.model.CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun prepareEntryForCustomer(customer: com.example.data.model.CustomerEntity) {
        _formState.value = _formState.value.copy(
            personInput = customer.name,
            customerPhoneInput = customer.phone,
            selectedType = TransactionType.SELL,
            selectedFlow = FlowDirection.INCOMING,
            unitPriceInput = "230"
        )
    }

    // Payment Request Operations (Link 1 - Customer portal and admin review)
    fun submitCustomerPaymentRequest(
        customerId: Long,
        customerName: String,
        phone: String,
        amount: Double,
        method: String,
        senderCode: String,
        receiverCode: String,
        trxId: String,
        note: String
    ) {
        if (amount <= 0 || customerName.isBlank()) return
        viewModelScope.launch {
            val dateStr = BanglaUtils.formatDateToBangla(System.currentTimeMillis())
            val request = com.example.data.model.PaymentRequestEntity(
                customerId = customerId,
                customerName = customerName.trim(),
                customerPhone = phone.trim(),
                amount = amount,
                paymentMethod = method,
                senderCode = senderCode.trim(),
                receiverCode = receiverCode.trim(),
                trxId = trxId.trim(),
                note = note.trim(),
                status = "PENDING",
                submissionDateMillis = System.currentTimeMillis(),
                submissionDateString = dateStr
            )
            repository.submitPaymentRequest(request)
        }
    }

    fun approvePaymentRequest(request: com.example.data.model.PaymentRequestEntity, adminNote: String = "") {
        viewModelScope.launch {
            val createdTx = repository.approvePaymentRequest(request, adminNote)
            val user = currentUser.value
            if (user != null) {
                syncService.uploadTransaction(user.uid, createdTx)
            }
        }
    }

    fun rejectPaymentRequest(request: com.example.data.model.PaymentRequestEntity, reason: String) {
        viewModelScope.launch {
            repository.rejectPaymentRequest(request, reason)
        }
    }

    // Product Operations (Link 2 - Live stock and availability)
    fun addProduct(
        name: String,
        category: String,
        buyPrice: Double,
        sellPrice: Double,
        stock: Int,
        isAvailable: Boolean
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val product = com.example.data.model.ProductItemEntity(
                name = name.trim(),
                category = category.trim().ifBlank { "ইলেকট্রনিক্স" },
                buyPrice = if (buyPrice > 0) buyPrice else 190.0,
                sellPrice = if (sellPrice > 0) sellPrice else 230.0,
                stockQuantity = stock,
                isAvailable = isAvailable
            )
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: com.example.data.model.ProductItemEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun toggleProductAvailability(product: com.example.data.model.ProductItemEntity) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(isAvailable = !product.isAvailable))
        }
    }

    fun deleteProduct(product: com.example.data.model.ProductItemEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun refreshFirestoreSummary() {
        val user = currentUser.value
        if (user != null) {
            viewModelScope.launch {
                syncService.fetchDashboardSummaryOnce(user.uid)
            }
        }
    }
}
