package com.example.foodpanda_rider.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodpanda_rider.R
import com.example.foodpanda_rider.data.repository.AuthSessionState
import com.example.foodpanda_rider.model.Order
import com.example.foodpanda_rider.ui.components.CustomTextButton
import com.example.foodpanda_rider.ui.components.CustomTextField
import com.example.foodpanda_rider.ui.components.MyAlertDialog
import com.example.foodpanda_rider.ui.components.OrderCard
import kotlinx.coroutines.launch
import java.util.Locale

private enum class DrawerModalType {
    NONE, EARNINGS, RATING, ACCOUNT, SETTINGS, TERMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authState: AuthSessionState,
    availableOrders: List<Order>,
    currentOrders: List<Order>,
    deliveredOrders: List<Order>,
    onPickOrder: (Order) -> Unit,
    onOpenOrderHistory: () -> Unit,
    onCreateIncomingOrder: () -> Unit,
    onResetSampleOrders: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onToggleApproval: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    var tabChoice by rememberSaveable { mutableStateOf("All Order") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var activeModal by remember { mutableStateOf(DrawerModalType.NONE) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val primary = MaterialTheme.colorScheme.primary

    val displayedOrders = if (tabChoice == "All Order") availableOrders else currentOrders

    if (showLogoutDialog) {
        MyAlertDialog(
            title = "Logging out?",
            subtitle = "Thanks for stopping by. See you again soon!",
            action1Name = "Cancel",
            action2Name = "Log out",
            onAction1 = { showLogoutDialog = false },
            onAction2 = {
                showLogoutDialog = false
                onSignOut()
            }
        )
    }

    when (activeModal) {
        DrawerModalType.EARNINGS -> {
            val totalEarned = deliveredOrders.sumOf { it.deliveryPrice }
            AlertDialog(
                onDismissRequest = { activeModal = DrawerModalType.NONE },
                containerColor = Color.White,
                title = {
                    Text("Rider Earnings", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = String.format(Locale.US, "Total Earned: $ %.2f", totalEarned),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                        Text("Completed Deliveries: ${deliveredOrders.size}")
                        Text("Active Orders in Progress: ${currentOrders.size}")
                        Text(
                            text = "Keep accepting orders to earn bonuses during peak hours!",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = DrawerModalType.NONE }) {
                        Text("Close", color = primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        DrawerModalType.RATING -> {
            AlertDialog(
                onDismissRequest = { activeModal = DrawerModalType.NONE },
                containerColor = Color.White,
                title = {
                    Text("Rider Rating", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "★ 4.95 / 5.0",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                        Text("On-Time Delivery Rate: 98%")
                        Text("Customer Satisfaction: 99%")
                        Text(
                            text = "Recent Feedback: \"Fast and polite rider, food arrived hot!\"",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = DrawerModalType.NONE }) {
                        Text("Close", color = primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        DrawerModalType.ACCOUNT -> {
            var editName by remember(authState.name) { mutableStateOf(authState.name) }
            var editPhone by remember(authState.phoneNumber) { mutableStateOf(authState.phoneNumber) }
            AlertDialog(
                onDismissRequest = { activeModal = DrawerModalType.NONE },
                containerColor = Color.White,
                title = {
                    Text("Rider Account", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            labelText = "Full Name",
                            testTag = "account_name_input"
                        )
                        CustomTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            labelText = "Phone Number",
                            isNumPad = true,
                            testTag = "account_phone_input"
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Admin Approved Status",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = authState.isApproved,
                                onCheckedChange = { approved ->
                                    activeModal = DrawerModalType.NONE
                                    onToggleApproval(approved)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primary)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onUpdateProfile(editName, editPhone)
                            activeModal = DrawerModalType.NONE
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModal = DrawerModalType.NONE }) {
                        Text("Cancel", color = primary)
                    }
                }
            )
        }

        DrawerModalType.SETTINGS -> {
            AlertDialog(
                onDismissRequest = { activeModal = DrawerModalType.NONE },
                containerColor = Color.White,
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Live Location Interval: Every 10 seconds")
                        Text("Near Customer Threshold: 0.50 km")
                        Button(
                            onClick = {
                                onResetSampleOrders()
                                activeModal = DrawerModalType.NONE
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset Sample Orders", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = DrawerModalType.NONE }) {
                        Text("Close", color = primary)
                    }
                }
            )
        }

        DrawerModalType.TERMS -> {
            AlertDialog(
                onDismissRequest = { activeModal = DrawerModalType.NONE },
                containerColor = Color.White,
                title = {
                    Text("Terms & Conditions / Privacy", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        text = "FoodPanda Rider Terms: Always wear your helmet, verify order numbers at pickup, respect customer delivery instructions, and follow road safety regulations.",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = DrawerModalType.NONE }) {
                        Text("Accept & Close", color = primary)
                    }
                }
            )
        }

        DrawerModalType.NONE -> Unit
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header matching lib/home/widgets/my_drawer.dart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(primary)
                        .padding(20.dp)
                ) {
                    val displayName = authState.name.ifBlank { "Foodpanda" }
                    val initial = displayName.firstOrNull()?.uppercase() ?: "F"
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = primary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (authState.email.isNotBlank()) {
                                Text(
                                    text = authState.email,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DrawerMenuTile(
                    text = "Delivery History",
                    icon = Icons.Default.DeliveryDining,
                    testTag = "drawer_delivery_history",
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenOrderHistory()
                    }
                )
                DrawerMenuTile(
                    text = "Earnings",
                    icon = Icons.Default.AttachMoney,
                    testTag = "drawer_earnings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeModal = DrawerModalType.EARNINGS
                    }
                )
                DrawerMenuTile(
                    text = "Rating",
                    icon = Icons.Default.StarBorder,
                    testTag = "drawer_rating",
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeModal = DrawerModalType.RATING
                    }
                )
                DrawerMenuTile(
                    text = "Account",
                    icon = Icons.Default.PersonOutline,
                    testTag = "drawer_account",
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeModal = DrawerModalType.ACCOUNT
                    }
                )

                HorizontalDivider(
                    color = Color(0xFFEEEEEE),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                DrawerMenuTile(
                    text = "Settings",
                    icon = null,
                    testTag = "drawer_settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeModal = DrawerModalType.SETTINGS
                    }
                )
                DrawerMenuTile(
                    text = "Terms & Conditions / Privacy",
                    icon = null,
                    testTag = "drawer_terms",
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeModal = DrawerModalType.TERMS
                    }
                )

                if (authState.isSignedIn) {
                    DrawerMenuTile(
                        text = "Log out",
                        icon = null,
                        testTag = "drawer_logout",
                        onClick = {
                            scope.launch { drawerState.close() }
                            showLogoutDialog = true
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Orders",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("home_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation drawer",
                                tint = primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onCreateIncomingOrder,
                            modifier = Modifier.testTag("simulate_new_order_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Dispatch new incoming order",
                                tint = primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("home_screen"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "All Orders",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.padding(start = 15.dp, top = 20.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 18.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OrderTabPill(
                            title = "All Order (${availableOrders.size})",
                            isSelected = tabChoice == "All Order",
                            onClick = { tabChoice = "All Order" },
                            testTag = "tab_all_order"
                        )
                        OrderTabPill(
                            title = "Current Order (${currentOrders.size})",
                            isSelected = tabChoice == "Current Order",
                            onClick = { tabChoice = "Current Order" },
                            testTag = "tab_current_order"
                        )
                    }
                }

                if (displayedOrders.isEmpty()) {
                    item {
                        EmptyOrdersCard(
                            isAllOrdersTab = tabChoice == "All Order",
                            onDispatchSample = onCreateIncomingOrder,
                            onSwitchToAllOrders = { tabChoice = "All Order" }
                        )
                    }
                } else {
                    items(
                        items = displayedOrders,
                        key = { it.id }
                    ) { order ->
                        OrderCard(
                            order = order,
                            onPickUpOrder = { onPickOrder(order) },
                            isHistoryView = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuTile(
    text: String,
    icon: ImageVector?,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        },
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            null
        },
        selected = false,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .testTag(testTag)
    )
}

@Composable
private fun OrderTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) primary else Color.Transparent)
            .border(2.dp, primary, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(testTag)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EmptyOrdersCard(
    isAllOrdersTab: Boolean,
    onDispatchSample: () -> Unit,
    onSwitchToAllOrders: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.panda_eating),
                contentDescription = "No orders illustration",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isAllOrdersTab) {
                    "No available orders right now"
                } else {
                    "No active order in progress"
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isAllOrdersTab) {
                    "Tap below to dispatch a new incoming order to your area."
                } else {
                    "Pick an order from the All Order tab to start a delivery."
                },
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = if (isAllOrdersTab) onDispatchSample else onSwitchToAllOrders,
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isAllOrdersTab) "Dispatch New Order" else "Browse All Orders",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenNoApprove(
    onApproveAccount: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "FoodPanda Rider",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .testTag("home_screen_no_approve"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.foodpanda_location),
                contentDescription = "Waiting for approval",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Waiting for admin to approve this account. You may contact via email - ",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF1E1E1E)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "hovvathana1@gmail.com",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = primary
            )
            Spacer(modifier = Modifier.height(28.dp))
            CustomTextButton(
                text = "Approve Rider Account Now",
                onPressed = onApproveAccount,
                testTag = "approve_account_button"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    deliveredOrders: List<Order>,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val totalEarned = remember(deliveredOrders) {
        deliveredOrders.sumOf { it.deliveryPrice }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Delivery History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("history_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh history",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("order_history_screen"),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COMPLETED DELIVERIES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9E9E9E)
                            )
                            Text(
                                text = "${deliveredOrders.size} Orders",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL EARNINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9E9E9E)
                            )
                            Text(
                                text = String.format(Locale.US, "$ %.2f", totalEarned),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                    }
                }
            }

            if (deliveredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completed deliveries yet.",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                items(
                    items = deliveredOrders,
                    key = { it.id }
                ) { order ->
                    OrderCard(
                        order = order,
                        onPickUpOrder = {},
                        isHistoryView = true
                    )
                }
            }
        }
    }
}

@Composable
fun FinishDeliverScreen(
    price: Double,
    onNewRide: () -> Unit
) {
    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .testTag("finish_deliver_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rider),
                contentDescription = "Delivery Completed Rider Illustration",
                modifier = Modifier.size(210.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = String.format(Locale.US, "Congratulations, you just earn $ %.2f", price),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1E1E1E)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Stay safe on your next ride",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF757575),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            CustomTextButton(
                text = "New Ride",
                onPressed = onNewRide,
                isDisabled = false,
                testTag = "new_ride_button"
            )
        }
    }
}
