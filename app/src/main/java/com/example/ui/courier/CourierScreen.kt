package com.example.ui.courier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.ContactActionButtons
import com.example.ui.components.OrderStatusPipeline
import com.example.ui.components.RegisterCourierDialog
import com.example.ui.components.SwitchCourierDialog
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun CourierScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val courierState by viewModel.courierState.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val allCouriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allLedger by viewModel.allLedger.collectAsStateWithLifecycle()

    // Find current courier entity
    val courier = allCouriers.find { it.courierId == courierState.currentCourierId }
    val isApproved = courier?.isApproved ?: true
    val isOnline = courier?.isOnline ?: true
    val currentLimit = courier?.purchaseLimitMad ?: 350.0

    // Filter orders assigned to this courier only
    val assignedOrders = allOrders.filter { it.courierId == courierState.currentCourierId }
    val activeOrders = assignedOrders.filter {
        it.status != OrderStatus.DELIVERED.name && it.status != OrderStatus.CANCELLED.name
    }
    val completedOrders = assignedOrders.filter { it.status == OrderStatus.DELIVERED.name }

    // Dialog state for finalizing order & entering cash ledger
    var orderToDeliver by remember { mutableStateOf<OrderEntity?>(null) }
    var actualItemCostInput by remember { mutableStateOf("") }
    var cashCollectedInput by remember { mutableStateOf("") }
    var receiptNoteInput by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // 1. Courier Profile & Approval Status Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(ChaouenCobalt, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = courier?.courierName ?: "حمزة البقالي",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = courier?.vehicleType ?: "Moped (موطور)",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.setShowSwitchCourierDialog(true) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("switch_courier_button")
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Courier", tint = ChaouenCobalt)
                                }

                                IconButton(
                                    onClick = { viewModel.setShowRegisterCourierDialog(true) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("register_courier_button")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Register Courier", tint = MoroccanMint)
                                }

                                // Approval Status Badge
                                Surface(
                                    color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isApproved) AppStrings.courierApprovedStatus(currentLanguage)
                                               else AppStrings.courierPendingApproval(currentLanguage),
                                        color = if (isApproved) MoroccanMint else MoroccanAmberDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Success / Toast message
                        courierState.toastMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = msg, fontSize = 11.sp, color = MoroccanMint, fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { viewModel.clearCourierToast() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = MoroccanMint, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFECEFF1))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Availability Toggle (Online / Offline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "جاهزية استقبال الطلبات",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isOnline) AppStrings.availableStatus(currentLanguage)
                                       else AppStrings.offlineStatus(currentLanguage),
                                fontSize = 12.sp,
                                color = if (isOnline) MoroccanMint else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = { viewModel.setCourierOnline(it) },
                            enabled = isApproved,
                            modifier = Modifier.testTag("courier_availability_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MoroccanMint
                            )
                        )
                    }

                    if (!isApproved) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تنبيه: لا يمكنك استقبال الطلبات حتى تقوم إدارة شفشاون بالموافقة على حسابك.",
                            color = MoroccanAmberDark,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purchase Limit Setting (Fronting cash)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "سقف الشراء النقدي (Plafond d'achat):",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "الحد الأقصى للمال الذي يمكنك دفعه مسبقاً",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${currentLimit.toInt()} ${AppStrings.currencyMad(currentLanguage)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = ChaouenCobalt
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Quick adjust chips
                            IconButton(
                                onClick = {
                                    val newL = if (currentLimit >= 500.0) 250.0 else currentLimit + 100.0
                                    viewModel.updateCourierPurchaseLimit(newL)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Adjust", tint = ChaouenPrimary)
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Orders Section
        item {
            Text(
                text = "الطلبات الحالية (${activeOrders.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ChaouenCobalt
            )
        }

        if (activeOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد طلبات جارية موجهة إليك حالياً",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "ابق في وضع الاتصال وسينبهك التطبيق فور اختيارك من طرف زبون",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(activeOrders) { order ->
                val status = try {
                    OrderStatus.valueOf(order.status)
                } catch (e: Exception) {
                    OrderStatus.REQUESTED
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "طلب #${order.id} - ${order.shopName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ChaouenCobalt
                            )

                            Surface(
                                color = if (status == OrderStatus.REQUESTED) MoroccanAmber else ChaouenPrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = AppStrings.statusLabel(status, currentLanguage),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "السلعة: ${order.itemDescription}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "نوع الطلب: ${if (order.requestType == "BUY_FOR_ME") "شري ليا وخلس (تخلص وترجع)" else "موصي عليها (استلام فقط)"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "واجب التوصيل: ${order.deliveryFeeMad.toInt()} درهم | التكلفة التقديرية: ${order.estimatedCostMad.toInt()} درهم",
                            fontSize = 12.sp,
                            color = MoroccanAmberDark,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Customer contact buttons
                        ContactActionButtons(
                            contactName = order.customerName,
                            phone = order.customerPhone,
                            isCourierContact = false,
                            orderId = order.id,
                            itemDescription = order.itemDescription,
                            currentLanguage = currentLanguage
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Progression Buttons
                        when (status) {
                            OrderStatus.REQUESTED -> {
                                // Accept or Decline
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.courierAcceptOrder(order.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("courier_accept_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("قبول الطلب", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.courierDeclineOrder(order.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("courier_decline_button"),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MoroccanRed),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("اعتذار", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            OrderStatus.ACCEPTED -> {
                                Button(
                                    onClick = { viewModel.advanceOrderStatus(order.id, OrderStatus.AT_SHOP) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("وصلت للمحل (Arrivé au magasin)", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.AT_SHOP -> {
                                Button(
                                    onClick = { viewModel.advanceOrderStatus(order.id, OrderStatus.PURCHASED) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("شريت السلعة / استلمت الطلب", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.PURCHASED -> {
                                Button(
                                    onClick = { viewModel.advanceOrderStatus(order.id, OrderStatus.ON_THE_WAY) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.TwoWheeler, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("بدء التوصيل إلى الزبون (En route)", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.ON_THE_WAY -> {
                                Button(
                                    onClick = {
                                        orderToDeliver = order
                                        actualItemCostInput = order.estimatedCostMad.toInt().toString()
                                        cashCollectedInput = (order.estimatedCostMad + order.deliveryFeeMad).toInt().toString()
                                        receiptNoteInput = "تم التوصيل كاش عند الباب"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("courier_deliver_cash_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تسليم واستلام الكاش (Cash Ledger)", fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // 3. Completed Deliveries & Ledger History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "سجل التوصيل والمدخول (${completedOrders.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ChaouenCobalt
            )
        }

        if (completedOrders.isEmpty()) {
            item {
                Text(
                    text = "لم تقم بتسليم أي طلب بعد اليوم",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        } else {
            items(completedOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "طلب #${order.id} - ${order.shopName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "مستلم كاش: ${order.cashCollectedMad.toInt()} درهم (ربح التوصيل: ${order.deliveryFeeMad.toInt()} درهم)",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoroccanMint)
                    }
                }
            }
        }
    }

    // Cash Ledger Dialog when completing delivery
    orderToDeliver?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToDeliver = null },
            title = {
                Text("تسجيل المدفوعات كاش (Cash Ledger)")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "طلب #${order.id} - الزبون: ${order.customerName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = actualItemCostInput,
                        onValueChange = { actualItemCostInput = it },
                        label = { Text("ثمن الشراء الحقيقي بالمحل (درهم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cashCollectedInput,
                        onValueChange = { cashCollectedInput = it },
                        label = { Text("مجموع المبلغ المقبوض كاش (درهم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = receiptNoteInput,
                        onValueChange = { receiptNoteInput = it },
                        label = { Text("ملاحظة أو رقم التوصيل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "عمولة التطبيق المقتطعة: 2 درهم",
                        fontSize = 11.sp,
                        color = ChaouenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = actualItemCostInput.toDoubleOrNull() ?: 0.0
                        val collected = cashCollectedInput.toDoubleOrNull() ?: (cost + order.deliveryFeeMad)
                        viewModel.finalizeDeliveryWithCash(
                            orderId = order.id,
                            actualCost = cost,
                            deliveryFee = order.deliveryFeeMad,
                            cashCollected = collected,
                            receiptNote = receiptNoteInput
                        )
                        orderToDeliver = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint)
                ) {
                    Text("حفظ وإتمام التوصيل")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDeliver = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialogs for registering a new courier and switching couriers
    RegisterCourierDialog(
        isOpen = courierState.showRegisterDialog,
        onDismiss = { viewModel.setShowRegisterCourierDialog(false) },
        onRegister = { name, phone, vehicleType, limit, address ->
            viewModel.registerNewCourier(name, phone, vehicleType, limit, address)
        },
        currentLanguage = currentLanguage
    )

    SwitchCourierDialog(
        isOpen = courierState.showSwitchDialog,
        onDismiss = { viewModel.setShowSwitchCourierDialog(false) },
        currentCourierId = courierState.currentCourierId,
        couriers = allCouriers,
        onSelectCourier = { courierId ->
            viewModel.switchCourier(courierId)
        },
        onOpenRegister = {
            viewModel.setShowSwitchCourierDialog(false)
            viewModel.setShowRegisterCourierDialog(true)
        },
        currentLanguage = currentLanguage
    )
}
}
