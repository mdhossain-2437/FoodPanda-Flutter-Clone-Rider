package com.example.foodpanda_rider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.foodpanda_rider.ui.screens.AuthenticationScreen
import com.example.foodpanda_rider.ui.screens.FinishDeliverScreen
import com.example.foodpanda_rider.ui.screens.HomeScreen
import com.example.foodpanda_rider.ui.screens.HomeScreenNoApprove
import com.example.foodpanda_rider.ui.screens.LoginScreen
import com.example.foodpanda_rider.ui.screens.MapScreen
import com.example.foodpanda_rider.ui.screens.OrderHistoryScreen
import com.example.foodpanda_rider.ui.screens.RegisterScreen
import com.example.foodpanda_rider.ui.screens.SendVerificationEmailScreen
import com.example.foodpanda_rider.ui.screens.SplashScreen
import com.example.foodpanda_rider.ui.theme.FoodPandaRiderTheme
import com.example.foodpanda_rider.ui.viewmodel.RiderViewModel
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object AuthRoute

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object VerifyEmailRoute

@Serializable
object HomeRoute

@Serializable
object HomeNoApproveRoute

@Serializable
data class MapDeliveryRoute(val orderId: String)

@Serializable
data class FinishDeliverRoute(val price: Double)

@Serializable
object OrderHistoryRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodPandaRiderTheme {
                FoodPandaRiderApp()
            }
        }
    }
}

@Composable
fun FoodPandaRiderApp(
    viewModel: RiderViewModel = viewModel(
        factory = RiderViewModel.provideFactory(LocalContext.current)
    )
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val availableOrders by viewModel.availableOrders.collectAsStateWithLifecycle()
    val currentOrders by viewModel.currentOrders.collectAsStateWithLifecycle()
    val deliveredOrders by viewModel.deliveredOrders.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<SplashRoute> {
            SplashScreen(
                authState = authState,
                onNavigateToAuth = {
                    navController.navigate(AuthRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToNoApprove = {
                    navController.navigate(HomeNoApproveRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<AuthRoute> {
            AuthenticationScreen(
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToLogin = { navController.navigate(LoginRoute) },
                onQuickDemoSignIn = {
                    viewModel.signInDemoRider {
                        navController.navigate(HomeRoute) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                authState = authState,
                onClearError = { viewModel.clearAuthError() },
                onLoginSubmit = { email, password ->
                    viewModel.signIn(email, password) { session ->
                        navController.navigate(HomeRoute) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                        if (!session.emailVerified) {
                            navController.navigate(VerifyEmailRoute)
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                authState = authState,
                onClearError = { viewModel.clearAuthError() },
                onRegisterSubmit = { firstName, lastName, email, password ->
                    viewModel.register(firstName, lastName, email, password) {
                        navController.navigate(HomeRoute) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                        navController.navigate(VerifyEmailRoute)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<VerifyEmailRoute> {
            SendVerificationEmailScreen(
                email = authState.email,
                onVerifiedComplete = {
                    viewModel.verifyEmail {
                        navController.navigate(HomeRoute) {
                            popUpTo(HomeRoute) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<HomeRoute> {
            if (!authState.isSignedIn) {
                AuthenticationScreen(
                    onNavigateToRegister = { navController.navigate(RegisterRoute) },
                    onNavigateToLogin = { navController.navigate(LoginRoute) },
                    onQuickDemoSignIn = {
                        viewModel.signInDemoRider {}
                    }
                )
            } else if (!authState.isApproved) {
                HomeScreenNoApprove(
                    onApproveAccount = { viewModel.setAccountApproval(true) }
                )
            } else {
                HomeScreen(
                    authState = authState,
                    availableOrders = availableOrders,
                    currentOrders = currentOrders,
                    deliveredOrders = deliveredOrders,
                    onPickOrder = { order ->
                        if (order.isRiderAccept) {
                            navController.navigate(MapDeliveryRoute(orderId = order.id))
                        } else {
                            viewModel.acceptOrder(order) { accepted ->
                                navController.navigate(MapDeliveryRoute(orderId = accepted.id))
                            }
                        }
                    },
                    onOpenOrderHistory = {
                        navController.navigate(OrderHistoryRoute)
                    },
                    onCreateIncomingOrder = {
                        viewModel.createIncomingSampleOrder()
                    },
                    onResetSampleOrders = {
                        viewModel.resetSampleOrders()
                    },
                    onUpdateProfile = { name, phone ->
                        viewModel.updateProfile(name, phone)
                    },
                    onToggleApproval = { approved ->
                        viewModel.setAccountApproval(approved)
                    },
                    onSignOut = {
                        viewModel.signOut {
                            navController.navigate(AuthRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        composable<HomeNoApproveRoute> {
            HomeScreenNoApprove(
                onApproveAccount = {
                    viewModel.setAccountApproval(true)
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeNoApproveRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<MapDeliveryRoute> { backStackEntry ->
            val route: MapDeliveryRoute = backStackEntry.toRoute()
            val order by viewModel.observeOrderById(route.orderId)
                .collectAsStateWithLifecycle(initialValue = null)

            val currentOrder = order
            if (currentOrder == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                MapScreen(
                    order = currentOrder,
                    onPickUpOrder = { viewModel.pickUpOrder(currentOrder.id) },
                    onStepRiderCloser = { viewModel.stepRiderTowardDestination(currentOrder) },
                    onArriveAtCustomer = { viewModel.arriveAtCustomerLocation(currentOrder) },
                    onDeliverOrder = {
                        viewModel.completeDelivery(currentOrder.id) { earnedPrice ->
                            navController.navigate(FinishDeliverRoute(price = earnedPrice)) {
                                popUpTo(MapDeliveryRoute(orderId = currentOrder.id)) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onRefreshGpsLocation = { viewModel.refreshDeviceLocation(context) },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable<FinishDeliverRoute> { backStackEntry ->
            val route: FinishDeliverRoute = backStackEntry.toRoute()
            FinishDeliverScreen(
                price = route.price,
                onNewRide = {
                    navController.popBackStack()
                }
            )
        }

        composable<OrderHistoryRoute> {
            OrderHistoryScreen(
                deliveredOrders = deliveredOrders,
                onRefresh = {},
                onBack = { navController.popBackStack() }
            )
        }
    }
}
