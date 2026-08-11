package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isDeal = 1")
    fun getDeals(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProduct(productId: String): CartItem?
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): Order?
}

@Dao
interface PharmacyDao {
    @Query("SELECT * FROM prescriptions")
    fun getAllPrescriptions(): Flow<List<PharmacyPrescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptions(prescriptions: List<PharmacyPrescription>)

    @Update
    suspend fun updatePrescription(prescription: PharmacyPrescription)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_cards")
    fun getSavedCards(): Flow<List<PaymentCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: PaymentCard)

    @Delete
    suspend fun deleteCard(card: PaymentCard)

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<com.example.data.model.Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: com.example.data.model.Transaction)
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM store_locations")
    fun getAllStores(): Flow<List<StoreLocation>>

    @Query("SELECT * FROM store_locations WHERE isSelected = 1 LIMIT 1")
    fun getSelectedStore(): Flow<StoreLocation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreLocation>)

    @Query("UPDATE store_locations SET isSelected = 0")
    suspend fun clearSelectedStore()

    @Query("UPDATE store_locations SET isSelected = 1 WHERE id = :storeId")
    suspend fun selectStoreById(storeId: String)

    @androidx.room.Transaction
    suspend fun setSelectedStore(storeId: String) {
        clearSelectedStore()
        selectStoreById(storeId)
    }
}
