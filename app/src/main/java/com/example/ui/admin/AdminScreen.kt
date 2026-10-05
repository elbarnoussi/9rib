package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.geo.ChefchaouenGeoFence
import com.example.data.geo.GeoPoint
import com.example.data.model.AppLanguage
import com.example.data.model.OrderStatus
import com.example.ui.components.ChefchaouenMapCanvas
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun AdminScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val serviceArea by viewModel.serviceArea.collectAsStateWithLifecycle()
    val allCouriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allLedger by viewModel.allLedger.collectAsStateWithLifecycle()
    val registeredClients by viewModel.registeredClients.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val isAreaApproved = serviceArea?.isApproved ?: true

    val polygonPoints = remember(serviceArea) {
        serviceArea?.let {
            it.polygonJson.split(";").mapNotNull { p ->
                val parts = p.split(",")
                if (parts.size == 2) GeoPoint(parts[0].toDoubleOrNull() ?: 0.0, parts[1].toDoubleOrNull() ?: 0.0) else null
            }
        } ?: ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON
    }

    val pendingCouriers = allCouriers.filter { !it.isApproved }
    val approvedCouriers = allCouriers.filter { it.isApproved }

    // Financial calculations
    val totalAppCommissions = allLedger.sumOf { it.appCommissionMad }
    val totalDeliveryFees = allLedger.sumOf { it.deliveryFeeMad }
    val totalCashVolume = allLedger.sumOf { it.totalCashCollectedMad }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Admin Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = ChaouenIndigo),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "لوحة تحكم سخرة شفشاون (Admin)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "إدارة منطقة الخدمة، الليفروغ، والسيولة النقدية",
                        fontSize = 11.sp,
                        color = Color(0xFFBBDEFB)
                    )
                }
            }
        }

        // Sub-tabs: 0 = Service Area & Map, 1 = Courier Approvals, 2 = Orders & Ledger
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = ChaouenCobalt
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("حيز الخدمة", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    BadgedBox(
                        badge = {
                            if (pendingCouriers.isNotEmpty()) {
                                Badge { Text("${pendingCouriers.size}") }
                            }
                        }
                    ) {
                        Text("موافقة الليفروغ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("المالية والطلبات", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    BadgedBox(
                        badge = {
                            if (registeredClients.isNotEmpty()) {
                                Badge { Text("${registeredClients.size}") }
                            }
                        }
                    ) {
                        Text("الزبائن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Service Area Management
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "حالة حيز خدمة شفشاون",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isAreaApproved) "الخدمة مفعلة داخل حيز المدينة"
                                                   else "الخدمة معطلة (حظر الطلبات الجديدة)",
                                            fontSize = 12.sp,
                                            color = if (isAreaApproved) MoroccanMint else MoroccanRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Switch(
                                        checked = isAreaApproved,
                                        onCheckedChange = { viewModel.toggleServiceAreaApproval(it) },
                                        modifier = Modifier.testTag("admin_service_area_switch"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MoroccanMint
                                        )
                                    )
                                }

                                if (!isAreaApproved) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "⚠️ الحيز معطل: جميع محاولات الطلب ستُرفض تلقائياً حتى يتم التفعيل.",
                                            color = MoroccanRed,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "خريطة الحيز الجغرافي المعتمد (GeoJSON Polygon)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ChaouenCobalt
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ChefchaouenMapCanvas(
                            selectedPin = ChefchaouenGeoFence.CHEFCHAOUEN_CENTER,
                            onPinSelected = {},
                            serviceAreaPolygon = polygonPoints,
                            couriers = allCouriers,
                            isInsideServiceArea = isAreaApproved,
                            currentLanguage = currentLanguage,
                            modifier = Modifier.height(240.dp)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "إحداثيات رؤوس مضلع شفشاون (${polygonPoints.size} نقاط):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                polygonPoints.forEachIndexed { i, p ->
                                    Text(
                                        text = "نقطة ${i + 1}: Lat ${p.lat}, Lng ${p.lng}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Courier Approvals
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "طلبات الليفروغ قيد المراجعة (${pendingCouriers.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ChaouenCobalt
                        )
                    }

                    if (pendingCouriers.isEmpty()) {
                        item {
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoroccanMint)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جميع حسابات الليفروغ مفعلة ومراجعة", fontSize = 13.sp, color = MoroccanMint)
                                }
                            }
                        }
                    } else {
                        items(pendingCouriers) { courier ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(courier.courierName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(courier.courierPhone, fontSize = 12.sp, color = Color.Gray)
                                            Text("المركبة: ${courier.vehicleType}", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "جديد",
                                                color = MoroccanAmberDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { viewModel.approveCourier(courier.courierId, true) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_approve_courier_${courier.courierId}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("موافقة واعتماد الليفروغ", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الليفروغات المعتمدون (${approvedCouriers.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ChaouenCobalt
                        )
                    }

                    items(approvedCouriers) { courier ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
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
                                    Text(courier.courierName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "${courier.courierPhone} | توصيلات: ${courier.totalDeliveries} | تقييم: ${courier.rating}★",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.approveCourier(courier.courierId, false) }
                                ) {
                                    Text("تجميد", color = MoroccanRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Orders & Financial Cash Ledger
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        // Financial KPI Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ملخص السيولة والعمولات النقدية (Cash Ledger)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ChaouenCobalt
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("عمولة التطبيق المستحقة", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${totalAppCommissions.toInt()} درهم",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MoroccanMint
                                        )
                                    }
                                    Column {
                                        Text("إجمالي رسوم التوصيل", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${totalDeliveryFees.toInt()} درهم",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ChaouenPrimary
                                        )
                                    }
                                    Column {
                                        Text("إجمالي الكاش المتداول", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${totalCashVolume.toInt()} درهم",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MoroccanAmberDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "سجل جميع الطلبات (${allOrders.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ChaouenCobalt
                        )
                    }

                    if (allOrders.isEmpty()) {
                        item {
                            Text("لا توجد طلبات مسجلة بعد بالنظام", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(allOrders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "طلب #${order.id} - ${order.shopName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            order.status,
                                            color = if (order.status == "DELIVERED") MoroccanMint else ChaouenPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        "الزبون: ${order.customerName} | الليفروغ: ${order.courierName ?: "غير معين"}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        "التكلفة: ${order.estimatedCostMad.toInt()} درهم | التوصيل: ${order.deliveryFeeMad.toInt()} درهم | عمولة التطبيق: 2 درهم",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Registered Clients List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "قائمة الزبائن المسجلين بشفشاون (${registeredClients.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ChaouenCobalt
                        )
                    }

                    if (registeredClients.isEmpty()) {
                        item {
                            Text("لا يوجد زبائن مسجلون بعد", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(registeredClients) { client ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFE3F2FD), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = ChaouenPrimary)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(client.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(client.phone, fontSize = 11.sp, color = Color.Gray)
                                            Text(client.address, fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
