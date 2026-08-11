package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreLocation
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.WalmartViewModel

@Composable
fun StoreCompanionScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier
) {
    val selectedStore by viewModel.selectedStore.collectAsState()
    val allStores by viewModel.stores.collectAsState()
    val products by viewModel.products.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showStoreSelector by remember { mutableStateOf(false) }

    val activeStore = selectedStore ?: allStores.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F7)) // Sleek grey background
    ) {
        // 1. Sleek Top Header with rounded bottom corners (rounded-b-[2rem])
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(WalmartBlue)
                .padding(top = 24.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "STORE COMPANION",
                            color = WalmartSparkYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activeStore?.name ?: "No Store Selected",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showStoreSelector = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Change", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Store Product / Aisle Locator search
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Find item aisle (e.g. Milk, Eggs, TV)", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = WalmartSparkYellow,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Aisle Locator Search Results
            if (searchQuery.isNotEmpty()) {
                val matches = products.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
                }

                item {
                    Text(
                        text = "Aisle Finder Results (${matches.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }

                if (matches.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "No products matching '$searchQuery' in stock.",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(matches) { match ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(match.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Price: $${match.price}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Availability: ${match.stockCount} available", color = Color.Gray, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(WalmartBlue.copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("AISLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WalmartBlue)
                                        Text(
                                            match.aisleLocation.replace("Aisle ", ""),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = WalmartBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Default Companion Interface
                item {
                    // Store details card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(WalmartBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, null, tint = WalmartBlue, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text("Store Hours", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(activeStore?.hours ?: "6:00 AM - 11:00 PM", color = Color.Gray, fontSize = 13.sp)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                InfoItem(icon = Icons.Default.Phone, title = "Phone", desc = activeStore?.phone ?: "(479) 273-4005")
                                InfoItem(icon = Icons.Default.Directions, title = "Distance", desc = activeStore?.distance ?: "1.2 mi")
                            }
                        }
                    }
                }

                // Interactive Store Map mockup card
                item {
                    Text(
                        text = "Digital Store Map",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Hand-crafted Store layout visual blueprint inside canvas
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Map, null, tint = WalmartBlue, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Interactive Layout Blueprint", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Aisles 1-10: Fresh Food & Groceries", color = Color.Gray, fontSize = 12.sp)
                                    Text("Aisles 11-20: Pharmacy & Personal Care", color = Color.Gray, fontSize = 12.sp)
                                    Text("Aisles G1-G20: Electronics & Apparel", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📍 Curbside Pickup Zone: Area A", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WalmartSparkYellow.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Gate 1", color = Color(0xFF7F5F00), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Services checklist inside store
                item {
                    Text(
                        text = "Store Services & Availability",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ServiceCheckRow(name = "Walmart Pharmacy", available = activeStore?.hasPharmacy == true, hours = "9:00 AM - 7:00 PM")
                            ServiceCheckRow(name = "Walmart Fuel Station", available = activeStore?.hasFuel == true, hours = "24 Hours")
                            ServiceCheckRow(name = "Curbside Pickup", available = true, hours = "7:00 AM - 10:00 PM")
                            ServiceCheckRow(name = "Walmart Pay at Register", available = true, hours = "Always Available")
                        }
                    }
                }
            }
        }
    }

    // Store selector modal
    if (showStoreSelector) {
        AlertDialog(
            onDismissRequest = { showStoreSelector = false },
            title = { Text("Select Store Location", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allStores) { store ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectStore(store.id)
                                    showStoreSelector = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (store.id == activeStore?.id) WalmartBlue.copy(alpha = 0.1f) else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (store.id == activeStore?.id) WalmartBlue else Color.LightGray
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(store.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(store.address, color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Distance: ${store.distance} • Pharmacy: ${if (store.hasPharmacy) "Yes" else "No"}", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStoreSelector = false }) {
                    Text("Close", color = WalmartBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun InfoItem(icon: ImageVector, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Column {
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Text(desc, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun ServiceCheckRow(name: String, available: Boolean, hours: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (available) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (available) Color(0xFF2E7D32) else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Hours: $hours", color = Color.Gray, fontSize = 11.sp)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (available) Color(0xFFE8F5E9) else Color(0xFFEEEEEE))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (available) "Active" else "Closed",
                color = if (available) Color(0xFF2E7D32) else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
