package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {

    // --- Users ---
    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles")
    fun getAllUsers(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    // --- Couriers ---
    @Query("SELECT * FROM courier_presence")
    fun getAllCouriers(): Flow<List<CourierPresenceEntity>>

    @Query("SELECT * FROM courier_presence WHERE isOnline = 1 AND isBusy = 0 AND isApproved = 1")
    fun getAvailableCouriers(): Flow<List<CourierPresenceEntity>>

    @Query("SELECT * FROM courier_presence WHERE courierId = :courierId")
    suspend fun getCourierDirect(courierId: String): CourierPresenceEntity?

    @Query("SELECT * FROM courier_presence WHERE courierId = :courierId")
    fun getCourierFlow(courierId: String): Flow<CourierPresenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourier(courier: CourierPresenceEntity)

    @Query("UPDATE courier_presence SET isOnline = :isOnline WHERE courierId = :courierId")
    suspend fun setCourierOnline(courierId: String, isOnline: Boolean)

    @Query("UPDATE courier_presence SET isBusy = :isBusy WHERE courierId = :courierId")
    suspend fun setCourierBusy(courierId: String, isBusy: Boolean)

    @Query("UPDATE courier_presence SET currentLat = :lat, currentLng = :lng, lastLocationUpdate = :now WHERE courierId = :courierId")
    suspend fun updateCourierLocation(courierId: String, lat: Double, lng: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE courier_presence SET purchaseLimitMad = :limit WHERE courierId = :courierId")
    suspend fun updatePurchaseLimit(courierId: String, limit: Double)

    @Query("UPDATE courier_presence SET isApproved = :approved WHERE courierId = :courierId")
    suspend fun setCourierApproved(courierId: String, approved: Boolean)

    // --- Service Area ---
    @Query("SELECT * FROM service_areas WHERE id = 1")
    fun getServiceArea(): Flow<ServiceAreaEntity?>

    @Query("SELECT * FROM service_areas WHERE id = 1")
    suspend fun getServiceAreaDirect(): ServiceAreaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceArea(area: ServiceAreaEntity)

    @Query("UPDATE service_areas SET isApproved = :isApproved, updatedAt = :now WHERE id = 1")
    suspend fun setServiceAreaApproved(isApproved: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE service_areas SET polygonJson = :polygonJson, updatedAt = :now WHERE id = 1")
    suspend fun updateServiceAreaPolygon(polygonJson: String, now: Long = System.currentTimeMillis())

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerOrders(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE courierId = :courierId ORDER BY createdAt DESC")
    fun getCourierOrders(courierId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderDirect(orderId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status, updatedAt = :now WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET courierId = :courierId, courierName = :courierName, courierPhone = :courierPhone, status = :status, updatedAt = :now WHERE id = :orderId")
    suspend fun reassignCourier(orderId: Long, courierId: String?, courierName: String?, courierPhone: String?, status: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET courierLat = :lat, courierLng = :lng WHERE id = :orderId")
    suspend fun updateOrderCourierLocation(orderId: Long, lat: Double, lng: Double)

    @Query("UPDATE orders SET status = 'DELIVERED', actualItemCostMad = :actualCost, cashCollectedMad = :cashCollected, receiptNote = :note, updatedAt = :now WHERE id = :orderId")
    suspend fun markOrderDelivered(orderId: Long, actualCost: Double, cashCollected: Double, note: String, now: Long = System.currentTimeMillis())

    // --- Order Events ---
    @Query("SELECT * FROM order_events WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getOrderEvents(orderId: Long): Flow<List<OrderEventEntity>>

    @Insert
    suspend fun insertOrderEvent(event: OrderEventEntity)

    // --- Cash Ledger ---
    @Query("SELECT * FROM cash_ledger ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<CashLedgerEntryEntity>>

    @Query("SELECT * FROM cash_ledger WHERE courierId = :courierId ORDER BY timestamp DESC")
    fun getCourierLedger(courierId: String): Flow<List<CashLedgerEntryEntity>>

    @Insert
    suspend fun insertLedgerEntry(entry: CashLedgerEntryEntity)
}
