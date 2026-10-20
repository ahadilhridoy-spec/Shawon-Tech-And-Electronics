package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InflowGreen
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.BanglaUtils
import kotlinx.coroutines.launch

/**
 * DashboardSummary component that calculates and displays:
 * 1. Total Sales (মোট বিক্রয়)
 * 2. Total Expenses (মোট খরচ)
 * 3. Net Balance (নেট উদ্বৃত্ত / ব্যালেন্স)
 *
 * Source: Firestore real-time cloud data with fallback to local database
 * when Google Sign-In is pending.
 */
@Composable
fun DashboardSummary(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val firestoreData by viewModel.firestoreSummary.collectAsState()
    val localUiState by viewModel.uiState.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    val scope = rememberCoroutineScope()

    // When signed in with Google, use live calculated Firestore data.
    // Otherwise fallback to current local database state with sign-in prompt.
    val isCloudConnected = currentUser != null && firestoreData.isFromCloud
    val totalSales = if (isCloudConnected) firestoreData.totalSales else localUiState.totalSell
    val totalExpenses = if (isCloudConnected) firestoreData.totalExpenses else localUiState.totalExpense
    val netBalance = totalSales - totalExpenses
    val count = if (isCloudConnected) firestoreData.transactionCount else localUiState.allTransactions.size

    DashboardSummaryCardLayout(
        totalSales = totalSales,
        totalExpenses = totalExpenses,
        netBalance = netBalance,
        transactionCount = count,
        isFromCloud = isCloudConnected,
        isLoading = firestoreData.isLoading || isSigningIn,
        errorMessage = if (currentUser == null) "গুগল সাইন-ইন করলে সরাসরি ফায়ারবেস ক্লাউড থেকে ক্যালকুলেশন হবে" else firestoreData.error,
        onRefresh = {
            viewModel.refreshFirestoreSummary()
        },
        onSignInClick = {
            viewModel.signInWithGoogle(context)
        },
        modifier = modifier
    )
}

/**
 * Standalone overload of DashboardSummary taking explicit financial figures.
 */
@Composable
fun DashboardSummary(
    totalSales: Double,
    totalExpenses: Double,
    netBalance: Double = totalSales - totalExpenses,
    transactionCount: Int = 0,
    isFromCloud: Boolean = true,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DashboardSummaryCardLayout(
        totalSales = totalSales,
        totalExpenses = totalExpenses,
        netBalance = netBalance,
        transactionCount = transactionCount,
        isFromCloud = isFromCloud,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRefresh = onRefresh,
        onSignInClick = {},
        modifier = modifier
    )
}

@Composable
private fun DashboardSummaryCardLayout(
    totalSales: Double,
    totalExpenses: Double,
    netBalance: Double,
    transactionCount: Int,
    isFromCloud: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title & Cloud Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (isFromCloud) TechNavyPrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFromCloud) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = "Cloud Status",
                            tint = if (isFromCloud) TechNavyPrimary else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ড্যাশবোর্ড সামারি",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isFromCloud) InflowGreen.copy(alpha = 0.15f) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = if (isFromCloud) "Firestore Live" else "Local / Cloud Sync",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromCloud) InflowGreen else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "মোট বিক্রয়, খরচ ও নেট ব্যালেন্স হিসাব",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("summary_refresh_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = TechNavyPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = TechNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Net Balance Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (netBalance >= 0) {
                                listOf(TechNavyDark, TechNavyPrimary, Color(0xFF1E3A8A))
                            } else {
                                listOf(Color(0xFF7F1D1D), OutflowRed, Color(0xFFB91C1C))
                            }
                        )
                    )
                    .padding(18.dp)
                    .testTag("summary_net_balance")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (netBalance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (netBalance >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (netBalance >= 0) "নেট লাভ / উদ্বৃত্ত (Net Balance)" else "নেট ঘাটতি (Net Deficit)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (netBalance >= 0) "লাভজনক (+)" else "ঘাটতি (-)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netBalance >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = BanglaUtils.formatTaka(netBalance, useSymbol = true),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "সূত্র: মোট বিক্রয় (${BanglaUtils.formatTaka(totalSales, useSymbol = true)}) - মোট খরচ (${BanglaUtils.formatTaka(totalExpenses, useSymbol = true)})",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Cards: Total Sales & Total Expenses side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Sales Summary Card
                SummaryMetricSubCard(
                    title = "মোট বিক্রয়",
                    englishSubtitle = "Total Sales",
                    amount = totalSales,
                    prefix = "+",
                    icon = Icons.Default.PointOfSale,
                    accentColor = InflowGreen,
                    backgroundColor = InflowGreen.copy(alpha = 0.08f),
                    testTag = "summary_total_sales",
                    modifier = Modifier.weight(1f)
                )

                // Total Expenses Summary Card
                SummaryMetricSubCard(
                    title = "মোট খরচ",
                    englishSubtitle = "Total Expenses",
                    amount = totalExpenses,
                    prefix = "-",
                    icon = Icons.Default.TrendingDown,
                    accentColor = OutflowRed,
                    backgroundColor = OutflowRed.copy(alpha = 0.08f),
                    testTag = "summary_total_expenses",
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Footer: Counts & Firestore status
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "মোট লেনদেন নথি: ${BanglaUtils.toBangla(transactionCount)} টি",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }

                if (!isFromCloud) {
                    Text(
                        text = "গুগল সাইন-ইন করুন ☁️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechNavyPrimary,
                        modifier = Modifier.clickable { onSignInClick() }
                    )
                } else {
                    Text(
                        text = "Firestore সিঙ্কড ✓",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InflowGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricSubCard(
    title: String,
    englishSubtitle: String,
    amount: Double,
    prefix: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = prefix,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = englishSubtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = BanglaUtils.formatTaka(amount, useSymbol = true),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}
