package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.WalmartViewModel

@Composable
fun TrackScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val deliveryMode by viewModel.deliveryMode.collectAsState()
    val shippingAddress by viewModel.shippingAddress.collectAsState()
    val activeStore by viewModel.selectedStore.collectAsState()

    var showLiveTrackingSimulator by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F7))
    ) {
        // Sleek top header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(WalmartBlue)
                .padding(top = 24.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Text(
                    text = "REAL-TIME TRACKING",
                    color = WalmartSparkYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Track My Shipments",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalShipping, null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No orders placed yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Go to the Shop tab, add fresh produce or gadgets to your cart, and place an order to track it live here.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(orders) { order ->
                    // High-contrast Sleek Slate-900 Card representing the premium theme package tracking panel
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Sleek Slate-900
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WalmartBlue)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.status.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = "ETA 2:45 PM",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Track order: #${order.id.take(8)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            val simTotal = ((order.id.hashCode() % 60) + 15).coerceAtLeast(10) + 0.99
                            Text(
                                text = "Fulfilment: ${order.type} • Total: $${String.format("%.2f", simTotal)}",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom horizontal step progress meter
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = when (order.status) {
                                        "Order Placed" -> 0.25f
                                        "Preparing" -> 0.5f
                                        "Out for Delivery" -> 0.75f
                                        "Delivered" -> 1.0f
                                        else -> 0.5f
                                    },
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color(0xFF1E293B),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = when (order.status) {
                                        "Order Placed" -> "25%"
                                        "Preparing" -> "50%"
                                        "Out for Delivery" -> "75%"
                                        "Delivered" -> "100%"
                                        else -> "50%"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            // Advance status simulating step changes
                                            viewModel.advanceOrderStatus(order.id)
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Simulate step", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(WalmartBlue)
                                        .clickable { showLiveTrackingSimulator = true }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Track Live Map", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Live Map Simulator Dialog
    if (showLiveTrackingSimulator) {
        Dialog(onDismissRequest = { showLiveTrackingSimulator = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Live Driver Tracking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WalmartBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Driver: Michael S. • White Ford Transit",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Map vector simulation box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsCar, null, tint = WalmartBlue, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("1.2 miles away • Heading North-West", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Destination: $shippingAddress", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showLiveTrackingSimulator = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Map Tracker", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
