package com.example.foodpanda_rider.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodpanda_rider.R
import com.example.foodpanda_rider.data.repository.AuthSessionState
import com.example.foodpanda_rider.ui.components.CustomTextButton
import com.example.foodpanda_rider.ui.components.CustomTextField
import com.example.foodpanda_rider.ui.components.PasswordStrengthValidator
import com.example.foodpanda_rider.ui.components.evaluatePasswordStrength
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

@Composable
fun SplashScreen(
    authState: AuthSessionState,
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToNoApprove: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(800L)
        if (!authState.isSignedIn) {
            onNavigateToAuth()
        } else if (authState.isApproved) {
            onNavigateToHome()
        } else {
            onNavigateToNoApprove()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "FoodPanda Rider Splash",
            modifier = Modifier.size(220.dp),
            contentScale = ContentScale.Fit
        )
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .size(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onQuickDemoSignIn: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("authentication_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.foodpanda_logo),
                    contentDescription = "FoodPanda Logo",
                    modifier = Modifier.size(190.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "Earn passive income with us, FoodPanda.",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF1E1E1E),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = "Welcome to FoodPanda!",
                color = Color(0xFF888888),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomTextButton(
                text = "I'm new here",
                onPressed = onNavigateToRegister,
                isDisabled = false,
                testTag = "auth_register_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomTextButton(
                text = "Sign In",
                onPressed = onNavigateToLogin,
                isDisabled = false,
                isOutlined = true,
                testTag = "auth_sign_in_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onQuickDemoSignIn,
                modifier = Modifier.testTag("auth_demo_login_button")
            ) {
                Text(
                    text = "Continue as Demo Rider (Sokha Vathana)",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authState: AuthSessionState,
    onClearError: () -> Unit,
    onLoginSubmit: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var emailText by remember { mutableStateOf("rider@foodpanda.com") }
    var passwordText by remember { mutableStateOf("Rider@123") }
    var errorEmailText by remember { mutableStateOf("") }
    var errorPasswordText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState.errorMessage) {
        val msg = authState.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearError()
        }
    }

    val handleLogin = {
        val trimmedEmail = emailText.trim()
        errorEmailText = if (trimmedEmail.isNotEmpty() && !EMAIL_REGEX.matches(trimmedEmail)) {
            "Enter a valid email address"
        } else {
            ""
        }
        errorPasswordText = if (passwordText.length < 6) {
            "Password has to be at least 6 characters long"
        } else {
            ""
        }
        if (errorEmailText.isEmpty() && errorPasswordText.isEmpty()) {
            onLoginSubmit(trimmedEmail, passwordText)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = handleLogin,
                        enabled = emailText.isNotBlank() && passwordText.isNotBlank(),
                        modifier = Modifier.testTag("login_top_continue_button")
                    ) {
                        Text(
                            text = "Continue",
                            color = if (emailText.isBlank() || passwordText.isBlank()) {
                                Color.LightGray
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("login_screen")
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login_icon),
                    contentDescription = "Login Icon",
                    modifier = Modifier
                        .padding(start = 8.dp, bottom = 18.dp)
                        .size(60.dp)
                )

                Text(
                    text = "Log in with your email",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.height(32.dp))

                CustomTextField(
                    value = emailText,
                    onValueChange = {
                        emailText = it
                        errorEmailText = ""
                    },
                    labelText = "Email",
                    errorText = errorEmailText,
                    testTag = "login_email_input"
                )

                Spacer(modifier = Modifier.height(18.dp))

                CustomTextField(
                    value = passwordText,
                    onValueChange = {
                        passwordText = it
                        errorPasswordText = ""
                    },
                    labelText = "Password",
                    isPassword = true,
                    errorText = errorPasswordText,
                    testTag = "login_password_input"
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "I forgot my password",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Password reset link sent to ${emailText.ifBlank { "your email" }}"
                                )
                            }
                        }
                        .padding(vertical = 6.dp)
                        .testTag("forgot_password_button")
                )
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextButton(
                text = "Continue",
                onPressed = handleLogin,
                isDisabled = emailText.isBlank() || passwordText.isBlank(),
                testTag = "login_continue_button"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authState: AuthSessionState,
    onClearError: () -> Unit,
    onRegisterSubmit: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var firstNameText by remember { mutableStateOf("") }
    var lastNameText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var errorEmailText by remember { mutableStateOf("") }
    var errorPasswordText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val pwStrength = remember(passwordText) { evaluatePasswordStrength(passwordText) }

    LaunchedEffect(authState.errorMessage) {
        val msg = authState.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearError()
        }
    }

    val handleRegister = {
        val trimmedEmail = emailText.trim()
        errorEmailText = if (trimmedEmail.isNotEmpty() && !EMAIL_REGEX.matches(trimmedEmail)) {
            "Enter a valid email address"
        } else {
            ""
        }
        errorPasswordText = if (!pwStrength.isValid) {
            "Please complete the requirement."
        } else {
            ""
        }

        if (errorEmailText.isEmpty() && errorPasswordText.isEmpty()) {
            onRegisterSubmit(firstNameText, lastNameText, trimmedEmail, passwordText)
        }
    }

    val isFormIncomplete = firstNameText.isBlank() ||
        lastNameText.isBlank() ||
        emailText.isBlank() ||
        passwordText.isBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = handleRegister,
                        enabled = !isFormIncomplete,
                        modifier = Modifier.testTag("register_top_continue_button")
                    ) {
                        Text(
                            text = "Continue",
                            color = if (isFormIncomplete) {
                                Color.LightGray
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("register_screen")
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_icon),
                    contentDescription = "Profile Icon",
                    modifier = Modifier
                        .padding(start = 8.dp, bottom = 16.dp)
                        .size(60.dp)
                )

                Text(
                    text = "Sign up with your email",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(
                        value = firstNameText,
                        onValueChange = { firstNameText = it },
                        labelText = "First name",
                        modifier = Modifier.weight(1f),
                        testTag = "register_first_name_input"
                    )
                    CustomTextField(
                        value = lastNameText,
                        onValueChange = { lastNameText = it },
                        labelText = "Last name",
                        modifier = Modifier.weight(1f),
                        testTag = "register_last_name_input"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = emailText,
                    onValueChange = {
                        emailText = it
                        errorEmailText = ""
                    },
                    labelText = "Email",
                    errorText = errorEmailText,
                    testTag = "register_email_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = passwordText,
                    onValueChange = {
                        passwordText = it
                        errorPasswordText = ""
                    },
                    labelText = "Password",
                    isPassword = true,
                    errorText = errorPasswordText,
                    testTag = "register_password_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordStrengthValidator(password = passwordText)

                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextButton(
                text = "Continue",
                onPressed = handleRegister,
                isDisabled = isFormIncomplete,
                testTag = "register_continue_button"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendVerificationEmailScreen(
    email: String,
    onVerifiedComplete: () -> Unit,
    onBack: () -> Unit
) {
    var alreadySent by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("verification_email_screen")
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.email_icon),
                    contentDescription = "Email Verification",
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .size(84.dp)
                )

                Text(
                    text = if (!alreadySent) {
                        "Verify your email address to get started"
                    } else {
                        "We've sent a verification link to your email"
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (!alreadySent) {
                        "This helps us mitigate fraud and keep your personal data safe ($email)"
                    } else {
                        "Please click the verification link in your inbox ($email)"
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color(0xFF616161)
                )
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))

            if (alreadySent) {
                CustomTextButton(
                    text = "Check inbox / Verify Now",
                    onPressed = {
                        try {
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_APP_EMAIL)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // If no email client is installed on emulator, complete verification directly
                        }
                        onVerifiedComplete()
                    },
                    isDisabled = false,
                    testTag = "check_inbox_button"
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            CustomTextButton(
                text = if (!alreadySent) "Send verification email" else "Resend verification link",
                onPressed = {
                    alreadySent = true
                    scope.launch {
                        snackbarHostState.showSnackbar("Verification link sent to $email")
                    }
                },
                isDisabled = false,
                isOutlined = alreadySent,
                testTag = "send_verification_button"
            )
        }
    }
}
