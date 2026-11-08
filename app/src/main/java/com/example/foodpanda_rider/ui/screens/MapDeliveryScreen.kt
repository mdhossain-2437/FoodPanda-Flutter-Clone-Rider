package com.example.foodpanda_rider.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.foodpanda_rider.model.Helper
import com.example.foodpanda_rider.model.Order
import com.example.foodpanda_rider.ui.components.CustomTextButton
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MapScreen(
    order: Order,
    onPickUpOrder: () -> Unit,
    onStepRiderCloser: () -> Unit,
    onArriveAtCustomer: () -> Unit,
    onDeliverOrder: () -> Unit,
    onRefreshGpsLocation: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isBottomContainerVisible by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onRefreshGpsLocation()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            onRefreshGpsLocation()
        }
    }

    // Live 10-second tracking loop after pickup (matching MapScreen Timer.periodic in Flutter)
    LaunchedEffect(order.id, order.isPickup, order.isNear) {
        if (order.isPickup && !order.isNear) {
            while (true) {
                delay(10_000L)
                onStepRiderCloser()
            }
        }
    }

    val riderLat = order.rider?.latitude ?: order.shop.latitude
    val riderLng = order.rider?.longitude ?: order.shop.longitude
    val destLat = if (!order.isPickup) order.shop.latitude else order.address.latitude
    val destLng = if (!order.isPickup) order.shop.longitude else order.address.longitude

    val currentDistanceKm = remember(riderLat, riderLng, destLat, destLng) {
        Helper.calculateDistance(riderLat, riderLng, destLat, destLng)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF2F5))
            .testTag("map_screen")
    ) {
        // Interactive Map Canvas (MapWidget equivalent)
        InteractiveDeliveryMapCanvas(
            order = order,
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar Controls: Back Button, Live Route Status Pill, and Toggle Bottom Sheet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("map_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (!order.isPickup) {
                            String.format(Locale.US, "To Shop • %.2f km", currentDistanceKm)
                        } else if (order.isNear) {
                            "Arrived at Customer (< 0.5 km)"
                        } else {
                            String.format(Locale.US, "To Customer • %.2f km", currentDistanceKm)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.testTag("map_my_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My GPS location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = { isBottomContainerVisible = !isBottomContainerVisible },
                        modifier = Modifier.testTag("map_toggle_bottom_card")
                    ) {
                        Icon(
                            imageVector = if (isBottomContainerVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = "Toggle order sheet",
                            tint = Color(0xFF424242)
                        )
                    }
                }
            }
        }

        // Bottom Container matching lib/map/widgets/bottom_container.dart
        AnimatedVisibility(
            visible = isBottomContainerVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            BottomContainer(
                order = order,
                distanceKm = currentDistanceKm,
                onPickUpHandler = onPickUpOrder,
                onStepCloser = onStepRiderCloser,
                onArriveNow = onArriveAtCustomer,
                onDeliverHandler = onDeliverOrder
            )
        }
    }
}

@Composable
private fun InteractiveDeliveryMapCanvas(
    order: Order,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.75f, 2.5f)
                    panOffset += pan
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Soft map background
        drawRect(color = Color(0xFFEAEFF2))

        // City park blocks & water accents
        drawRect(
            color = Color(0xFFD8EBD4),
            topLeft = Offset(w * 0.08f + panOffset.x * 0.3f, h * 0.14f + panOffset.y * 0.3f),
            size = androidx.compose.ui.geometry.Size(w * 0.28f * zoom, h * 0.18f * zoom)
        )
        drawRect(
            color = Color(0xFFD8EBD4),
            topLeft = Offset(w * 0.62f + panOffset.x * 0.3f, h * 0.38f + panOffset.y * 0.3f),
            size = androidx.compose.ui.geometry.Size(w * 0.26f * zoom, h * 0.16f * zoom)
        )

        // Mekong / Tonle Sap river ribbon on the right edge
        val riverPath = Path().apply {
            moveTo(w * 0.88f + panOffset.x * 0.2f, 0f)
            cubicTo(
                w * 0.82f + panOffset.x * 0.2f, h * 0.35f,
                w * 0.94f + panOffset.x * 0.2f, h * 0.65f,
                w * 0.85f + panOffset.x * 0.2f, h
            )
        }
        drawPath(
            path = riverPath,
            color = Color(0xFFB3DCF2),
            style = Stroke(width = 48f * zoom, cap = StrokeCap.Round)
        )

        // Street grid lines
        val gridStep = 95f * zoom
        var x = (panOffset.x % gridStep)
        while (x < w) {
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 8f
            )
            x += gridStep
        }
        var y = (panOffset.y % gridStep)
        while (y < h) {
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 8f
            )
            y += gridStep
        }

        // Major arterial boulevards
        drawLine(
            color = Color(0xFFFDF6D8),
            start = Offset(0f, h * 0.34f + panOffset.y * 0.2f),
            end = Offset(w, h * 0.42f + panOffset.y * 0.2f),
            strokeWidth = 18f * zoom
        )
        drawLine(
            color = Color(0xFFFDF6D8),
            start = Offset(w * 0.36f + panOffset.x * 0.2f, 0f),
            end = Offset(w * 0.44f + panOffset.x * 0.2f, h),
            strokeWidth = 18f * zoom
        )

        // Anchor positions for Shop, Customer, and Rider on the map canvas
        val shopPoint = Offset(
            x = w * 0.30f + panOffset.x * 0.4f,
            y = h * 0.30f + panOffset.y * 0.4f
        )
        val customerPoint = Offset(
            x = w * 0.72f + panOffset.x * 0.4f,
            y = h * 0.48f + panOffset.y * 0.4f
        )

        // Calculate Rider progress between start and destination
        val totalShopToCustomer = Helper.calculateDistance(
            order.shop.latitude,
            order.shop.longitude,
            order.address.latitude,
            order.address.longitude
        ).coerceAtLeast(0.1)

        val riderLat = order.rider?.latitude ?: order.shop.latitude
        val riderLng = order.rider?.longitude ?: order.shop.longitude
        val remainingToCustomer = Helper.calculateDistance(
            riderLat,
            riderLng,
            order.address.latitude,
            order.address.longitude
        )

        val riderPoint = if (!order.isPickup) {
            Offset(
                x = w * 0.20f + panOffset.x * 0.4f,
                y = h * 0.46f + panOffset.y * 0.4f
            )
        } else if (order.isNear) {
            Offset(customerPoint.x - 18f, customerPoint.y - 12f)
        } else {
            val fraction = (1.0 - (remainingToCustomer / totalShopToCustomer)).toFloat().coerceIn(0.05f, 0.92f)
            Offset(
                x = shopPoint.x + (customerPoint.x - shopPoint.x) * fraction,
                y = shopPoint.y + (customerPoint.y - shopPoint.y) * fraction
            )
        }

        val activeOrigin = if (!order.isPickup) riderPoint else shopPoint
        val activeDest = if (!order.isPickup) shopPoint else customerPoint
        val midWaypoint = Offset(
            x = (activeOrigin.x + activeDest.x) * 0.5f,
            y = minOf(activeOrigin.y, activeDest.y) - 38f
        )

        // Route Polyline (FoodPanda Pink, width 6dp equivalent)
        val routePath = Path().apply {
            moveTo(activeOrigin.x, activeOrigin.y)
            quadraticTo(midWaypoint.x, midWaypoint.y, activeDest.x, activeDest.y)
        }
        drawPath(
            path = routePath,
            color = primary.copy(alpha = 0.25f),
            style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = routePath,
            color = primary,
            style = Stroke(
                width = 11f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
            )
        )

        // Shop Marker Circle
        drawCircle(
            color = Color.White,
            radius = 28f,
            center = shopPoint
        )
        drawCircle(
            color = Color(0xFF1E1E1E),
            radius = 22f,
            center = shopPoint
        )

        // Customer Marker Circle (with 0.5km arrival halo)
        drawCircle(
            color = primary.copy(alpha = 0.16f),
            radius = 58f,
            center = customerPoint
        )
        drawCircle(
            color = Color.White,
            radius = 28f,
            center = customerPoint
        )
        drawCircle(
            color = Color(0xFF2ECC71),
            radius = 22f,
            center = customerPoint
        )

        // Rider Live Marker Circle
        drawCircle(
            color = primary.copy(alpha = 0.28f),
            radius = 40f,
            center = riderPoint
        )
        drawCircle(
            color = Color.White,
            radius = 26f,
            center = riderPoint
        )
        drawCircle(
            color = primary,
            radius = 20f,
            center = riderPoint
        )
    }
}

@Composable
fun BottomContainer(
    order: Order,
    distanceKm: Double,
    onPickUpHandler: () -> Unit,
    onStepCloser: () -> Unit,
    onArriveNow: () -> Unit,
    onDeliverHandler: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCommentSeeMore by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_bottom_container")
    ) {
        // Top Card: Shop (before pickup) or Customer (after pickup) + ORDER #id
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!order.isPickup) Icons.Default.Storefront else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!order.isPickup) order.shop.shopName else order.user.name,
                        fontSize = 18.sp,
                        color = primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ORDER #${order.id}",
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Card: Pickup/Delivery Address, Payment/Comment, Movement Controls & Primary Action
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (!order.isPickup) {
                    // Before Pickup: PICK UP ORDER & PAYMENT METHOD
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "Pick up location",
                                tint = primary
                            )
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PICK UP ORDER",
                                color = Color(0xFF9E9E9E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${order.shop.houseNumber} ${order.shop.street}".trim(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF212121)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PAYMENT METHOD: ",
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (order.isPaid) {
                                String.format(Locale.US, "$ %.2f (Already paid)", order.totalPrice)
                            } else {
                                String.format(Locale.US, "$ %.2f (Not yet paid)", order.totalPrice)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // After Pickup: DELIVER ORDER & COMMENT
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = "Deliver location",
                                tint = primary
                            )
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DELIVER ORDER",
                                color = Color(0xFF9E9E9E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${order.address.houseNumber} ${order.address.street}".trim(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF212121)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "COMMENT",
                        color = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = order.address.deliveryInstruction.ifBlank { "No special delivery instructions." },
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        maxLines = if (isCommentSeeMore) Int.MAX_VALUE else 2,
                        overflow = if (isCommentSeeMore) TextOverflow.Visible else TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCommentSeeMore = !isCommentSeeMore }
                    )

                    if (!order.isNear) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onStepCloser,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("step_closer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ride Closer", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onArriveNow,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2ECC71),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("arrive_destination_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Arrive (<0.5km)", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                CustomTextButton(
                    text = if (!order.isPickup) "Pick Up" else "Delivered",
                    onPressed = if (!order.isPickup) onPickUpHandler else onDeliverHandler,
                    isDisabled = if (!order.isPickup) false else !order.isNear,
                    testTag = if (!order.isPickup) "map_pick_up_button" else "map_delivered_button"
                )
            }
        }
    }
}
