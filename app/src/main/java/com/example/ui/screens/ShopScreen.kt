package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartDarkBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.WalmartViewModel

@Composable
fun ShopScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier,
    initialProduct: Product? = null
) {
    val products by viewModel.products.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val deliveryMode by viewModel.deliveryMode.collectAsState()
    val pickupTime by viewModel.selectedPickupTime.collectAsState()
    val address by viewModel.shippingAddress.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var activeProductDetail by remember { mutableStateOf<Product?>(initialProduct) }
    var showDeliveryDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "produce", "dairy", "meat", "drinks", 
        "electronics", "household", "personal_care", "pharmacy"
    )

    LaunchedEffect(initialProduct) {
        if (initialProduct != null) {
            activeProductDetail = initialProduct
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // 1. Pickup vs Delivery Selector Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (deliveryMode == "Pickup") WalmartBlue else Color.Transparent)
                            .clickable { viewModel.setDeliveryMode("Pickup") }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Grocery Pickup",
                            color = if (deliveryMode == "Pickup") Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (deliveryMode == "Delivery") WalmartBlue else Color.Transparent)
                            .clickable { viewModel.setDeliveryMode("Delivery") }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Standard Delivery",
                            color = if (deliveryMode == "Delivery") Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (deliveryMode == "Pickup") Icons.Default.Storefront else Icons.Default.LocalShipping,
                            contentDescription = "Fulfilment icon",
                            tint = WalmartBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (deliveryMode == "Pickup") "Store: ${selectedStore?.name ?: "Springfield Supercenter"}" else "Deliver to: $address",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (deliveryMode == "Pickup") "Slot: $pickupTime" else "ETA: Arrives today by 6:00 PM",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = "Change",
                        color = WalmartBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { showDeliveryDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. Search / Live Query Input Bar
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search catalog products (e.g., Milk, Apple, TV)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = WalmartBlue,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // 3. Category scroll chips (Only if NOT searching, or category filter fits searches)
        if (searchQuery.isBlank()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    val isChipSelected = selectedCategory == category
                    FilterChip(
                        selected = isChipSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = if (category == "All") "All Products" else category.replace("_", " ").replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WalmartBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isChipSelected,
                            borderColor = Color.LightGray,
                            selectedBorderColor = WalmartBlue,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 4. Products Display (Grid vs. List Search results)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (searchQuery.isNotBlank()) {
                // Search list results
                if (searchResults.isEmpty()) {
                    EmptySearchState()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults) { product ->
                            ProductGridItem(
                                product = product,
                                onProductClick = { activeProductDetail = it },
                                onAddToCart = { viewModel.addToCart(it) }
                            )
                        }
                    }
                }
            } else {
                // Main Category Grid view
                val filteredProducts = if (selectedCategory == "All") {
                    products
                } else {
                    products.filter { it.category == selectedCategory }
                }

                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products in this category yet.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductGridItem(
                                product = product,
                                onProductClick = { activeProductDetail = it },
                                onAddToCart = { viewModel.addToCart(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Product Detail Sheet Dialog
    activeProductDetail?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { activeProductDetail = null },
            onAddToCart = { qty ->
                viewModel.addToCart(product, qty)
                activeProductDetail = null
            }
        )
    }

    // Delivery Options Settings Dialog
    if (showDeliveryDialog) {
        DeliveryOptionsDialog(
            currentMode = deliveryMode,
            currentAddress = address,
            currentPickupTime = pickupTime,
            onDismiss = { showDeliveryDialog = false },
            onSave = { mode, newAddr, newTime ->
                viewModel.setDeliveryMode(mode)
                viewModel.setShippingAddress(newAddr)
                viewModel.setSelectedPickupTime(newTime)
                showDeliveryDialog = false
            }
        )
    }
}

@Composable
fun ProductGridItem(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFEEEEEE))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (product.isDeal) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE53935))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.dealType,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(38.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$${product.price}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                    if (product.isDeal && product.originalPrice > 0) {
                        Text(
                            text = "$${product.originalPrice}",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product.aisleLocation} • Stock: ${product.stockCount}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onAddToCart(product) },
                    colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "Not found",
                tint = Color.LightGray,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No products found",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Try searching for 'Milk', 'Bananas', 'TV', or 'Tide'.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Product Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WalmartBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFFEEEEEE))
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Category & Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = product.category.replace("_", " ").uppercase(),
                            color = WalmartBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        if (product.isDeal) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE53935))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = product.dealType,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Name
                    Text(
                        text = product.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$${product.price}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        if (product.isDeal && product.originalPrice > 0) {
                            Text(
                                text = "Was $${product.originalPrice}",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Aisle Location & Stock Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F8FC)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("STORE COMPANION DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WalmartBlue)
                                Text(
                                    text = "Located in ${product.aisleLocation}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${product.stockCount} available at your store",
                                    color = if (product.stockCount > 5) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 13.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Store location indicator",
                                tint = WalmartBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fulfilment eta
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, "Delivery", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Free Shipping: ${product.deliveryTime}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, "Pickup", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (product.pickupAvailable) "Free store pickup: Available in 2 hours" else "Store pickup: Out of stock at this location",
                            fontSize = 13.sp,
                            color = if (product.pickupAvailable) Color.Black else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Product Description
                    Text("Product Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Great Choice! This premium quality ${product.name} is selected for its superior reliability and flavor. Guaranteed to meet high-quality US consumer safety standards. Direct from verified suppliers.",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 8.dp),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mock Customer reviews
                    Text("Customer Reviews (4.8 ★)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewItem(author = "Sarah K.", rating = 5, comment = "Excellent fresh quality, bought this twice already. Perfect!")
                        ReviewItem(author = "Marcus M.", rating = 4, comment = "Good value and fast delivery. Exactly as described.")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quantity Counter & Add button row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEEEEE))
                        ) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                Icon(Icons.Default.Remove, "Decrease")
                            }
                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { if (quantity < product.stockCount) quantity++ }) {
                                Icon(Icons.Default.Add, "Increase")
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { onAddToCart(quantity) },
                            colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Add to Cart • $${String.format("%.2f", product.price * quantity)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(author: String, rating: Int, comment: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row {
                    repeat(rating) {
                        Icon(Icons.Default.Star, null, tint = WalmartSparkYellow, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun DeliveryOptionsDialog(
    currentMode: String,
    currentAddress: String,
    currentPickupTime: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var mode by remember { mutableStateOf(currentMode) }
    var address by remember { mutableStateOf(currentAddress) }
    var pickupTime by remember { mutableStateOf(currentPickupTime) }

    val pickupSlots = listOf(
        "Today, 4:00 PM - 5:00 PM",
        "Today, 6:00 PM - 7:00 PM",
        "Tomorrow, 9:00 AM - 10:00 AM",
        "Tomorrow, 11:00 AM - 12:00 PM"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Delivery Preferences",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WalmartBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Select mode
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { mode = "Pickup" },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (mode == "Pickup") WalmartBlue.copy(alpha = 0.08f) else Color.Transparent,
                            contentColor = if (mode == "Pickup") WalmartBlue else Color.Black
                        ),
                        border = BorderStroke(1.5.dp, if (mode == "Pickup") WalmartBlue else Color.LightGray),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("Pickup", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { mode = "Delivery" },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (mode == "Delivery") WalmartBlue.copy(alpha = 0.08f) else Color.Transparent,
                            contentColor = if (mode == "Delivery") WalmartBlue else Color.Black
                        ),
                        border = BorderStroke(1.5.dp, if (mode == "Delivery") WalmartBlue else Color.LightGray),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("Delivery", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mode == "Delivery") {
                    Text("Shipping Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F4F8),
                            unfocusedContainerColor = Color(0xFFF0F4F8)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Select Pickup Time Slot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    pickupSlots.forEach { slot ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pickupTime = slot }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = pickupTime == slot,
                                onClick = { pickupTime = slot },
                                colors = RadioButtonDefaults.colors(selectedColor = WalmartBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slot, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1.0f)) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSave(mode, address, pickupTime) },
                        colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
