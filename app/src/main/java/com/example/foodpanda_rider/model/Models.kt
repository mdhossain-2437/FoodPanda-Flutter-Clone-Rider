package com.example.foodpanda_rider.model

import kotlinx.serialization.Serializable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt

@Serializable
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String = ""
)

@Serializable
data class Address(
    val id: String? = null,
    val houseNumber: String = "",
    val street: String = "",
    val area: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val province: String = "",
    val floor: String = "",
    val deliveryInstruction: String = "",
    val label: String = ""
)

@Serializable
data class Shop(
    val uid: String = "",
    val shopName: String = "",
    val shopDescription: String = "",
    val remainingTime: Int = 20,
    val deliveryPrice: Double = 1.5,
    val shopImage: String = "",
    val rating: Double = 4.8,
    val totalRating: Double = 250.0,
    val houseNumber: String = "",
    val street: String = "",
    val area: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val province: String = "",
    val floor: String = "",
    val isApproved: Boolean = true
)

@Serializable
data class FoodOrder(
    val foodName: String = "",
    val quantity: Int = 1,
    val foodPrice: Double = 0.0,
    val customize: String = ""
)

@Serializable
data class Rider(
    val user: User = User(),
    val latitude: Double = 11.5564,
    val longitude: Double = 104.9282
)

@Serializable
data class Order(
    val id: String = "",
    val user: User = User(),
    val address: Address = Address(),
    val shop: Shop = Shop(),
    val foodOrders: List<FoodOrder> = emptyList(),
    val totalPrice: Double = 0.0,
    val deliveryPrice: Double = 0.0,
    val discountPrice: Double = 0.0,
    val voucherId: String? = null,
    val isPaid: Boolean = false,
    val isShopAccept: Boolean = true,
    val isRiderAccept: Boolean = false,
    val isPickup: Boolean = false,
    val isNear: Boolean = false,
    val isDelivered: Boolean = false,
    val isCancelled: Boolean = false,
    val isCutlery: Boolean = false,
    val time: Long = System.currentTimeMillis(),
    val deliveredTime: Long = 0L,
    val rider: Rider? = null
)

object Helper {
    /**
     * Calculates the Haversine distance in kilometers between two coordinates,
     * matching lib/constants/helper.dart.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p = 0.017453292519943295
        val a = 0.5 -
            cos((lat2 - lat1) * p) / 2.0 +
            cos(lat1 * p) * cos(lat2 * p) * (1.0 - cos((lon2 - lon1) * p)) / 2.0
        return 12742.0 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}
