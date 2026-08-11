package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.WalmartViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var activeOverlay by remember { mutableStateOf<String?>(null) }
    var initialSelectedProductForShop by remember { mutableStateOf<Product?>(null) }

    // Tab items
    val tabItems = listOf(
        TabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Shop", Icons.Filled.LocalGroceryStore, Icons.Outlined.LocalGroceryStore),
        TabItem("Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
        TabItem("Store", Icons.Filled.Storefront, Icons.Outlined.Storefront),
        TabItem("Track", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping)
    )

    val cartTotalCount by viewModel.cartTotalCount.collectAsState()

    Scaffold(
        bottomBar = {
            if (activeOverlay == null) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .height(84.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    tabItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedTab = index
                                if (index != 1) {
                                    initialSelectedProductForShop = null
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (index == 2 && cartTotalCount > 0) {
                                            Badge(
                                                containerColor = WalmartSparkYellow,
                                                contentColor = WalmartBlue
                                            ) {
                                                Text(
                                                    text = cartTotalCount.toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) WalmartBlue else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) WalmartBlue else Color.Gray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = WalmartBlue.copy(alpha = 0.08f)
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render active tab with fade transition
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { tab ->
                            selectedTab = tab
                        },
                        onOpenService = { service ->
                            if (service == "store_mode") {
                                selectedTab = 3 // companion
                            } else {
                                activeOverlay = service
                            }
                        },
                        onOpenProductDetail = { prod ->
                            initialSelectedProductForShop = prod
                            selectedTab = 1 // Navigate to shop
                        }
                    )
                    1 -> ShopScreen(
                        viewModel = viewModel,
                        initialProduct = initialSelectedProductForShop
                    )
                    2 -> CartScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { tab ->
                            selectedTab = tab
                        }
                    )
                    3 -> StoreCompanionScreen(
                        viewModel = viewModel
                    )
                    4 -> TrackScreen(
                        viewModel = viewModel
                    )
                }
            }

            // Beautiful full screen service overlays
            AnimatedVisibility(
                visible = activeOverlay != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF3F5F7))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Custom close overlay row on top
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { activeOverlay = null }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = WalmartBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (activeOverlay) {
                                    "pharmacy" -> "Pharmacy Portal"
                                    "walmart_pay" -> "Walmart Pay Secure"
                                    "walmart_plus" -> "Walmart+ Premium Benefits"
                                    "deals" -> "Hot Deals Rollbacks"
                                    else -> "Walmart Services"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = WalmartBlue
                            )
                        }

                        // Overlay content switcher
                        Box(modifier = Modifier.weight(1f)) {
                            when (activeOverlay) {
                                "pharmacy" -> PharmacyScreen(viewModel = viewModel)
                                "walmart_pay" -> WalmartPayScreen(viewModel = viewModel)
                                "walmart_plus" -> WalmartPlusScreen(viewModel = viewModel)
                                "deals" -> ShopScreen(
                                    viewModel = viewModel,
                                    initialProduct = null // Can load deal filters inside ShopScreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
