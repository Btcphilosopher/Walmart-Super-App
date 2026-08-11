package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.WalmartRepository
import java.util.UUID
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartItemWithProduct(
    val cartItem: CartItem,
    val product: Product
)

class WalmartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WalmartRepository(application)

    // State flows from Room
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deals: StateFlow<List<Product>> = repository.deals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prescriptions: StateFlow<List<PharmacyPrescription>> = repository.prescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCards: StateFlow<List<PaymentCard>> = repository.savedCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stores: StateFlow<List<StoreLocation>> = repository.allStores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStore: StateFlow<StoreLocation?> = repository.selectedStore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Combined cart state
    val cartWithProducts: StateFlow<List<CartItemWithProduct>> = combine(
        repository.cartItems,
        repository.allProducts
    ) { cartList, productList ->
        cartList.mapNotNull { cartItem ->
            val product = productList.find { it.id == cartItem.productId }
            product?.let { CartItemWithProduct(cartItem, it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart total calculations
    val cartTotal: StateFlow<Double> = cartWithProducts.map { items ->
        items.sumOf { it.product.price * it.cartItem.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotalCount: StateFlow<Int> = cartWithProducts.map { items ->
        items.sumOf { it.cartItem.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI Input States
    val searchQuery = MutableStateFlow("")
    val deliveryMode = MutableStateFlow("Pickup") // Pickup or Delivery
    val shippingAddress = MutableStateFlow("123 Maple Dr, Springfield, MO 65804")
    val selectedPickupTime = MutableStateFlow("Today, 4:00 PM - 5:00 PM")
    val checkoutSuccessOrder = MutableStateFlow<Order?>(null)
    
    // Walmart+ States
    val walmartPlusStatus = MutableStateFlow(true) // Start as member by default for demonstration
    val fuelDiscountCode = MutableStateFlow<String?>(null)

    // Walmart Pay State
    // IDLE -> SCANNING -> CONFIRMING -> SUCCESS
    val walmartPayState = MutableStateFlow("IDLE")
    val payAmount = MutableStateFlow(0.0)

    // Search results filtering
    val searchResults: StateFlow<List<Product>> = combine(
        searchQuery,
        repository.allProducts
    ) { query, allProds ->
        if (query.isBlank()) {
            emptyList()
        } else {
            allProds.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.aisleLocation.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Populates standard mock products, stores, prescriptions, saved cards if database is empty.
        viewModelScope.launch {
            repository.populateMockDataIfNeeded()
        }
    }

    // Actions
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setDeliveryMode(mode: String) {
        deliveryMode.value = mode
    }

    fun setShippingAddress(address: String) {
        shippingAddress.value = address
    }

    fun setSelectedPickupTime(time: String) {
        selectedPickupTime.value = time
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product.id, quantity)
        }
    }

    fun updateCartQuantity(cartItemId: Int, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, newQuantity)
        }
    }

    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun selectStore(storeId: String) {
        viewModelScope.launch {
            repository.selectStore(storeId)
        }
    }

    fun refillPrescription(prescriptionId: String) {
        viewModelScope.launch {
            repository.requestRefill(prescriptionId)
        }
    }

    fun requestRefill(prescriptionId: String) {
        viewModelScope.launch {
            repository.requestRefill(prescriptionId)
        }
    }

    fun addPrescription(name: String, rxNumber: String) {
        viewModelScope.launch {
            repository.addPrescription(name, rxNumber)
        }
    }

    fun advanceOrderStatus(orderId: String) {
        viewModelScope.launch {
            repository.advanceOrderStatus(orderId)
        }
    }

    fun addPaymentCard(holder: String, number: String, expiry: String, type: String) {
        viewModelScope.launch {
            // Standard formatting
            val masked = if (number.length >= 4) {
                "•••• •••• •••• " + number.takeLast(4)
            } else {
                "•••• •••• •••• $number"
            }
            repository.saveCard(PaymentCard(cardholderName = holder, cardNumber = masked, expiryDate = expiry, cardType = type))
        }
    }

    fun deleteCard(card: PaymentCard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun startWalmartPay(amount: Double) {
        payAmount.value = amount
        walmartPayState.value = "SCANNING"
    }

    fun confirmWalmartPayPayment() {
        viewModelScope.launch {
            walmartPayState.value = "CONFIRMING"
            kotlinx.coroutines.delay(1000) // visual flow
            
            // Generate receipt & transaction
            val transactionId = "TX-" + UUID.randomUUID().toString().take(8).uppercase()
            val simpleTime = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.US).format(java.util.Date())
            
            // Add transaction
            val activeStore = selectedStore.value?.name ?: "Springfield Supercenter"
            
            repository.checkout(
                paymentMethod = "Walmart Pay",
                deliveryType = deliveryMode.value,
                selectedStoreName = activeStore,
                pickupTime = selectedPickupTime.value,
                address = shippingAddress.value
            )
            
            walmartPayState.value = "SUCCESS"
        }
    }

    fun resetWalmartPay() {
        walmartPayState.value = "IDLE"
    }

    fun simulateCheckout() {
        viewModelScope.launch {
            val activeStore = selectedStore.value?.name ?: "Springfield Supercenter"
            val order = repository.checkout(
                paymentMethod = "Saved Card",
                deliveryType = deliveryMode.value,
                selectedStoreName = activeStore,
                pickupTime = selectedPickupTime.value,
                address = shippingAddress.value
            )
            checkoutSuccessOrder.value = order
        }
    }

    fun clearCheckoutSuccess() {
        checkoutSuccessOrder.value = null
    }

    fun toggleWalmartPlus() {
        walmartPlusStatus.value = !walmartPlusStatus.value
    }

    fun generateFuelDiscount() {
        val randCode = (100000..999999).random().toString()
        fuelDiscountCode.value = "${randCode.take(3)}-${randCode.takeLast(3)}"
    }

    fun resetFuelDiscount() {
        fuelDiscountCode.value = null
    }
}
