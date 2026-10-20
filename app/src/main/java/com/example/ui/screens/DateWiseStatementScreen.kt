package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CashGreen
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.DateGroupSummary
import com.example.util.BanglaUtils

@Composable
fun DateWiseStatementScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredGroups = remember(uiState.dateGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.dateGroups
        } else {
            val q = searchQuery.trim().lowercase()
            uiState.dateGroups.mapNotNull { group ->
                val matchingItems = group.items.filter { item ->
                    item.rawCode.lowercase().contains(q) ||
                            item.senderAccount.lowercase().contains(q) ||
                            item.receiverAccount.lowercase().contains(q) ||
                            item.personName.lowercase().contains(q) ||
                            group.dateString.lowercase().contains(q)
                }
                if (matchingItems.isNotEmpty()) {
                    group.copy(
                        items = matchingItems,
                        dailyTotal = matchingItems.sumOf { it.amount }
                    )
                } else null
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("date_wise_statement_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "২. তারিখভিত্তিক লেনদেনের বিস্তারিত হিসাব",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "তারিখ, কোড / বিস্তারিত এন্ট্রি ও দৈনিক মোট",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                OutlinedButton(
                    onClick = {
                        copyReportToClipboard(context, uiState.dateGroups, uiState.dateWiseTotal)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechNavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Report",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রিপোর্ট কপি", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Summary Total Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(TechNavyPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "তারিখভিত্তিক মোট লেনদেন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${BanglaUtils.toBangla(uiState.dateGroups.size)} দিনের হিসাব নথিভুক্ত",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Text(
                        text = BanglaUtils.formatTaka(uiState.dateWiseTotal, useSymbol = true),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechNavyPrimary
                    )
                }
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("তারিখ বা কোড দিয়ে খুঁজুন (যেমন: ১০ সেপ্টেম্বর বা 2050)", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_statement_search")
            )
        }

        // Empty state
        if (filteredGroups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
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
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোন তারিখভিত্তিক হিসাব পাওয়া যায়নি",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "পণ্য ক্রয়, বিক্রয় বা লেনদেন এন্ট্রি করলে এখানে তারিখ অনুযায়ী স্বয়ংক্রিয়ভাবে সাজানো হবে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(filteredGroups, key = { it.dateString }) { group ->
                DateStatementCard(group = group)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DateStatementCard(group: DateGroupSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date Header + Daily Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TechNavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = group.dateString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = InflowGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "দৈনিক মোট: ${BanglaUtils.formatTaka(group.dailyTotal, useSymbol = true)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = InflowGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Table Entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group.items.forEach { item ->
                    val methodBadgeColor = when (item.method) {
                        PaymentMethod.BKASH.name -> BkashPink
                        PaymentMethod.NAGAD.name -> NagadOrange
                        else -> CashGreen
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Method code chip
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(methodBadgeColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (item.method == PaymentMethod.BKASH.name) "B"
                                    else if (item.method == PaymentMethod.NAGAD.name) "N"
                                    else "C",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = item.rawCode.ifEmpty {
                                        BanglaUtils.generateTransactionCode(
                                            amount = item.amount,
                                            method = PaymentMethod.fromString(item.method),
                                            sender = item.senderAccount,
                                            receiver = item.receiverAccount
                                        )
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.personName.isNotBlank() || item.description.isNotBlank()) {
                                    Text(
                                        text = listOf(item.personName, item.description).filter { it.isNotBlank() }.joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Text(
                            text = BanglaUtils.formatTaka(item.amount, useSymbol = true),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TechNavyPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun copyReportToClipboard(
    context: Context,
    groups: List<DateGroupSummary>,
    total: Double
) {
    val sb = StringBuilder()
    sb.append("Shawon Tech And Electronics\n")
    sb.append("২. তারিখভিত্তিক লেনদেনের বিস্তারিত হিসাব\n\n")

    for (g in groups) {
        sb.append("📅 ${g.dateString} (দৈনিক মোট: ${BanglaUtils.formatTaka(g.dailyTotal)})\n")
        for (item in g.items) {
            val code = item.rawCode.ifEmpty { "${BanglaUtils.formatCompactAmount(item.amount)} (${item.method})" }
            sb.append("  • $code - ${BanglaUtils.formatTaka(item.amount)}\n")
        }
        sb.append("\n")
    }

    sb.append("----------------------------\n")
    sb.append("তারিখভিত্তিক মোট: ${BanglaUtils.formatTaka(total)}\n")

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Shawon Tech Statement", sb.toString())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "রিপোর্ট ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
}
