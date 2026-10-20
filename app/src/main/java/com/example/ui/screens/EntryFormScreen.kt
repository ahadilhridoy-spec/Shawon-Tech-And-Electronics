package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountScope
import com.example.data.model.FlowDirection
import com.example.data.model.PaymentAccountEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType
import com.example.ui.components.AccountEditDialog
import com.example.ui.components.AccountManagementDialog
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CashGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils
import java.util.Calendar

@Composable
fun EntryFormScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showQuickCodeBox by remember { mutableStateOf(false) }
    val allAccounts by viewModel.paymentAccounts.collectAsState()
    var showAccountManagementDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var receiverMenuExpanded by remember { mutableStateOf(false) }
    var senderMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("entry_form_screen")
    ) {
        // Form Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "পণ্য ক্রয়, বিক্রয় ও খরচ এন্ট্রি ফর্ম",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "সঠিক ও নির্ভুল তথ্য লিখুন (ফর্মটি নতুন ইনপুটের জন্য প্রস্তুত)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            IconButton(
                onClick = { showQuickCodeBox = !showQuickCodeBox },
                modifier = Modifier
                    .background(TechNavyPrimary.copy(alpha = 0.1f), CircleShape)
                    .testTag("toggle_quick_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Quick Code Parser",
                    tint = TechNavyPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Success / Error notification
        AnimatedVisibility(visible = formState.successMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = InflowGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = InflowGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formState.successMessage ?: "",
                        color = InflowGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        AnimatedVisibility(visible = formState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = OutflowRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = OutflowRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formState.errorMessage ?: "",
                        color = OutflowRed,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Quick Code Parser Box (Collapsible)
        AnimatedVisibility(visible = showQuickCodeBox) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.06f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "স্মার্ট কোড পার্সার (PDF / মেসেজ থেকে এন্ট্রি)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TechNavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "উদাহরণ: 30K (B 915/689) বা 12.5K (B 2050/5822) বা 9.5K (N 4010/689)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = formState.quickCodeInput,
                            onValueChange = { viewModel.updateQuickCodeInput(it) },
                            placeholder = { Text("যেমন: 30K (B 915/689)", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_code_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.applyQuickCode(formState.quickCodeInput) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                            modifier = Modifier.testTag("apply_quick_code_button")
                        ) {
                            Text("বসাও", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 1. Transaction Type Selector
        Text(
            text = "লেনদেনের ধরন (Transaction Type)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionType.entries.forEach { type ->
                val isSelected = formState.selectedType == type
                val chipColor = when (type) {
                    TransactionType.SELL -> InflowGreen
                    TransactionType.BUY -> TechNavyPrimary
                    TransactionType.EXPENSE -> OutflowRed
                    TransactionType.PERSONAL_EXPENSE -> Color(0xFFD97706)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) chipColor else MaterialTheme.colorScheme.surface,
                    border = borderIf(!isSelected, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clickable { viewModel.updateType(type) }
                        .testTag("type_${type.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = type.banglaLabel,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Date Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "তারিখ (Date)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Today chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TechNavyPrimary.copy(alpha = 0.1f),
                modifier = Modifier.clickable { viewModel.setDateToToday() }
            ) {
                Text(
                    text = "আজকের তারিখ দিন",
                    fontSize = 11.sp,
                    color = TechNavyPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = formState.dateString,
            onValueChange = { viewModel.updateDate(it) },
            placeholder = { Text("তারিখ লিখুন বা ক্যালেন্ডার থেকে বাছুন", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Date",
                    tint = TechNavyPrimary
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCal = Calendar.getInstance()
                            selectedCal.set(year, month, dayOfMonth)
                            val banglaStr = BanglaUtils.formatDateToBangla(selectedCal.timeInMillis)
                            viewModel.updateDate(banglaStr, selectedCal.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pick Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("date_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2.5. Product Quantity & Unit Price (পিস ও দর ক্যালকুলেটর)
        val isProductTrade = formState.selectedType == TransactionType.SELL || formState.selectedType == TransactionType.BUY
        if (isProductTrade) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (formState.selectedType == TransactionType.SELL) "বিক্রয় পণ্যের পিস ও দর (ডিফল্ট: ২৩০৳)" else "ক্রয় পণ্যের পিস ও দর (ডিফল্ট: ১৯০৳)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechNavyPrimary
                        )
                        Text(
                            text = "দর পরিবর্তনযোগ্য",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পিস সংখ্যা (Quantity)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.quantityInput,
                                onValueChange = { viewModel.updateQuantity(it) },
                                placeholder = { Text("যেমন: 10, 50...", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quantity_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "প্রতি পিসের দর ৳ (Unit Price)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.unitPriceInput,
                                onValueChange = { viewModel.updateUnitPrice(it) },
                                placeholder = { Text(if (formState.selectedType == TransactionType.SELL) "230" else "190", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unit_price_input")
                            )
                        }
                    }

                    // Quick piece chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("দ্রুত পিস যোগ:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        listOf(5, 10, 25, 50, 100).forEach { qty ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier.clickable {
                                    val current = formState.quantityInput.toIntOrNull() ?: 0
                                    viewModel.updateQuantity((current + qty).toString())
                                }
                            ) {
                                Text("+$qty পিস", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Payment Mode & Amount (Full vs Partial Cash & Due)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (formState.hasDue) GoldAccent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আংশিক নগদ পেমেন্ট এবং আংশিক বাকি?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (formState.hasDue) "নগদ পরিশোধ ও বাকি আলাদাভাবে নথিভুক্ত হবে" else "সম্পূর্ণ নগদ বা এককালীন পরিশোধ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }

                    Switch(
                        checked = formState.hasDue,
                        onCheckedChange = { viewModel.toggleHasDue(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldAccent,
                            checkedTrackColor = GoldAccent.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("partial_due_switch")
                    )
                }

                if (formState.hasDue) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 1: Total Bill Amount
                    Text(
                        text = "মোট বিল / পণ্যের মোট মূল্য (Total Bill ৳) *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formState.totalBillInput,
                        onValueChange = { viewModel.updateTotalBill(it) },
                        placeholder = { Text("যেমন: 10000", fontSize = 13.sp) },
                        leadingIcon = {
                            Text("৳", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TechNavyPrimary, modifier = Modifier.padding(start = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("total_bill_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Field 2 & 3: Paid Cash + Due Amount Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "আংশিক নগদ পরিশোধ ৳",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InflowGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.amountInput,
                                onValueChange = { viewModel.updateAmount(it) },
                                placeholder = { Text("পরিশোধিত", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("partial_paid_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "আংশিক বাকি টাকা ৳",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OutflowRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.dueAmountInput,
                                onValueChange = { viewModel.updateDueAmount(it) },
                                placeholder = { Text("বাকি", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("partial_due_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Due Ratio Chips
                    val currentBill = formState.totalBillInput.toDoubleOrNull() ?: 0.0
                    if (currentBill > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    val half = currentBill / 2.0
                                    val str = if (half % 1.0 == 0.0) half.toLong().toString() else half.toString()
                                    viewModel.updateAmount(str)
                                }
                            ) {
                                Text("৫০% নগদ / ৫০% বাকি", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    viewModel.updateAmount("0")
                                }
                            ) {
                                Text("সম্পূর্ণ বাকি (০ পরিশোধ)", fontSize = 10.sp, color = OutflowRed, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Field 4: Customer/Party Phone & Due Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মোবাইল নম্বর",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.customerPhoneInput,
                                onValueChange = { viewModel.updateCustomerPhone(it) },
                                placeholder = { Text("017...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = TechNavyPrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বাকি পরিশোধের তারিখ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.dueDateInput,
                                onValueChange = { viewModel.updateDueDate(it) },
                                placeholder = { Text("যেমন: ২৫ অক্টোবর", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Standard Single Amount Input
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "টাকার পরিমাণ (Amount ৳) *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formState.amountInput,
                        onValueChange = { viewModel.updateAmount(it) },
                        placeholder = { Text("টাকার অংক লিখুন (যেমন: 30000 বা 24500)", fontSize = 13.sp) },
                        leadingIcon = {
                            Text(
                                text = "৳",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechNavyPrimary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        isError = formState.errorMessage != null && formState.amountInput.isBlank()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    // Quick Amount Add Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1000.0 to "+১K", 5000.0 to "+৫K", 10000.0 to "+১০K", 20000.0 to "+২০K", 50000.0 to "+৫০K").forEach { (amt, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { viewModel.addAmount(amt) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Payment Method
        Text(
            text = "পেমেন্ট মাধ্যম (Payment Method)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethod.entries.forEach { method ->
                val isSelected = formState.selectedMethod == method
                val brandColor = when (method) {
                    PaymentMethod.BKASH -> BkashPink
                    PaymentMethod.NAGAD -> NagadOrange
                    PaymentMethod.CASH -> CashGreen
                    PaymentMethod.BANK -> TechNavyPrimary
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) brandColor else MaterialTheme.colorScheme.surface,
                    border = borderIf(!isSelected, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.updateMethod(method) }
                        .testTag("method_${method.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = method.banglaLabel,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${method.codePrefix})",
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Flow Direction (জমা vs খরচ)
        Text(
            text = "টাকার প্রবাহ (Flow Direction)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FlowDirection.entries.forEach { flow ->
                val isSelected = formState.selectedFlow == flow
                val flowColor = if (flow.isIncoming) InflowGreen else OutflowRed

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) flowColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) flowColor else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.updateFlow(flow) }
                        .testTag("flow_${flow.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (flow.isIncoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = flowColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = flow.banglaLabel,
                            color = if (isSelected) flowColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Sender & Receiver Numbers / Accounts
        val isIncoming = formState.selectedFlow.isIncoming || formState.selectedType == TransactionType.SELL

        // Filter relevant accounts by scope
        val receiverAccounts = remember(allAccounts, isIncoming) {
            if (isIncoming) {
                allAccounts.filter { it.scope == AccountScope.SALE_RECEIVER.name || it.scope == AccountScope.ALL.name }
            } else {
                allAccounts.filter { it.scope == AccountScope.BUY_RECEIVER.name || it.scope == AccountScope.ALL.name }
            }
        }

        val senderAccounts = remember(allAccounts, isIncoming) {
            if (!isIncoming) {
                allAccounts.filter { it.scope == AccountScope.BUY_SENDER.name || it.scope == AccountScope.ALL.name }
            } else {
                allAccounts.filter { it.scope == AccountScope.BUY_SENDER.name || it.scope == AccountScope.ALL.name }
            }
        }

        // Quick chips matching currently selected payment method
        val currentMethodReceiverChips = remember(receiverAccounts, formState.selectedMethod) {
            val matched = receiverAccounts.filter { it.method == formState.selectedMethod.name }
            if (matched.isNotEmpty()) matched else receiverAccounts
        }

        val currentMethodSenderChips = remember(senderAccounts, formState.selectedMethod) {
            val matched = senderAccounts.filter { it.method == formState.selectedMethod.name }
            if (matched.isNotEmpty()) matched else senderAccounts
        }

        // Section: Receiver Account (প্রাপক নম্বর / কোড)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isIncoming) InflowGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "প্রাপক নম্বর / কোড (Receiver)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isIncoming) InflowGreen.copy(alpha = 0.2f) else TechNavyPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isIncoming) "দোকানে জমা (+)" else "মহাজন/সাপ্লায়ার",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncoming) InflowGreen else TechNavyPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { receiverMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("মেনু তালিকা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { showAccountManagementDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(16.dp), tint = TechNavyPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formState.receiverInput,
                        onValueChange = { viewModel.updateReceiver(it) },
                        placeholder = {
                            Text(
                                if (isIncoming) "মেনু বা চিপ থেকে একাউন্ট বাছুন (যেমন: 2774, 2689...)"
                                else "যেমন: 4010, 2050...",
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { receiverMenuExpanded = !receiverMenuExpanded },
                                modifier = Modifier.testTag("receiver_dropdown_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown Menu")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receiver_input")
                    )

                    DropdownMenu(
                        expanded = receiverMenuExpanded,
                        onDismissRequest = { receiverMenuExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("receiver_dropdown_menu")
                    ) {
                        Text(
                            text = if (isIncoming) "— বিক্রয়ের প্রাপক একাউন্টসমূহ (জমা) —" else "— ক্রয়ের প্রাপক একাউন্টসমূহ (মহাজন) —",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechNavyPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        receiverAccounts.forEach { acc ->
                            val mColor = when (acc.method) {
                                PaymentMethod.BKASH.name -> BkashPink
                                PaymentMethod.NAGAD.name -> NagadOrange
                                PaymentMethod.CASH.name -> CashGreen
                                else -> TechNavyPrimary
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = mColor,
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = acc.method.take(1),
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = acc.displayLabel,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        if (acc.note.isNotBlank()) {
                                            Text(
                                                text = acc.note,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectReceiverAccount(acc)
                                    receiverMenuExpanded = false
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ নতুন একাউন্ট যোগ করুন",
                                    color = TechNavyPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TechNavyPrimary, modifier = Modifier.size(16.dp))
                            },
                            onClick = {
                                receiverMenuExpanded = false
                                showAddAccountDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "⚙️ সকল একাউন্ট ম্যানেজ করুন",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            onClick = {
                                receiverMenuExpanded = false
                                showAccountManagementDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Chips Row for Receiver
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("দ্রুত নির্বাচন:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                    currentMethodReceiverChips.forEach { acc ->
                        val isSelected = formState.receiverInput == acc.accountNumber
                        val brandColor = when (acc.method) {
                            PaymentMethod.BKASH.name -> BkashPink
                            PaymentMethod.NAGAD.name -> NagadOrange
                            PaymentMethod.CASH.name -> CashGreen
                            else -> TechNavyPrimary
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) brandColor else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) brandColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.clickable { viewModel.selectReceiverAccount(acc) }
                        ) {
                            Text(
                                text = acc.displayLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // + Add Account Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { showAddAccountDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = TechNavyPrimary)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("+ নতুন", fontSize = 11.sp, color = TechNavyPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section: Sender Account (প্রেরক নম্বর / কোড)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (!isIncoming) OutflowRed.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "প্রেরক নম্বর / কোড (Sender)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isIncoming) OutflowRed.copy(alpha = 0.15f) else TechNavyPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (!isIncoming) "আমাদের পেমেন্ট (-)" else "গ্রাহক / কাস্টমার",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isIncoming) OutflowRed else TechNavyPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = { senderMenuExpanded = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("মেনু তালিকা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formState.senderInput,
                        onValueChange = { viewModel.updateSender(it) },
                        placeholder = {
                            Text(
                                if (!isIncoming) "মেনু বা চিপ থেকে একাউন্ট বাছুন (যেমন: 2774, 915...)"
                                else "গ্রাহকের নম্বর (যেমন: 017... বা 2050)",
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { senderMenuExpanded = !senderMenuExpanded },
                                modifier = Modifier.testTag("sender_dropdown_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown Menu")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sender_input")
                    )

                    DropdownMenu(
                        expanded = senderMenuExpanded,
                        onDismissRequest = { senderMenuExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("sender_dropdown_menu")
                    ) {
                        Text(
                            text = if (!isIncoming) "— ক্রয়ের প্রেরক একাউন্টসমূহ (খরচ) —" else "— প্রেরক একাউন্ট তালিকা —",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechNavyPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        senderAccounts.forEach { acc ->
                            val mColor = when (acc.method) {
                                PaymentMethod.BKASH.name -> BkashPink
                                PaymentMethod.NAGAD.name -> NagadOrange
                                PaymentMethod.CASH.name -> CashGreen
                                else -> TechNavyPrimary
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = mColor,
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = acc.method.take(1),
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = acc.displayLabel,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        if (acc.note.isNotBlank()) {
                                            Text(
                                                text = acc.note,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectSenderAccount(acc)
                                    senderMenuExpanded = false
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ নতুন একাউন্ট যোগ করুন",
                                    color = TechNavyPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TechNavyPrimary, modifier = Modifier.size(16.dp))
                            },
                            onClick = {
                                senderMenuExpanded = false
                                showAddAccountDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Chips Row for Sender
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("দ্রুত নির্বাচন:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                    currentMethodSenderChips.forEach { acc ->
                        val isSelected = formState.senderInput == acc.accountNumber
                        val brandColor = when (acc.method) {
                            PaymentMethod.BKASH.name -> BkashPink
                            PaymentMethod.NAGAD.name -> NagadOrange
                            PaymentMethod.CASH.name -> CashGreen
                            else -> TechNavyPrimary
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) brandColor else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) brandColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.clickable { viewModel.selectSenderAccount(acc) }
                        ) {
                            Text(
                                text = acc.displayLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // + Add Account Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { showAddAccountDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = TechNavyPrimary)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("+ নতুন", fontSize = 11.sp, color = TechNavyPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Person / Entity Name (কার টাকা)
        Text(
            text = "কার টাকা / ব্যক্তি / পার্টির নাম (Person / Party)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = formState.personInput,
            onValueChange = { viewModel.updatePerson(it) },
            placeholder = { Text("যেমন: রানা ভাই, মিরাজ ভাই, কাস্টমার...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TechNavyPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("person_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Description / Notes
        Text(
            text = "বিবরণ / পণ্যের বিবরণ (Description / Notes)",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = formState.descriptionInput,
            onValueChange = { viewModel.updateDescription(it) },
            placeholder = { Text("যেমন: Carry Cost, ক্যাবল, ডিসপ্লে ইত্যাদি...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = TechNavyPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("description_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 9. Live Code Preview (matches statement style)
        val previewAmt = formState.amountInput.toDoubleOrNull() ?: 0.0
        val generatedCode = BanglaUtils.generateTransactionCode(
            amount = previewAmt,
            method = formState.selectedMethod,
            sender = formState.senderInput,
            receiver = formState.receiverInput
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = TechNavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "স্টেটমেন্ট কোড প্রিভিউ:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = generatedCode,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TechNavyPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearForm() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("reset_form_button")
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("রিসেট", fontSize = 14.sp)
            }

            Button(
                onClick = { viewModel.saveTransaction() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .testTag("submit_transaction_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("লেনদেন সংরক্ষণ করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAccountManagementDialog) {
        AccountManagementDialog(
            viewModel = viewModel,
            onDismissRequest = { showAccountManagementDialog = false }
        )
    }

    if (showAddAccountDialog) {
        AccountEditDialog(
            initialAccount = null,
            onDismiss = { showAddAccountDialog = false },
            onSave = { number, code, method, scope, note ->
                viewModel.addCustomAccount(number, code, method, scope, note)
                showAddAccountDialog = false
            }
        )
    }
}

private fun borderIf(condition: Boolean, color: Color) =
    if (condition) androidx.compose.foundation.BorderStroke(1.dp, color) else null
