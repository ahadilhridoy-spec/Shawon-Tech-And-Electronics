package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.MonthlyFinancialData
import com.example.util.BanglaUtils

@Composable
fun FinancialBarChart(
    monthlyData: List<MonthlyFinancialData>,
    dailyData: List<MonthlyFinancialData>,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf("MONTHLY") } // "MONTHLY" or "DAILY"
    var selectedItem by remember { mutableStateOf<MonthlyFinancialData?>(null) }

    val activeList = if (viewMode == "MONTHLY") monthlyData else dailyData

    // Find maximum value to scale the bars proportionally (D3 scaleLinear logic)
    val maxValue = remember(activeList) {
        val maxVal = activeList.maxOfOrNull { maxOf(it.sales, it.expenses) } ?: 1.0
        if (maxVal <= 0.0) 1.0 else maxVal
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_bar_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header & View Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TechNavyPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Chart",
                            tint = TechNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "বিক্রয় বনাম খরচ তুলনা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sales vs. Expenses Visual Analytics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Switcher pill (Monthly vs Daily)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        val isMonthly = viewMode == "MONTHLY"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMonthly) TechNavyPrimary else Color.Transparent,
                            modifier = Modifier.clickable {
                                viewMode = "MONTHLY"
                                selectedItem = null
                            }
                        ) {
                            Text(
                                text = "মাসিক",
                                fontSize = 11.sp,
                                fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Normal,
                                color = if (isMonthly) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isMonthly) TechNavyPrimary else Color.Transparent,
                            modifier = Modifier.clickable {
                                viewMode = "DAILY"
                                selectedItem = null
                            }
                        ) {
                            Text(
                                text = "দৈনিক",
                                fontSize = 11.sp,
                                fontWeight = if (!isMonthly) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isMonthly) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-style Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sales Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(InflowGreen, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "মোট বিক্রয় (Sales)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                // Expenses Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(OutflowRed, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "মোট খরচ (Expenses)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bar Chart Area
            if (activeList.isEmpty() || maxValue <= 1.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "চার্ট প্রদর্শনের জন্য লেনদেন এন্ট্রি করুন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                // Interactive Scrollable Bar Chart Canvas Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                ) {
                    // Background reference gridlines
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.78f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(4) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 0.8.dp
                            )
                        }
                    }

                    // Bars Container
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        activeList.forEach { item ->
                            val isSelected = selectedItem == item
                            BarGroupItem(
                                data = item,
                                maxValue = maxValue,
                                isSelected = isSelected,
                                onClick = {
                                    selectedItem = if (isSelected) null else item
                                }
                            )
                        }
                    }
                }
            }

            // Recharts-style Tooltip / Selection Card
            selectedItem?.let { item ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TechNavyDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সময়কাল: ${item.periodLabel}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            val marginPercent = if (item.sales > 0) {
                                ((item.netMargin / item.sales) * 100).toInt()
                            } else 0
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (item.netMargin >= 0) InflowGreen else OutflowRed
                            ) {
                                Text(
                                    text = "মার্জিন: ${BanglaUtils.toBangla(marginPercent)}%",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("মোট বিক্রয়", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    BanglaUtils.formatTaka(item.sales, useSymbol = true),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InflowGreen
                                )
                            }

                            Column {
                                Text("মোট খরচ", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    BanglaUtils.formatTaka(item.expenses, useSymbol = true),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OutflowRed
                                )
                            }

                            Column {
                                Text("নিট লাভ / ব্যালেন্স", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    BanglaUtils.formatTaka(item.netMargin, useSymbol = true),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.netMargin >= 0) Color(0xFFFDE68A) else OutflowRed
                                )
                            }
                        }
                    }
                }
            }

            // Financial Insight Note
            if (activeList.isNotEmpty() && maxValue > 1.0) {
                Spacer(modifier = Modifier.height(14.dp))
                val totalSales = activeList.sumOf { it.sales }
                val totalExp = activeList.sumOf { it.expenses }
                val overallMargin = totalSales - totalExp
                val expenseRatio = if (totalSales > 0) ((totalExp / totalSales) * 100).toInt() else 0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TechNavyPrimary.copy(alpha = 0.06f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(GoldAccent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "সিদ্ধান্ত সহায়িকা (Decision Insight):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TechNavyPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val comment = if (overallMargin >= 0) {
                                "বিক্রির তুলনায় খরচ ${BanglaUtils.toBangla(expenseRatio)}%। ব্যবসায়ের আর্থিক নগদ প্রবাহ ও নিট মার্জিন ইতিবাচক।"
                            } else {
                                "বিক্রির চেয়ে খরচের পরিমাণ বেশি। খরচ নিয়ন্ত্রণে দ্রুত সিদ্ধান্ত প্রয়োজন।"
                            }
                            Text(
                                text = comment,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarGroupItem(
    data: MonthlyFinancialData,
    maxValue: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Normalizing height between 10dp and 130dp
    val maxBarHeight = 130f
    val salesHeightFraction = ((data.sales / maxValue).coerceIn(0.05, 1.0)).toFloat()
    val expensesHeightFraction = ((data.expenses / maxValue).coerceIn(0.05, 1.0)).toFloat()

    val animSalesHeight by animateFloatAsState(
        targetValue = maxBarHeight * salesHeightFraction,
        animationSpec = tween(durationMillis = 600),
        label = "sales_anim"
    )

    val animExpHeight by animateFloatAsState(
        targetValue = maxBarHeight * expensesHeightFraction,
        animationSpec = tween(durationMillis = 600),
        label = "exp_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        // Paired Bars Row
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(140.dp)
        ) {
            // Sales Bar (Green)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isSelected || data.sales > 0) {
                    Text(
                        text = BanglaUtils.formatCompactAmount(data.sales),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = InflowGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(animSalesHeight.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF34D399), InflowGreen)
                            ),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }

            // Expenses Bar (Red/Rose)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isSelected || data.expenses > 0) {
                    Text(
                        text = BanglaUtils.formatCompactAmount(data.expenses),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = OutflowRed
                    )
                }
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(animExpHeight.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFB7185), OutflowRed)
                            ),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Period Label
        Text(
            text = data.periodLabel,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TechNavyPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(60.dp),
            maxLines = 1
        )
    }
}
