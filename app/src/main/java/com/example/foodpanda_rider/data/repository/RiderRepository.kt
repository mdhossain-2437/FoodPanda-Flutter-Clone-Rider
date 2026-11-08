package com.example.foodpanda_rider.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.foodpanda_rider.data.local.AppDatabase
import com.example.foodpanda_rider.data.local.OrderEntity
import com.example.foodpanda_rider.data.local.RiderAccountEntity
import com.example.foodpanda_rider.model.Address
import com.example.foodpanda_rider.model.FoodOrder
import com.example.foodpanda_rider.model.Helper
import com.example.foodpanda_rider.model.Order
import com.example.foodpanda_rider.model.Rider
import com.example.foodpanda_rider.model.Shop
import com.example.foodpanda_rider.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

data class AuthSessionState(
    val isSignedIn: Boolean = false,
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val imageUrl: String = "",
    val emailVerified: Boolean = true,
    val isApproved: Boolean = true,
    val errorMessage: String? = null
)

class RiderRepository(
    context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("foodpanda_rider_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val orderDao = database.orderDao()
    private val riderAccountDao = database.riderAccountDao()

    private val _authState = MutableStateFlow(loadSessionFromPrefs())
    val authState: StateFlow<AuthSessionState> = _authState.asStateFlow()

    val availableOrders: Flow<List<Order>> = orderDao.observeAvailableOrders().map { list ->
        list.mapNotNull { entity -> decodeOrder(entity.orderJson) }
    }

    val currentOrders: Flow<List<Order>> = orderDao.observeCurrentOrders().map { list ->
        list.mapNotNull { entity -> decodeOrder(entity.orderJson) }
    }

    val deliveredOrders: Flow<List<Order>> = orderDao.observeDeliveredOrders().map { list ->
        list.mapNotNull { entity -> decodeOrder(entity.orderJson) }
    }

    fun observeOrderById(orderId: String): Flow<Order?> =
        orderDao.observeOrderById(orderId).map { entity ->
            entity?.let { decodeOrder(it.orderJson) }
        }

    private fun decodeOrder(orderJson: String): Order? {
        return try {
            json.decodeFromString<Order>(orderJson)
        } catch (e: Exception) {
            null
        }
    }

    private fun encodeOrderEntity(order: Order): OrderEntity {
        val id = order.id.ifBlank { "FP-${UUID.randomUUID().toString().take(6).uppercase()}" }
        val normalized = order.copy(id = id)
        return OrderEntity(
            id = id,
            orderJson = json.encodeToString(normalized),
            isRiderAccept = normalized.isRiderAccept,
            isDelivered = normalized.isDelivered,
            riderUid = normalized.rider?.user?.uid,
            time = normalized.time,
            deliveredTime = normalized.deliveredTime
        )
    }

    private fun loadSessionFromPrefs(): AuthSessionState {
        val signedIn = prefs.getBoolean("signed_in", true)
        val uid = prefs.getString("uid", "rider_vathana_01") ?: "rider_vathana_01"
        val name = prefs.getString("name", "Sokha Vathana") ?: "Sokha Vathana"
        val email = prefs.getString("email", "rider@foodpanda.com") ?: "rider@foodpanda.com"
        val phone = prefs.getString("phoneNumber", "+855 12 889 402") ?: "+855 12 889 402"
        val imageUrl = prefs.getString("image_url", "") ?: ""
        val emailVerified = prefs.getBoolean("emailVerified", true)
        val isApproved = prefs.getBoolean("isApproved", true)

        if (!prefs.contains("signed_in")) {
            // Save initial default rider session so the app boots smoothly
            prefs.edit()
                .putBoolean("signed_in", true)
                .putString("uid", uid)
                .putString("name", name)
                .putString("email", email)
                .putString("phoneNumber", phone)
                .putString("image_url", imageUrl)
                .putBoolean("emailVerified", true)
                .putBoolean("isApproved", true)
                .apply()
        }

        return AuthSessionState(
            isSignedIn = signedIn,
            uid = uid,
            name = name,
            email = email,
            phoneNumber = phone,
            imageUrl = imageUrl,
            emailVerified = emailVerified,
            isApproved = isApproved
        )
    }

    private fun persistSession(state: AuthSessionState) {
        prefs.edit()
            .putBoolean("signed_in", state.isSignedIn)
            .putString("uid", state.uid)
            .putString("name", state.name)
            .putString("email", state.email)
            .putString("phoneNumber", state.phoneNumber)
            .putString("image_url", state.imageUrl)
            .putBoolean("emailVerified", state.emailVerified)
            .putBoolean("isApproved", state.isApproved)
            .apply()
        _authState.value = state
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    suspend fun ensureSeeded() {
        // Ensure default demo account exists
        val existingDemo = riderAccountDao.getByEmail("rider@foodpanda.com")
        if (existingDemo == null) {
            riderAccountDao.upsertAccount(
                RiderAccountEntity(
                    uid = "rider_vathana_01",
                    name = "Sokha Vathana",
                    email = "rider@foodpanda.com",
                    passwordHash = "Rider@123",
                    phoneNumber = "+855 12 889 402",
                    imageUrl = "",
                    emailVerified = true,
                    isApproved = true
                )
            )
        }

        if (orderDao.getOrderCount() == 0) {
            seedDefaultOrders()
        }
    }

    suspend fun seedDefaultOrders() {
        val now = System.currentTimeMillis()
        val demoRider = Rider(
            user = User(
                uid = "rider_vathana_01",
                email = "rider@foodpanda.com",
                name = "Sokha Vathana",
                phoneNumber = "+855 12 889 402"
            ),
            latitude = 11.5564,
            longitude = 104.9282
        )

        val initialOrders = listOf(
            Order(
                id = "FP-8041",
                user = User(
                    uid = "cust_01",
                    email = "dara.chan@gmail.com",
                    name = "Dara Chan",
                    phoneNumber = "+855 17 204 511"
                ),
                address = Address(
                    id = "addr_01",
                    houseNumber = "No. 42B",
                    street = "St. 288, BKK1",
                    area = "Boeung Keng Kang",
                    latitude = 11.5518,
                    longitude = 104.9261,
                    province = "Phnom Penh",
                    floor = "3rd Floor, Apt 302",
                    deliveryInstruction = "Please ring the intercom at gate B and leave with reception if busy. Extra chili sauce requested!",
                    label = "Home"
                ),
                shop = Shop(
                    uid = "shop_brown_coffee",
                    shopName = "BROWN Coffee & Bakery (BKK)",
                    shopDescription = "Artisanal coffee, pastries, and all-day brunch",
                    remainingTime = 15,
                    deliveryPrice = 2.50,
                    shopImage = "https://images.unsplash.com/photo-1509042239860-f550ce710b93",
                    rating = 4.9,
                    totalRating = 820.0,
                    houseNumber = "No. 25",
                    street = "St. 51 (Pasteur)",
                    area = "Boeung Keng Kang 1",
                    latitude = 11.5558,
                    longitude = 104.9278,
                    province = "Phnom Penh",
                    floor = "Ground Floor",
                    isApproved = true
                ),
                foodOrders = listOf(
                    FoodOrder("Iced Palm Sugar Latte (Large)", 2, 3.65, "Less sweet, extra shot"),
                    FoodOrder("Smoked Salmon Croissant", 1, 5.20, "Warmed up"),
                    FoodOrder("Dark Chocolate Tart", 1, 3.40, "Include fork")
                ),
                totalPrice = 15.90,
                deliveryPrice = 2.50,
                discountPrice = 1.00,
                voucherId = "PANDA10",
                isPaid = true,
                isShopAccept = true,
                isRiderAccept = false,
                isPickup = false,
                isNear = false,
                isDelivered = false,
                isCancelled = false,
                isCutlery = true,
                time = now - 8 * 60 * 1000L
            ),
            Order(
                id = "FP-8042",
                user = User(
                    uid = "cust_02",
                    email = "sophea.lim@gmail.com",
                    name = "Sophea Lim",
                    phoneNumber = "+855 96 441 908"
                ),
                address = Address(
                    id = "addr_02",
                    houseNumber = "No. 118",
                    street = "St. 130, Daun Penh",
                    area = "Riverside",
                    latitude = 11.5685,
                    longitude = 104.9296,
                    province = "Phnom Penh",
                    floor = "Lobby",
                    deliveryInstruction = "Call upon arrival at the main glass entrance. Paying cash on delivery.",
                    label = "Office"
                ),
                shop = Shop(
                    uid = "shop_malis",
                    shopName = "Malis Cambodian Restaurant",
                    shopDescription = "Authentic Living Cambodian Cuisine",
                    remainingTime = 20,
                    deliveryPrice = 3.25,
                    shopImage = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5",
                    rating = 4.8,
                    totalRating = 540.0,
                    houseNumber = "No. 136",
                    street = "Norodom Blvd",
                    area = "Tonle Bassac",
                    latitude = 11.5529,
                    longitude = 104.9310,
                    province = "Phnom Penh",
                    floor = "Ground Floor",
                    isApproved = true
                ),
                foodOrders = listOf(
                    FoodOrder("Kampot Crab Fried Rice", 1, 11.50, "Medium spicy, fresh lime"),
                    FoodOrder("Fish Amok in Banana Leaf", 1, 9.80, "Steamed jasmine rice"),
                    FoodOrder("Fresh Coconut Juice", 2, 2.50, "Chilled, no ice")
                ),
                totalPrice = 26.30,
                deliveryPrice = 3.25,
                discountPrice = 0.0,
                voucherId = null,
                isPaid = false,
                isShopAccept = true,
                isRiderAccept = false,
                isPickup = false,
                isNear = false,
                isDelivered = false,
                isCancelled = false,
                isCutlery = true,
                time = now - 14 * 60 * 1000L
            ),
            Order(
                id = "FP-8043",
                user = User(
                    uid = "cust_03",
                    email = "virak.kem@gmail.com",
                    name = "Virak Kem",
                    phoneNumber = "+855 10 553 190"
                ),
                address = Address(
                    id = "addr_03",
                    houseNumber = "No. 19",
                    street = "St. 310, Olympic",
                    area = "Chamkarmon",
                    latitude = 11.5475,
                    longitude = 104.9188,
                    province = "Phnom Penh",
                    floor = "2nd Floor",
                    deliveryInstruction = "Beware of friendly dog in front yard. Hook bag on the pink gate if no answer.",
                    label = "Apartment"
                ),
                shop = Shop(
                    uid = "shop_burger_king",
                    shopName = "Burger King (Toul Tom Poung)",
                    shopDescription = "Flame-grilled burgers, fries & shakes",
                    remainingTime = 12,
                    deliveryPrice = 1.85,
                    shopImage = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
                    rating = 4.6,
                    totalRating = 1120.0,
                    houseNumber = "No. 88",
                    street = "St. 155, Russian Market",
                    area = "Toul Tom Poung",
                    latitude = 11.5408,
                    longitude = 104.9152,
                    province = "Phnom Penh",
                    floor = "Ground Floor",
                    isApproved = true
                ),
                foodOrders = listOf(
                    FoodOrder("Double Whopper Cheese Meal", 1, 8.90, "Large fries, Coke Zero"),
                    FoodOrder("Crispy Chicken Tenders (6 pcs)", 1, 4.50, "BBQ & Honey Mustard dip")
                ),
                totalPrice = 13.40,
                deliveryPrice = 1.85,
                discountPrice = 1.50,
                voucherId = "BURGER15",
                isPaid = true,
                isShopAccept = true,
                isRiderAccept = false,
                isPickup = false,
                isNear = false,
                isDelivered = false,
                isCancelled = false,
                isCutlery = false,
                time = now - 22 * 60 * 1000L
            ),
            // Completed orders in Delivery History
            Order(
                id = "FP-7998",
                user = User(
                    uid = "cust_04",
                    email = "channary.p@gmail.com",
                    name = "Channary Pich",
                    phoneNumber = "+855 12 312 778"
                ),
                address = Address(
                    id = "addr_04",
                    houseNumber = "No. 64",
                    street = "St. 240, Chaktomuk",
                    area = "Daun Penh",
                    latitude = 11.5592,
                    longitude = 104.9295,
                    province = "Phnom Penh",
                    floor = "1st Floor",
                    deliveryInstruction = "Handed directly to customer at front porch.",
                    label = "Home"
                ),
                shop = Shop(
                    uid = "shop_koi_the",
                    shopName = "KOI Thé (Norodom)",
                    shopDescription = "Signature Golden Bubble Milk Tea & Macchiato",
                    remainingTime = 10,
                    deliveryPrice = 2.00,
                    shopImage = "https://images.unsplash.com/photo-1558857563-b371033873b8",
                    rating = 4.9,
                    totalRating = 1500.0,
                    houseNumber = "No. 102",
                    street = "Norodom Blvd",
                    area = "Daun Penh",
                    latitude = 11.5630,
                    longitude = 104.9265,
                    province = "Phnom Penh",
                    floor = "Ground Floor",
                    isApproved = true
                ),
                foodOrders = listOf(
                    FoodOrder("Golden Bubble Milk Tea (M)", 3, 2.90, "70% sugar, less ice"),
                    FoodOrder("Matcha Macchiato (M)", 1, 3.20, "50% sugar, normal ice")
                ),
                totalPrice = 11.90,
                deliveryPrice = 2.00,
                discountPrice = 0.0,
                isPaid = true,
                isShopAccept = true,
                isRiderAccept = true,
                isPickup = true,
                isNear = true,
                isDelivered = true,
                time = now - 95 * 60 * 1000L,
                deliveredTime = now - 65 * 60 * 1000L,
                rider = demoRider
            ),
            Order(
                id = "FP-7992",
                user = User(
                    uid = "cust_05",
                    email = "rithy.meas@gmail.com",
                    name = "Rithy Meas",
                    phoneNumber = "+855 89 774 112"
                ),
                address = Address(
                    id = "addr_05",
                    houseNumber = "No. 15A",
                    street = "St. 360, BKK3",
                    area = "Boeung Keng Kang 3",
                    latitude = 11.5460,
                    longitude = 104.9210,
                    province = "Phnom Penh",
                    floor = "4th Floor",
                    deliveryInstruction = "Leave on the shoe rack outside door 4A. Thank you!",
                    label = "Home"
                ),
                shop = Shop(
                    uid = "shop_pizza_company",
                    shopName = "The Pizza Company (AEON 1)",
                    shopDescription = "Hot pan pizzas, pasta & chicken wings",
                    remainingTime = 25,
                    deliveryPrice = 2.75,
                    shopImage = "https://images.unsplash.com/photo-1513104890138-7c749659a591",
                    rating = 4.7,
                    totalRating = 690.0,
                    houseNumber = "No. 132",
                    street = "Sothearos Blvd",
                    area = "Tonle Bassac",
                    latitude = 11.5479,
                    longitude = 104.9332,
                    province = "Phnom Penh",
                    floor = "Ground Floor",
                    isApproved = true
                ),
                foodOrders = listOf(
                    FoodOrder("Super Deluxe Pan Pizza (Large)", 1, 14.90, "Cheese crust"),
                    FoodOrder("BBQ Chicken Wings (6 pcs)", 1, 5.50, "Extra hot sauce")
                ),
                totalPrice = 20.40,
                deliveryPrice = 2.75,
                discountPrice = 2.00,
                isPaid = true,
                isShopAccept = true,
                isRiderAccept = true,
                isPickup = true,
                isNear = true,
                isDelivered = true,
                time = now - 180 * 60 * 1000L,
                deliveredTime = now - 150 * 60 * 1000L,
                rider = demoRider
            )
        )

        orderDao.upsertOrders(initialOrders.map { encodeOrderEntity(it) })
    }

    suspend fun createIncomingSampleOrder(): Order {
        val id = "FP-${(8044..8999).random()}"
        val sampleShops = listOf(
            Shop(
                uid = "shop_starbucks_bkk",
                shopName = "Starbucks Reserve (BKK Flagship)",
                shopDescription = "Handcrafted espresso beverages & fresh pastries",
                remainingTime = 14,
                deliveryPrice = 2.25,
                houseNumber = "No. 14",
                street = "St. 57, BKK1",
                area = "Boeung Keng Kang 1",
                latitude = 11.5542,
                longitude = 104.9250,
                province = "Phnom Penh",
                floor = "Ground Floor",
                isApproved = true
            ),
            Shop(
                uid = "shop_sushi_tei",
                shopName = "Sushi Tei (Vattanac Capital)",
                shopDescription = "Fresh Japanese sashimi, rolls & bento sets",
                remainingTime = 18,
                deliveryPrice = 3.00,
                houseNumber = "No. 66",
                street = "Monivong Blvd",
                area = "Wat Phnom",
                latitude = 11.5720,
                longitude = 104.9205,
                province = "Phnom Penh",
                floor = "Level 2",
                isApproved = true
            ),
            Shop(
                uid = "shop_texas_chicken",
                shopName = "Texas Chicken (Mao Tse Toung)",
                shopDescription = "Crunchier, juicier fried chicken & honey butter biscuits",
                remainingTime = 15,
                deliveryPrice = 2.10,
                houseNumber = "No. 210",
                street = "Mao Tse Toung Blvd",
                area = "Toul Svay Prey",
                latitude = 11.5450,
                longitude = 104.9120,
                province = "Phnom Penh",
                floor = "Ground Floor",
                isApproved = true
            )
        )
        val chosenShop = sampleShops.random()
        val newOrder = Order(
            id = id,
            user = User(
                uid = "cust_${UUID.randomUUID().toString().take(4)}",
                email = "nary.sok@gmail.com",
                name = "Nary Sok",
                phoneNumber = "+855 11 908 321"
            ),
            address = Address(
                id = "addr_new",
                houseNumber = "No. 77",
                street = "St. 294, BKK1",
                area = "Boeung Keng Kang",
                latitude = chosenShop.latitude + 0.0065,
                longitude = chosenShop.longitude + 0.0055,
                province = "Phnom Penh",
                floor = "5th Floor, Room 5B",
                deliveryInstruction = "Please call when arriving downstairs. Elevator code is #2580.",
                label = "Condo"
            ),
            shop = chosenShop,
            foodOrders = listOf(
                FoodOrder("Rider Special Combo Set", 2, 7.50, "No onions, extra sauce"),
                FoodOrder("Iced Passion Fruit Tea", 2, 2.75, "Less ice")
            ),
            totalPrice = 20.50,
            deliveryPrice = chosenShop.deliveryPrice,
            discountPrice = 0.0,
            isPaid = true,
            isShopAccept = true,
            isRiderAccept = false,
            isPickup = false,
            isNear = false,
            isDelivered = false,
            time = System.currentTimeMillis()
        )
        orderDao.upsertOrder(encodeOrderEntity(newOrder))
        return newOrder
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): Boolean {
        val existing = riderAccountDao.getByEmail(email)
        if (existing != null) {
            _authState.value = _authState.value.copy(
                errorMessage = "This email is already registered. Please sign in."
            )
            return false
        }
        val uid = "rider_${UUID.randomUUID().toString().take(8)}"
        val account = RiderAccountEntity(
            uid = uid,
            name = name.trim(),
            email = email.trim(),
            passwordHash = password,
            phoneNumber = "+855 12 889 402",
            imageUrl = "",
            emailVerified = false,
            isApproved = true
        )
        riderAccountDao.upsertAccount(account)
        val newState = AuthSessionState(
            isSignedIn = true,
            uid = account.uid,
            name = account.name,
            email = account.email,
            phoneNumber = account.phoneNumber,
            imageUrl = account.imageUrl,
            emailVerified = false,
            isApproved = true,
            errorMessage = null
        )
        persistSession(newState)
        return true
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Boolean {
        val existing = riderAccountDao.getByEmail(email.trim())
        if (existing != null) {
            if (existing.passwordHash != password) {
                _authState.value = _authState.value.copy(
                    errorMessage = "Username or password is incorrect"
                )
                return false
            }
            val newState = AuthSessionState(
                isSignedIn = true,
                uid = existing.uid,
                name = existing.name,
                email = existing.email,
                phoneNumber = existing.phoneNumber,
                imageUrl = existing.imageUrl,
                emailVerified = existing.emailVerified,
                isApproved = existing.isApproved,
                errorMessage = null
            )
            persistSession(newState)
            return true
        } else {
            // Allow signing in with any valid email/password for smooth testing
            val derivedName = email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { it.uppercase() }
                }.ifBlank { "FoodPanda Rider" }
            val uid = "rider_${UUID.randomUUID().toString().take(8)}"
            val account = RiderAccountEntity(
                uid = uid,
                name = derivedName,
                email = email.trim(),
                passwordHash = password,
                phoneNumber = "+855 12 889 402",
                imageUrl = "",
                emailVerified = true,
                isApproved = true
            )
            riderAccountDao.upsertAccount(account)
            val newState = AuthSessionState(
                isSignedIn = true,
                uid = account.uid,
                name = account.name,
                email = account.email,
                phoneNumber = account.phoneNumber,
                imageUrl = account.imageUrl,
                emailVerified = true,
                isApproved = true,
                errorMessage = null
            )
            persistSession(newState)
            return true
        }
    }

    suspend fun signInDemoRider() {
        ensureSeeded()
        val demo = riderAccountDao.getByEmail("rider@foodpanda.com")
        val state = AuthSessionState(
            isSignedIn = true,
            uid = demo?.uid ?: "rider_vathana_01",
            name = demo?.name ?: "Sokha Vathana",
            email = demo?.email ?: "rider@foodpanda.com",
            phoneNumber = demo?.phoneNumber ?: "+855 12 889 402",
            imageUrl = "",
            emailVerified = true,
            isApproved = true,
            errorMessage = null
        )
        persistSession(state)
    }

    suspend fun markEmailVerified() {
        val current = _authState.value
        val existing = riderAccountDao.getByUid(current.uid)
        if (existing != null) {
            riderAccountDao.upsertAccount(existing.copy(emailVerified = true))
        }
        persistSession(current.copy(emailVerified = true))
    }

    suspend fun setAccountApproval(isApproved: Boolean) {
        val current = _authState.value
        val existing = riderAccountDao.getByUid(current.uid)
        if (existing != null) {
            riderAccountDao.upsertAccount(existing.copy(isApproved = isApproved))
        }
        persistSession(current.copy(isApproved = isApproved))
    }

    suspend fun updateRiderProfile(name: String, phone: String) {
        val current = _authState.value
        val existing = riderAccountDao.getByUid(current.uid)
        if (existing != null) {
            riderAccountDao.upsertAccount(
                existing.copy(
                    name = name.trim().ifBlank { current.name },
                    phoneNumber = phone.trim().ifBlank { current.phoneNumber }
                )
            )
        }
        persistSession(
            current.copy(
                name = name.trim().ifBlank { current.name },
                phoneNumber = phone.trim().ifBlank { current.phoneNumber }
            )
        )
    }

    fun signOut() {
        prefs.edit().clear().putBoolean("signed_in", false).apply()
        _authState.value = AuthSessionState(isSignedIn = false)
    }

    suspend fun acceptOrder(order: Order, riderLat: Double, riderLng: Double): Order {
        val current = _authState.value
        val rider = Rider(
            user = User(
                uid = current.uid.ifBlank { "rider_vathana_01" },
                email = current.email.ifBlank { "rider@foodpanda.com" },
                name = current.name.ifBlank { "Sokha Vathana" },
                phoneNumber = current.phoneNumber
            ),
            latitude = riderLat,
            longitude = riderLng
        )
        val updated = order.copy(
            isRiderAccept = true,
            isShopAccept = true,
            rider = rider
        )
        orderDao.upsertOrder(encodeOrderEntity(updated))
        return updated
    }

    suspend fun pickUpOrder(orderId: String): Order? {
        val existingEntity = orderDao.getOrderById(orderId) ?: return null
        val order = decodeOrder(existingEntity.orderJson) ?: return null
        val distanceToCustomer = Helper.calculateDistance(
            order.shop.latitude,
            order.shop.longitude,
            order.address.latitude,
            order.address.longitude
        )
        val updated = order.copy(
            isPickup = true,
            isNear = distanceToCustomer <= 0.5,
            rider = order.rider?.copy(
                latitude = order.shop.latitude,
                longitude = order.shop.longitude
            ) ?: Rider(
                user = User(
                    uid = _authState.value.uid,
                    email = _authState.value.email,
                    name = _authState.value.name
                ),
                latitude = order.shop.latitude,
                longitude = order.shop.longitude
            )
        )
        orderDao.upsertOrder(encodeOrderEntity(updated))
        return updated
    }

    suspend fun updateRiderLocation(orderId: String, latitude: Double, longitude: Double): Order? {
        val existingEntity = orderDao.getOrderById(orderId) ?: return null
        val order = decodeOrder(existingEntity.orderJson) ?: return null
        val distance = Helper.calculateDistance(
            latitude,
            longitude,
            order.address.latitude,
            order.address.longitude
        )
        val isNearNow = order.isNear || (distance <= 0.5)
        val updatedRider = Rider(
            user = User(
                uid = _authState.value.uid.ifBlank { "rider_vathana_01" },
                email = _authState.value.email.ifBlank { "rider@foodpanda.com" },
                name = _authState.value.name.ifBlank { "Sokha Vathana" },
                phoneNumber = _authState.value.phoneNumber
            ),
            latitude = latitude,
            longitude = longitude
        )
        val updated = order.copy(
            rider = updatedRider,
            isNear = isNearNow
        )
        orderDao.upsertOrder(encodeOrderEntity(updated))
        return updated
    }

    suspend fun deliverOrder(orderId: String): Order? {
        val existingEntity = orderDao.getOrderById(orderId) ?: return null
        val order = decodeOrder(existingEntity.orderJson) ?: return null
        val updated = order.copy(
            isPickup = true,
            isNear = true,
            isDelivered = true,
            deliveredTime = System.currentTimeMillis()
        )
        orderDao.upsertOrder(encodeOrderEntity(updated))
        return updated
    }
}
