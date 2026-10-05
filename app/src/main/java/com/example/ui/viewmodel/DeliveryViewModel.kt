package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.geo.ChefchaouenGeoFence
import com.example.data.geo.GeoPoint
import com.example.data.model.*
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.OrderSubmissionResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerUiState(
    val customerId: String = "customer_1",
    val customerName: String = "فاطمة الزهراء العلمي (Fatima Zahra)",
    val customerPhone: String = "+212 663-774411",
    val customerAddress: String = "درب الصور، حي الأندلس، شفشاون",
    val deliveryPin: GeoPoint = ChefchaouenGeoFence.CHEFCHAOUEN_CENTER,
    val isInsideServiceArea: Boolean = true,
    val serviceAreaErrorMessage: String? = null,
    val selectedCourier: CourierPresenceEntity? = null,
    val requestType: RequestType = RequestType.BUY_FOR_ME,
    val shopName: String = "",
    val itemDescription: String = "",
    val estimatedCostMad: Double = 40.0,
    val deliveryFeeMad: Double = 12.0,
    val isSubmitting: Boolean = false,
    val submissionError: String? = null,
    val activeOrderId: Long? = null,
    val showRegisterDialog: Boolean = false,
    val showSwitchDialog: Boolean = false,
    val toastMessage: String? = null
)

data class CourierUiState(
    val currentCourierId: String = "courier_1",
    val isOnline: Boolean = true,
    val purchaseLimitMad: Double = 350.0,
    val isApproved: Boolean = true,
    val showRegisterDialog: Boolean = false,
    val showSwitchDialog: Boolean = false,
    val toastMessage: String? = null
)

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeliveryRepository

    // Role & Localization
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.DARIJA)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Customer UI State
    private val _customerState = MutableStateFlow(CustomerUiState())
    val customerState: StateFlow<CustomerUiState> = _customerState.asStateFlow()

    // Courier UI State
    private val _courierState = MutableStateFlow(CourierUiState())
    val courierState: StateFlow<CourierUiState> = _courierState.asStateFlow()

    // Database reactive streams
    val availableCouriers: StateFlow<List<CourierPresenceEntity>>
    val allCouriers: StateFlow<List<CourierPresenceEntity>>
    val registeredClients: StateFlow<List<UserProfileEntity>>
    val allOrders: StateFlow<List<OrderEntity>>
    val serviceArea: StateFlow<ServiceAreaEntity?>
    val allLedger: StateFlow<List<CashLedgerEntryEntity>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DeliveryRepository(db.deliveryDao())

        availableCouriers = repository.availableCouriers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allCouriers = repository.allCouriers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        registeredClients = repository.registeredClients
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allOrders = repository.allOrders
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        serviceArea = repository.serviceArea
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allLedger = repository.allLedgerEntries
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Seed initial data
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            validateCurrentPin(_customerState.value.deliveryPin)
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.DARIJA) {
            AppLanguage.FRENCH
        } else {
            AppLanguage.DARIJA
        }
    }

    // --- Customer Actions & Registration ---
    fun setShowRegisterClientDialog(show: Boolean) {
        _customerState.update { it.copy(showRegisterDialog = show) }
    }

    fun setShowSwitchClientDialog(show: Boolean) {
        _customerState.update { it.copy(showSwitchDialog = show) }
    }

    fun registerNewClient(fullName: String, phone: String, address: String) {
        viewModelScope.launch {
            val user = repository.registerNewClient(fullName, phone, address)
            _customerState.update {
                it.copy(
                    customerId = user.id,
                    customerName = user.fullName,
                    customerPhone = user.phone,
                    customerAddress = user.address,
                    showRegisterDialog = false,
                    toastMessage = "مرحباً بك! تم تسجيل حسابك بنجاح (${user.fullName})"
                )
            }
        }
    }

    fun switchClient(user: UserProfileEntity) {
        _customerState.update {
            it.copy(
                customerId = user.id,
                customerName = user.fullName,
                customerPhone = user.phone,
                customerAddress = user.address,
                showSwitchDialog = false,
                toastMessage = "تم التبديل إلى حساب: ${user.fullName}"
            )
        }
    }

    fun clearCustomerToast() {
        _customerState.update { it.copy(toastMessage = null) }
    }

    fun onPinSelected(point: GeoPoint) {
        _customerState.update { it.copy(deliveryPin = point) }
        validateCurrentPin(point)
    }

    private fun validateCurrentPin(point: GeoPoint) {
        viewModelScope.launch {
            val poly = serviceArea.value?.let { repository.parsePolygon(it.polygonJson) }
                ?: ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON

            val isAreaApproved = serviceArea.value?.isApproved ?: true
            val inside = ChefchaouenGeoFence.isInsidePolygon(point, poly)

            val dist = ChefchaouenGeoFence.distanceKm(point, ChefchaouenGeoFence.CHEFCHAOUEN_CENTER)
            val fee = ChefchaouenGeoFence.calculateDeliveryFeeMad(dist)

            _customerState.update { state ->
                state.copy(
                    isInsideServiceArea = inside && isAreaApproved,
                    serviceAreaErrorMessage = when {
                        !isAreaApproved -> "الخدمة معطلة حالياً بقرار الإدارة"
                        !inside -> "الموقع المختار خارج نطاق التوصيل بمدينة شفشاون"
                        else -> null
                    },
                    deliveryFeeMad = fee
                )
            }
        }
    }

    fun selectCourier(courier: CourierPresenceEntity) {
        _customerState.update { it.copy(selectedCourier = courier) }
    }

    fun setRequestType(type: RequestType) {
        _customerState.update { it.copy(requestType = type) }
    }

    fun updateShopName(name: String) {
        _customerState.update { it.copy(shopName = name) }
    }

    fun updateItemDescription(desc: String) {
        _customerState.update { it.copy(itemDescription = desc) }
    }

    fun updateEstimatedCost(cost: Double) {
        _customerState.update { it.copy(estimatedCostMad = cost) }
    }

    fun submitOrder() {
        val state = _customerState.value
        val courier = state.selectedCourier

        if (courier == null) {
            _customerState.update { it.copy(submissionError = "يرجى اختيار ليفروغ قبل إرسال الطلب") }
            return
        }
        if (state.shopName.isBlank()) {
            _customerState.update { it.copy(submissionError = "يرجى كتابة اسم المحل أو المطعم") }
            return
        }
        if (state.itemDescription.isBlank()) {
            _customerState.update { it.copy(submissionError = "يرجى وصف السخرة والمشتريات") }
            return
        }

        _customerState.update { it.copy(isSubmitting = true, submissionError = null) }

        viewModelScope.launch {
            val result = repository.submitOrder(
                customerId = state.customerId,
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                courierId = courier.courierId,
                requestType = state.requestType,
                shopName = state.shopName,
                itemDescription = state.itemDescription,
                deliveryAddress = "${state.customerAddress} - موقع محدد بالخريطة",
                deliveryPoint = state.deliveryPin,
                estimatedCostMad = state.estimatedCostMad,
                deliveryFeeMad = state.deliveryFeeMad
            )

            when (result) {
                is OrderSubmissionResult.Success -> {
                    _customerState.update {
                        it.copy(
                            isSubmitting = false,
                            activeOrderId = result.orderId,
                            submissionError = null
                        )
                    }
                }
                is OrderSubmissionResult.CourierUnavailable -> {
                    _customerState.update {
                        it.copy(
                            isSubmitting = false,
                            selectedCourier = null,
                            submissionError = result.reason
                        )
                    }
                }
                is OrderSubmissionResult.OutsideServiceArea -> {
                    _customerState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionError = result.reason
                        )
                    }
                }
                is OrderSubmissionResult.ServiceAreaDisabled -> {
                    _customerState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionError = result.reason
                        )
                    }
                }
                is OrderSubmissionResult.Error -> {
                    _customerState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionError = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearActiveOrder() {
        _customerState.update {
            it.copy(
                activeOrderId = null,
                selectedCourier = null,
                shopName = "",
                itemDescription = "",
                submissionError = null
            )
        }
    }

    // --- Courier Actions & Registration ---
    fun setShowRegisterCourierDialog(show: Boolean) {
        _courierState.update { it.copy(showRegisterDialog = show) }
    }

    fun setShowSwitchCourierDialog(show: Boolean) {
        _courierState.update { it.copy(showSwitchDialog = show) }
    }

    fun registerNewCourier(
        fullName: String,
        phone: String,
        vehicleType: String,
        purchaseLimitMad: Double,
        address: String
    ) {
        viewModelScope.launch {
            val courier = repository.registerNewCourier(fullName, phone, vehicleType, purchaseLimitMad, address)
            _courierState.update {
                it.copy(
                    currentCourierId = courier.courierId,
                    purchaseLimitMad = courier.purchaseLimitMad,
                    isOnline = false,
                    isApproved = false,
                    showRegisterDialog = false,
                    toastMessage = "تم تسجيل طلبك بنجاح! حسابك بانتظار موافقة الإدارة."
                )
            }
        }
    }

    fun switchCourier(courierId: String) {
        val courier = allCouriers.value.find { it.courierId == courierId }
        _courierState.update {
            it.copy(
                currentCourierId = courierId,
                purchaseLimitMad = courier?.purchaseLimitMad ?: 350.0,
                isOnline = courier?.isOnline ?: false,
                isApproved = courier?.isApproved ?: false,
                showSwitchDialog = false,
                toastMessage = "تم الانتقال إلى حساب: ${courier?.courierName ?: courierId}"
            )
        }
    }

    fun clearCourierToast() {
        _courierState.update { it.copy(toastMessage = null) }
    }

    fun setCourierOnline(online: Boolean) {
        val courierId = _courierState.value.currentCourierId
        _courierState.update { it.copy(isOnline = online) }
        viewModelScope.launch {
            repository.setCourierOnline(courierId, online)
        }
    }

    fun updateCourierPurchaseLimit(limitMad: Double) {
        val courierId = _courierState.value.currentCourierId
        _courierState.update { it.copy(purchaseLimitMad = limitMad) }
        viewModelScope.launch {
            repository.updateCourierPurchaseLimit(courierId, limitMad)
        }
    }

    fun courierAcceptOrder(orderId: Long) {
        val courierId = _courierState.value.currentCourierId
        viewModelScope.launch {
            repository.acceptOrder(orderId, courierId)
        }
    }

    fun courierDeclineOrder(orderId: Long) {
        val courierId = _courierState.value.currentCourierId
        viewModelScope.launch {
            repository.declineOrder(orderId, courierId)
        }
    }

    fun advanceOrderStatus(orderId: Long, nextStatus: OrderStatus) {
        viewModelScope.launch {
            repository.advanceOrderStatus(orderId, nextStatus)
        }
    }

    fun finalizeDeliveryWithCash(
        orderId: Long,
        actualCost: Double,
        deliveryFee: Double,
        cashCollected: Double,
        receiptNote: String
    ) {
        val courierId = _courierState.value.currentCourierId
        val appCommission = 2.0
        viewModelScope.launch {
            repository.finalizeDeliveryWithLedger(
                orderId = orderId,
                courierId = courierId,
                actualItemCost = actualCost,
                deliveryFee = deliveryFee,
                appCommission = appCommission,
                cashCollected = cashCollected,
                receiptNote = receiptNote
            )
        }
    }

    // --- Admin Actions ---
    fun toggleServiceAreaApproval(isApproved: Boolean) {
        viewModelScope.launch {
            repository.setServiceAreaApproved(isApproved)
            validateCurrentPin(_customerState.value.deliveryPin)
        }
    }

    fun approveCourier(courierId: String, approved: Boolean) {
        viewModelScope.launch {
            repository.updateCourierApproval(courierId, approved)
        }
    }

    fun getOrderFlow(orderId: Long): Flow<OrderEntity?> {
        return repository.getOrderById(orderId)
    }

    fun getOrderEvents(orderId: Long): Flow<List<OrderEventEntity>> {
        return repository.getOrderEvents(orderId)
    }
}
