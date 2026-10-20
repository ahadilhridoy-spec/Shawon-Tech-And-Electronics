package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountScope
import com.example.data.model.CustomerEntity
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentRequestEntity
import com.example.data.model.ProductItemEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils

/**
 * Dialog displaying the 2 Customer Sharable Links
 * Link 1: Customer Self-Service Payment Submission / Request
 * Link 2: Customer Live Statement & Product Availability Portal
 */
@Composable
fun CustomerLinksShareDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onOpenPaymentPortal: () -> Unit,
    onOpenStatementPortal: () -> Unit
) {
    val context = LocalContext.current
    val payLink = "https://shawontech.app/customer/pay?cid=${customer.id}&name=${customer.name.replace(" ", "%20")}&phone=${customer.phone}"
    val statementLink = "https://shawontech.app/customer/statement?cid=${customer.id}&name=${customer.name.replace(" ", "%20")}"

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    fun shareLink(text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = TechNavyPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "কাস্টমার পোর্টাল লিংকসমূহ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "গ্রাহক: ${customer.name} (${customer.phone.ifEmpty { "ফোন নেই" }})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "কাস্টমারকে নিচের যেকোনো লিংক শেয়ার করতে পারেন। গ্রাহক সহজে পেমেন্ট রিকোয়েস্ট পাঠাতে ও খাতা দেখতে পারবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Link 1 Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TechNavyPrimary,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("১", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পেমেন্ট রিকোয়েস্ট লিংক",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TechNavyDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "কাস্টমার এই লিংকে ঢুকে টাকার পরিমাণ, সেন্ডার কোড, রিসিভার কোড ও মাধ্যম দিয়ে সরাসরি পেমেন্ট রিকোয়েস্ট পাঠাতে পারবে যা পেন্ডিং থাকবে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = payLink,
                                fontSize = 10.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onOpenPaymentPortal,
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পোর্টাল খুলুন", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { copyToClipboard(payLink, "পেমেন্ট লিংক") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("কপি", fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = {
                                    val msg = "Shawon Tech And Electronics-এ আপনার পেমেন্ট রিকোয়েস্ট পাঠাতে এই লিংকে ক্লিক করুন:\n$payLink"
                                    shareLink(msg, "পেমেন্ট রিকোয়েস্ট লিংক শেয়ার করুন")
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = TechNavyPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Link 2 Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = InflowGreen.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = InflowGreen,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("২", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "কাস্টমার খাতা ও প্রডাক্ট পোর্টাল লিংক",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF166534)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "কাস্টমার তার সমস্ত সিরিয়াল বাই সিরিয়াল লেনদেন দেখতে পারবে, আমাদের বিকাশ/নগদ পেমেন্ট নাম্বারগুলো পাবে এবং প্রডাক্ট 'এভেইলেবল' নাকি 'নট এভেইলেবল' দেখতে পারবে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = statementLink,
                                fontSize = 10.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onOpenStatementPortal,
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = InflowGreen)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পোর্টাল খুলুন", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { copyToClipboard(statementLink, "খাতা ও প্রডাক্ট পোর্টাল লিংক") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("কপি", fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = {
                                    val msg = "Shawon Tech And Electronics-এ আপনার ডিজিটাল খাতা ও প্রডাক্ট স্টক দেখতে এই লিংকে ক্লিক করুন:\n$statementLink"
                                    shareLink(msg, "খাতা ও প্রডাক্ট পোর্টাল লিংক শেয়ার করুন")
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = InflowGreen)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

/**
 * Link 1 Interactive Portal & Simulator:
 * Customer enters Amount, Sender Code, Receiver Code, Payment Method, TrxID, Note
 * Submits to database as PENDING.
 * Admin can also review pending requests, verify details, and on approval:
 * Auto-creates transaction, attaches to customer, and updates customer due balance (+/-)!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPaymentRequestPortalDialog(
    customer: CustomerEntity,
    viewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Customer Submission Form, 1: Pending & History
    val paymentAccounts by viewModel.paymentAccounts.collectAsState()
    val allRequests by viewModel.allPaymentRequests.collectAsState()
    val customerRequests = allRequests.filter { it.customerId == customer.id || it.customerName == customer.name }

    // Form inputs
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }
    var senderCodeText by remember { mutableStateOf("") }
    var receiverCodeText by remember { mutableStateOf("2774 B/S") }
    var trxIdText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var receiverDropdownExpanded by remember { mutableStateOf(false) }

    var submittedMessage by remember { mutableStateOf<String?>(null) }
    var requestToReview by remember { mutableStateOf<PaymentRequestEntity?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var showRejectDialog by remember { mutableStateOf(false) }

    // Official receiver account options
    val receiverOptions = remember(paymentAccounts, selectedMethod) {
        val matchedAccounts = paymentAccounts.filter {
            (it.scope == AccountScope.SALE_RECEIVER.name || it.scope == AccountScope.ALL.name) &&
                    (it.method == selectedMethod.name || it.method == "MIXED")
        }
        if (matchedAccounts.isNotEmpty()) {
            matchedAccounts.map { "${it.accountNumber} ${it.accountCode}" }
        } else {
            when (selectedMethod) {
                PaymentMethod.BKASH -> listOf("2774 B/S", "2689 B/S", "5822 B/S", "8960 B/S", "3915 B/A", "8787 B/P")
                PaymentMethod.NAGAD -> listOf("2774 N/S", "2689 N/S", "6965 N/A", "8787 N/P")
                PaymentMethod.CASH -> listOf("ক্যাশ কাউন্টার (Shawon Tech)")
                PaymentMethod.BANK -> listOf("ব্যাংক অ্যাকাউন্ট")
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TechNavyDark)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "কাস্টমার পেমেন্ট পোর্টাল (লিংক ১)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "LIVE LINK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7DD3FC),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "গ্রাহক: ${customer.name} | ফোন: ${customer.phone.ifEmpty { "N/A" }}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Tabs: Customer View vs Requests Status / Admin Review
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("গ্রাহক পেমেন্ট ফর্ম", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            val pendingCount = customerRequests.count { it.status == "PENDING" }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("রিকোয়েস্ট তালিকা", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = OutflowRed,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$pendingCount",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                // Body content
                if (selectedTab == 0) {
                    // Customer Input Form
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.06f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = TechNavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "কাস্টমার নিজে বিকাশ/নগদে টাকা পাঠিয়ে এই ফর্মে তথ্য সাবমিট করবে। আপনার অনুমোদনের পর তা স্বয়ংক্রিয়ভাবে লেনদেনে যুক্ত হবে।",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        // Success banner if submitted
                        if (submittedMessage != null) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = InflowGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = InflowGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = submittedMessage ?: "",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        }

                        // Payment Method Selector
                        item {
                            Text("১. পেমেন্ট মাধ্যম নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(PaymentMethod.BKASH, PaymentMethod.NAGAD, PaymentMethod.CASH).forEach { method ->
                                    val isSelected = selectedMethod == method
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedMethod = method
                                            receiverCodeText = when (method) {
                                                PaymentMethod.BKASH -> "2774 B/S"
                                                PaymentMethod.NAGAD -> "2774 N/S"
                                                PaymentMethod.CASH -> "ক্যাশ টাকা"
                                                else -> ""
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = method.banglaLabel,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Amount Input
                        item {
                            Text("২. পেমেন্ট করা টাকার পরিমাণ (৳):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("টাকার পরিমাণ (যেমন: ৫০০০)") },
                                placeholder = { Text("টাকার পরিমাণ লিখুন") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Sender Code / Number
                        item {
                            Text("৩. আপনার সেন্ডার কোড / নম্বর:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = senderCodeText,
                                onValueChange = { senderCodeText = it },
                                label = { Text("যে নম্বর বা কোড থেকে টাকা পাঠিয়েছেন") },
                                placeholder = { Text("যেমন: 017... বা শেষ ৪ ডিজিট 2774") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Receiver Code / Number (Our account)
                        item {
                            Text("৪. প্রাপক নম্বর / কোড (Receiver):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = receiverDropdownExpanded,
                                onExpandedChange = { receiverDropdownExpanded = !receiverDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = receiverCodeText,
                                    onValueChange = { receiverCodeText = it },
                                    label = { Text("আমাদের বিকাশ/নগদ রিসিভার একাউন্ট") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = receiverDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = receiverDropdownExpanded,
                                    onDismissRequest = { receiverDropdownExpanded = false }
                                ) {
                                    receiverOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                receiverCodeText = opt
                                                receiverDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // TrxID & Note
                        item {
                            Text("৫. TrxID (ট্রানজেকশন আইডি) ও নোট (ঐচ্ছিক):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = trxIdText,
                                    onValueChange = { trxIdText = it },
                                    label = { Text("TrxID") },
                                    placeholder = { Text("9J4K8LMN") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    label = { Text("মন্তব্য / নোট") },
                                    placeholder = { Text("ডিসপ্লে বিল") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Submit Button
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val amount = amountText.toDoubleOrNull() ?: 0.0
                                    if (amount <= 0.0) {
                                        Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (senderCodeText.isBlank()) {
                                        Toast.makeText(context, "সেন্ডার কোড বা নম্বর লিখুন", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    viewModel.submitCustomerPaymentRequest(
                                        customerId = customer.id,
                                        customerName = customer.name,
                                        phone = customer.phone,
                                        amount = amount,
                                        method = selectedMethod.name,
                                        senderCode = senderCodeText,
                                        receiverCode = receiverCodeText,
                                        trxId = trxIdText,
                                        note = noteText
                                    )

                                    submittedMessage = "পেমেন্ট রিকোয়েস্ট সফলভাবে পাঠানো হয়েছে! স্ট্যাটাস: পেন্ডিং (Pending)। অ্যাডমিন যাচাই করে অনুমোদন করবেন।"
                                    amountText = ""
                                    senderCodeText = ""
                                    trxIdText = ""
                                    noteText = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("পেমেন্ট রিকোয়েস্ট পাঠান (Send Request)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Admin & Customer Request Tracking List
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${customer.name}-এর সকল পেমেন্ট রিকোয়েস্ট (${customerRequests.size} টি)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (customerRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "এখনো কোনো পেমেন্ট রিকোয়েস্ট পাঠানো হয়নি।",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(customerRequests) { req ->
                                    val isPending = req.status == "PENDING"
                                    val isApproved = req.status == "APPROVED"

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isPending -> Color(0xFFFEF3C7) // Amber
                                                isApproved -> Color(0xFFDCFCE7) // Emerald
                                                else -> Color(0xFFFEE2E2) // Red
                                            }
                                        )
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
                                                        color = when {
                                                            isPending -> Color(0xFFD97706)
                                                            isApproved -> Color(0xFF16A34A)
                                                            else -> Color(0xFFDC2626)
                                                        }
                                                    ) {
                                                        Text(
                                                            text = when {
                                                                isPending -> "পেন্ডিং (Pending)"
                                                                isApproved -> "অনুমোদিত (Success)"
                                                                else -> "বাতিল (Rejected)"
                                                            },
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = req.paymentMethod,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }

                                                Text(
                                                    text = "${BanglaUtils.formatTaka(req.amount)} ৳",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isPending -> Color(0xFFB45309)
                                                        isApproved -> Color(0xFF15803D)
                                                        else -> Color(0xFFB91C1C)
                                                    }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "সেন্ডার: ${req.senderCode} ➔ রিসিভার: ${req.receiverCode}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (req.trxId.isNotBlank()) {
                                                Text(
                                                    text = "TrxID: ${req.trxId}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                            if (req.note.isNotBlank()) {
                                                Text(
                                                    text = "নোট: ${req.note}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }

                                            // If Pending: Show Admin Action Buttons (Approve / Reject)
                                            if (isPending) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "চেকিং করে অনুমোদন করুন:",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF78350F),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedButton(
                                                        onClick = {
                                                            requestToReview = req
                                                            showRejectDialog = true
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OutflowRed)
                                                    ) {
                                                        Text("বাতিল", fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Button(
                                                        onClick = {
                                                            viewModel.approvePaymentRequest(req, "ম্যানুয়াল চেকিং সঠিক")
                                                            Toast.makeText(context, "পেমেন্ট সফলভাবে অনুমোদিত এবং খাতার সাথে যুক্ত হয়েছে!", Toast.LENGTH_LONG).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = InflowGreen)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("অনুমোদন (Approve)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Dialog
    if (showRejectDialog && requestToReview != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("পেমেন্ট রিকোয়েস্ট বাতিল করতে চান?") },
            text = {
                Column {
                    Text("বাতিল করার কারণ লিখুন (যেমন: TrxID ভুল, ব্যালেন্স আসেনি ইত্যাদি):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("বাতিলের কারণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        requestToReview?.let { req ->
                            viewModel.rejectPaymentRequest(req, rejectionReasonText.ifBlank { "তথ্য অসম্পূর্ণ বা ভুল" })
                        }
                        showRejectDialog = false
                        requestToReview = null
                        rejectionReasonText = ""
                    }
                ) {
                    Text("হ্যাঁ, বাতিল করুন", color = OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("ফিরে যান")
                }
            }
        )
    }
}

/**
 * Link 2 Interactive Portal:
 * 1. Customer's Serial-by-Serial Transaction Statement
 * 2. Shawon Tech's Official bKash/Nagad Payment Numbers & Codes
 * 3. Product Catalog with Live Availability ("এভেইলেবল" / "নট এভেইলেবল")
 */
@Composable
fun CustomerStatementAndProductPortalDialog(
    customer: CustomerEntity,
    viewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentAccounts by viewModel.paymentAccounts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: সিরিয়াল খাতা (Statement), 1: প্রডাক্ট স্টক ও এভেইলেবিলিটি (Products)
    var productSearchQuery by remember { mutableStateOf("") }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Customer specific transactions serial by serial
    val customerTransactions = remember(uiState.allTransactions, customer) {
        uiState.allTransactions.filter { tx ->
            tx.personName.trim().equals(customer.name.trim(), ignoreCase = true) ||
                    (customer.phone.isNotBlank() && tx.customerPhone == customer.phone)
        }.sortedBy { it.dateMillis } // Serial by serial order
    }

    // Totals for this customer
    val totalBill = customerTransactions.filter { it.type == "SELL" }.sumOf { if (it.totalBillAmount > 0) it.totalBillAmount else it.amount }
    val totalPaid = customerTransactions.filter { it.flowDirection == "INCOMING" }.sumOf { it.paidAmount.coerceAtLeast(it.amount) }
    val currentDue = (totalBill - totalPaid).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TechNavyDark)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Shawon Tech & Electronics",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = InflowGreen.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "ডিজিটাল খাতা ও প্রডাক্ট পোর্টাল",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF86EFAC),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "গ্রাহক: ${customer.name} | ${customer.phone.ifEmpty { "ফোন দেওয়া নেই" }}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Customer Financial Balance Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("মোট ক্রয় / বিল", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text("${BanglaUtils.formatTaka(totalBill)} ৳", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TechNavyDark)
                        }
                        Column {
                            Text("মোট পরিশোধ / জমা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text("${BanglaUtils.formatTaka(totalPaid)} ৳", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InflowGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("বর্তমান বকেয়া দেনা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text(
                                text = "${BanglaUtils.formatTaka(currentDue)} ৳",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentDue > 0) OutflowRed else InflowGreen
                            )
                        }
                    }
                }

                // Tabs: 0 -> সিরিয়াল খাতা ও পেমেন্ট নম্বর, 1 -> প্রডাক্ট স্টক ও এভেইলেবিলিটি
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("সিরিয়াল খাতা ও পেমেন্ট নম্বর", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("প্রডাক্ট স্টক ও এভেইলেবিলিটি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Tab Content
                if (activeTab == 0) {
                    // Statement + Payment Accounts Section
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Official Payment Accounts Box
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.07f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📌 আমাদের অফিসিয়াল বিকাশ ও নগদ পেমেন্ট নম্বর ও কোড:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TechNavyDark
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // bKash Accounts
                                    Text(
                                        text = "বিকাশ: 2774 (B/S), 2689 (B/S), 5822 (B/S), 8960 (B/S), 3915 (B/A), 8787 (B/P)",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD946EF),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Nagad Accounts
                                    Text(
                                        text = "নগদ: 2774 (N/S), 2689 (N/S), 6965 (N/A), 8787 (N/P)",
                                        fontSize = 11.sp,
                                        color = Color(0xFFEA580C),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "ক্যাশ টাকা: সরাসরি শোরুম বা কাউন্টারে জমা",
                                        fontSize = 11.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Serial by serial statement
                        item {
                            Text(
                                text = "সিরিয়াল বাই সিরিয়াল লেনদেন বিবরণী (${customerTransactions.size} টি):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (customerTransactions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "এই কাস্টমারের নামে কোনো পূর্বের লেনদেন রেকর্ড নেই।",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            items(customerTransactions.withIndex().toList()) { (index, tx) ->
                                val isIncoming = tx.flowDirection == "INCOMING"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Serial badge
                                        Surface(
                                            shape = CircleShape,
                                            color = TechNavyPrimary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TechNavyPrimary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = tx.dateString,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${tx.method})",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                            if (tx.quantity > 0) {
                                                Text(
                                                    text = "পরিমাণ: ${tx.quantity} পিস | প্রতি পিস: ${tx.unitPrice}৳",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                )
                                            }
                                            if (tx.description.isNotBlank()) {
                                                Text(
                                                    text = tx.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                            if (tx.senderAccount.isNotBlank() || tx.receiverAccount.isNotBlank()) {
                                                Text(
                                                    text = "প্রেরক: ${tx.senderAccount.ifEmpty { "-" }} ➔ প্রাপক: ${tx.receiverAccount.ifEmpty { "-" }}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${if (isIncoming) "+" else "-"}${BanglaUtils.formatTaka(tx.amount)} ৳",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isIncoming) InflowGreen else OutflowRed
                                            )
                                            if (tx.dueAmount > 0) {
                                                Text(
                                                    text = "বাকি: ${BanglaUtils.formatTaka(tx.dueAmount)}৳",
                                                    fontSize = 10.sp,
                                                    color = OutflowRed,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Products & Stock Availability Section
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        // Search and Add Product Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = productSearchQuery,
                                onValueChange = { productSearchQuery = it },
                                placeholder = { Text("প্রডাক্ট সার্চ করুন...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = { showAddProductDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন পণ্য", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredProducts = allProducts.filter {
                            productSearchQuery.isBlank() || it.name.contains(productSearchQuery, ignoreCase = true) ||
                                    it.category.contains(productSearchQuery, ignoreCase = true)
                        }

                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো পণ্য পাওয়া যায়নি।",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredProducts) { product ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (product.isAvailable) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            else Color(0xFFFEE2E2).copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = product.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = TechNavyPrimary.copy(alpha = 0.1f)
                                                    ) {
                                                        Text(
                                                            text = product.category,
                                                            fontSize = 10.sp,
                                                            color = TechNavyPrimary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "বিক্রয়মূল্য: ${BanglaUtils.formatTaka(product.sellPrice)} ৳ / ${product.unit} (ক্রয়: ${BanglaUtils.formatTaka(product.buyPrice)} ৳)",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                                )
                                                Text(
                                                    text = "মজুদ: ${product.stockQuantity} ${product.unit}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }

                                            // Availability Badge with Toggle
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (product.isAvailable) Color(0xFF16A34A) else Color(0xFFDC2626),
                                                modifier = Modifier.clickable {
                                                    viewModel.toggleProductAvailability(product)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (product.isAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (product.isAvailable) "এভেইলেবল" else "নট এভেইলেবল",
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
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        var newProductName by remember { mutableStateOf("") }
        var newProductCategory by remember { mutableStateOf("ইলেকট্রনিক্স") }
        var newProductBuyPrice by remember { mutableStateOf("190") }
        var newProductSellPrice by remember { mutableStateOf("230") }
        var newProductStock by remember { mutableStateOf("10") }
        var newProductAvailable by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("নতুন পণ্য যুক্ত করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProductName,
                        onValueChange = { newProductName = it },
                        label = { Text("পণ্যের নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProductCategory,
                        onValueChange = { newProductCategory = it },
                        label = { Text("ক্যাটাগরি") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newProductBuyPrice,
                            onValueChange = { newProductBuyPrice = it },
                            label = { Text("ক্রয়মূল্য (৳)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newProductSellPrice,
                            onValueChange = { newProductSellPrice = it },
                            label = { Text("বিক্রয়মূল্য (৳)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = newProductStock,
                        onValueChange = { newProductStock = it },
                        label = { Text("স্টক পরিমাণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val buy = newProductBuyPrice.toDoubleOrNull() ?: 190.0
                        val sell = newProductSellPrice.toDoubleOrNull() ?: 230.0
                        val stock = newProductStock.toIntOrNull() ?: 0
                        viewModel.addProduct(
                            name = newProductName,
                            category = newProductCategory,
                            buyPrice = buy,
                            sellPrice = sell,
                            stock = stock,
                            isAvailable = stock > 0
                        )
                        showAddProductDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
