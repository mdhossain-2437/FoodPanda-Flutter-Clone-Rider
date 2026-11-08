package com.example.foodpanda_rider.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodpanda_rider.model.Helper
import com.example.foodpanda_rider.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomTextButton(
    text: String,
    onPressed: () -> Unit,
    isDisabled: Boolean = false,
    isOutlined: Boolean = false,
    modifier: Modifier = Modifier,
    testTag: String = "custom_text_button"
) {
    val primary = MaterialTheme.colorScheme.primary
    if (isOutlined) {
        OutlinedButton(
            onClick = onPressed,
            enabled = !isDisabled,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, if (isDisabled) Color.LightGray else primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = primary,
                disabledContentColor = Color.Gray
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag(testTag)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Button(
            onClick = onPressed,
            enabled = !isDisabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBDBDBD),
                disabledContentColor = Color.White
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag(testTag)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isDisabled: Boolean = false,
    errorText: String = "",
    isNumPad: Boolean = false,
    testTag: String = "custom_text_field"
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = isDisabled,
            enabled = !isDisabled,
            label = { Text(labelText) },
            isError = errorText.isNotEmpty(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = when {
                    isNumPad -> KeyboardType.Phone
                    isPassword -> KeyboardType.Password
                    labelText.contains("Email", ignoreCase = true) -> KeyboardType.Email
                    else -> KeyboardType.Text
                }
            ),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.testTag("${testTag}_toggle_visibility")
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color(0xFF888888),
                focusedLabelColor = Color.Black,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
        if (errorText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

data class PasswordValidationResult(
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecial: Boolean
) {
    val isValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecial

    val progress: Float
        get() = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecial)
            .count { it } / 5f
}

fun evaluatePasswordStrength(password: String): PasswordValidationResult {
    return PasswordValidationResult(
        hasMinLength = password.length >= 6,
        hasUppercase = password.any { it.isUpperCase() },
        hasLowercase = password.any { it.isLowerCase() },
        hasDigit = password.any { it.isDigit() },
        hasSpecial = password.any { !it.isLetterOrDigit() }
    )
}

@Composable
fun PasswordStrengthValidator(
    password: String,
    modifier: Modifier = Modifier
) {
    val result = remember(password) { evaluatePasswordStrength(password) }
    val activeColor = if (result.isValid) Color(0xFF2ECC71) else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        LinearProgressIndicator(
            progress = { result.progress },
            color = activeColor,
            trackColor = Color(0xFFE0E0E0),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        RequirementRow("At least 6 characters", result.hasMinLength)
        RequirementRow("At least 1 uppercase letter (A-Z)", result.hasUppercase)
        RequirementRow("At least 1 lowercase letter (a-z)", result.hasLowercase)
        RequirementRow("At least 1 numeric digit (0-9)", result.hasDigit)
        RequirementRow("At least 1 special character (!@#\$%)", result.hasSpecial)
    }
}

@Composable
private fun RequirementRow(text: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (met) "Requirement met" else "Requirement pending",
            tint = if (met) Color(0xFF2ECC71) else Color(0xFF9E9E9E),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (met) Color(0xFF2E7D32) else Color(0xFF757575)
        )
    }
}

@Composable
fun MyAlertDialog(
    title: String,
    subtitle: String,
    action1Name: String,
    action2Name: String,
    onAction1: () -> Unit,
    onAction2: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    AlertDialog(
        onDismissRequest = onAction1,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E1E)
            )
        },
        text = {
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAction1,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dialog_cancel_button")
                ) {
                    Text(action1Name, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onAction2,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dialog_confirm_button")
                ) {
                    Text(action2Name, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    )
}

@Composable
fun OrderCard(
    order: Order,
    onPickUpOrder: () -> Unit,
    isHistoryView: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isCommentSeeMore by remember { mutableStateOf(false) }
    var isFoodDetailsExpanded by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    val distance = remember(order.shop, order.address) {
        Helper.calculateDistance(
            order.shop.latitude,
            order.shop.longitude,
            order.address.latitude,
            order.address.longitude
        )
    }

    val timeFormatted = remember(order.time) {
        SimpleDateFormat("h:mm a", Locale.US).format(Date(order.time))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 8.dp)
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Order ID Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ORDER ID",
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (order.isRiderAccept && !order.isDelivered) {
                    Surface(
                        color = primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (order.isPickup) "EN ROUTE TO CUSTOMER" else "HEADING TO SHOP",
                            color = primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = "#${order.id}",
                    color = primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shop avatar & name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Shop icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.shop.shopName,
                        fontSize = 17.sp,
                        color = primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (order.shop.shopDescription.isNotBlank()) {
                        Text(
                            text = order.shop.shopDescription,
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PICK UP ORDER row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = "Pick up address",
                        tint = primary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PICK UP ORDER",
                        color = Color(0xFF9E9E9E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${order.shop.houseNumber} ${order.shop.street}".trim(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF212121)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DELIVER ORDER row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = "Deliver address",
                        tint = primary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DELIVER ORDER • ${order.user.name}",
                        color = Color(0xFF9E9E9E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${order.address.houseNumber} ${order.address.street}".trim(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF212121)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics row: Delivery Price, Time, Distance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Delivery earning",
                        tint = Color(0xFF616161),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", order.deliveryPrice),
                        color = Color(0xFF424242),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Order time",
                        tint = Color(0xFF616161),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        color = Color(0xFF424242),
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = "Delivery distance",
                        tint = Color(0xFF616161),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f km", distance),
                        color = Color(0xFF424242),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Method row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PAYMENT METHOD: ",
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (order.isPaid) {
                        String.format(Locale.US, "$ %.2f (Already paid)", order.totalPrice)
                    } else {
                        String.format(Locale.US, "$ %.2f (Not yet paid)", order.totalPrice)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (order.isPaid) Color(0xFF2E7D32) else Color(0xFFD35400)
                )
            }

            // Expandable Order Items summary
            if (order.foodOrders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFoodDetailsExpanded = !isFoodDetailsExpanded }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "ORDER ITEMS (${order.foodOrders.sumOf { it.quantity }})",
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (isFoodDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle order items",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(18.dp)
                    )
                }
                AnimatedVisibility(visible = isFoodDetailsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        order.foodOrders.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.foodName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f", item.foodPrice * item.quantity),
                                    fontSize = 13.sp,
                                    color = Color(0xFF616161)
                                )
                            }
                            if (item.customize.isNotBlank()) {
                                Text(
                                    text = "Note: ${item.customize}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(10.dp))

            // Comment section
            Text(
                text = "COMMENT",
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = order.address.deliveryInstruction.ifBlank { "No special delivery instructions." },
                fontSize = 13.sp,
                color = Color(0xFF424242),
                maxLines = if (isCommentSeeMore) Int.MAX_VALUE else 2,
                overflow = if (isCommentSeeMore) TextOverflow.Visible else TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCommentSeeMore = !isCommentSeeMore }
            )

            if (!isHistoryView) {
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextButton(
                    text = if (order.isRiderAccept) "OPEN LIVE MAP & DELIVER" else "PICK ORDER",
                    onPressed = onPickUpOrder,
                    isDisabled = false,
                    testTag = "pick_order_button_${order.id}"
                )
            }
        }
    }
}
