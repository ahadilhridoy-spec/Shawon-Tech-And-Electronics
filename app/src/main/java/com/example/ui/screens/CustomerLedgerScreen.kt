package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import java.util.Calendar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CustomerEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CustomerLinksShareDialog
import com.example.ui.components.CustomerPaymentRequestPortalDialog
import com.example.ui.components.CustomerStatementAndProductPortalDialog
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils

@Composable
fun CustomerLedgerScreen(
    viewModel: DashboardViewModel,
    onNavigateToEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCustomers by viewModel.allCustomers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val pendingRequests by viewModel.pendingPaymentRequests.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTxType by remember { mutableStateOf("ALL") } // ALL, SELL, INCOMING, DUE, EXPENSE
    var selectedDateRange by remember { mutableStateOf("ALL") } // ALL, TODAY, WEEK, MONTH

    val now = remember { System.currentTimeMillis() }
    val startOfToday = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val sevenDaysAgo = remember(now) {
        now - 7L * 24 * 60 * 60 * 1000L
    }
    val startOfMonth = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    var selectedCustomerForDetail by remember { mutableStateOf<CustomerEntity?>(null) }

    if (selectedCustomerForDetail != null) {
        CustomerDetailScreen(
            customer = selectedCustomerForDetail!!,
            viewModel = viewModel,
            onBack = { selectedCustomerForDetail = null },
            onNavigateToEntry = onNavigateToEntry,
            modifier = modifier
        )
        return
    }

    // Dialog state for portals
    var customerForLinks by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForPayPortal by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForStatementPortal by remember { mutableStateOf<CustomerEntity?>(null) }

    // Compute customer statistics
    data class CustomerStat(
        val customer: CustomerEntity,
        val totalBill: Double,
        val totalPaid: Double,
        val currentDue: Double,
        val txCount: Int,
        val matchingTxCount: Int,
        val matchingTotalAmount: Double,
        val hasMatchingRecords: Boolean
    )

    val customerStats = remember(allCustomers, uiState.allTransactions, selectedTxType, selectedDateRange, searchQuery) {
        allCustomers.map { cust ->
            val custTx = uiState.allTransactions.filter { tx ->
                tx.personName.trim().equals(cust.name.trim(), ignoreCase = true) ||
                        (cust.phone.isNotBlank() && tx.customerPhone == cust.phone)
            }
            val totalBill = custTx.filter { it.type == "SELL" }
                .sumOf { if (it.totalBillAmount > 0) it.totalBillAmount else it.amount }
            val totalPaid = custTx.filter { it.flowDirection == "INCOMING" }
                .sumOf { it.paidAmount.coerceAtLeast(it.amount) }
            val currentDue = (totalBill - totalPaid).coerceAtLeast(0.0)

            val matchingTx = custTx.filter { tx ->
                val matchesDate = when (selectedDateRange) {
                    "TODAY" -> tx.dateMillis >= startOfToday
                    "WEEK" -> tx.dateMillis >= sevenDaysAgo
                    "MONTH" -> tx.dateMillis >= startOfMonth
                    else -> true
                }
                val matchesType = when (selectedTxType) {
                    "SELL" -> tx.type.equals("SELL", ignoreCase = true)
                    "INCOMING" -> tx.flowDirection.equals("INCOMING", ignoreCase = true) || tx.paidAmount > 0
                    "DUE" -> tx.dueAmount > 0
                    "EXPENSE" -> tx.type in listOf("EXPENSE", "BUY", "PERSONAL_EXPENSE") || tx.flowDirection.equals("OUTGOING", ignoreCase = true)
                    else -> true
                }
                matchesDate && matchesType
            }

            val matchingAmount = matchingTx.sumOf { it.amount }

            CustomerStat(
                customer = cust,
                totalBill = totalBill,
                totalPaid = totalPaid,
                currentDue = currentDue,
                txCount = custTx.size,
                matchingTxCount = matchingTx.size,
                matchingTotalAmount = matchingAmount,
                hasMatchingRecords = matchingTx.isNotEmpty() || (selectedTxType == "DUE" && currentDue > 0)
            )
        }
    }

    val filteredStats = customerStats.filter { stat ->
        val cust = stat.customer
        val matchesQuery = searchQuery.isBlank() ||
                cust.name.contains(searchQuery, ignoreCase = true) ||
                cust.phone.contains(searchQuery) ||
                cust.address.contains(searchQuery, ignoreCase = true) ||
                cust.note.contains(searchQuery, ignoreCase = true) ||
                uiState.allTransactions.any { tx ->
                    (tx.personName.trim().equals(cust.name.trim(), ignoreCase = true) ||
                            (cust.phone.isNotBlank() && tx.customerPhone == cust.phone)) &&
                            (tx.description.contains(searchQuery, ignoreCase = true) ||
                                    tx.rawCode.contains(searchQuery, ignoreCase = true) ||
                                    tx.senderAccount.contains(searchQuery) ||
                                    tx.receiverAccount.contains(searchQuery))
                }

        val matchesFilter = when {
            selectedTxType == "DUE" -> stat.currentDue > 0 || stat.matchingTxCount > 0
            selectedTxType != "ALL" || selectedDateRange != "ALL" -> stat.hasMatchingRecords
            else -> true
        }

        matchesQuery && matchesFilter
    }

    val totalReceivableAllCustomers = customerStats.sumOf { it.currentDue }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TechNavyDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "কাস্টমার খাতা ও লেজার",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "মোট গ্রাহক: ${allCustomers.size} জন",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = { showAddCustomerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = InflowGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন কাস্টমার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "কাস্টমারদের কাছে মোট পাওনা বকেয়া:",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${BanglaUtils.formatTaka(totalReceivableAllCustomers)} ৳",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }

                    // Pending requests alert if any
                    if (pendingRequests.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD97706),
                            modifier = Modifier.clickable {
                                // Open first pending request or customer
                                val firstCust = allCustomers.firstOrNull { it.id == pendingRequests.first().customerId }
                                    ?: allCustomers.firstOrNull()
                                if (firstCust != null) {
                                    customerForPayPortal = firstCust
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${pendingRequests.size} টি রিকোয়েস্ট পেন্ডিং!",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search & Filters Header
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            // 1. Search Bar with Clear Button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("কাস্টমারের নাম, ফোন, বিবরণ বা লেনদেন খুঁজুন...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TechNavyPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_bar"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechNavyPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Transaction Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TechNavyPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ধরন:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                FilterChip(
                    selected = selectedTxType == "ALL",
                    onClick = { selectedTxType = "ALL" },
                    label = { Text("সকল (${customerStats.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechNavyPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TechNavyPrimary
                    ),
                    modifier = Modifier.testTag("chip_type_ALL")
                )
                FilterChip(
                    selected = selectedTxType == "SELL",
                    onClick = { selectedTxType = "SELL" },
                    label = { Text("বিক্রয় রেকর্ড (${customerStats.count { stat -> stat.customer.let { c -> uiState.allTransactions.any { tx -> (tx.personName.trim().equals(c.name.trim(), ignoreCase = true) || (c.phone.isNotBlank() && tx.customerPhone == c.phone)) && tx.type == "SELL" } } }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InflowGreen.copy(alpha = 0.15f),
                        selectedLabelColor = InflowGreen
                    ),
                    modifier = Modifier.testTag("chip_type_SELL")
                )
                FilterChip(
                    selected = selectedTxType == "INCOMING",
                    onClick = { selectedTxType = "INCOMING" },
                    label = { Text("পেমেন্ট/জমা (${customerStats.count { it.totalPaid > 0 }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InflowGreen.copy(alpha = 0.15f),
                        selectedLabelColor = InflowGreen
                    ),
                    modifier = Modifier.testTag("chip_type_INCOMING")
                )
                FilterChip(
                    selected = selectedTxType == "DUE",
                    onClick = { selectedTxType = "DUE" },
                    label = { Text("বকেয়া আছে (${customerStats.count { it.currentDue > 0 }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OutflowRed.copy(alpha = 0.15f),
                        selectedLabelColor = OutflowRed
                    ),
                    modifier = Modifier.testTag("chip_type_DUE")
                )
                FilterChip(
                    selected = selectedTxType == "EXPENSE",
                    onClick = { selectedTxType = "EXPENSE" },
                    label = { Text("ক্রয়/খরচ (${customerStats.count { stat -> stat.customer.let { c -> uiState.allTransactions.any { tx -> (tx.personName.trim().equals(c.name.trim(), ignoreCase = true) || (c.phone.isNotBlank() && tx.customerPhone == c.phone)) && (tx.type in listOf("EXPENSE", "BUY", "PERSONAL_EXPENSE") || tx.flowDirection == "OUTGOING") } } }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706).copy(alpha = 0.15f),
                        selectedLabelColor = Color(0xFFB45309)
                    ),
                    modifier = Modifier.testTag("chip_type_EXPENSE")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Date Range Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TechNavyPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "তারিখ:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                FilterChip(
                    selected = selectedDateRange == "ALL",
                    onClick = { selectedDateRange = "ALL" },
                    label = { Text("সব সময়") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechNavyPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TechNavyPrimary
                    ),
                    modifier = Modifier.testTag("chip_date_ALL")
                )
                FilterChip(
                    selected = selectedDateRange == "TODAY",
                    onClick = { selectedDateRange = "TODAY" },
                    label = { Text("আজ") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechNavyPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TechNavyPrimary
                    ),
                    modifier = Modifier.testTag("chip_date_TODAY")
                )
                FilterChip(
                    selected = selectedDateRange == "WEEK",
                    onClick = { selectedDateRange = "WEEK" },
                    label = { Text("গত ৭ দিন") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechNavyPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TechNavyPrimary
                    ),
                    modifier = Modifier.testTag("chip_date_WEEK")
                )
                FilterChip(
                    selected = selectedDateRange == "MONTH",
                    onClick = { selectedDateRange = "MONTH" },
                    label = { Text("এই মাস") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechNavyPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TechNavyPrimary
                    ),
                    modifier = Modifier.testTag("chip_date_MONTH")
                )
            }

            // 4. Active Filters Status & Quick Reset Row
            val isFiltered = searchQuery.isNotBlank() || selectedTxType != "ALL" || selectedDateRange != "ALL"
            if (isFiltered) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = TechNavyPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍 ফিল্টারকৃত ফলাফল: ${filteredStats.size} জন কাস্টমার",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TechNavyPrimary
                    )

                    Row(
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            selectedTxType = "ALL"
                            selectedDateRange = "ALL"
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Filters",
                            tint = OutflowRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "রিসেট",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutflowRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customers List
        if (filteredStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো কাস্টমার পাওয়া যায়নি।",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStats, key = { it.customer.id }) { stat ->
                    val cust = stat.customer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCustomerForDetail = cust }
                            .testTag("customer_card_${cust.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Row: Avatar + Name + Phone + Edit/Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TechNavyPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = cust.name.take(1),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TechNavyPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cust.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (cust.phone.isNotBlank()) {
                                            Text(
                                                text = cust.phone,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        if (cust.address.isNotBlank()) {
                                            Text(
                                                text = " | ${cust.address}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { customerToEdit = cust }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = TechNavyPrimary)
                                }
                                IconButton(onClick = { customerToDelete = cust }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = OutflowRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Stats Row: Total Bill, Paid, Due
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("মোট ক্রয়/বিল", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("${BanglaUtils.formatTaka(stat.totalBill)} ৳", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("মোট পরিশোধ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("${BanglaUtils.formatTaka(stat.totalPaid)} ৳", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InflowGreen)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("বর্তমান দেনা/পাওনা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(
                                        text = if (stat.currentDue > 0) "বকেয়া: ${BanglaUtils.formatTaka(stat.currentDue)} ৳" else "পরিশোধিত ✓",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stat.currentDue > 0) OutflowRed else InflowGreen
                                    )
                                }
                            }

                            // Highlight matching financial records if a filter or search is active
                            if ((selectedTxType != "ALL" || selectedDateRange != "ALL" || searchQuery.isNotBlank()) && stat.matchingTxCount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TechNavyPrimary.copy(alpha = 0.08f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🎯 ফিল্টারকৃত রেকর্ড: ${stat.matchingTxCount} টি",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TechNavyPrimary
                                        )
                                        Text(
                                            text = "লেনদেন: ${BanglaUtils.formatTaka(stat.matchingTotalAmount)} ৳",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TechNavyPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons:
                            // 1. ২ টি লিংক (Links)
                            // 2. বিস্তারিত খাতা (Statement)
                            // 3. নতুন এন্ট্রি (Add Entry)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { customerForLinks = cust },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = TechNavyPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("২ টি লিংক", color = TechNavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { selectedCustomerForDetail = cust },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .testTag("customer_detail_btn_${cust.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("খাতা ও হিস্ট্রি (${stat.txCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.prepareEntryForCustomer(cust)
                                        onNavigateToEntry()
                                    },
                                    modifier = Modifier.weight(1.1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = InflowGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("নতুন এন্ট্রি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        var nameInput by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf("") }
        var addressInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("নতুন কাস্টমার যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("কাস্টমারের নাম *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("ঠিকানা বা মার্কেট") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("নোট বা বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.addCustomer(nameInput, phoneInput, addressInput, noteInput)
                            showAddCustomerDialog = false
                            Toast.makeText(context, "কাস্টমার সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Edit Customer Dialog
    customerToEdit?.let { cust ->
        var editName by remember { mutableStateOf(cust.name) }
        var editPhone by remember { mutableStateOf(cust.phone) }
        var editAddress by remember { mutableStateOf(cust.address) }
        var editNote by remember { mutableStateOf(cust.note) }

        AlertDialog(
            onDismissRequest = { customerToEdit = null },
            title = { Text("কাস্টমার তথ্য সম্পাদনা করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("কাস্টমারের নাম *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("ঠিকানা বা মার্কেট") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("নোট বা বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateCustomer(
                                cust.copy(
                                    name = editName.trim(),
                                    phone = editPhone.trim(),
                                    address = editAddress.trim(),
                                    note = editNote.trim()
                                )
                            )
                            customerToEdit = null
                            Toast.makeText(context, "কাস্টমার আপডেট সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                ) {
                    Text("আপডেট")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToEdit = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Delete Customer Dialog
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("কাস্টমার মুছে ফেলতে চান?") },
            text = { Text("${cust.name}-কে তালিকা থেকে মুছে ফেলা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(cust)
                        customerToDelete = null
                        Toast.makeText(context, "কাস্টমার মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 2 Sharable Links Dialog
    customerForLinks?.let { cust ->
        CustomerLinksShareDialog(
            customer = cust,
            onDismiss = { customerForLinks = null },
            onOpenPaymentPortal = {
                val c = cust
                customerForLinks = null
                customerForPayPortal = c
            },
            onOpenStatementPortal = {
                val c = cust
                customerForLinks = null
                customerForStatementPortal = c
            }
        )
    }

    // Link 1 Portal Dialog (Customer Payment Submission & Admin Review)
    customerForPayPortal?.let { cust ->
        CustomerPaymentRequestPortalDialog(
            customer = cust,
            viewModel = viewModel,
            onDismiss = { customerForPayPortal = null }
        )
    }

    // Link 2 Portal Dialog (Customer Statement & Product Stock Availability)
    customerForStatementPortal?.let { cust ->
        CustomerStatementAndProductPortalDialog(
            customer = cust,
            viewModel = viewModel,
            onDismiss = { customerForStatementPortal = null }
        )
    }
}
