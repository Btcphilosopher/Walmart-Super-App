package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Product
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.WalmartViewModel

@Composable
fun HomeScreen(
    viewModel: WalmartViewModel,
    onNavigateToTab: (Int) -> Unit,
    onOpenService: (String) -> Unit,
    onOpenProductDetail: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val deals by viewModel.deals.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val walmartPlus by viewModel.walmartPlusStatus.collectAsState()
    
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F7)) // Sleek Interface background
            .verticalScroll(scrollState)
    ) {
        // 1. Personalized Header with bottom-rounded 32.dp shape (rounded-b-[2rem])
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(WalmartBlue)
                .padding(top = 28.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Good morning",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Hi, Tom 👋",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Walmart+ Badge
                    if (walmartPlus) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(WalmartSparkYellow)
                                .clickable { onOpenService("walmart_plus") }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Walmart+",
                                    tint = WalmartBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "W+",
                                    color = WalmartBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onOpenService("walmart_plus") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color.White)),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Join W+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Search Box
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onNavigateToTab(1) } // Navigate to Shop
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search Walmart",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Services quick links grid with 28.dp rounded shape
        Spacer(modifier = Modifier.height(16.dp))
        ServicesGrid(
            onNavigateToTab = onNavigateToTab,
            onOpenService = onOpenService
        )

        // 3. Active Orders Teaser
        val activeOrders = orders.filter { it.trackingStep < 3 }
        if (activeOrders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToTab(4) }, // Track Tab
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Sleek Dark Slate-900 Teaser
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Active Orders",
                            tint = WalmartSparkYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${activeOrders.size} Order Out for Delivery",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Track your parcel delivery map live",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Tracking",
                        tint = Color.White
                    )
                }
            }
        }

        // 4. Your Local Store Mode Card with 28.dp rounded shape
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE0F2FE), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Store icon",
                            tint = WalmartBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Your local store",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedStore?.name ?: "Bentonville Supercenter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = selectedStore?.address ?: "406 S Walton Blvd, Bentonville, AR",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Open • 6:00 AM - 11:00 PM",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WalmartBlue.copy(alpha = 0.1f))
                            .clickable { onOpenService("store_mode") }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Aisle Map",
                            color = WalmartBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 5. Promotional Hero Banner (Deals / W+)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp)
                .clickable { onOpenService("walmart_plus") },
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background fallback
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)))
                
                Image(
                    painter = painterResource(id = R.drawable.img_plus_banner),
                    contentDescription = "Walmart Plus Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "WALMART+",
                            color = WalmartSparkYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Save over $800/year on groceries",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Free home delivery & 10¢/gal fuel discounts",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 6. Today's Deals Rollback Carousel
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Today's Hot Rollbacks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "See all",
                color = WalmartBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onOpenService("deals") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            items(deals) { dealProduct ->
                DealCard(product = dealProduct, onProductClick = onOpenProductDetail, onAddToCart = { viewModel.addToCart(it) })
            }
        }
    }
}

@Composable
fun ServicesGrid(
    onNavigateToTab: (Int) -> Unit,
    onOpenService: (String) -> Unit
) {
    val items = listOf(
        ServiceItem("Grocery", Icons.Default.LocalGroceryStore, Color(0xFFFDF2E9), "🍎") { onNavigateToTab(1) },
        ServiceItem("Deals", Icons.Default.LocalOffer, Color(0xFFEFF6FF), "🔌") { onOpenService("deals") },
        ServiceItem("Walmart Pay", Icons.Default.QrCodeScanner, Color(0xFFEEF2F6), "💳") { onOpenService("walmart_pay") },
        ServiceItem("Store Mode", Icons.Default.Storefront, Color(0xFFEEF2F6), "🏪") { onOpenService("store_mode") },
        ServiceItem("Pharmacy", Icons.Default.LocalPharmacy, Color(0xFFECFDF5), "💊") { onOpenService("pharmacy") },
        ServiceItem("Walmart+", Icons.Default.Star, Color(0xFFFEF9C3), "✨") { onOpenService("walmart_plus") }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Quick Services",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.take(3).forEach { item ->
                    ServiceButton(item = item)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.takeLast(3).forEach { item ->
                    ServiceButton(item = item)
                }
            }
        }
    }
}

data class ServiceItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val emoji: String,
    val onClick: () -> Unit
)

@Composable
fun ServiceButton(item: ServiceItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable { item.onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(item.color, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DealCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFEEEEEE))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Rollback Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.dealType,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$${product.price}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 15.sp
                    )
                    Text(
                        text = "$${product.originalPrice}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onAddToCart(product) },
                    colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
