package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.AppLanguage
import com.example.data.model.CourierPresenceEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*

@Composable
fun RegisterClientDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onRegister: (name: String, phone: String, address: String) -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+212 6") }
    var address by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val chefchaouenQuartiers = listOf(
        "حي الأندلس",
        "ساحة وطاء الحمام",
        "باب العين",
        "باب السوق",
        "رأس الماء",
        "حي العيون",
        "سيدي عبد الحميد",
        "شارع الحسن الثاني"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ChaouenCobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = AppStrings.registerNewClientTitle(currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(AppStrings.fullNameLabel(currentLanguage)) },
                    placeholder = { Text("مثال: رشيد العلمي") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ChaouenPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_client_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.phoneLabel(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ChaouenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_client_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(AppStrings.addressLabel(currentLanguage)) },
                    placeholder = { Text("مثال: درب الصور، قرب دار الضيافة") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = ChaouenPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_client_address_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Quartier selection chips
                Text(
                    text = "أحياء شفشاون المقترحة:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(chefchaouenQuartiers) { quartier ->
                        Surface(
                            color = if (address.contains(quartier)) ChaouenPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                address = if (address.isBlank()) quartier else "$quartier، $address"
                            }
                        ) {
                            Text(
                                text = quartier,
                                fontSize = 11.sp,
                                color = if (address.contains(quartier)) Color.White else Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MoroccanRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "يرجى كتابة الاسم الكامل"
                        return@Button
                    }
                    if (phone.replace(" ", "").length < 10) {
                        errorMessage = "يرجى كتابة رقم هاتف مغربي صحيح"
                        return@Button
                    }
                    if (address.isBlank()) {
                        errorMessage = "يرجى تحديد الحي أو العنوان بشفشاون"
                        return@Button
                    }
                    errorMessage = null
                    onRegister(fullName, phone, address)
                },
                modifier = Modifier.testTag("reg_client_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary)
            ) {
                Text(AppStrings.confirmRegisterBtn(currentLanguage), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancelBtn(currentLanguage))
            }
        }
    )
}

@Composable
fun SwitchClientDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentClientId: String,
    clients: List<UserProfileEntity>,
    onSelectClient: (UserProfileEntity) -> Unit,
    onOpenRegister: () -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تبديل حساب الزبون",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
                IconButton(onClick = onOpenRegister) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Client", tint = ChaouenPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "اختر زبوناً مسجلاً أو أضف زبوناً جديداً:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(clients) { client ->
                        val isSelected = client.id == currentClientId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectClient(client) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) ChaouenPrimary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (isSelected) ChaouenPrimary else Color(0xFFCFD8DC), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = client.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${client.phone} | ${client.address}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChaouenPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenRegister,
                colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تسجيل زبون جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancelBtn(currentLanguage))
            }
        }
    )
}

@Composable
fun RegisterCourierDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onRegister: (name: String, phone: String, vehicleType: String, purchaseLimitMad: Double, address: String) -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+212 6") }
    var vehicleType by remember { mutableStateOf("Moped (موطور)") }
    var purchaseLimitText by remember { mutableStateOf("300") }
    var address by remember { mutableStateOf("شفشاون") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val vehicleOptions = listOf(
        "Moped (موطور)",
        "Scooter (سكوتر)",
        "Bicycle (بيكالا)",
        "On Foot (مشياً على الأقدام)",
        "Electric Scooter (تروتينيت)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ChaouenCobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = AppStrings.registerNewCourierTitle(currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(AppStrings.fullNameLabel(currentLanguage)) },
                    placeholder = { Text("مثال: عبد الإله الطاهري") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ChaouenPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_courier_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.phoneLabel(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ChaouenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_courier_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Vehicle selection
                Text(
                    text = AppStrings.vehicleTypeLabel(currentLanguage),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(vehicleOptions) { v ->
                        val isSelected = vehicleType == v
                        Surface(
                            color = if (isSelected) ChaouenPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { vehicleType = v }
                        ) {
                            Text(
                                text = v,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Purchase Limit
                OutlinedTextField(
                    value = purchaseLimitText,
                    onValueChange = { purchaseLimitText = it },
                    label = { Text(AppStrings.purchaseLimitInputLabel(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MoroccanAmber) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_courier_limit_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Notice about Admin Approval
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ملاحظة: يتطلب الحساب موافقة الإدارة قبل البدء في استقبال الطلبات رسمياً.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                errorMessage?.let { err ->
                    Text(text = err, color = MoroccanRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "يرجى كتابة الاسم الكامل"
                        return@Button
                    }
                    if (phone.replace(" ", "").length < 10) {
                        errorMessage = "يرجى كتابة رقم هاتف مغربي صحيح"
                        return@Button
                    }
                    val limit = purchaseLimitText.toDoubleOrNull() ?: 300.0
                    errorMessage = null
                    onRegister(fullName, phone, vehicleType, limit, address)
                },
                modifier = Modifier.testTag("reg_courier_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary)
            ) {
                Text(AppStrings.confirmRegisterBtn(currentLanguage), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancelBtn(currentLanguage))
            }
        }
    )
}

@Composable
fun SwitchCourierDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentCourierId: String,
    couriers: List<CourierPresenceEntity>,
    onSelectCourier: (String) -> Unit,
    onOpenRegister: () -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تبديل حساب الليفروغ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaouenCobalt
                )
                IconButton(onClick = onOpenRegister) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Courier", tint = ChaouenPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "اختر حساب ليفروغ لتجربته أو أضف حساباً جديداً:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(couriers) { courier ->
                        val isSelected = courier.courierId == currentCourierId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectCourier(courier.courierId) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) ChaouenPrimary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (isSelected) ChaouenPrimary else Color(0xFFCFD8DC), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = courier.courierName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        if (!courier.isApproved) {
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("قيد المراجعة", fontSize = 9.sp, color = MoroccanAmberDark, modifier = Modifier.padding(horizontal = 4.dp))
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${courier.vehicleType} | سقف: ${courier.purchaseLimitMad.toInt()} درهم",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChaouenPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenRegister,
                colors = ButtonDefaults.buttonColors(containerColor = MoroccanMint)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تسجيل ليفروغ جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancelBtn(currentLanguage))
            }
        }
    )
}
