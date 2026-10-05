package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.admin.AdminScreen
import com.example.ui.courier.CourierScreen
import com.example.ui.customer.CustomerScreen
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeliveryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChefchaouenDeliveryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefchaouenDeliveryApp(
    viewModel: DeliveryViewModel = viewModel()
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val layoutDirection = AppStrings.getLayoutDirection(currentLanguage)

    // Handle system back navigation to return to Customer role
    BackHandler(enabled = currentRole != UserRole.CUSTOMER) {
        viewModel.setRole(UserRole.CUSTOMER)
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ChaouenCobalt, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = "Delivery",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.appTitle(currentLanguage),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = ChaouenCobalt
                                )
                                Text(
                                    text = AppStrings.appSubtitle(currentLanguage),
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    },
                    actions = {
                        // Language switcher button (Darija RTL <-> French LTR)
                        FilledTonalButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(34.dp)
                                .testTag("language_toggle_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFE3F2FD),
                                contentColor = ChaouenCobalt
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.DARIJA) "FR" else "عربي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChaouenWhitewash
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentRole == UserRole.CUSTOMER,
                        onClick = { viewModel.setRole(UserRole.CUSTOMER) },
                        icon = {
                            Icon(
                                imageVector = if (currentRole == UserRole.CUSTOMER) Icons.Default.Person else Icons.Default.PersonOutline,
                                contentDescription = "Customer"
                            )
                        },
                        label = {
                            Text(
                                text = AppStrings.roleCustomer(currentLanguage),
                                fontWeight = if (currentRole == UserRole.CUSTOMER) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChaouenCobalt,
                            selectedTextColor = ChaouenCobalt,
                            indicatorColor = Color(0xFFD6E4FF)
                        ),
                        modifier = Modifier.testTag("nav_customer_tab")
                    )

                    NavigationBarItem(
                        selected = currentRole == UserRole.COURIER,
                        onClick = { viewModel.setRole(UserRole.COURIER) },
                        icon = {
                            Icon(
                                imageVector = if (currentRole == UserRole.COURIER) Icons.Default.Moped else Icons.Default.TwoWheeler,
                                contentDescription = "Courier"
                            )
                        },
                        label = {
                            Text(
                                text = AppStrings.roleCourier(currentLanguage),
                                fontWeight = if (currentRole == UserRole.COURIER) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChaouenCobalt,
                            selectedTextColor = ChaouenCobalt,
                            indicatorColor = Color(0xFFD6E4FF)
                        ),
                        modifier = Modifier.testTag("nav_courier_tab")
                    )

                    NavigationBarItem(
                        selected = currentRole == UserRole.ADMIN,
                        onClick = { viewModel.setRole(UserRole.ADMIN) },
                        icon = {
                            Icon(
                                imageVector = if (currentRole == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Security,
                                contentDescription = "Admin"
                            )
                        },
                        label = {
                            Text(
                                text = AppStrings.roleAdmin(currentLanguage),
                                fontWeight = if (currentRole == UserRole.ADMIN) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChaouenCobalt,
                            selectedTextColor = ChaouenCobalt,
                            indicatorColor = Color(0xFFD6E4FF)
                        ),
                        modifier = Modifier.testTag("nav_admin_tab")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(ChaouenWhitewash)
            ) {
                when (currentRole) {
                    UserRole.CUSTOMER -> CustomerScreen(viewModel = viewModel)
                    UserRole.COURIER -> CourierScreen(viewModel = viewModel)
                    UserRole.ADMIN -> AdminScreen(viewModel = viewModel)
                }
            }
        }
    }
}
