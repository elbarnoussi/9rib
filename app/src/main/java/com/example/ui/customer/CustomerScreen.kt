package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.geo.ChefchaouenGeoFence
import com.example.data.geo.GeoPoint
import com.example.data.model.AppLanguage
import com.example.data.model.CourierPresenceEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RequestType
import com.example.ui.components.ChefchaouenMapCanvas
import com.example.ui.components.ContactActionButtons
import com.example.ui.components.OrderStatusPipeline
import com.example.ui.components.RegisterClientDialog
import com.example.ui.components.SwitchClientDialog
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun CustomerScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val customerState by viewModel.customerState.collectAsStateWithLifecycle()
    val availableCouriers by viewModel.availableCouriers.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val serviceArea by viewModel.serviceArea.collectAsStateWithLifecycle()
    val registeredClients by viewModel.registeredClients.collectAsStateWithLifecycle()

    // If active order exists, observe it
    val activeOrderId = customerState.activeOrderId
    val activeOrder = if (activeOrderId != null) {
        viewModel.getOrderFlow(activeOrderId).collectAsStateWithLifecycle(initialValue = null).value
    } else null

    // Sorted couriers by distance to current delivery pin
    val sortedCouriers = remember(availableCouriers, customerState.deliveryPin) {
        availableCouriers.map { courier ->
            val dist = ChefchaouenGeoFence.distanceKm(
                customerState.deliveryPin,
                GeoPoint(courier.currentLat, courier.currentLng)
            )
            Triple(courier, dist, ChefchaouenGeoFence.estimateEtaMinutes(dist))
        }.sortedBy { it.second }
    }

    val polygonPoints = remember(serviceArea) {
        serviceArea?.let {
            it.polygonJson.split(";").mapNotNull { p ->
                val parts = p.split(",")
                if (parts.size == 2) GeoPoint(parts[0].toDoubleOrNull() ?: 0.0, parts[1].toDoubleOrNull() ?: 0.0) else null
            }
        } ?: ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Customer Profile Header & New Client Registration Affordance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFFE3F2FD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ChaouenPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = customerState.customerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${customerState.customerPhone} • ${customerState.customerAddress.take(28)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.setShowSwitchClientDialog(true) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("switch_client_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Switch Client",
                                        tint = ChaouenCobalt
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.setShowRegisterClientDialog(true) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("register_client_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Add Client",
                                        tint = MoroccanMint
                                    )
                                }
                            }
                        }

                        // Success / Toast message
                        customerState.toastMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
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
                                        onClick = { viewModel.clearCustomerToast() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = MoroccanMint, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Order Tracking View
            if (activeOrder != null) {
            item {
                ActiveOrderCard(
                    order = activeOrder,
                    currentLanguage = currentLanguage,
                    polygon = polygonPoints,
                    onClearOrder = { viewModel.clearActiveOrder() }
                )
            }
        } else {
            // New Order Creation Flow
            // 1. Delivery Location First (Map & Pin)
            item {
                Text(
                    text = AppStrings.deliveryLocationTitle(currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
                Spacer(modifier = Modifier.height(8.dp))

                ChefchaouenMapCanvas(
                    selectedPin = customerState.deliveryPin,
                    onPinSelected = { viewModel.onPinSelected(it) },
                    serviceAreaPolygon = polygonPoints,
                    couriers = availableCouriers,
                    isInsideServiceArea = customerState.isInsideServiceArea,
                    currentLanguage = currentLanguage
                )

                // Error alert if outside service area or disabled
                if (customerState.serviceAreaErrorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MoroccanRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = customerState.serviceAreaErrorMessage ?: "",
                                color = MoroccanRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 2. Select Request Type
            item {
                Text(
                    text = "نوع الطلب (Type de demande)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Type 1: Courier buys it
                    RequestTypeCard(
                        type = RequestType.BUY_FOR_ME,
                        isSelected = customerState.requestType == RequestType.BUY_FOR_ME,
                        currentLanguage = currentLanguage,
                        onClick = { viewModel.setRequestType(RequestType.BUY_FOR_ME) },
                        modifier = Modifier.weight(1f)
                    )

                    // Type 2: Pickup only
                    RequestTypeCard(
                        type = RequestType.PICKUP_ONLY,
                        isSelected = customerState.requestType == RequestType.PICKUP_ONLY,
                        currentLanguage = currentLanguage,
                        onClick = { viewModel.setRequestType(RequestType.PICKUP_ONLY) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Choose Courier (Mandatory before submit)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.selectCourierTitle(currentLanguage),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaouenCobalt
                    )
                    Text(
                        text = "${sortedCouriers.size} متاحين",
                        fontSize = 12.sp,
                        color = MoroccanMint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (sortedCouriers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MoroccanAmber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "لا يوجد ليفروغ متاح حالياً بشفشاون. يرجى المحاولة بعد قليل.",
                                fontSize = 13.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            } else {
                items(sortedCouriers) { (courier, distKm, etaMin) ->
                    val isSelected = customerState.selectedCourier?.courierId == courier.courierId
                    CourierSelectableCard(
                        courier = courier,
                        distKm = distKm,
                        etaMin = etaMin,
                        deliveryFeeMad = customerState.deliveryFeeMad,
                        isSelected = isSelected,
                        currentLanguage = currentLanguage,
                        onSelect = { viewModel.selectCourier(courier) }
                    )
                }
            }

            // 4. Order Details (Any Shop & Item description)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.orderDetailsTitle(currentLanguage),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerState.shopName,
                    onValueChange = { viewModel.updateShopName(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_name_input"),
                    label = { Text("المحل / المطعم (Magasin / Restaurant)") },
                    placeholder = { Text(AppStrings.shopPlaceholder(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = ChaouenPrimary) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChaouenPrimary,
                        unfocusedBorderColor = Color(0xFFCFD8DC)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerState.itemDescription,
                    onValueChange = { viewModel.updateItemDescription(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_description_input"),
                    label = { Text("وصف الطلب والسلعة (Description des articles)") },
                    placeholder = { Text(AppStrings.itemsPlaceholder(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ChaouenPrimary) },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChaouenPrimary,
                        unfocusedBorderColor = Color(0xFFCFD8DC)
                    )
                )

                if (customerState.requestType == RequestType.BUY_FOR_ME) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = if (customerState.estimatedCostMad == 0.0) "" else customerState.estimatedCostMad.toInt().toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: 0.0
                            viewModel.updateEstimatedCost(v)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("estimated_cost_input"),
                        label = { Text(AppStrings.estimatedCostLabel(currentLanguage)) },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MoroccanAmber) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoroccanAmber,
                            unfocusedBorderColor = Color(0xFFCFD8DC)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pricing Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("واجب التوصيل لشفشاون:", fontSize = 13.sp, color = Color.DarkGray)
                            Text(
                                "${customerState.deliveryFeeMad.toInt()} ${AppStrings.currencyMad(currentLanguage)}",
                                fontWeight = FontWeight.Bold,
                                color = ChaouenCobalt
                            )
                        }
                        if (customerState.requestType == RequestType.BUY_FOR_ME) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ثمن المشتريات التقديري:", fontSize = 13.sp, color = Color.DarkGray)
                                Text(
                                    "${customerState.estimatedCostMad.toInt()} ${AppStrings.currencyMad(currentLanguage)}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFB0BEC5))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المجموع التقريبي كاش:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "${(customerState.estimatedCostMad + customerState.deliveryFeeMad).toInt()} ${AppStrings.currencyMad(currentLanguage)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MoroccanAmberDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MoroccanMint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.cashOnlyNote(currentLanguage),
                                fontSize = 11.sp,
                                color = MoroccanMint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Submission Error
                customerState.submissionError?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = MoroccanRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Button
                Button(
                    onClick = { viewModel.submitOrder() },
                    enabled = !customerState.isSubmitting && customerState.isInsideServiceArea && customerState.selectedCourier != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_order_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChaouenPrimary,
                        disabledContainerColor = Color(0xFFB0BEC5)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (customerState.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Moped, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.confirmOrderBtn(currentLanguage),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialogs for registering a new client and switching clients
        RegisterClientDialog(
            isOpen = customerState.showRegisterDialog,
            onDismiss = { viewModel.setShowRegisterClientDialog(false) },
            onRegister = { name, phone, address ->
                viewModel.registerNewClient(name, phone, address)
            },
            currentLanguage = currentLanguage
        )

        SwitchClientDialog(
            isOpen = customerState.showSwitchDialog,
            onDismiss = { viewModel.setShowSwitchClientDialog(false) },
            currentClientId = customerState.customerId,
            clients = registeredClients,
            onSelectClient = { client ->
                viewModel.switchClient(client)
            },
            onOpenRegister = {
                viewModel.setShowSwitchClientDialog(false)
                viewModel.setShowRegisterClientDialog(true)
            },
            currentLanguage = currentLanguage
        )
    }
}

@Composable
fun RequestTypeCard(
    type: RequestType,
    isSelected: Boolean,
    currentLanguage: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ChaouenPrimary else Color(0xFFCFD8DC),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = if (type == RequestType.BUY_FOR_ME) Icons.Default.ShoppingCart else Icons.Default.Inventory2,
                contentDescription = null,
                tint = if (isSelected) ChaouenPrimary else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (type == RequestType.BUY_FOR_ME) "شري ليا وخلس" else "موصي عليها جيبها",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) ChaouenCobalt else Color.DarkGray
            )
            Text(
                text = if (type == RequestType.BUY_FOR_ME) "الليفروغ يخلص ويرجع" else "سلعة واجدة ومخلصة",
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun CourierSelectableCard(
    courier: CourierPresenceEntity,
    distKm: Double,
    etaMin: Int,
    deliveryFeeMad: Double,
    isSelected: Boolean,
    currentLanguage: AppLanguage,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ChaouenPrimary else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("courier_card_${courier.courierId}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Courier Avatar / Scooter
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isSelected) ChaouenPrimary else Color(0xFFE2E8F0),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else ChaouenCobalt,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = courier.courierName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${courier.rating}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "يبعد: ${ChefchaouenGeoFence.formatDistance(distKm)}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "الوصول: ~$etaMin دقيقة",
                        fontSize = 12.sp,
                        color = MoroccanMint,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "سقف الشراء: ${courier.purchaseLimitMad.toInt()} درهم",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Radio Indicator
            RadioButton(
                selected = isSelected,
                onClick = { onSelect() },
                colors = RadioButtonDefaults.colors(selectedColor = ChaouenPrimary)
            )
        }
    }
}

@Composable
fun ActiveOrderCard(
    order: com.example.data.model.OrderEntity,
    currentLanguage: AppLanguage,
    polygon: List<GeoPoint>,
    onClearOrder: () -> Unit
) {
    val orderStatus = try {
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
                Column {
                    Text(
                        text = "طلب رقم #${order.id}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = ChaouenCobalt
                    )
                    Text(
                        text = "المحل: ${order.shopName}",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Surface(
                    color = when (orderStatus) {
                        OrderStatus.DELIVERED -> MoroccanMint
                        OrderStatus.CANCELLED -> MoroccanRed
                        else -> MoroccanAmber
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = AppStrings.statusLabel(orderStatus, currentLanguage),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map showing courier's live location and customer pin
            ChefchaouenMapCanvas(
                selectedPin = GeoPoint(order.deliveryLat, order.deliveryLng),
                onPinSelected = {},
                serviceAreaPolygon = polygon,
                couriers = emptyList(),
                activeCourierPos = GeoPoint(order.courierLat, order.courierLng),
                isInsideServiceArea = true,
                currentLanguage = currentLanguage,
                modifier = Modifier.height(180.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Order Status Pipeline
            OrderStatusPipeline(
                currentStatus = orderStatus,
                currentLanguage = currentLanguage
            )

            Spacer(modifier = Modifier.height(12.dp))

            // If courier has accepted, show Contact Buttons for Tel & WhatsApp
            if (orderStatus != OrderStatus.REQUESTED && orderStatus != OrderStatus.CANCELLED) {
                ContactActionButtons(
                    contactName = order.courierName ?: "ليفروغ شفشاون",
                    phone = order.courierPhone ?: "+212 672-451298",
                    isCourierContact = true,
                    orderId = order.id,
                    itemDescription = order.itemDescription,
                    currentLanguage = currentLanguage
                )
            } else if (orderStatus == OrderStatus.CANCELLED) {
                // If courier declined / cancelled
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "اعتذر الليفروغ عن قبول هذا الطلب أو أصبح غير متاح.",
                            color = MoroccanRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onClearOrder() },
                            colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(AppStrings.chooseAnotherCourier(currentLanguage), fontSize = 12.sp)
                        }
                    }
                }
            }

            if (orderStatus == OrderStatus.DELIVERED) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onClearOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("طلب جديد (Nouvelle commande)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
