package com.example.ui.screens

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
import com.example.ui.theme.WalmartBlue
import com.example.ui.theme.WalmartSparkYellow
import com.example.ui.viewmodel.WalmartViewModel

@Composable
fun PharmacyScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier
) {
    val prescriptions by viewModel.prescriptions.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val allStores by viewModel.stores.collectAsState()

    var showRefillSuccessDialog by remember { mutableStateOf(false) }
    var refilledRxName by remember { mutableStateOf("") }
    var rxNumberInput by remember { mutableStateOf("") }
    var rxNameInput by remember { mutableStateOf("") }
    var showNewRxDialog by remember { mutableStateOf(false) }

    val activeStore = selectedStore ?: allStores.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F7))
    ) {
        // Sleek Top Header (rounded bottom corners)
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
                        text = "WALMART HEALTH & RX",
                        color = WalmartSparkYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "My Prescriptions",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { showNewRxDialog = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Add RX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Pharmacy Location card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalPharmacy, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(
                                    text = "Store Pharmacy: ${activeStore?.name ?: "Supercenter"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (activeStore?.hasPharmacy == true) "Open Now • 9:00 AM - 7:00 PM" else "No pharmacy at this store location",
                                    color = if (activeStore?.hasPharmacy == true) Color(0xFF2E7D32) else Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Prescriptions List
            item {
                Text(
                    text = "Active Prescriptions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            if (prescriptions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(Icons.Default.MedicalServices, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No prescriptions on file", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "Add a medication to schedule automated refills or track status.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(prescriptions) { rx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(rx.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("RX #: ${rx.id} • Dosage: ${rx.dosage}", color = Color.Gray, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (rx.status) {
                                                "Ready for Pickup" -> Color(0xFFE8F5E9)
                                                "Refill in Progress" -> Color(0xFFFFF3E0)
                                                else -> Color(0xFFEEEEEE)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = rx.status,
                                        color = when (rx.status) {
                                            "Ready for Pickup" -> Color(0xFF2E7D32)
                                            "Refill in Progress" -> Color(0xFFE65100)
                                            else -> Color.DarkGray
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Refills Left", color = Color.Gray, fontSize = 11.sp)
                                    Text("${rx.refillsRemaining} remaining", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                if (rx.refillsRemaining > 0 && rx.status != "Refill in Progress") {
                                    Button(
                                        onClick = {
                                            viewModel.requestRefill(rx.id)
                                            refilledRxName = rx.name
                                            showRefillSuccessDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Request Refill", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else if (rx.status == "Refill in Progress") {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("In Progress", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                    }
                                } else {
                                    Text(
                                        "No Refills Remaining",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Refill success modal dialog
    if (showRefillSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRefillSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp)) },
            title = { Text("Refill Submitted", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "Your refill request for $refilledRxName has been sent to our pharmacy. We'll update the status to 'Ready for Pickup' in moments.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRefillSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // New Prescription Dialog
    if (showNewRxDialog) {
        AlertDialog(
            onDismissRequest = { showNewRxDialog = false },
            title = { Text("Enter Prescription Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = rxNameInput,
                        onValueChange = { rxNameInput = it },
                        placeholder = { Text("Medication Name (e.g., Amoxicillin)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = rxNumberInput,
                        onValueChange = { rxNumberInput = it },
                        placeholder = { Text("RX Number (e.g., 98213)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rxNameInput.isNotBlank() && rxNumberInput.isNotBlank()) {
                            viewModel.addPrescription(rxNameInput, rxNumberInput)
                            rxNameInput = ""
                            rxNumberInput = ""
                            showNewRxDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WalmartBlue)
                ) {
                    Text("Add RX", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewRxDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
