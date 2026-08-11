package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // produce, meat, dairy, drinks, electronics, household, pharmacy, deals
    val price: Double,
    val originalPrice: Double = 0.0,
    val isDeal: Boolean = false,
    val dealType: String = "", // Rollback, Clearance, Weekly Deal
    val imageUrl: String = "",
    val rating: Float = 4.5f,
    val reviewCount: Int = 120,
    val isGrocery: Boolean = true,
    val stockCount: Int = 10,
    val aisleLocation: String = "Aisle 1",
    val deliveryTime: String = "Arriving tomorrow",
    val pickupAvailable: Boolean = true
) : Serializable

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val quantity: Int
) : Serializable

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val type: String, // Grocery Pickup, Electronics Delivery, Household Shipping, etc.
    val status: String, // Ready for collection, Out for delivery, Arriving tomorrow, etc.
    val dateTime: String,
    val trackingStep: Int, // 0 = Ordered, 1 = Preparing, 2 = Ready / Out for delivery, 3 = Completed
    val progressPercentage: Float, // 0.0f to 1.0f
    val addressOrStore: String,
    val itemSummary: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "prescriptions")
data class PharmacyPrescription(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: String,
    val patientName: String,
    val refillsRemaining: Int,
    val status: String, // Ready for pickup, Refill requested, Refill in progress, Active
    val storeId: String,
    val instructions: String
) : Serializable

@Entity(tableName = "payment_cards")
data class PaymentCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardholderName: String,
    val cardNumber: String, // Obfuscated like **** **** **** 1234
    val expiryDate: String,
    val cardType: String // Visa, Mastercard, AMEX
) : Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val dateTime: String,
    val amount: Double,
    val storeName: String,
    val itemsCount: Int,
    val itemsSummary: String
) : Serializable

@Entity(tableName = "store_locations")
data class StoreLocation(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val hours: String,
    val phone: String,
    val hasPharmacy: Boolean = true,
    val hasFuel: Boolean = true,
    val distance: String,
    val isSelected: Boolean = false
) : Serializable
