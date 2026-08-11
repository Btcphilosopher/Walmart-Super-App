package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun WalmartPlusScreen(
    viewModel: WalmartViewModel,
    modifier: Modifier = Modifier
) {
    val isPlusMember by viewModel.walmartPlusStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F7))
    ) {
        // Sleek Walmart+ Brand Header with Rounded bottom corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(WalmartSparkYellow)
                .padding(top = 28.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREMIUM MEMBERSHIP",
                            color = Color(0xFF7F5F00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Walmart+",
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Icon(Icons.Default.Star, "Spark symbol", tint = Color(0xFF7F5F00), modifier = Modifier.size(24.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .clickable { viewModel.toggleWalmartPlus() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isPlusMember) "Cancel W+" else "Try Free",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Membership Status Panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isPlusMember) Color(0xFFE8F5E9) else Color(0xFFFFF3E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlusMember) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPlusMember) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isPlusMember) "Your Walmart+ membership is ACTIVE!" else "Unlock Premium benefits",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isPlusMember) "Renews in 24 days • $12.95/mo waived" else "Get free delivery, fuel savings, and video streaming",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Member Benefits & Perks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                BenefitCard(
                    emoji = "🚗",
                    title = "Free Delivery from Store",
                    desc = "Get grocery produce & items delivered straight to your door with $0 delivery fees (normally $5.99)."
                )
            }

            item {
                BenefitCard(
                    emoji = "⛽",
                    title = "Fuel Savings at Pump",
                    desc = "Save up to 10¢ per gallon at over 14,000 participating Exxon, Mobil, Conoco, and Walmart stations."
                )
            }

            item {
                BenefitCard(
                    emoji = "📱",
                    title = "Mobile Scan & Go",
                    desc = "Scan items on your phone as you shop in-store, check out instantly with Walmart Pay, and bypass registers."
                )
            }

            item {
                BenefitCard(
                    emoji = "🍿",
                    title = "Paramount+ Included",
                    desc = "Enjoy a complimentary Paramount+ Essential subscription with over 40,000 episodes, hit movies, and live sports."
                )
            }

            item {
                BenefitCard(
                    emoji = "⚡",
                    title = "Early Access Deals",
                    desc = "Be first in line for Black Friday sales, exclusive tech drops, and holiday rollbacks before non-members."
                )
            }
        }
    }
}

@Composable
fun BenefitCard(
    emoji: String,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, color = Color.Gray, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}
