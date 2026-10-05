package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.geo.ChefchaouenGeoFence
import com.example.data.geo.GeoPoint
import com.example.data.model.AppLanguage
import com.example.data.model.CourierPresenceEntity
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*

@Composable
fun ChefchaouenMapCanvas(
    selectedPin: GeoPoint,
    onPinSelected: (GeoPoint) -> Unit,
    serviceAreaPolygon: List<GeoPoint>,
    couriers: List<CourierPresenceEntity>,
    activeCourierPos: GeoPoint? = null,
    shopPos: GeoPoint? = null,
    isInsideServiceArea: Boolean,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    // Pulse animation for pins
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Geographic bounding box for mapping Lat/Lng to Canvas pixels
    // Chefchaouen box: lat 35.155 to 35.185, lng -5.285 to -5.245
    val minLat = 35.155
    val maxLat = 35.185
    val minLng = -5.285
    val maxLng = -5.245

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F0FE))
            .border(
                width = 1.5.dp,
                color = if (isInsideServiceArea) ChaouenPrimary else MoroccanRed,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("chefchaouen_map_canvas")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Convert pixel offset to Lat/Lng
                        val lat = maxLat - (offset.y / size.height) * (maxLat - minLat)
                        val lng = minLng + (offset.x / size.width) * (maxLng - minLng)
                        onPinSelected(GeoPoint(lat, lng))
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            fun toOffset(point: GeoPoint): Offset {
                val x = ((point.lng - minLng) / (maxLng - minLng) * canvasW).toFloat()
                val y = (((maxLat - point.lat) / (maxLat - minLat)) * canvasH).toFloat()
                return Offset(x, y)
            }

            // 1. Draw Chefchaouen mountain ridge backdrop (Jebel El Kelaa)
            val mountainPath = Path().apply {
                moveTo(0f, canvasH * 0.25f)
                cubicTo(canvasW * 0.25f, canvasH * 0.08f, canvasW * 0.5f, canvasH * 0.28f, canvasW * 0.75f, canvasH * 0.12f)
                lineTo(canvasW, canvasH * 0.2f)
                lineTo(canvasW, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(mountainPath, color = Color(0xFFC7D7EC))

            // 2. Draw Chefchaouen Service Area Polygon
            if (serviceAreaPolygon.size >= 3) {
                val polyPath = Path()
                val start = toOffset(serviceAreaPolygon[0])
                polyPath.moveTo(start.x, start.y)
                for (i in 1 until serviceAreaPolygon.size) {
                    val p = toOffset(serviceAreaPolygon[i])
                    polyPath.lineTo(p.x, p.y)
                }
                polyPath.close()

                // Semi-transparent blue fill
                drawPath(
                    path = polyPath,
                    color = Color(0x301976D2)
                )
                // Boundary perimeter stroke
                drawPath(
                    path = polyPath,
                    color = ChaouenPrimary,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 3. Draw Medina main paths & alleys (stylized blue lanes)
            val lanePath = Path().apply {
                val outa = toOffset(GeoPoint(35.1688, -5.2636))
                val rasMaa = toOffset(GeoPoint(35.1712, -5.2575))
                val babAin = toOffset(GeoPoint(35.1675, -5.2678))
                val babSouk = toOffset(GeoPoint(35.1715, -5.2662))

                moveTo(babAin.x, babAin.y)
                quadraticTo(outa.x - 20f, outa.y + 10f, outa.x, outa.y)
                quadraticTo(outa.x + 30f, outa.y - 15f, rasMaa.x, rasMaa.y)

                moveTo(outa.x, outa.y)
                lineTo(babSouk.x, babSouk.y)
            }
            drawPath(lanePath, color = Color(0x600D47A1), style = Stroke(width = 2.dp.toPx()))

            // 4. Draw Chefchaouen Landmarks
            ChefchaouenGeoFence.LANDMARKS.take(4).forEach { (name, pos) ->
                val offset = toOffset(pos)
                // Traditional blue stone dot
                drawCircle(
                    color = ChaouenCobalt,
                    radius = 4.dp.toPx(),
                    center = offset
                )
                // Label
                val shortName = name.split("(").first().trim()
                val textLayout = textMeasurer.measure(
                    text = AnnotatedString(shortName),
                    style = TextStyle(fontSize = 9.sp, color = Color(0xFF1E3A8A))
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = shortName,
                    topLeft = Offset(offset.x - textLayout.size.width / 2, offset.y + 6.dp.toPx()),
                    style = TextStyle(fontSize = 9.sp, color = Color(0xFF1E3A8A))
                )
            }

            // 5. Draw active couriers
            couriers.forEach { courier ->
                val pos = toOffset(GeoPoint(courier.currentLat, courier.currentLng))
                // Green halo for online
                drawCircle(
                    color = MoroccanMint.copy(alpha = 0.4f),
                    radius = 10.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = MoroccanMint,
                    radius = 6.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = pos
                )
            }

            // 6. Draw shop marker if active
            shopPos?.let { sPos ->
                val sOffset = toOffset(sPos)
                drawCircle(
                    color = MoroccanAmber,
                    radius = 7.dp.toPx(),
                    center = sOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = sOffset
                )
            }

            // 7. Route line if active courier & destination
            activeCourierPos?.let { cPos ->
                val cOffset = toOffset(cPos)
                val destOffset = toOffset(selectedPin)

                // Dashed line from courier to pin
                drawLine(
                    color = MoroccanAmber,
                    start = cOffset,
                    end = destOffset,
                    strokeWidth = 3.dp.toPx()
                )

                // Active courier marker (Scooter)
                drawCircle(
                    color = ChaouenIndigo,
                    radius = 9.dp.toPx(),
                    center = cOffset
                )
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = 5.dp.toPx(),
                    center = cOffset
                )
            }

            // 8. Draw Customer Selected Pin
            val pinOffset = toOffset(selectedPin)
            // Animated pulse
            drawCircle(
                color = (if (isInsideServiceArea) ChaouenPrimary else MoroccanRed).copy(alpha = 0.35f),
                radius = 16.dp.toPx() * pulseScale,
                center = pinOffset
            )
            // Pin base
            drawCircle(
                color = if (isInsideServiceArea) ChaouenCobalt else MoroccanRed,
                radius = 8.dp.toPx(),
                center = pinOffset
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = pinOffset
            )
        }

        // Overlay status badges on the map
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isInsideServiceArea) MoroccanMint else MoroccanRed,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInsideServiceArea) AppStrings.insideServiceArea(currentLanguage)
                               else AppStrings.outsideServiceArea(currentLanguage),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        // Action button to recenter / select Medina Center
        FilledTonalIconButton(
            onClick = { onPinSelected(ChefchaouenGeoFence.CHEFCHAOUEN_CENTER) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(40.dp)
                .testTag("recenter_map_button")
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "Center on Outa El Hammam",
                tint = ChaouenCobalt
            )
        }

        // Quick tip instruction
        Text(
            text = "انقر على الخريطة لتغيير مكان التسليم بشفشاون",
            color = Color(0xFF1E3A8A),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 8.dp)
                .background(Color(0xD0FFFFFF), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
