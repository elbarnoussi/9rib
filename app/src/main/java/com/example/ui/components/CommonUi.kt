package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.OrderStatus
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import java.net.URLEncoder

object CommunicationsHelper {

    /**
     * Launch normal phone dialer with tel: scheme.
     */
    fun dialPhone(context: Context, rawPhone: String) {
        try {
            val cleanPhone = rawPhone.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح الهاتف: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launch WhatsApp chat via wa.me link with prefilled Moroccan Darija/French message.
     */
    fun openWhatsApp(context: Context, rawPhone: String, messageText: String) {
        try {
            // Clean Moroccan number to international 212XXXXXXXXX format (remove +, spaces, leading 0)
            var cleanPhone = rawPhone.replace("+", "").replace(" ", "").replace("-", "")
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "212" + cleanPhone.substring(1)
            }
            val encodedMessage = URLEncoder.encode(messageText, "UTF-8")
            val url = "https://wa.me/$cleanPhone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If WhatsApp app is not installed, open via web browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح واتساب: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ContactActionButtons(
    contactName: String,
    phone: String,
    isCourierContact: Boolean, // True if customer is contacting courier, false if courier contacting customer
    orderId: Long,
    itemDescription: String,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Prefilled authentic message
    val waMessage = if (isCourierContact) {
        "السلام عليكم أخي الليفروغ، أنا الزبون بخصوص طلبي بشفشاون #${orderId} (${itemDescription.take(40)}). أين وصلت؟"
    } else {
        "السلام عليكم، أنا الليفروغ بخصوص طلبك في شفشاون #${orderId} (${itemDescription.take(40)}). أنا في طريقي إليك."
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = contactName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ChaouenCobalt
                    )
                    Text(
                        text = phone,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
                Surface(
                    color = ChaouenSky.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isCourierContact) "الليفروغ" else "الزبون",
                        fontSize = 11.sp,
                        color = ChaouenCobalt,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Regular Phone Call Button (tel:)
                Button(
                    onClick = { CommunicationsHelper.dialPhone(context, phone) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dial_phone_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ChaouenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Appel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.callBtn(currentLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // WhatsApp Chat Button (wa.me)
                Button(
                    onClick = { CommunicationsHelper.openWhatsApp(context, phone, waMessage) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("whatsapp_chat_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.whatsappBtn(currentLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strict User Note: Voice call must be initiated inside WhatsApp
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MoroccanMint,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.whatsappCallDisclaimer(currentLanguage),
                    fontSize = 11.sp,
                    color = Color(0xFF1B5E20),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun OrderStatusPipeline(
    currentStatus: OrderStatus,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OrderStatus.REQUESTED,
        OrderStatus.ACCEPTED,
        OrderStatus.AT_SHOP,
        OrderStatus.PURCHASED,
        OrderStatus.ON_THE_WAY,
        OrderStatus.DELIVERED
    )

    val currentIndex = steps.indexOf(currentStatus).let { if (it == -1) 0 else it }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "مراحل السخرة بشفشاون",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ChaouenCobalt
            )
            Spacer(modifier = Modifier.height(10.dp))

            steps.forEachIndexed { index, step ->
                val isCompleted = index <= currentIndex
                val isCurrent = index == currentIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = when {
                                    isCurrent -> MoroccanAmber
                                    isCompleted -> MoroccanMint
                                    else -> Color(0xFFE2E8F0)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted && !isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) Color.White else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = AppStrings.statusLabel(step, currentLanguage),
                        fontSize = if (isCurrent) 13.sp else 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCurrent -> MoroccanAmberDark
                            isCompleted -> Color(0xFF1E293B)
                            else -> Color.Gray
                        }
                    )
                }
            }
        }
    }
}
