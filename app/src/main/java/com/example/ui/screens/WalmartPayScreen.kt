package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
fun WalmartPayScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier
) {
    val savedCards by viewModel.savedCards.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var showAddCardDialog by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("Visa") }

    var showQRScanner by remember { mutableStateOf(false) }
    var paymentProgress by remember { mutableStateOf(0f) }
    var scanSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F7))
    ) {
        // Sleek Walmart Pay Brand Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(WalmartBlue)
                .padding(top = 24.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURE WALLET",
                        color = WalmartSparkYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Walmart Pay",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { showQRScanner = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Scan & Pay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Payment Cards list
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Payment Cards",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "+ Add Card",
                        color = WalmartBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { showAddCardDialog = true }
                            .padding(4.dp)
                    )
                }
            }

            if (savedCards.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CreditCardOff, null, tint = Color.LightGray, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No cards saved yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Save a card to pay instantly at registers.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(savedCards) { card ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(WalmartBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CreditCard, null, tint = WalmartBlue, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text(
                                        text = "${card.cardType} Card",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = card.cardNumber,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteCard(card) }) {
                                Icon(Icons.Default.Delete, "Remove card", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }

            // Transaction History
            item {
                Text(
                    text = "Recent Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "No transaction history recorded yet.",
                            modifier = Modifier.padding(24.dp),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = "Order ID: ${order.id.take(8)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Type: ${order.type} • Status: ${order.status}",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            val simulatedPrice = ((order.id.hashCode() % 60) + 15).coerceAtLeast(10) + 0.99
                            Text(
                                text = "$${String.format("%.2f", simulatedPrice)}",
                                fontWeight = FontWeight.Bold,
                                color = WalmartBlue,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Scan & Pay simulator modal
    if (showQRScanner) {
        Dialog(onDismissRequest = {
            showQRScanner = false
            scanSuccess = false
            paymentProgress = 0f
        }) {
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
                        "Scan Register QR Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WalmartBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!scanSuccess) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCode, null, modifier = Modifier.size(80.dp), tint = Color.DarkGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Point camera at register screen", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                scanSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate Scan Match", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(44.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Walmart Pay Matched!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Register matched with your secure wallet card.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                showQRScanner = false
                                scanSuccess = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Card Modal Dialog
    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Add Credit/Debit Card", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Card Network", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Visa", "MasterCard", "Amex").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (cardType == type) WalmartBlue else Color(0xFFEEEEEE))
                                    .clickable { cardType = type }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    type,
                                    color = if (cardType == type) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    TextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        placeholder = { Text("Card Number (e.g. •••• 4821)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardNumber.isNotBlank()) {
                            viewModel.addPaymentCard("Tom", cardNumber, "12/28", cardType)
                            cardNumber = ""
                            showAddCardDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue)
                ) {
                    Text("Save Card", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
