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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Order
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.CartItemWithProduct
import com.example.ui.viewmodel.WalmartViewModel

@Composable
fun CartScreen(
    viewModel: WalmartViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartWithProducts.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val cartTotalCount by viewModel.cartTotalCount.collectAsState()
    val deliveryMode by viewModel.deliveryMode.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val pickupTime by viewModel.selectedPickupTime.collectAsState()
    val shippingAddress by viewModel.shippingAddress.collectAsState()
    val savedCards by viewModel.savedCards.collectAsState()
    val isPlusMember by viewModel.walmartPlusStatus.collectAsState()
    val successOrder by viewModel.checkoutSuccessOrder.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Top Summary Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WalmartBlue)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Cart ($cartTotalCount items)",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (cartItems.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = WalmartSparkYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { viewModel.clearCart() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (cartItems.isEmpty()) {
            EmptyCartState(onNavigateToTab)
        } else {
            Column(modifier = Modifier.weight(1f)) {
                // Cart Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems) { item ->
                        CartListItem(
                            item = item,
                            onQuantityIncrease = { viewModel.updateCartQuantity(item.cartItem.id, item.cartItem.quantity + 1) },
                            onQuantityDecrease = { viewModel.updateCartQuantity(item.cartItem.id, item.cartItem.quantity - 1) },
                            onRemove = { viewModel.removeFromCart(item.cartItem.id) }
                        )
                    }
                }

                // Subtotal Price Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Order Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = Color.Gray, fontSize = 14.sp)
                            Text("$${String.format("%.2f", cartTotal)}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee", color = Color.Gray, fontSize = 14.sp)
                            if (isPlusMember) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("$0.00", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
                                    Text("W+ waived", color = WalmartBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("$5.99", fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Tax (6%)", color = Color.Gray, fontSize = 14.sp)
                            Text("$${String.format("%.2f", cartTotal * 0.06)}", fontSize = 14.sp)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        val finalTotal = if (isPlusMember) cartTotal * 1.06 else (cartTotal * 1.06) + 5.99
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            Text("$${String.format("%.2f", finalTotal)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = WalmartBlue)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Payment, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // Checkout Summary sheet
    if (showCheckoutDialog) {
        Dialog(onDismissRequest = { showCheckoutDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Secure Checkout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = WalmartBlue,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Delivery Details Summary
                    Text("FULFILMENT METHOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (deliveryMode == "Pickup") Icons.Default.Storefront else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = WalmartBlue
                        )
                        Column {
                            Text(
                                text = if (deliveryMode == "Pickup") "Store Pickup at ${selectedStore?.name ?: "Springfield Supercenter"}" else "Delivery to Home",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (deliveryMode == "Pickup") pickupTime else shippingAddress,
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Payment cards option selector
                    Text("SELECT PAYMENT CARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    var selectedCardIndex by remember { mutableStateOf(0) }
                    
                    if (savedCards.isEmpty()) {
                        Text(
                            "No saved cards. Defaulting to Walmart Pay Sim",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        savedCards.forEachIndexed { idx, card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCardIndex = idx }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCardIndex == idx,
                                    onClick = { selectedCardIndex = idx },
                                    colors = RadioButtonDefaults.colors(selectedColor = WalmartBlue)
                                )
                                Icon(Icons.Default.CreditCard, null, tint = WalmartBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${card.cardType} - ${card.cardNumber}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Final Total display
                    val finalTotal = if (isPlusMember) cartTotal * 1.06 else (cartTotal * 1.06) + 5.99
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("$${String.format("%.2f", finalTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WalmartBlue)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { showCheckoutDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.simulateCheckout()
                                showCheckoutDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Place Order", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Checkout Success Screen Modal Dialog
    successOrder?.let { order ->
        Dialog(onDismissRequest = { viewModel.clearCheckoutSuccess() }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success check",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Order Placed Successfully!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your order has been registered in our database system.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show Order details code
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F8FC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Order ID:", fontSize = 12.sp, color = Color.Gray)
                                Text(order.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WalmartBlue)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Type:", fontSize = 12.sp, color = Color.Gray)
                                Text(order.type, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status:", fontSize = 12.sp, color = Color.Gray)
                                Text(order.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.clearCheckoutSuccess()
                            onNavigateToTab(4) // Navigate to Track tab
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Track Order Live", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { viewModel.clearCheckoutSuccess() }) {
                        Text("Continue Shopping", color = WalmartBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartListItem(
    item: CartItemWithProduct,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = item.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Aisle: ${item.product.aisleLocation}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${String.format("%.2f", item.product.price)} each",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity controls / Remove column
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.LightGray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    IconButton(onClick = onQuantityDecrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = item.cartItem.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    IconButton(onClick = onQuantityIncrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState(onNavigateToTab: (Int) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Empty shopping cart",
                tint = Color.LightGray,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your cart is empty",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = "Add some fresh grocery produce, rollbacks, or electronics to get started with simulated checkout.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
            Button(
                onClick = { onNavigateToTab(1) }, // Shop tab
                colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Browse Products & Grocery", fontWeight = FontWeight.Bold)
            }
        }
    }
}
