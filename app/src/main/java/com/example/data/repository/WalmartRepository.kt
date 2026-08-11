package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class WalmartRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val orderDao = database.orderDao()
    private val pharmacyDao = database.pharmacyDao()
    private val paymentDao = database.paymentDao()
    private val storeDao = database.storeDao()

    // Flow streams
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val deals: Flow<List<Product>> = productDao.getDeals()
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val prescriptions: Flow<List<PharmacyPrescription>> = pharmacyDao.getAllPrescriptions()
    val savedCards: Flow<List<PaymentCard>> = paymentDao.getSavedCards()
    val transactions: Flow<List<Transaction>> = paymentDao.getAllTransactions()
    val allStores: Flow<List<StoreLocation>> = storeDao.getAllStores()
    val selectedStore: Flow<StoreLocation?> = storeDao.getSelectedStore()

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)
    }

    // Cart operations
    suspend fun addToCart(productId: String, quantity: Int = 1) = withContext(Dispatchers.IO) {
        val existingItem = cartDao.getCartItemByProduct(productId)
        if (existingItem != null) {
            cartDao.updateCartItem(existingItem.copy(quantity = existingItem.quantity + quantity))
        } else {
            cartDao.insertCartItem(CartItem(productId = productId, quantity = quantity))
        }
    }

    suspend fun updateCartQuantity(cartItemId: Int, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            // Find the item first to delete it
            val cartList = cartDao.getCartItems().first()
            val itemToDelete = cartList.find { it.id == cartItemId }
            if (itemToDelete != null) {
                cartDao.deleteCartItem(itemToDelete)
            }
        } else {
            val cartList = cartDao.getCartItems().first()
            val itemToUpdate = cartList.find { it.id == cartItemId }
            if (itemToUpdate != null) {
                cartDao.updateCartItem(itemToUpdate.copy(quantity = newQuantity))
            }
        }
    }

    suspend fun removeFromCart(cartItemId: Int) = withContext(Dispatchers.IO) {
        val cartList = cartDao.getCartItems().first()
        val itemToDelete = cartList.find { it.id == cartItemId }
        if (itemToDelete != null) {
            cartDao.deleteCartItem(itemToDelete)
        }
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    // Store operations
    suspend fun selectStore(storeId: String) = withContext(Dispatchers.IO) {
        storeDao.setSelectedStore(storeId)
    }

    // Prescription operations
    suspend fun addPrescription(name: String, rxNumber: String) = withContext(Dispatchers.IO) {
        val newRx = PharmacyPrescription(
            id = rxNumber,
            name = name,
            dosage = "10mg - Take 1 daily as needed",
            patientName = "Tom",
            refillsRemaining = 3,
            status = "Ready for pickup",
            storeId = "S101",
            instructions = "Walmart Pharmacy #101, Springfield"
        )
        pharmacyDao.insertPrescriptions(listOf(newRx))
    }

    suspend fun advanceOrderStatus(orderId: String) = withContext(Dispatchers.IO) {
        val order = orderDao.getOrderById(orderId)
        if (order != null) {
            val nextStep = (order.trackingStep + 1) % 4
            val (nextStatus, nextPercentage) = when (nextStep) {
                0 -> "Order Placed" to 0.25f
                1 -> "Preparing" to 0.5f
                2 -> "Out for Delivery" to 0.75f
                3 -> "Delivered" to 1.0f
                else -> "Preparing" to 0.5f
            }
            val updated = order.copy(
                trackingStep = nextStep,
                status = nextStatus,
                progressPercentage = nextPercentage
            )
            orderDao.updateOrder(updated)
        }
    }

    suspend fun requestRefill(prescriptionId: String) = withContext(Dispatchers.IO) {
        val list = pharmacyDao.getAllPrescriptions().first()
        val prescription = list.find { it.id == prescriptionId }
        if (prescription != null && prescription.refillsRemaining > 0) {
            val updated = prescription.copy(
                refillsRemaining = prescription.refillsRemaining - 1,
                status = "Refill requested"
            )
            pharmacyDao.updatePrescription(updated)
        }
    }

    // Payment & Checkout Simulation
    suspend fun saveCard(card: PaymentCard) = withContext(Dispatchers.IO) {
        paymentDao.insertCard(card)
    }

    suspend fun deleteCard(card: PaymentCard) = withContext(Dispatchers.IO) {
        paymentDao.deleteCard(card)
    }

    suspend fun checkout(
        paymentMethod: String,
        deliveryType: String, // Pickup or Delivery
        selectedStoreName: String,
        pickupTime: String = "",
        address: String = ""
    ): Order = withContext(Dispatchers.IO) {
        // 1. Get current cart items and compile them
        val cartList = cartDao.getCartItems().first()
        val productsList = productDao.getAllProducts().first()
        
        var totalPrice = 0.0
        val itemsSummaryBuilder = StringBuilder()
        
        cartList.forEachIndexed { index, cartItem ->
            val product = productsList.find { it.id == cartItem.productId }
            if (product != null) {
                val price = if (product.isDeal && product.originalPrice > 0) product.price else product.price
                totalPrice += price * cartItem.quantity
                if (index < 3) {
                    if (index > 0) itemsSummaryBuilder.append(", ")
                    itemsSummaryBuilder.append("${cartItem.quantity}x ${product.name}")
                }
            }
        }
        
        if (cartList.size > 3) {
            itemsSummaryBuilder.append(" and ${cartList.size - 3} more items")
        }

        val transactionId = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"
        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val orderTime = if (deliveryType == "Pickup") "Pickup: $pickupTime" else "Delivery: Arrives today by 6:00 PM"

        // 2. Insert into Transactions
        paymentDao.insertTransaction(
            Transaction(
                id = transactionId,
                dateTime = "Today, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date()),
                amount = totalPrice,
                storeName = selectedStoreName,
                itemsCount = cartList.sumOf { it.quantity },
                itemsSummary = itemsSummaryBuilder.toString()
            )
        )

        // 3. Create simulated Order
        val newOrder = Order(
            id = orderId,
            type = if (deliveryType == "Pickup") "Grocery Pickup" else "Retail Delivery",
            status = if (deliveryType == "Pickup") "Preparing for pickup" else "Preparing for delivery",
            dateTime = orderTime,
            trackingStep = 1, // Ordered -> Preparing
            progressPercentage = 0.33f,
            addressOrStore = if (deliveryType == "Pickup") selectedStoreName else address,
            itemSummary = itemsSummaryBuilder.toString()
        )
        
        orderDao.insertOrder(newOrder)

        // 4. Clear cart
        cartDao.clearCart()

        return@withContext newOrder
    }

    // Prepopulate Mock Data
    suspend fun populateMockDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isNotEmpty()) {
            Log.d("WalmartRepository", "Database already populated")
            return@withContext
        }

        Log.d("WalmartRepository", "Populating database with realistic mock data...")

        // 1. Store Locations
        val mockStores = listOf(
            StoreLocation("S101", "Springfield Supercenter", "3434 S Campbell Ave, Springfield, MO", "6:00 AM - 11:00 PM", "(417) 887-3400", hasPharmacy = true, hasFuel = true, "1.2 miles", isSelected = true),
            StoreLocation("S102", "Bentonville Flagship Store", "406 S Walton Blvd, Bentonville, AR", "6:00 AM - 11:00 PM", "(479) 273-4005", hasPharmacy = true, hasFuel = true, "4.8 miles"),
            StoreLocation("S103", "Rogers Neighborhood Market", "1819 W Walnut St, Rogers, AR", "7:00 AM - 10:00 PM", "(479) 636-6330", hasPharmacy = true, hasFuel = false, "6.5 miles"),
            StoreLocation("S104", "Orlando Airport Supercenter", "11930 Narcoossee Rd, Orlando, FL", "6:00 AM - 11:00 PM", "(407) 500-2012", hasPharmacy = false, hasFuel = true, "12.3 miles")
        )
        storeDao.insertStores(mockStores)

        // 2. Saved Cards
        val mockCards = listOf(
            PaymentCard(1, "John Doe", "•••• •••• •••• 4242", "12/28", "Visa"),
            PaymentCard(2, "John Doe", "•••• •••• •••• 5555", "08/29", "Mastercard")
        )
        mockCards.forEach { paymentDao.insertCard(it) }

        // 3. Transactions / receipts
        val mockTransactions = listOf(
            Transaction("TX-9842A1", "Aug 10, 2026 04:32 PM", 34.82, "Springfield Supercenter", 5, "Whole Milk, Grade A Eggs, Tillamook Cheddar, Wheat Bread"),
            Transaction("TX-9121B4", "Aug 05, 2026 11:15 AM", 129.50, "Springfield Supercenter", 2, "Samsung 32\" TV, HDMI Cable")
        )
        mockTransactions.forEach { paymentDao.insertTransaction(it) }

        // 4. Pharmacy Prescriptions
        val mockPrescriptions = listOf(
            PharmacyPrescription("RX-200159", "Lipitor (Atorvastatin)", "20mg - Take 1 daily", "John Doe", 3, "Ready for pickup", "S101", "Walmart Pharmacy #101, Springfield"),
            PharmacyPrescription("RX-554211", "Amoxicillin", "500mg - Take 3 daily for 10 days", "John Doe", 0, "Active", "S101", "Walmart Pharmacy #101, Springfield"),
            PharmacyPrescription("RX-884920", "Claritin (Loratadine)", "10mg - Take 1 daily as needed", "John Doe", 5, "Refill in progress", "S101", "Walmart Pharmacy #101, Springfield")
        )
        pharmacyDao.insertPrescriptions(mockPrescriptions)

        // 5. Initial Active Orders
        val mockOrders = listOf(
            Order("ORD-54821", "Grocery Pickup", "Ready for collection", "Pickup: Ready today (Aisle A)", 2, 0.66f, "Springfield Supercenter", "Whole Milk, Fresh Strawberries, Honeycrisp Apples"),
            Order("ORD-19405", "Electronics Delivery", "Out for delivery", "Delivery: Today by 5:00 PM", 2, 0.75f, "123 Maple Dr, Springfield", "Apple iPad 10.2\", Slim iPad Case"),
            Order("ORD-30219", "Household Shipping", "Arriving tomorrow", "Delivery: Tomorrow", 1, 0.33f, "123 Maple Dr, Springfield", "Tide Liquid Detergent, Bounty Paper Towels")
        )
        mockOrders.forEach { orderDao.insertOrder(it) }

        // 6. Products catalog (Grocery & Retail Shopping)
        val mockProducts = listOf(
            // Grocery -> produce (Fresh Food)
            Product("GP01", "Organic Bananas", "produce", 1.89, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&q=80", isGrocery = true, stockCount = 25, aisleLocation = "Aisle 1", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GP02", "Honeycrisp Apples (3 lb)", "produce", 4.98, originalPrice = 5.98, isDeal = true, dealType = "Rollback", imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&q=80", isGrocery = true, stockCount = 12, aisleLocation = "Aisle 1", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GP03", "Fresh Strawberries (1 lb)", "produce", 2.98, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400&q=80", isGrocery = true, stockCount = 18, aisleLocation = "Aisle 2", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GP04", "Avocados (4-pack)", "produce", 3.48, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400&q=80", isGrocery = true, stockCount = 15, aisleLocation = "Aisle 2", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GP05", "Organic Baby Spinach (1 lb)", "produce", 3.28, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&q=80", isGrocery = true, stockCount = 8, aisleLocation = "Aisle 3", deliveryTime = "Arrives today", pickupAvailable = true),

            // Grocery -> dairy
            Product("GD01", "Great Value Whole Milk (1 Gal)", "dairy", 3.48, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&q=80", isGrocery = true, stockCount = 50, aisleLocation = "Aisle 12", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GD02", "Grade A Large White Eggs (18ct)", "dairy", 3.97, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1516448620398-c5f44bf9f441?w=400&q=80", isGrocery = true, stockCount = 30, aisleLocation = "Aisle 12", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GD03", "Tillamook Cheddar Cheese slices", "dairy", 4.48, originalPrice = 4.98, isDeal = true, dealType = "Rollback", imageUrl = "https://images.unsplash.com/photo-1486887396153-fa416525c108?w=400&q=80", isGrocery = true, stockCount = 20, aisleLocation = "Aisle 11", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GD04", "Chobani Greek Yogurt (4-pack)", "dairy", 3.98, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&q=80", isGrocery = true, stockCount = 15, aisleLocation = "Aisle 13", deliveryTime = "Arrives today", pickupAvailable = true),

            // Grocery -> meat
            Product("GM01", "Lean Ground Beef 93/7 (1 lb)", "meat", 5.94, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1588168333986-5078647aa981?w=400&q=80", isGrocery = true, stockCount = 14, aisleLocation = "Aisle 15", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GM02", "Boneless Chicken Breasts (3 lb)", "meat", 9.48, originalPrice = 11.48, isDeal = true, dealType = "Clearance", imageUrl = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400&q=80", isGrocery = true, stockCount = 9, aisleLocation = "Aisle 15", deliveryTime = "Arrives today", pickupAvailable = true),

            // Grocery -> pantry
            Product("GY01", "Great Value Peanut Butter (28 oz)", "pantry", 2.84, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=400&q=80", isGrocery = true, stockCount = 22, aisleLocation = "Aisle 5", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GY02", "Organic Clover Honey (12 oz)", "pantry", 4.28, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=400&q=80", isGrocery = true, stockCount = 16, aisleLocation = "Aisle 6", deliveryTime = "Arrives today", pickupAvailable = true),

            // Grocery -> drinks
            Product("GK01", "Pure Leaf Sweet Iced Tea (6-pack)", "drinks", 5.98, originalPrice = 6.98, isDeal = true, dealType = "Weekly Deal", imageUrl = "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&q=80", isGrocery = true, stockCount = 14, aisleLocation = "Aisle 8", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("GK02", "Coca-Cola Classic Soda (12-pack)", "drinks", 6.48, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&q=80", isGrocery = true, stockCount = 40, aisleLocation = "Aisle 8", deliveryTime = "Arrives today", pickupAvailable = true),

            // Retail -> electronics
            Product("RE01", "Samsung 55\" 4K Smart TV", "electronics", 328.00, originalPrice = 378.00, isDeal = true, dealType = "Rollback", imageUrl = "https://images.unsplash.com/photo-1593305841991-05c297ba4575?w=400&q=80", isGrocery = false, stockCount = 4, aisleLocation = "Aisle G12", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("RE02", "Apple iPad 10.2\" (64GB)", "electronics", 249.00, originalPrice = 329.00, isDeal = true, dealType = "Clearance", imageUrl = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&q=80", isGrocery = false, stockCount = 5, aisleLocation = "Aisle G2", deliveryTime = "Arrives today", pickupAvailable = false),
            Product("RE03", "Sony Over-Ear Active Noise Cancelling", "electronics", 148.00, originalPrice = 198.00, isDeal = true, dealType = "Weekly Deal", imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&q=80", isGrocery = false, stockCount = 8, aisleLocation = "Aisle G5", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("RE04", "Roku Streaming Stick 4K", "electronics", 39.98, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1546054454-aa26e2b734c7?w=400&q=80", isGrocery = false, stockCount = 15, aisleLocation = "Aisle G1", deliveryTime = "Arrives tomorrow", pickupAvailable = true),

            // Retail -> household
            Product("RH01", "Tide Liquid Laundry Detergent", "household", 12.97, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?w=400&q=80", isGrocery = false, stockCount = 20, aisleLocation = "Aisle 22", deliveryTime = "Arrives tomorrow", pickupAvailable = true),
            Product("RH02", "Bounty Paper Towels (8 Double)", "household", 16.98, originalPrice = 19.98, isDeal = true, dealType = "Rollback", imageUrl = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=400&q=80", isGrocery = false, stockCount = 14, aisleLocation = "Aisle 24", deliveryTime = "Arrives tomorrow", pickupAvailable = true),
            Product("RH03", "Charmin Ultra Strong Toilet Paper", "household", 14.97, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1584556812952-905ffd0c611a?w=400&q=80", isGrocery = false, stockCount = 18, aisleLocation = "Aisle 24", deliveryTime = "Arrives tomorrow", pickupAvailable = true),

            // Retail -> personal_care
            Product("RP01", "Colgate Total Whitening Paste", "personal_care", 3.96, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1559599101-f09722fb4948?w=400&q=80", isGrocery = false, stockCount = 25, aisleLocation = "Aisle 28", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("RP02", "Dove Deep Moisture Body Wash", "personal_care", 6.97, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1608248597279-f99d160bfcbc?w=400&q=80", isGrocery = false, stockCount = 12, aisleLocation = "Aisle 29", deliveryTime = "Arrives today", pickupAvailable = true),

            // Pharmacy -> over the counter / health
            Product("RX01", "Advil Pain Reliever (100ct)", "pharmacy", 9.48, isDeal = false, imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400&q=80", isGrocery = false, stockCount = 30, aisleLocation = "Pharmacy", deliveryTime = "Arrives today", pickupAvailable = true),
            Product("RX02", "Flintstones Complete Vitamins", "pharmacy", 7.98, originalPrice = 8.98, isDeal = true, dealType = "Weekly Deal", imageUrl = "https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=400&q=80", isGrocery = false, stockCount = 15, aisleLocation = "Pharmacy", deliveryTime = "Arrives today", pickupAvailable = true)
        )
        productDao.insertProducts(mockProducts)
    }
}
