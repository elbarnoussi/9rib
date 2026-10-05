package com.example.data.repository

import com.example.data.dao.DeliveryDao
import com.example.data.geo.ChefchaouenGeoFence
import com.example.data.geo.GeoPoint
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class OrderSubmissionResult {
    data class Success(val orderId: Long) : OrderSubmissionResult()
    data class CourierUnavailable(val reason: String) : OrderSubmissionResult()
    data class OutsideServiceArea(val reason: String) : OrderSubmissionResult()
    data class ServiceAreaDisabled(val reason: String) : OrderSubmissionResult()
    data class Error(val message: String) : OrderSubmissionResult()
}

class DeliveryRepository(private val dao: DeliveryDao) {

    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val availableCouriers: Flow<List<CourierPresenceEntity>> = dao.getAvailableCouriers()
    val allCouriers: Flow<List<CourierPresenceEntity>> = dao.getAllCouriers()
    val registeredClients: Flow<List<UserProfileEntity>> = dao.getUsersByRole(UserRole.CUSTOMER.name)
    val serviceArea: Flow<ServiceAreaEntity?> = dao.getServiceArea()
    val allLedgerEntries: Flow<List<CashLedgerEntryEntity>> = dao.getAllLedgerEntries()

    fun getCustomerOrders(customerId: String): Flow<List<OrderEntity>> = dao.getCustomerOrders(customerId)
    fun getCourierOrders(courierId: String): Flow<List<OrderEntity>> = dao.getCourierOrders(courierId)
    fun getOrderById(orderId: Long): Flow<OrderEntity?> = dao.getOrderById(orderId)
    fun getOrderEvents(orderId: Long): Flow<List<OrderEventEntity>> = dao.getOrderEvents(orderId)
    fun getCourierLedger(courierId: String): Flow<List<CashLedgerEntryEntity>> = dao.getCourierLedger(courierId)

    /**
     * Registers a new customer profile in Chefchaouen.
     */
    suspend fun registerNewClient(fullName: String, phone: String, address: String): UserProfileEntity = withContext(Dispatchers.IO) {
        val newId = "customer_${System.currentTimeMillis()}"
        val user = UserProfileEntity(
            id = newId,
            role = UserRole.CUSTOMER.name,
            fullName = fullName.trim(),
            phone = phone.trim(),
            address = address.trim(),
            isApproved = true
        )
        dao.insertUser(user)
        user
    }

    /**
     * Registers a new delivery courier in Chefchaouen (Pending Admin approval).
     */
    suspend fun registerNewCourier(
        fullName: String,
        phone: String,
        vehicleType: String,
        purchaseLimitMad: Double,
        address: String
    ): CourierPresenceEntity = withContext(Dispatchers.IO) {
        val newId = "courier_${System.currentTimeMillis()}"
        // Add to user profiles
        val user = UserProfileEntity(
            id = newId,
            role = UserRole.COURIER.name,
            fullName = fullName.trim(),
            phone = phone.trim(),
            address = address.trim(),
            isApproved = false // Pending admin approval
        )
        dao.insertUser(user)

        // Add to courier presence
        val courier = CourierPresenceEntity(
            courierId = newId,
            courierName = fullName.trim(),
            courierPhone = phone.trim(),
            isOnline = false, // starts offline until approved
            isBusy = false,
            currentLat = ChefchaouenGeoFence.CHEFCHAOUEN_CENTER.lat,
            currentLng = ChefchaouenGeoFence.CHEFCHAOUEN_CENTER.lng,
            vehicleType = vehicleType.trim(),
            rating = 5.0,
            totalDeliveries = 0,
            purchaseLimitMad = purchaseLimitMad,
            isApproved = false // Must be approved by admin
        )
        dao.insertCourier(courier)
        courier
    }

    /**
     * Seeds initial database state if empty.
     */
    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingArea = dao.getServiceAreaDirect()
        if (existingArea == null) {
            val polygonJson = serializePolygon(ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON)
            dao.insertServiceArea(
                ServiceAreaEntity(
                    id = 1,
                    name = "شفشاون - الحيز البلدي والمدينة القديمة",
                    polygonJson = polygonJson,
                    isApproved = true
                )
            )
        }

        // Seed default profiles
        val users = listOf(
            UserProfileEntity(
                id = "customer_1",
                role = UserRole.CUSTOMER.name,
                fullName = "فاطمة الزهراء العلمي (Fatima Zahra)",
                phone = "+212 663-774411",
                address = "درب الصور، حي الأندلس، شفشاون",
                isApproved = true
            ),
            UserProfileEntity(
                id = "courier_1",
                role = UserRole.COURIER.name,
                fullName = "حمزة البقالي (Hamza El Bakkali)",
                phone = "+212 672-451298",
                address = "باب العين، شفشاون",
                isApproved = true
            ),
            UserProfileEntity(
                id = "admin_1",
                role = UserRole.ADMIN.name,
                fullName = "إدارة سخرة شفشاون (Admin Chaouen)",
                phone = "+212 539-986000",
                address = "شارع الحسن الثاني، شفشاون",
                isApproved = true
            )
        )
        for (u in users) {
            dao.insertUser(u)
        }

        // Seed couriers
        val initialCouriers = listOf(
            CourierPresenceEntity(
                courierId = "courier_1",
                courierName = "حمزة البقالي (Hamza El Bakkali)",
                courierPhone = "+212 672-451298",
                isOnline = true,
                isBusy = false,
                currentLat = 35.1685,
                currentLng = -5.2630, // Near Outa El Hammam
                vehicleType = "Moped (موطور)",
                rating = 4.95,
                totalDeliveries = 124,
                purchaseLimitMad = 400.0,
                isApproved = true
            ),
            CourierPresenceEntity(
                courierId = "courier_2",
                courierName = "سفيان الريفي (Soufiane Rifi)",
                courierPhone = "+212 661-892341",
                isOnline = true,
                isBusy = false,
                currentLat = 35.1710,
                currentLng = -5.2655, // Near Bab Souk
                vehicleType = "Moped (موطور)",
                rating = 4.88,
                totalDeliveries = 89,
                purchaseLimitMad = 300.0,
                isApproved = true
            ),
            CourierPresenceEntity(
                courierId = "courier_3",
                courierName = "عثمان الشاوني (Othmane Chaouni)",
                courierPhone = "+212 615-998877",
                isOnline = true,
                isBusy = false,
                currentLat = 35.1725,
                currentLng = -5.2585, // Near Ras El Maa
                vehicleType = "Bicycle (بيكالا)",
                rating = 4.82,
                totalDeliveries = 53,
                purchaseLimitMad = 250.0,
                isApproved = true
            ),
            CourierPresenceEntity(
                courierId = "courier_4_pending",
                courierName = "يوسف بنعلي (Youssef Benali)",
                courierPhone = "+212 654-112233",
                isOnline = false,
                isBusy = false,
                currentLat = 35.1640,
                currentLng = -5.2680,
                vehicleType = "Moped (موطور)",
                rating = 5.0,
                totalDeliveries = 0,
                purchaseLimitMad = 200.0,
                isApproved = false // Pending approval by Admin
            )
        )
        for (c in initialCouriers) {
            dao.insertCourier(c)
        }
    }

    /**
     * Validates a pin location against the Chefchaouen service area polygon.
     */
    suspend fun validateLocation(point: GeoPoint): Result<Unit> = withContext(Dispatchers.IO) {
        val area = dao.getServiceAreaDirect()
            ?: return@withContext Result.failure(IllegalStateException("لم يتم تكوين حيز الخدمة في النظام (No service area configured)"))

        if (!area.isApproved) {
            return@withContext Result.failure(IllegalStateException("حيز خدمة شفشاون معطل حالياً من طرف الإدارة (Service area is currently disabled)"))
        }

        val polygon = parsePolygon(area.polygonJson)
        if (!ChefchaouenGeoFence.isInsidePolygon(point, polygon)) {
            return@withContext Result.failure(IllegalArgumentException("الموقع المحدد خارج حيز التوصيل المعتمد لمدينة شفشاون (Location outside Chefchaouen boundary)"))
        }

        Result.success(Unit)
    }

    /**
     * Atomically validates location, checks courier availability, reserves courier, and places order.
     */
    suspend fun submitOrder(
        customerId: String,
        customerName: String,
        customerPhone: String,
        courierId: String,
        requestType: RequestType,
        shopName: String,
        itemDescription: String,
        deliveryAddress: String,
        deliveryPoint: GeoPoint,
        estimatedCostMad: Double,
        deliveryFeeMad: Double
    ): OrderSubmissionResult = withContext(Dispatchers.IO) {
        // 1. Check Service Area
        val area = dao.getServiceAreaDirect()
            ?: return@withContext OrderSubmissionResult.ServiceAreaDisabled("حيز الخدمة غير متاح في النظام")
        if (!area.isApproved) {
            return@withContext OrderSubmissionResult.ServiceAreaDisabled("الطلبات معلقة مؤقتاً في شفشاون بقرار الإدارة")
        }
        val polygon = parsePolygon(area.polygonJson)
        if (!ChefchaouenGeoFence.isInsidePolygon(deliveryPoint, polygon)) {
            return@withContext OrderSubmissionResult.OutsideServiceArea("الموقع المختار خارج نطاق التوصيل بمدينة شفشاون")
        }

        // 2. Concurrency re-check: Verify courier availability
        val courier = dao.getCourierDirect(courierId)
            ?: return@withContext OrderSubmissionResult.CourierUnavailable("الليفروغ غير مسجل بالنظام")

        if (!courier.isApproved) {
            return@withContext OrderSubmissionResult.CourierUnavailable("حساب الليفروغ لم تتم الموافقة عليه بعد")
        }
        if (!courier.isOnline) {
            return@withContext OrderSubmissionResult.CourierUnavailable("الليفروغ أصبح غير متصل حالياً. يرجى اختيار ليفروغ آخر")
        }
        if (courier.isBusy) {
            return@withContext OrderSubmissionResult.CourierUnavailable("الليفروغ مشغول بطلب آخر حالياً. يرجى اختيار ليفروغ آخر")
        }

        // 3. Atomically reserve courier
        dao.setCourierBusy(courierId, true)

        // 4. Create Order
        val order = OrderEntity(
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            courierId = courierId,
            courierName = courier.courierName,
            courierPhone = courier.courierPhone,
            requestType = requestType.name,
            shopName = shopName,
            itemDescription = itemDescription,
            deliveryAddressText = deliveryAddress,
            deliveryLat = deliveryPoint.lat,
            deliveryLng = deliveryPoint.lng,
            status = OrderStatus.REQUESTED.name,
            estimatedCostMad = estimatedCostMad,
            deliveryFeeMad = deliveryFeeMad,
            appCommissionMad = 2.0,
            courierPurchaseLimit = courier.purchaseLimitMad,
            courierLat = courier.currentLat,
            courierLng = courier.currentLng
        )
        val orderId = dao.insertOrder(order)

        // 5. Add event
        dao.insertOrderEvent(
            OrderEventEntity(
                orderId = orderId,
                status = OrderStatus.REQUESTED.name,
                note = "تم تقديم الطلب وحجز الليفروغ ${courier.courierName}"
            )
        )

        OrderSubmissionResult.Success(orderId)
    }

    /**
     * Courier accepts an assigned order.
     */
    suspend fun acceptOrder(orderId: Long, courierId: String) = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, OrderStatus.ACCEPTED.name)
        dao.insertOrderEvent(
            OrderEventEntity(
                orderId = orderId,
                status = OrderStatus.ACCEPTED.name,
                note = "وافق الليفروغ على الطلب وهو في طريقه إلى المحل"
            )
        )
    }

    /**
     * Courier declines an assigned order or cancels.
     */
    suspend fun declineOrder(orderId: Long, courierId: String, reason: String = "اعتذر الليفروغ عن قبول الطلب") = withContext(Dispatchers.IO) {
        // Free the courier
        dao.setCourierBusy(courierId, false)
        // Mark order cancelled so customer is prompted to pick another courier
        dao.updateOrderStatus(orderId, OrderStatus.CANCELLED.name)
        dao.insertOrderEvent(
            OrderEventEntity(
                orderId = orderId,
                status = OrderStatus.CANCELLED.name,
                note = reason
            )
        )
    }

    /**
     * Advance order status through lifecycle steps.
     */
    suspend fun advanceOrderStatus(orderId: Long, nextStatus: OrderStatus, note: String = "") = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, nextStatus.name)
        dao.insertOrderEvent(
            OrderEventEntity(
                orderId = orderId,
                status = nextStatus.name,
                note = note.ifBlank { "تم تحديث حالة الطلب إلى: ${nextStatus.name}" }
            )
        )
    }

    /**
     * Finalize delivery and write cash ledger entry.
     */
    suspend fun finalizeDeliveryWithLedger(
        orderId: Long,
        courierId: String,
        actualItemCost: Double,
        deliveryFee: Double,
        appCommission: Double,
        cashCollected: Double,
        receiptNote: String
    ) = withContext(Dispatchers.IO) {
        // Update Order
        dao.markOrderDelivered(orderId, actualItemCost, cashCollected, receiptNote)
        // Free courier
        dao.setCourierBusy(courierId, false)
        // Insert Cash Ledger
        dao.insertLedgerEntry(
            CashLedgerEntryEntity(
                orderId = orderId,
                courierId = courierId,
                itemCostMad = actualItemCost,
                deliveryFeeMad = deliveryFee,
                appCommissionMad = appCommission,
                totalCashCollectedMad = cashCollected,
                receiptNote = receiptNote
            )
        )
        // Event
        dao.insertOrderEvent(
            OrderEventEntity(
                orderId = orderId,
                status = OrderStatus.DELIVERED.name,
                note = "تم تسليم الطلب واستلام المبلغ كاش: $cashCollected درهم"
            )
        )
    }

    // Courier Presence Controls
    suspend fun setCourierOnline(courierId: String, online: Boolean) = withContext(Dispatchers.IO) {
        dao.setCourierOnline(courierId, online)
    }

    suspend fun updateCourierLocation(courierId: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        dao.updateCourierLocation(courierId, lat, lng)
    }

    suspend fun updateCourierPurchaseLimit(courierId: String, limitMad: Double) = withContext(Dispatchers.IO) {
        dao.updatePurchaseLimit(courierId, limitMad)
    }

    suspend fun updateCourierApproval(courierId: String, approved: Boolean) = withContext(Dispatchers.IO) {
        dao.setCourierApproved(courierId, approved)
    }

    // Service Area Controls
    suspend fun setServiceAreaApproved(approved: Boolean) = withContext(Dispatchers.IO) {
        dao.setServiceAreaApproved(approved)
    }

    suspend fun updateServiceAreaPolygon(polygon: List<GeoPoint>) = withContext(Dispatchers.IO) {
        dao.updateServiceAreaPolygon(serializePolygon(polygon))
    }

    // Helper functions for polygon serialization
    fun serializePolygon(points: List<GeoPoint>): String {
        return points.joinToString(";") { "${it.lat},${it.lng}" }
    }

    fun parsePolygon(serialized: String): List<GeoPoint> {
        if (serialized.isBlank()) return ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON
        return try {
            serialized.split(";").mapNotNull { part ->
                val coords = part.split(",")
                if (coords.size == 2) {
                    GeoPoint(coords[0].trim().toDouble(), coords[1].trim().toDouble())
                } else null
            }
        } catch (e: Exception) {
            ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON
        }
    }
}
