package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.ui.components.DashboardSummary
import com.example.ui.components.FinancialBarChart
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.GrandTotalCard
import com.example.ui.components.MethodSummaryCard
import com.example.ui.components.QuickMetricCard
import com.example.ui.components.ShawonBrandHeroBanner
import com.example.ui.theme.BkashPink
import com.example.ui.theme.BkashPinkLight
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.NagadOrangeLight
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToEntry: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Official Shawon Tech Brand Banner (Matching Logo)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ShawonBrandHeroBanner()
        }

        // 0b. Google Sign-In & Firebase Cloud Sync Card Widget
        item {
            GoogleSignInCard(viewModel = viewModel)
        }

        // Welcome and Store Subtitle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shawon Tech And Electronics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "হিসাব সারসংক্ষেপ ও রিয়েলটাইম ফাইন্যান্সিয়াল ড্যাশবোর্ড",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                Button(
                    onClick = onNavigateToEntry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                    modifier = Modifier.testTag("dashboard_new_entry_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Entry",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন এন্ট্রি", fontSize = 12.sp)
                }
            }
        }

        // 1. Dashboard Summary Card (Calculates & Displays Total Sales, Total Expenses, and Net Balance from Firestore)
        item {
            DashboardSummary(viewModel = viewModel)
        }

        // 1b. Grand Total Card (Hero Section as in PDF Summary)
        item {
            GrandTotalCard(
                grandTotal = uiState.grandTotal,
                dateWiseTotal = uiState.dateWiseTotal,
                personalTotal = uiState.personalExpensesTotal
            )
        }

        // 2. Buy vs Sell vs Expense Quick Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickMetricCard(
                    title = "মোট বিক্রি",
                    amount = uiState.totalSell,
                    icon = Icons.Default.PointOfSale,
                    iconColor = InflowGreen,
                    containerColor = InflowGreen.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )

                QuickMetricCard(
                    title = "মোট ক্রয়",
                    amount = uiState.totalBuy,
                    icon = Icons.Default.ShoppingCart,
                    iconColor = TechNavyPrimary,
                    containerColor = TechNavyPrimary.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2b. Due Summary (বাকি পাওনা ও দেনা)
        if (uiState.totalDueReceivable > 0 || uiState.totalDuePayable > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickMetricCard(
                        title = "বাকি পাওনা (গ্রাহক)",
                        amount = uiState.totalDueReceivable,
                        icon = Icons.Default.PendingActions,
                        iconColor = Color(0xFFD97706),
                        containerColor = Color(0xFFFEF3C7).copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    )

                    QuickMetricCard(
                        title = "বাকি দেনা (মহাজন)",
                        amount = uiState.totalDuePayable,
                        icon = Icons.Default.ReceiptLong,
                        iconColor = OutflowRed,
                        containerColor = OutflowRed.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Customer Ledger & 2 Portal Links Quick Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCustomers() }
                    .testTag("dashboard_customer_portal_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TechNavyPrimary,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "কাস্টমার খাতা ও ২ টি পোর্টাল লিংক",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TechNavyPrimary
                        )
                        Text(
                            text = "গ্রাহক লেজার, স্বয়ংক্রিয় পেমেন্ট রিকোয়েস্ট যাচাই ও প্রডাক্ট স্টক",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TechNavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Visual Bar Chart (Sales vs. Expenses - D3 / Recharts style)
        item {
            FinancialBarChart(
                monthlyData = uiState.monthlyComparison,
                dailyData = uiState.dailyComparison
            )
        }

        // 3. Mobile Banking Summary (bKash & Nagad)
        item {
            Text(
                text = "মোবাইল ব্যাংকিং প্রবাহ (বিকাশ ও নগদ)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MethodSummaryCard(
                    methodName = "বিকাশ লেনদেন (bKash)",
                    codePrefix = "B",
                    inflow = uiState.bkashInflow,
                    outflow = uiState.bkashOutflow,
                    balance = uiState.bkashBalance,
                    brandColor = BkashPink,
                    brandLightColor = BkashPinkLight
                )

                MethodSummaryCard(
                    methodName = "নগদ লেনদেন (Nagad)",
                    codePrefix = "N",
                    inflow = uiState.nagadInflow,
                    outflow = uiState.nagadOutflow,
                    balance = uiState.nagadBalance,
                    brandColor = NagadOrange,
                    brandLightColor = NagadOrangeLight
                )
            }
        }

        // 4. Decision Support Analytics (সিদ্ধান্ত গ্রহণে সহায়তা)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = TechNavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সিদ্ধান্ত সহায়িকা (Financial Insights)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "বিস্তারিত দেখুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TechNavyPrimary,
                            modifier = Modifier.clickable { onNavigateToLedger() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.accounts.isEmpty()) {
                        Text(
                            text = "কোন অ্যাকাউন্টে কত টাকা ঢুকলো বা বের হলো তা দেখতে নতুন লেনদেন এন্ট্রি করুন। তথ্য যুক্ত হওয়ার সাথে সাথে এখানে স্বয়ংক্রিয় বিশ্লেষণ দেখা যাবে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        // Top Receiving Account
                        val topReceiver = uiState.accounts.maxByOrNull { it.totalReceived }
                        val topSender = uiState.accounts.maxByOrNull { it.totalSent }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            topReceiver?.let { rec ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = InflowGreen.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("সর্বোচ্চ টাকা ঢুকেছে", fontSize = 11.sp, color = InflowGreen)
                                        Text(
                                            "#${rec.accountCode}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            BanglaUtils.formatTaka(rec.totalReceived, useSymbol = true),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = InflowGreen
                                        )
                                    }
                                }
                            }

                            topSender?.let { sen ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = OutflowRed.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("সর্বোচ্চ টাকা পাঠানো", fontSize = 11.sp, color = OutflowRed)
                                        Text(
                                            "#${sen.accountCode}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            BanglaUtils.formatTaka(sen.totalSent, useSymbol = true),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OutflowRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক লেনদেন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uiState.recentTransactions.isNotEmpty()) {
                    Text(
                        text = "সবগুলো দেখুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TechNavyPrimary,
                        modifier = Modifier.clickable { onNavigateToLedger() }
                    )
                }
            }
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "এখনো কোন লেনদেন নেই",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রথম এন্ট্রি করতে 'নতুন এন্ট্রি' বাটনে চাপুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(uiState.recentTransactions.take(5), key = { it.id }) { tx ->
                TransactionCard(
                    transaction = tx,
                    onDeleteClick = { viewModel.deleteTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
