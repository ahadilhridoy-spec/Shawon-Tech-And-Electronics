package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import java.util.Calendar
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerEntity
import com.example.data.model.PaymentRequestEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CustomerLinksShareDialog
import com.example.ui.components.CustomerPaymentRequestPortalDialog
import com.example.ui.components.CustomerStatementAndProductPortalDialog
import com.example.ui.theme.BkashPink
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils

/**
 * Customer Detail Screen
 * Displays complete transaction history, live balance calculations,
 * approval of pending payments with instant balance update,
 * edit/delete transaction flows, customer edit/delete options,
 * and direct links to self-service portals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: CustomerEntity,
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onNavigateToEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back gesture / hardware back button
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val allPaymentRequests by viewModel.allPaymentRequests.collectAsState()

    // Transaction filter and search state
    var txSearchQuery by remember { mutableStateOf("") }
    var selectedTxFilter by remember { mutableStateOf("ALL") } // ALL, SELL, INCOMING, DUE
    var selectedDateRange by remember { mutableStateOf("ALL") } // ALL, TODAY, WEEK, MONTH
    var showMenu by remember { mutableStateOf(false) }

    val now = remember { System.currentTimeMillis() }
    val startOfToday = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val sevenDaysAgo = remember(now) { now - 7L * 24 * 60 * 60 * 1000L }
    val startOfMonth = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Dialog state
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showLinksDialog by remember { mutableStateOf(false) }
    var showPayPortalDialog by remember { mutableStateOf(false) }
    var showStatementPortalDialog by remember { mutableStateOf(false) }
    var requestToReject by remember { mutableStateOf<PaymentRequestEntity?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    // Specific transactions for this customer
    val customerTransactions = remember(uiState.allTransactions, customer) {
        uiState.allTransactions.filter { tx ->
            tx.personName.trim().equals(customer.name.trim(), ignoreCase = true) ||
                    (customer.phone.isNotBlank() && tx.customerPhone == customer.phone)
        }.sortedByDescending { it.dateMillis }
    }

    // Specific pending payment requests for this customer
    val customerPendingRequests = remember(allPaymentRequests, customer) {
        allPaymentRequests.filter { req ->
            (req.customerId == customer.id || req.customerName.trim().equals(customer.name.trim(), ignoreCase = true)) &&
                    req.status == "PENDING"
        }
    }

    // Dynamic Financial Calculations (Real-time update on approved payment or transaction changes)
    val totalBill = remember(customerTransactions) {
        customerTransactions.filter { it.type == "SELL" }
            .sumOf { if (it.totalBillAmount > 0) it.totalBillAmount else it.amount }
    }

    val totalPaid = remember(customerTransactions) {
        customerTransactions.filter { it.flowDirection == "INCOMING" }
            .sumOf { it.paidAmount.coerceAtLeast(it.amount) }
    }

    val currentDue = remember(totalBill, totalPaid) {
        (totalBill - totalPaid).coerceAtLeast(0.0)
    }

    val filteredTransactions = remember(customerTransactions, txSearchQuery, selectedTxFilter, selectedDateRange) {
        customerTransactions.filter { tx ->
            val matchesQuery = txSearchQuery.isBlank() ||
                    tx.description.contains(txSearchQuery, ignoreCase = true) ||
                    tx.dateString.contains(txSearchQuery, ignoreCase = true) ||
                    tx.senderAccount.contains(txSearchQuery) ||
                    tx.receiverAccount.contains(txSearchQuery) ||
                    tx.method.contains(txSearchQuery, ignoreCase = true)

            val matchesFilter = when (selectedTxFilter) {
                "SELL" -> tx.type == "SELL"
                "INCOMING" -> tx.flowDirection == "INCOMING"
                "DUE" -> tx.dueAmount > 0
                else -> true
            }

            val matchesDate = when (selectedDateRange) {
                "TODAY" -> tx.dateMillis >= startOfToday
                "WEEK" -> tx.dateMillis >= sevenDaysAgo
                "MONTH" -> tx.dateMillis >= startOfMonth
                else -> true
            }

            matchesQuery && matchesFilter && matchesDate
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (customer.phone.isNotBlank()) customer.phone else "কাস্টমার বিবরণী ও খাতা",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("customer_detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Call button if phone exists
                    if (customer.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(callIntent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Customer",
                                tint = Color(0xFF86EFAC)
                            )
                        }
                    }

                    // Share 2 Portal Links
                    IconButton(
                        onClick = { showLinksDialog = true },
                        modifier = Modifier.testTag("customer_detail_links_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Portal Links",
                            tint = Color.White
                        )
                    }

                    // Overflow options menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("কাস্টমার তথ্য সম্পাদনা") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TechNavyPrimary) },
                            onClick = {
                                showMenu = false
                                showEditCustomerDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("পেমেন্ট পোর্টাল খুলুন (Link 1)") },
                            leadingIcon = { Icon(Icons.Default.HourglassTop, contentDescription = null, tint = TechNavyPrimary) },
                            onClick = {
                                showMenu = false
                                showPayPortalDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("খাতা ও প্রডাক্ট পোর্টাল (Link 2)") },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = InflowGreen) },
                            onClick = {
                                showMenu = false
                                showStatementPortalDialog = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("কাস্টমার মুছে ফেলুন", color = OutflowRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = OutflowRed) },
                            onClick = {
                                showMenu = false
                                showDeleteCustomerDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechNavyDark,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.prepareEntryForCustomer(customer)
                    onNavigateToEntry()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                text = { Text("নতুন এন্ট্রি", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = InflowGreen,
                modifier = Modifier.testTag("customer_detail_new_entry_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Customer Profile & Current Real-time Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = TechNavyDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TechNavyPrimary.copy(alpha = 0.35f),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = customer.name.take(1),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (customer.address.isNotBlank()) {
                                    Text(
                                        text = "ঠিকানা: ${customer.address}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                if (customer.note.isNotBlank()) {
                                    Text(
                                        text = "নোট: ${customer.note}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Balance Metrics: Total Purchases vs Total Paid vs Current Balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "সর্বমোট ক্রয় / বিল",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${BanglaUtils.formatTaka(totalBill)} ৳",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column {
                                Text(
                                    text = "মোট পরিশোধ / জমা",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${BanglaUtils.formatTaka(totalPaid)} ৳",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF86EFAC)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "বর্তমান ব্যালেন্স (বকেয়া)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${BanglaUtils.formatTaka(currentDue)} ৳",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentDue > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentDue > 0) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF22C55E).copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentDue > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (currentDue > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentDue > 0) {
                                        "কাস্টমারের কাছে বাকি পাওনা রয়েছে: ${BanglaUtils.formatTaka(currentDue)} টাকা"
                                    } else {
                                        "আলহামদুলিল্লাহ! এই কাস্টমারের হিসাব সম্পূর্ণ পরিশোধিত ✓"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentDue > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Pending Payment Requests Review Banner (If any submitted from Link 1)
            if (customerPendingRequests.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "পেন্ডিং পেমেন্ট রিকোয়েস্ট (${customerPendingRequests.size} টি যাচাই করুন):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        customerPendingRequests.forEach { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFD97706)
                                            ) {
                                                Text(
                                                    text = "পেন্ডিং যাচাই",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${req.paymentMethod} পেমেন্ট",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Text(
                                            text = "${BanglaUtils.formatTaka(req.amount)} ৳",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "সেন্ডার: ${req.senderCode} ➔ প্রাপক: ${req.receiverCode}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (req.trxId.isNotBlank()) {
                                        Text(text = "TrxID: ${req.trxId}", fontSize = 11.sp, color = Color(0xFF78350F))
                                    }
                                    if (req.note.isNotBlank()) {
                                        Text(text = "নোট: ${req.note}", fontSize = 11.sp, color = Color(0xFF78350F))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFB45309).copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                requestToReject = req
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OutflowRed)
                                        ) {
                                            Text("বাতিল", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.approvePaymentRequest(req, "অ্যাডমিন অনুমোদিত")
                                                Toast.makeText(
                                                    context,
                                                    "${req.amount} ৳ পেমেন্ট অনুমোদিত! বর্তমান ব্যালেন্স স্বয়ংক্রিয়ভাবে আপডেট হয়েছে।",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = InflowGreen)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("অনুমোদন করুন (Approve)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // 3. Quick Action Buttons Bar (Share Links, Open Portals)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showLinksDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("২ টি লিংক শেয়ার", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showStatementPortalDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("লাইভ খাতা প্রিভিউ", fontSize = 12.sp)
                    }
                }
            }

            // 4. Transaction Search and Filter Header
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "লেনদেন বিবরণী (${filteredTransactions.size}/${customerTransactions.size} টি)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "সিরিয়াল বাই সিরিয়াল",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = txSearchQuery,
                        onValueChange = { txSearchQuery = it },
                        placeholder = { Text("লেনদেন খুঁজুন (তারিখ, মাধ্যম, বিবরণ, অ্যাকাউন্ট)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechNavyPrimary) },
                        trailingIcon = {
                            if (txSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { txSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_detail_search_bar"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transaction Type Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ধরন:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        FilterChip(
                            selected = selectedTxFilter == "ALL",
                            onClick = { selectedTxFilter = "ALL" },
                            label = { Text("সকল (${customerTransactions.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedTxFilter == "SELL",
                            onClick = { selectedTxFilter = "SELL" },
                            label = { Text("বিক্রি (${customerTransactions.count { it.type == "SELL" }})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedTxFilter == "INCOMING",
                            onClick = { selectedTxFilter = "INCOMING" },
                            label = { Text("জমা (${customerTransactions.count { it.flowDirection == "INCOMING" }})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedTxFilter == "DUE",
                            onClick = { selectedTxFilter = "DUE" },
                            label = { Text("বাকি (${customerTransactions.count { it.dueAmount > 0 }})", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date Range Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "তারিখ:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        FilterChip(
                            selected = selectedDateRange == "ALL",
                            onClick = { selectedDateRange = "ALL" },
                            label = { Text("সব সময়", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedDateRange == "TODAY",
                            onClick = { selectedDateRange = "TODAY" },
                            label = { Text("আজ", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedDateRange == "WEEK",
                            onClick = { selectedDateRange = "WEEK" },
                            label = { Text("গত ৭ দিন", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedDateRange == "MONTH",
                            onClick = { selectedDateRange = "MONTH" },
                            label = { Text("এই মাস", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 5. Specific Transactions List with Serial Number, Edit & Delete
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (customerTransactions.isEmpty()) "এই কাস্টমারের কোনো লেনদেন পাওয়া যায়নি।" else "খুঁজে পাওয়া যায়নি। ফিল্টার পরিবর্তন করুন।",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                itemsIndexed(filteredTransactions, key = { _, tx -> tx.id }) { index, tx ->
                    val isIncoming = tx.flowDirection == "INCOMING"
                    val methodColor = when (tx.method) {
                        "BKASH" -> BkashPink
                        "NAGAD" -> NagadOrange
                        else -> InflowGreen
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Serial number, Date, Method, Amount (+/-)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = TechNavyPrimary,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tx.dateString,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = methodColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = tx.method,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = methodColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (isIncoming) "+" else "-"}${BanglaUtils.formatTaka(tx.amount)} ৳",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isIncoming) InflowGreen else OutflowRed
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quantity, Rate, Bill, Paid, Due
                            if (tx.quantity > 0 || tx.totalBillAmount > 0 || tx.dueAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (tx.quantity > 0) {
                                        Text(
                                            text = "পরিমাণ: ${tx.quantity} পিস | দর: ${tx.unitPrice}৳",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                        )
                                    }
                                    if (tx.dueAmount > 0) {
                                        Text(
                                            text = "বাকি: ${BanglaUtils.formatTaka(tx.dueAmount)} ৳",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OutflowRed
                                        )
                                    }
                                }
                            }

                            // Accounts
                            if (tx.senderAccount.isNotBlank() || tx.receiverAccount.isNotBlank()) {
                                Text(
                                    text = "প্রেরক: ${tx.senderAccount.ifEmpty { "N/A" }} ➔ প্রাপক: ${tx.receiverAccount.ifEmpty { "N/A" }}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }

                            // Description / Raw Code
                            if (tx.description.isNotBlank()) {
                                Text(
                                    text = tx.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Action buttons: Edit & Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { txToEdit = tx },
                                    modifier = Modifier.testTag("edit_tx_btn_${tx.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = TechNavyPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("সম্পাদনা", fontSize = 11.sp, color = TechNavyPrimary)
                                }

                                TextButton(
                                    onClick = { txToDelete = tx },
                                    modifier = Modifier.testTag("delete_tx_btn_${tx.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = OutflowRed)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("মুছে ফেলুন", fontSize = 11.sp, color = OutflowRed)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Edit Transaction Dialog
    txToEdit?.let { tx ->
        var editAmount by remember { mutableStateOf(tx.amount.toString()) }
        var editDue by remember { mutableStateOf(tx.dueAmount.toString()) }
        var editQuantity by remember { mutableStateOf(if (tx.quantity > 0) tx.quantity.toString() else "") }
        var editRate by remember { mutableStateOf(if (tx.unitPrice > 0) tx.unitPrice.toString() else "") }
        var editDesc by remember { mutableStateOf(tx.description) }

        AlertDialog(
            onDismissRequest = { txToEdit = null },
            title = { Text("লেনদেন সম্পাদনা করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("তারিখ: ${tx.dateString} (${tx.method})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editQuantity,
                            onValueChange = { editQuantity = it },
                            label = { Text("পিস সংখ্যা") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editRate,
                            onValueChange = { editRate = it },
                            label = { Text("দর (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("পরিশোধিত টাকা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDue,
                        onValueChange = { editDue = it },
                        label = { Text("বাকি টাকা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = editAmount.toDoubleOrNull() ?: tx.amount
                        val due = editDue.toDoubleOrNull() ?: tx.dueAmount
                        val qty = editQuantity.toIntOrNull() ?: tx.quantity
                        val rate = editRate.toDoubleOrNull() ?: tx.unitPrice
                        val updated = tx.copy(
                            amount = amt,
                            paidAmount = amt,
                            dueAmount = due,
                            totalBillAmount = if (qty > 0 && rate > 0) qty * rate else (amt + due),
                            quantity = qty,
                            unitPrice = rate,
                            description = editDesc
                        )
                        viewModel.updateTransaction(updated)
                        txToEdit = null
                        Toast.makeText(context, "লেনদেন আপডেট সম্পন্ন এবং ব্যালেন্স সমন্বয় করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToEdit = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Delete Transaction Dialog
    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("লেনদেনটি মুছে ফেলতে চান?") },
            text = { Text("${tx.dateString}-এর ${tx.amount} টাকার লেনদেনটি মুছে ফেলা হবে। বর্তমান দেনা/পাওনা ব্যালেন্স স্বয়ংক্রিয়ভাবে সমন্বয় হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        txToDelete = null
                        Toast.makeText(context, "লেনদেন সফলভাবে মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Reject Payment Request Dialog
    requestToReject?.let { req ->
        AlertDialog(
            onDismissRequest = { requestToReject = null },
            title = { Text("পেমেন্ট রিকোয়েস্ট বাতিল করুন") },
            text = {
                Column {
                    Text("বাতিল করার কারণ লিখুন:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("বাতিলের কারণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rejectPaymentRequest(req, rejectReason.ifBlank { "তথ্য অসম্পূর্ণ বা মেলেনি" })
                        requestToReject = null
                        rejectReason = ""
                        Toast.makeText(context, "রিকোয়েস্ট বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("বাতিল করুন", color = OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToReject = null }) {
                    Text("ফিরে যান")
                }
            }
        )
    }

    // Edit Customer Profile Dialog
    if (showEditCustomerDialog) {
        var editName by remember { mutableStateOf(customer.name) }
        var editPhone by remember { mutableStateOf(customer.phone) }
        var editAddress by remember { mutableStateOf(customer.address) }
        var editNote by remember { mutableStateOf(customer.note) }

        AlertDialog(
            onDismissRequest = { showEditCustomerDialog = false },
            title = { Text("কাস্টমার তথ্য সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("নাম *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("ফোন নম্বর") },
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
                                customer.copy(
                                    name = editName.trim(),
                                    phone = editPhone.trim(),
                                    address = editAddress.trim(),
                                    note = editNote.trim()
                                )
                            )
                            showEditCustomerDialog = false
                            Toast.makeText(context, "কাস্টমার প্রোফাইল আপডেট সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                ) {
                    Text("আপডেট")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCustomerDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Delete Customer Profile Dialog
    if (showDeleteCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomerDialog = false },
            title = { Text("কাস্টমার প্রোফাইল মুছে ফেলতে চান?") },
            text = { Text("${customer.name}-কে তালিকা থেকে মুছে ফেলা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        showDeleteCustomerDialog = false
                        onBack()
                        Toast.makeText(context, "কাস্টমার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCustomerDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 2 Sharable Links Dialog
    if (showLinksDialog) {
        CustomerLinksShareDialog(
            customer = customer,
            onDismiss = { showLinksDialog = false },
            onOpenPaymentPortal = {
                showLinksDialog = false
                showPayPortalDialog = true
            },
            onOpenStatementPortal = {
                showLinksDialog = false
                showStatementPortalDialog = true
            }
        )
    }

    // Link 1 Portal Dialog (Customer Payment Submission & Admin Review)
    if (showPayPortalDialog) {
        CustomerPaymentRequestPortalDialog(
            customer = customer,
            viewModel = viewModel,
            onDismiss = { showPayPortalDialog = false }
        )
    }

    // Link 2 Portal Dialog (Customer Statement & Product Stock Availability)
    if (showStatementPortalDialog) {
        CustomerStatementAndProductPortalDialog(
            customer = customer,
            viewModel = viewModel,
            onDismiss = { showStatementPortalDialog = false }
        )
    }
}
