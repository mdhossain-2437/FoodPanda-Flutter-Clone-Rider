package com.example.foodpanda_rider.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodpanda_rider.data.repository.AuthSessionState
import com.example.foodpanda_rider.data.repository.RiderRepository
import com.example.foodpanda_rider.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RiderLocationState(
    val latitude: Double = 11.5564,
    val longitude: Double = 104.9282,
    val isGpsActive: Boolean = false
)

class RiderViewModel(
    private val repository: RiderRepository
) : ViewModel() {

    val authState: StateFlow<AuthSessionState> = repository.authState

    val availableOrders: StateFlow<List<Order>> = repository.availableOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentOrders: StateFlow<List<Order>> = repository.currentOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveredOrders: StateFlow<List<Order>> = repository.deliveredOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _locationState = MutableStateFlow(RiderLocationState())
    val locationState: StateFlow<RiderLocationState> = _locationState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun observeOrderById(orderId: String): Flow<Order?> = repository.observeOrderById(orderId)

    @SuppressLint("MissingPermission")
    fun refreshDeviceLocation(context: Context) {
        try {
            val locationManager =
                context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (gpsLocation != null) {
                _locationState.value = RiderLocationState(
                    latitude = gpsLocation.latitude,
                    longitude = gpsLocation.longitude,
                    isGpsActive = true
                )
            }
        } catch (_: Exception) {
            // Keep default Phnom Penh coordinates if device GPS is unavailable in emulator
        }
    }

    fun setRiderLocationManually(latitude: Double, longitude: Double) {
        _locationState.value = _locationState.value.copy(
            latitude = latitude,
            longitude = longitude
        )
    }

    fun clearAuthError() {
        repository.clearError()
    }

    fun signIn(email: String, password: String, onSuccess: (AuthSessionState) -> Unit) {
        viewModelScope.launch {
            val ok = repository.signInWithEmailAndPassword(email, password)
            if (ok) {
                onSuccess(repository.authState.value)
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: (AuthSessionState) -> Unit
    ) {
        viewModelScope.launch {
            val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
            val ok = repository.registerWithEmail(fullName, email, password)
            if (ok) {
                onSuccess(repository.authState.value)
            }
        }
    }

    fun signInDemoRider(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signInDemoRider()
            onSuccess()
        }
    }

    fun verifyEmail(onVerified: () -> Unit) {
        viewModelScope.launch {
            repository.markEmailVerified()
            onVerified()
        }
    }

    fun setAccountApproval(isApproved: Boolean) {
        viewModelScope.launch {
            repository.setAccountApproval(isApproved)
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            repository.updateRiderProfile(name, phone)
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        repository.signOut()
        onSignedOut()
    }

    fun acceptOrder(order: Order, onAccepted: (Order) -> Unit) {
        viewModelScope.launch {
            val loc = _locationState.value
            val updated = repository.acceptOrder(order, loc.latitude, loc.longitude)
            onAccepted(updated)
        }
    }

    fun pickUpOrder(orderId: String) {
        viewModelScope.launch {
            val updated = repository.pickUpOrder(orderId)
            if (updated != null) {
                _locationState.value = _locationState.value.copy(
                    latitude = updated.shop.latitude,
                    longitude = updated.shop.longitude
                )
            }
        }
    }

    fun stepRiderTowardDestination(order: Order) {
        viewModelScope.launch {
            val currentLat = order.rider?.latitude ?: order.shop.latitude
            val currentLng = order.rider?.longitude ?: order.shop.longitude
            val targetLat = if (!order.isPickup) order.shop.latitude else order.address.latitude
            val targetLng = if (!order.isPickup) order.shop.longitude else order.address.longitude

            // Move 45% closer each step, snapping within 0.0015 degrees (~150m < 0.5km)
            val nextLat = currentLat + (targetLat - currentLat) * 0.45
            val nextLng = currentLng + (targetLng - currentLng) * 0.45
            _locationState.value = _locationState.value.copy(latitude = nextLat, longitude = nextLng)
            repository.updateRiderLocation(order.id, nextLat, nextLng)
        }
    }

    fun arriveAtCustomerLocation(order: Order) {
        viewModelScope.launch {
            val targetLat = order.address.latitude
            val targetLng = order.address.longitude
            _locationState.value = _locationState.value.copy(latitude = targetLat, longitude = targetLng)
            repository.updateRiderLocation(order.id, targetLat, targetLng)
        }
    }

    fun completeDelivery(orderId: String, onDelivered: (Double) -> Unit) {
        viewModelScope.launch {
            val delivered = repository.deliverOrder(orderId)
            if (delivered != null) {
                onDelivered(delivered.deliveryPrice)
            }
        }
    }

    fun createIncomingSampleOrder() {
        viewModelScope.launch {
            repository.createIncomingSampleOrder()
        }
    }

    fun resetSampleOrders() {
        viewModelScope.launch {
            repository.seedDefaultOrders()
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RiderViewModel(RiderRepository(context.applicationContext))
            }
        }
    }
}
