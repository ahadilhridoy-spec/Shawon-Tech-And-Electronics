package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlowDirection
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CashGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.AccountSummary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils

@Composable
fun BkashNagadLedgerScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMethodFilter by remember { mutableStateOf("ALL") } // ALL, BKASH, NAGAD, CASH
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, SELL, BUY, EXPENSE, PERSONAL_EXPENSE, DUE
    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var itemToSettleDue by remember { mutableStateOf<TransactionEntity?>(null) }

    // Filter transactions based on method, transaction type, and search query
    val filteredTransactions = remember(
        uiState.allTransactions,
        selectedMethodFilter,
        selectedTypeFilter,
        searchQuery
    ) {
        uiState.allTransactions.filter { tx ->
            val matchMethod = when (selectedMethodFilter) {
                "BKASH" -> tx.method == PaymentMethod.BKASH.name
                "NAGAD" -> tx.method == PaymentMethod.NAGAD.name
                "CASH" -> tx.method == PaymentMethod.CASH.name
                else -> true
            }

            val matchType = when (selectedTypeFilter) {
                "SELL" -> tx.type == TransactionType.SELL.name
                "BUY" -> tx.type == TransactionType.BUY.name
                "EXPENSE" -> tx.type == TransactionType.EXPENSE.name
                "PERSONAL_EXPENSE" -> tx.type == TransactionType.PERSONAL_EXPENSE.name
                "DUE" -> tx.dueAmount > 0
                else -> true
            }

            val matchSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                tx.description.lowercase().contains(q) ||
                        tx.senderAccount.lowercase().contains(q) ||
                        tx.receiverAccount.lowercase().contains(q) ||
                        tx.personName.lowercase().contains(q) ||
                        tx.customerPhone.lowercase().contains(q) ||
                        tx.rawCode.lowercase().contains(q) ||
                        tx.dateString.lowercase().contains(q)
            }

            matchMethod && matchType && matchSearch
        }
    }

    // Filtered accounts based on method
    val filteredAccounts = remember(uiState.accounts, selectedMethodFilter) {
        if (selectedMethodFilter == "ALL") uiState.accounts
        else uiState.accounts.filter { it.method == selectedMethodFilter }
    }

    val isFilterActive = selectedMethodFilter != "ALL" || selectedTypeFilter != "ALL" || searchQuery.isNotBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("bkash_nagad_ledger_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "লেনদেন খাতা ও লেজার",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "বিবরণ দিয়ে খুঁজুন এবং ধরন ও মাধ্যম অনুযায়ী ফিল্টার করুন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                if (isFilterActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OutflowRed.copy(alpha = 0.1f),
                        modifier = Modifier
                            .clickable {
                                selectedMethodFilter = "ALL"
                                selectedTypeFilter = "ALL"
                                searchQuery = ""
                            }
                            .testTag("reset_all_filters_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Filters",
                                tint = OutflowRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "রিসেট",
                                color = OutflowRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Search Bar with Description & Account Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "বিবরণ, পণ্য, ব্যক্তি বা নম্বর খুঁজুন...",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TechNavyPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechNavyPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_input")
            )
        }

        // Transaction Type Filter Chips (Buy, Sell, Expense, Personal)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "লেনদেনের ধরন (Transaction Type)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to ("সব ধরন" to TechNavyPrimary),
                        "SELL" to ("বিক্রি (Sell)" to InflowGreen),
                        "BUY" to ("ক্রয় (Buy)" to TechNavyPrimary),
                        "EXPENSE" to ("দোকান খরচ (Expense)" to OutflowRed),
                        "PERSONAL_EXPENSE" to ("ব্যক্তিভিত্তিক (Personal)" to GoldAccent),
                        "DUE" to ("বাকি লেনদেন (With Due)" to Color(0xFFD97706))
                    ).forEach { (key, pair) ->
                        val (label, chipColor) = pair
                        val isSelected = selectedTypeFilter == key

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) chipColor else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) chipColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clickable { selectedTypeFilter = key }
                                .testTag("type_filter_$key")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Payment Method Filter Chips (bKash, Nagad, Cash, All)
        item {
            Column {
                Text(
                    text = "পেমেন্ট মাধ্যম (Payment Method)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to ("সব মাধ্যম" to TechNavyPrimary),
                        "BKASH" to ("বিকাশ (bKash)" to BkashPink),
                        "NAGAD" to ("নগদ (Nagad)" to NagadOrange),
                        "CASH" to ("ক্যাশ (Cash)" to CashGreen)
                    ).forEach { (key, pair) ->
                        val (label, color) = pair
                        val isSelected = selectedMethodFilter == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clickable { selectedMethodFilter = key }
                                .testTag("method_filter_$key")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Banner for filtered transactions
        item {
            val totalIn = filteredTransactions
                .filter { it.flowDirection == FlowDirection.INCOMING.name }
                .sumOf { it.amount }
            val totalOut = filteredTransactions
                .filter { it.flowDirection == FlowDirection.OUTGOING.name }
                .sumOf { it.amount }
            val balance = totalIn - totalOut

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("নির্বাচিত প্রাপ্তি (+)", fontSize = 11.sp, color = InflowGreen)
                        Text(
                            text = BanglaUtils.formatTaka(totalIn, useSymbol = true),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InflowGreen
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("নির্বাচিত প্রদান (-)", fontSize = 11.sp, color = OutflowRed)
                        Text(
                            text = BanglaUtils.formatTaka(totalOut, useSymbol = true),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutflowRed
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("নেট প্রভাব", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = BanglaUtils.formatTaka(balance, useSymbol = true),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) InflowGreen else OutflowRed
                        )
                    }
                }
            }
        }

        // Account balances horizontal strip
        if (filteredAccounts.isNotEmpty()) {
            item {
                Text(
                    text = "নম্বর / অ্যাকাউন্টভিত্তিক স্থিতি (Account Balances)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredAccounts.forEach { acc ->
                        AccountBalanceChip(
                            account = acc,
                            onAccountClick = { searchQuery = acc.accountCode }
                        )
                    }
                }
            }
        }

        // Section Title & Result Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "লেনদেনের তালিকা (${BanglaUtils.toBangla(filteredTransactions.size)} টি পাওয়া গেছে)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (searchQuery.isNotBlank()) {
                    Text(
                        text = "\"$searchQuery\"-এর ফলাফল",
                        fontSize = 11.sp,
                        color = TechNavyPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // List of transactions
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isFilterActive) "ফিল্টারের শর্ত অনুযায়ী কোন লেনদেন পাওয়া যায়নি"
                            else "এখনো কোন লেনদেন এন্ট্রি যোগ করা হয়নি",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isFilterActive) {
                            TextButton(
                                onClick = {
                                    selectedMethodFilter = "ALL"
                                    selectedTypeFilter = "ALL"
                                    searchQuery = ""
                                }
                            ) {
                                Text("ফিল্টার রিসেট করুন", color = TechNavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "নতুন লেনদেন এন্ট্রি করতে 'এন্ট্রি ফর্ম' ট্যাবে যান।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionCard(
                    transaction = tx,
                    onDeleteClick = { itemToDelete = tx },
                    onSettleDueClick = { itemToSettleDue = tx }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("লেনদেন মুছে ফেলতে চান?") },
            text = { Text("${tx.dateString}-এর ${tx.rawCode.ifEmpty { tx.description }} মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        itemToDelete = null
                    }
                ) {
                    Text("মুছুন", color = OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Settle Due Dialog
    itemToSettleDue?.let { tx ->
        SettleDueDialog(
            transaction = tx,
            onDismiss = { itemToSettleDue = null },
            onConfirm = { amount ->
                viewModel.settleDue(tx, amount)
                itemToSettleDue = null
            }
        )
    }
}

@Composable
fun AccountBalanceChip(
    account: AccountSummary,
    onAccountClick: () -> Unit
) {
    val methodColor = when (account.method) {
        PaymentMethod.BKASH.name -> BkashPink
        PaymentMethod.NAGAD.name -> NagadOrange
        else -> TechNavyPrimary
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .clickable { onAccountClick() }
            .width(150.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${account.accountCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = methodColor
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(methodColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (account.method == PaymentMethod.BKASH.name) "B" else "N",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = methodColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ঢুকলো: +${BanglaUtils.formatTaka(account.totalReceived, useSymbol = true)}",
                fontSize = 10.sp,
                color = InflowGreen
            )
            Text(
                text = "গেলো: -${BanglaUtils.formatTaka(account.totalSent, useSymbol = true)}",
                fontSize = 10.sp,
                color = OutflowRed
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "নেট: ${BanglaUtils.formatTaka(account.netBalance, useSymbol = true)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (account.netBalance >= 0) InflowGreen else OutflowRed
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSettleDueClick: () -> Unit = {}
) {
    val isIncoming = transaction.flowDirection == FlowDirection.INCOMING.name
    val methodColor = when (transaction.method) {
        PaymentMethod.BKASH.name -> BkashPink
        PaymentMethod.NAGAD.name -> NagadOrange
        PaymentMethod.CASH.name -> CashGreen
        else -> TechNavyPrimary
    }

    val typeLabel = when (transaction.type) {
        TransactionType.SELL.name -> "পণ্য বিক্রয়"
        TransactionType.BUY.name -> "পণ্য ক্রয়"
        TransactionType.EXPENSE.name -> "দোকান খরচ"
        TransactionType.PERSONAL_EXPENSE.name -> "ব্যক্তিভিত্তিক"
        else -> transaction.type
    }

    val typeColor = when (transaction.type) {
        TransactionType.SELL.name -> InflowGreen
        TransactionType.BUY.name -> TechNavyPrimary
        TransactionType.EXPENSE.name -> OutflowRed
        TransactionType.PERSONAL_EXPENSE.name -> GoldAccent
        else -> TechNavyPrimary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Method + Type Pill + Date + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Method badge
                    Box(
                        modifier = Modifier
                            .background(methodColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (transaction.method == PaymentMethod.BKASH.name) "বিকাশ"
                            else if (transaction.method == PaymentMethod.NAGAD.name) "নগদ"
                            else "ক্যাশ",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Transaction Type pill (Buy / Sell / Expense / Personal)
                    Box(
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            color = typeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = transaction.dateString,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                // Amount
                Text(
                    text = "${if (isIncoming) "+" else "-"}${BanglaUtils.formatTaka(transaction.amount, useSymbol = true)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isIncoming) InflowGreen else OutflowRed
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sender -> Receiver transfer info
            if (transaction.senderAccount.isNotBlank() || transaction.receiverAccount.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "হিসাব কোড:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = transaction.senderAccount.ifEmpty { "-" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaction.receiverAccount.ifEmpty { "-" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Raw statement code if available
                    if (transaction.rawCode.isNotBlank()) {
                        Text(
                            text = transaction.rawCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TechNavyPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description and Person Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Description (searched field)
                    if (transaction.description.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = TechNavyPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = transaction.description,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (transaction.personName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ব্যক্তি: ${transaction.personName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Due and Partial Payment Info Banner
            if (transaction.dueAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            GoldAccent.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "আংশিক বাকি:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নগদ: ${BanglaUtils.formatTaka(transaction.paidAmount, useSymbol = true)}",
                                fontSize = 11.sp,
                                color = InflowGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "বাকি: ${BanglaUtils.formatTaka(transaction.dueAmount, useSymbol = true)}",
                                fontSize = 11.sp,
                                color = OutflowRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (transaction.customerPhone.isNotBlank() || transaction.dueDate.isNotBlank()) {
                            Text(
                                text = listOf(
                                    if (transaction.customerPhone.isNotBlank()) "ফোন: ${transaction.customerPhone}" else "",
                                    if (transaction.dueDate.isNotBlank()) "পরিশোধের তারিখ: ${transaction.dueDate}" else ""
                                ).filter { it.isNotBlank() }.joinToString(" • "),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TechNavyPrimary,
                        modifier = Modifier
                            .clickable { onSettleDueClick() }
                            .testTag("settle_due_button_${transaction.id}")
                    ) {
                        Text(
                            text = "বাকি আদায়",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettleDueDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var settleAmountInput by remember {
        mutableStateOf(
            if (transaction.dueAmount % 1.0 == 0.0) transaction.dueAmount.toLong().toString()
            else transaction.dueAmount.toString()
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("বাকি টাকা আদায় / পরিশোধ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "খাত: ${transaction.rawCode.ifEmpty { transaction.description }}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                if (transaction.personName.isNotBlank()) {
                    Text(
                        text = "ব্যক্তি/গ্রাহক: ${transaction.personName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "বর্তমান বাকি: ${BanglaUtils.formatTaka(transaction.dueAmount, useSymbol = true)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OutflowRed
                )

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = settleAmountInput,
                    onValueChange = {
                        settleAmountInput = it
                        error = null
                    },
                    label = { Text("আদায়কৃত টাকার পরিমাণ ৳ *") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick buttons for full or half due collection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            val half = transaction.dueAmount / 2.0
                            settleAmountInput = if (half % 1.0 == 0.0) half.toLong().toString() else half.toString()
                        }
                    ) {
                        Text("৫০% পরিশোধ", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = InflowGreen.copy(alpha = 0.15f),
                        modifier = Modifier.clickable {
                            settleAmountInput = if (transaction.dueAmount % 1.0 == 0.0) transaction.dueAmount.toLong().toString() else transaction.dueAmount.toString()
                        }
                    ) {
                        Text("সম্পূর্ণ বাকি পরিশোধ", fontSize = 11.sp, color = InflowGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                if (error != null) {
                    Text(text = error!!, color = OutflowRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = settleAmountInput.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        error = "সঠিক টাকার পরিমাণ লিখুন"
                    } else if (amt > transaction.dueAmount) {
                        error = "আদায়কৃত টাকা বাকির পরিমাণের চেয়ে বেশি হতে পারে না"
                    } else {
                        onConfirm(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = InflowGreen)
            ) {
                Text("জমা নিন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
