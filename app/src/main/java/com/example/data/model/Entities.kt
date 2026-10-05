package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val role: String, // CUSTOMER, COURIER, ADMIN
    val fullName: String,
    val phone: String, // E.g. +212 612-345678
    val address: String,
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courier_presence")
data class CourierPresenceEntity(
    @PrimaryKey val courierId: String,
    val courierName: String,
    val courierPhone: String,
    val isOnline: Boolean = true,
    val isBusy: Boolean = false, // True when courier is reserved or actively delivering
    val currentLat: Double,
    val currentLng: Double,
    val lastLocationUpdate: Long = System.currentTimeMillis(),
    val vehicleType: String = "Moped", // Moped, Scooter, Bicycle, On Foot
    val rating: Double = 4.9,
    val totalDeliveries: Int = 42,
    val purchaseLimitMad: Double = 350.0, // Configurable max cash courier can front
    val isApproved: Boolean = true // Admin approval required
)

@Entity(tableName = "service_areas")
data class ServiceAreaEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "شفشاون - الحيز البلدي والمدينة القديمة",
    val polygonJson: String, // Comma-separated or serialized points
    val isApproved: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val courierId: String?,
    val courierName: String?,
    val courierPhone: String?,
    val requestType: String, // BUY_FOR_ME vs PICKUP_ONLY
    val shopName: String,
    val itemDescription: String,
    val deliveryAddressText: String,
    val deliveryLat: Double,
    val deliveryLng: Double,
    val status: String, // REQUESTED, ACCEPTED, AT_SHOP, PURCHASED, ON_THE_WAY, DELIVERED, CANCELLED
    val estimatedCostMad: Double = 50.0,
    val actualItemCostMad: Double = 0.0,
    val deliveryFeeMad: Double = 15.0,
    val appCommissionMad: Double = 2.0,
    val cashCollectedMad: Double = 0.0,
    val receiptNote: String = "",
    val courierPurchaseLimit: Double = 350.0,
    val courierLat: Double = 35.1688,
    val courierLng: Double = -5.2636,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_events")
data class OrderEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "cash_ledger")
data class CashLedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val courierId: String,
    val itemCostMad: Double,
    val deliveryFeeMad: Double,
    val appCommissionMad: Double,
    val totalCashCollectedMad: Double,
    val receiptNote: String,
    val timestamp: Long = System.currentTimeMillis()
)
