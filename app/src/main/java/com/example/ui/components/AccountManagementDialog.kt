package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountScope
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.PaymentMethod
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CashGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementDialog(
    viewModel: DashboardViewModel,
    onDismissRequest: () -> Unit
) {
    val accounts by viewModel.paymentAccounts.collectAsState()
    var selectedMethodFilter by remember { mutableStateOf<String?>("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<PaymentAccountEntity?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val filteredAccounts = remember(accounts, selectedMethodFilter) {
        if (selectedMethodFilter == "ALL") accounts
        else accounts.filter { it.method == selectedMethodFilter }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("account_management_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TechNavyPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = TechNavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "পেমেন্ট একাউন্ট ও নম্বর সেটিংস",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "বিকাশ, নগদ ও ক্যাশ একাউন্ট ম্যানেজ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Add Account & Reset Defaults
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_account_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ নতুন একাউন্ট যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OutflowRed),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডিফল্ট রিস্টোর", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs (All, Bkash, Nagad, Cash)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to "সকল (${accounts.size})",
                        PaymentMethod.BKASH.name to "বিকাশ (${accounts.count { it.method == PaymentMethod.BKASH.name }})",
                        PaymentMethod.NAGAD.name to "নগদ (${accounts.count { it.method == PaymentMethod.NAGAD.name }})",
                        PaymentMethod.CASH.name to "ক্যাশ (${accounts.count { it.method == PaymentMethod.CASH.name }})"
                    ).forEach { (key, label) ->
                        val isSelected = selectedMethodFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMethodFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TechNavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account List
                if (filteredAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো একাউন্ট পাওয়া যায়নি",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAccounts, key = { it.id }) { acc ->
                            AccountCardItem(
                                account = acc,
                                onEdit = { accountToEdit = acc },
                                onDelete = { viewModel.deleteAccount(acc) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("বন্ধ করুন", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Add or Edit Account Dialog
    if (showAddDialog || accountToEdit != null) {
        AccountEditDialog(
            initialAccount = accountToEdit,
            onDismiss = {
                showAddDialog = false
                accountToEdit = null
            },
            onSave = { number, code, method, scope, note ->
                if (accountToEdit != null) {
                    val updated = accountToEdit!!.copy(
                        accountNumber = number,
                        accountCode = code,
                        method = method.name,
                        scope = scope.name,
                        note = note
                    )
                    viewModel.updateAccount(updated)
                } else {
                    viewModel.addCustomAccount(number, code, method, scope, note)
                }
                showAddDialog = false
                accountToEdit = null
            }
        )
    }

    // Reset Defaults Confirmation
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("ডিফল্ট একাউন্ট রিস্টোর করবেন?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("এতে আপনার বিকাশ, নগদ ও ক্যাশের মূল নির্ধারিত তালিকা (যেমন: 2774, 2689, 5822, 8960, 3915, 8787, 4010, 2050 ইত্যাদি) পুনরায় সেট হবে।", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreDefaultAccounts()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OutflowRed)
                ) {
                    Text("রিস্টোর করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun AccountCardItem(
    account: PaymentAccountEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val methodColor = when (account.method) {
        PaymentMethod.BKASH.name -> BkashPink
        PaymentMethod.NAGAD.name -> NagadOrange
        PaymentMethod.CASH.name -> CashGreen
        else -> TechNavyPrimary
    }

    val scopeLabel = when (account.scope) {
        AccountScope.SALE_RECEIVER.name -> "বিক্রয় জমা (+)"
        AccountScope.BUY_SENDER.name -> "ক্রয় খরচ (-)"
        AccountScope.BUY_RECEIVER.name -> "মহাজন প্রাপক"
        else -> "সবক্ষেত্রে"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Method indicator badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = methodColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (account.method) {
                                PaymentMethod.BKASH.name -> "B"
                                PaymentMethod.NAGAD.name -> "N"
                                PaymentMethod.CASH.name -> "C"
                                else -> "A"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.accountNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (account.accountCode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = methodColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "☞ ${account.accountCode}",
                                    color = methodColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scopeLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        if (account.note.isNotBlank()) {
                            Text(
                                text = "• ${account.note}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TechNavyPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = OutflowRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditDialog(
    initialAccount: PaymentAccountEntity?,
    onDismiss: () -> Unit,
    onSave: (number: String, code: String, method: PaymentMethod, scope: AccountScope, note: String) -> Unit
) {
    var number by remember { mutableStateOf(initialAccount?.accountNumber ?: "") }
    var code by remember { mutableStateOf(initialAccount?.accountCode ?: "B/S") }
    var selectedMethod by remember {
        mutableStateOf(
            initialAccount?.let { PaymentMethod.fromString(it.method) } ?: PaymentMethod.BKASH
        )
    }
    var selectedScope by remember {
        mutableStateOf(
            initialAccount?.let {
                try { AccountScope.valueOf(it.scope) } catch (_: Exception) { AccountScope.ALL }
            } ?: AccountScope.ALL
        )
    }
    var note by remember { mutableStateOf(initialAccount?.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialAccount != null) "একাউন্ট এডিট করুন" else "নতুন পেমেন্ট একাউন্ট যোগ করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Number
                Text("একাউন্ট নম্বর / কোড *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        errorMessage = null
                    },
                    placeholder = { Text("যেমন: 2774, 5822, Cash", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Method
                Text("পেমেন্ট মাধ্যম", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(PaymentMethod.BKASH, PaymentMethod.NAGAD, PaymentMethod.CASH).forEach { m ->
                        val isSelected = selectedMethod == m
                        val col = when (m) {
                            PaymentMethod.BKASH -> BkashPink
                            PaymentMethod.NAGAD -> NagadOrange
                            PaymentMethod.CASH -> CashGreen
                            else -> TechNavyPrimary
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) col else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMethod = m
                                    if (m == PaymentMethod.BKASH && (code.startsWith("N") || code == "ক্যাশ")) code = "B/S"
                                    if (m == PaymentMethod.NAGAD && (code.startsWith("B") || code == "ক্যাশ")) code = "N/S"
                                    if (m == PaymentMethod.CASH) code = "ক্যাশ"
                                }
                        ) {
                            Text(
                                text = m.banglaLabel,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Code / Type
                Text("একাউন্ট ধরন / টাইপ কোড", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = { Text("যেমন: B/S, B/A, B/P, N/S, N/A, N/P, ক্যাশ", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick code selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val codeChips = when (selectedMethod) {
                        PaymentMethod.BKASH -> listOf("B/S", "B/A", "B/P")
                        PaymentMethod.NAGAD -> listOf("N/S", "N/A", "N/P")
                        PaymentMethod.CASH -> listOf("ক্যাশ", "কাউন্টার")
                        else -> listOf("Bank")
                    }
                    codeChips.forEach { c ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (code == c) TechNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { code = c }
                        ) {
                            Text(
                                text = c,
                                color = if (code == c) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Scope
                Text("কোথায় ব্যবহার হবে?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        AccountScope.SALE_RECEIVER to "বিক্রয়ের জমা / প্রাপক (+)",
                        AccountScope.BUY_SENDER to "ক্রয়ের খরচ / প্রেরক (-)",
                        AccountScope.BUY_RECEIVER to "ক্রয়ের প্রাপক (মহাজন)",
                        AccountScope.ALL to "সবক্ষেত্রে প্রযোজ্য (উভয়)"
                    ).forEach { (sc, scLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedScope = sc }
                        ) {
                            RadioButton(
                                selected = selectedScope == sc,
                                onClick = { selectedScope = sc },
                                colors = RadioButtonDefaults.colors(selectedColor = TechNavyPrimary)
                            )
                            Text(scLabel, fontSize = 11.sp)
                        }
                    }
                }

                // Note
                Text("বিবরণ / নোট (ঐচ্ছিক)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("যেমন: দোকান বিকাশ, এজেন্ট সিম", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = OutflowRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (number.isBlank()) {
                        errorMessage = "দয়া করে একাউন্ট নম্বর লিখুন"
                    } else {
                        onSave(number, code, selectedMethod, selectedScope, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
