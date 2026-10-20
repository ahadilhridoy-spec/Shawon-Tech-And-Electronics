package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import com.example.ui.theme.InflowGreen
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import com.example.ui.components.AccountManagementDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.OutflowRed
import com.example.ui.theme.TechNavyDark
import com.example.ui.theme.TechNavyPrimary
import com.example.ui.viewmodel.DashboardViewModel

enum class NavigationTab(val label: String, val icon: ImageVector, val showInBottomNav: Boolean = true) {
    DASHBOARD("ড্যাশবোর্ড", Icons.Default.Dashboard, true),
    TRANSACTIONS("লেনদেন", Icons.Default.ReceiptLong, true),
    CUSTOMERS("কাস্টমার", Icons.Default.People, true),
    LEDGER("বিকাশ ও নগদ", Icons.Default.AccountBalanceWallet, true),
    PERSONAL("ব্যক্তিভিত্তিক", Icons.Default.AccountCircle, true),
    DATE_WISE("দৈনিক খাতা", Icons.Default.CalendarMonth, false),
    ENTRY("এন্ট্রি ফর্ম", Icons.Default.AddCircle, false);
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showEntryBottomSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showLoadDemoDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAccountManagementDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val pendingRequests by viewModel.pendingPaymentRequests.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_fg),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Shawon Tech And Electronics",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (currentUser != null) "ক্লাউড সিঙ্ক সক্রিয় ☁️" else "লেনদেন ও হিসাব বিবরণী ট্র্যাকার",
                                fontSize = 11.sp,
                                color = if (currentUser != null) Color(0xFF86EFAC) else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    // Google Sign-In / Cloud Sync Quick Action
                    if (currentUser == null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { viewModel.signInWithGoogle(context) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Google Sign-In",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSigningIn) "লগইন হচ্ছে..." else "গুগল সাইন-ইন",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { showCloudSyncDialog = true },
                            modifier = Modifier.testTag("cloud_sync_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Cloud Synced",
                                tint = Color(0xFF86EFAC)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("app_overflow_menu_button")
                    ) {
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
                            text = { Text("একাউন্ট ব্যবস্থাপনা (Manage Accounts)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = TechNavyPrimary
                                )
                            },
                            onClick = {
                                showMenu = false
                                showAccountManagementDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(if (currentUser != null) "ক্লাউড সিঙ্ক অ্যাকাউন্ট" else "গুগল সাইন-ইন (Cloud Sync)")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (currentUser != null) Color(0xFF16A34A) else TechNavyPrimary
                                )
                            },
                            onClick = {
                                showMenu = false
                                if (currentUser != null) {
                                    showCloudSyncDialog = true
                                } else {
                                    viewModel.signInWithGoogle(context)
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("পিডিএফ নমুনা ডেটা লোড করুন") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = TechNavyPrimary
                                )
                            },
                            onClick = {
                                showMenu = false
                                showLoadDemoDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("সমস্ত হিসাব মুছুন (Clear All)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = OutflowRed
                                )
                            },
                            onClick = {
                                showMenu = false
                                showClearDataDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("অ্যাপ তথ্য (About)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TechNavyPrimary
                                )
                            },
                            onClick = {
                                showMenu = false
                                showAboutDialog = true
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
                onClick = { showEntryBottomSheet = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "নতুন লেনদেন এন্ট্রি",
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = "নতুন এন্ট্রি",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                containerColor = InflowGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_entry")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.entries.filter { it.showInBottomNav }.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == NavigationTab.CUSTOMERS && pendingRequests.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = OutflowRed) {
                                            Text(
                                                text = "${pendingRequests.size}",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TechNavyPrimary,
                            selectedTextColor = TechNavyPrimary,
                            indicatorColor = TechNavyPrimary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { tab ->
            when (tab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToEntry = { showEntryBottomSheet = true },
                    onNavigateToLedger = { selectedTab = NavigationTab.LEDGER },
                    onNavigateToCustomers = { selectedTab = NavigationTab.CUSTOMERS }
                )
                NavigationTab.TRANSACTIONS -> DateWiseStatementScreen(viewModel = viewModel)
                NavigationTab.CUSTOMERS -> CustomerLedgerScreen(
                    viewModel = viewModel,
                    onNavigateToEntry = { showEntryBottomSheet = true }
                )
                NavigationTab.LEDGER -> BkashNagadLedgerScreen(viewModel = viewModel)
                NavigationTab.DATE_WISE -> DateWiseStatementScreen(viewModel = viewModel)
                NavigationTab.PERSONAL -> PersonalExpenseScreen(viewModel = viewModel)
                NavigationTab.ENTRY -> EntryFormScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Bottom Sheet for Transaction Entry Form (Triggered via FAB)
    if (showEntryBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEntryBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.testTag("entry_modal_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(InflowGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = InflowGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "নতুন লেনদেন এন্ট্রি (Entry Form)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "বিক্রয়, খরচ, জমা বা উত্তোলনের হিসাব যোগ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEntryBottomSheet = false },
                        modifier = Modifier.testTag("close_entry_modal_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Sheet",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                EntryFormScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Demo Data Dialog
    if (showLoadDemoDialog) {
        AlertDialog(
            onDismissRequest = { showLoadDemoDialog = false },
            title = { Text("পিডিএফ এর নমুনা ডেটা লোড করতে চান?") },
            text = {
                Text("আপনার দেওয়া পিডিএফ স্টেটমেন্টের ১০ সেপ্টেম্বর হতে ১৯ সেপ্টেম্বরের সমস্ত বিকাশ ও নগদ লেনদেন (৭,৮৪,১৩৯ টাকা) এবং ব্যক্তিভিত্তিক খরচ (৮৬,০০০ টাকা) স্বয়ংক্রিয়ভাবে লোড হবে।")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.loadSamplePdfData()
                        showLoadDemoDialog = false
                    }
                ) {
                    Text("হ্যাঁ, লোড করুন", fontWeight = FontWeight.Bold, color = TechNavyPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoadDemoDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("সমস্ত হিসাব পরিষ্কার করতে চান?") },
            text = { Text("সমস্ত সংরক্ষিত লেনদেন মুছে ফেলা হবে। ডেটাবেস আবার সম্পূর্ণ খালি হয়ে যাবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("সব মুছুন", fontWeight = FontWeight.Bold, color = OutflowRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Shawon Tech And Electronics") },
            text = {
                Column {
                    Text("একটি আধুনিক বিজনেস ড্যাশবোর্ড ও মোবাইল ব্যাংকিং (বিকাশ/নগদ) হিসাব ট্র্যাকার।")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("বৈশিষ্ট্যসমূহ:")
                    Text("• সম্পূর্ণ খালি ফর্ম থেকে নতুন এন্ট্রি সুবিধা")
                    Text("• বিকাশ ও নগদ অ্যাকাউন্টের আগমন ও বহির্গমন বিশ্লেষণ")
                    Text("• ব্যক্তিভিত্তিক ও অন্যান্য খরচের পৃথক হিসাব")
                    Text("• তারিখভিত্তিক দৈনিক ও সর্বমোট লেনদেন রিপোর্ট")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    if (showAccountManagementDialog) {
        AccountManagementDialog(
            viewModel = viewModel,
            onDismissRequest = { showAccountManagementDialog = false }
        )
    }

    if (showCloudSyncDialog) {
        AlertDialog(
            onDismissRequest = { showCloudSyncDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ক্লাউড সিঙ্ক স্ট্যাটাস (Firebase)")
                }
            },
            text = {
                Column {
                    Text("আপনার হিসাব ডেটাবেস Firebase Firestore ক্লাউডে সংরক্ষিত ও সুরক্ষিত আছে।")
                    Spacer(modifier = Modifier.height(8.dp))
                    currentUser?.let { user ->
                        Text("ব্যবহারকারী: ${user.displayName ?: user.email ?: "লগইনকৃত ব্যবহারকারী"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        user.email?.let { email ->
                            Text("ইমেইল: $email", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.syncNow()
                            showCloudSyncDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("এখনই ক্লাউড ব্যাকআপ সিঙ্ক করুন")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showCloudSyncDialog = false
                    }
                ) {
                    Text("লগআউট", color = OutflowRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudSyncDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    if (authErrorMessage != null && currentUser == null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearAuthError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = OutflowRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("গুগল সাইন-ইন সংক্রান্ত তথ্য", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(text = authErrorMessage ?: "", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "টিপস: ডিভাইসের Settings > Google Accounts থেকে একাউন্ট যুক্ত থাকলে এক ক্লিকেই সাইন-ইন সম্পন্ন হবে।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAuthError()
                        viewModel.signInWithGoogle(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechNavyPrimary)
                ) {
                    Text("আবার চেষ্টা করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearAuthError() }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
