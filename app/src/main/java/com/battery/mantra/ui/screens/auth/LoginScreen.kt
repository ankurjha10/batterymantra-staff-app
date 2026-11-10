package com.battery.mantra.ui.screens.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.battery.mantra.R
import com.battery.mantra.ui.theme.BrandRed
import kotlinx.coroutines.launch
import java.util.Locale

class OrganicWaveHeaderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.38f)
            cubicTo(
                size.width * 0.94f, size.height * 0.54f,
                size.width * 0.84f, size.height * 0.70f,
                size.width * 0.70f, size.height * 0.74f
            )
            cubicTo(
                size.width * 0.56f, size.height * 0.78f,
                size.width * 0.44f, size.height * 0.96f,
                size.width * 0.33f, size.height * 0.96f
            )
            cubicTo(
                size.width * 0.20f, size.height * 0.96f,
                size.width * 0.10f, size.height * 0.74f,
                0f, size.height * 0.62f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

private fun canAuthenticateWithBiometrics(context: Context): Boolean {
    val manager = BiometricManager.from(context)
    val result = manager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
    )
    return result == BiometricManager.BIOMETRIC_SUCCESS
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    authState: AuthState,
    loginUiState: LoginUiState,
    adminGateState: AdminGateState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResetOtpState: () -> Unit,
    onLoginModeChange: (LoginMode) -> Unit,
    onAdminTriggerActivated: () -> Unit,
    onSubmitGatePin: (pin: String, canUseBiometricOnDevice: Boolean) -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricFailed: (String) -> Unit,
    onLockAdminGate: () -> Unit,
    onAdminLoginClick: () -> Unit,
    onLoginSuccess: (role: String) -> Unit,
    onResetState: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Stealth 7-tap state on the brand logo (zero visual hints on taps 1..6)
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapMs by remember { mutableLongStateOf(0L) }

    // Automatically re-lock hidden Admin gate if app goes to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                onLockAdminGate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onLoginSuccess(authState.loginResponse.role)
                onResetState()
            }
            is AuthState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(authState.message)
                }
                onResetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE53935),
                            Color(0xFFD32F2F),
                            Color(0xFFB71C1C),
                            Color(0xFF7F1D1D)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(OrganicWaveHeaderShape())
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Battery Mantra",
                                modifier = Modifier
                                    .height(34.dp)
                                    .width(135.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val now = System.currentTimeMillis()
                                        tapCount = if (now - lastTapMs <= 3000L) tapCount + 1 else 1
                                        lastTapMs = now
                                        if (tapCount >= 7) {
                                            tapCount = 0
                                            lastTapMs = 0L
                                            onAdminTriggerActivated()
                                        }
                                    },
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "Sign In",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            letterSpacing = (-1.5).sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Visible Public Tabs: Engineer vs Partner (strictly OTP-only)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .height(48.dp)
                        .background(Color.Black.copy(alpha = 0.32f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoginModeTabPill(
                        label = "Engineer",
                        isSelected = loginUiState.loginMode == LoginMode.ENGINEER_OTP,
                        onClick = { onLoginModeChange(LoginMode.ENGINEER_OTP) },
                        modifier = Modifier.weight(1f)
                    )
                    LoginModeTabPill(
                        label = "Partner",
                        isSelected = loginUiState.loginMode == LoginMode.PARTNER_OTP,
                        onClick = { onLoginModeChange(LoginMode.PARTNER_OTP) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                AnimatedContent(
                    targetState = loginUiState.loginMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "loginModeTransition"
                ) { mode ->
                    val roleSubtitle = when (mode) {
                        LoginMode.ENGINEER_OTP -> "Registered Engineer Mobile"
                        LoginMode.PARTNER_OTP -> "Registered Partner Branch Mobile"
                    }
                    PhoneOtpLoginView(
                        subtitle = roleSubtitle,
                        loginUiState = loginUiState,
                        authState = authState,
                        onPhoneNumberChange = onPhoneNumberChange,
                        onOtpChange = onOtpChange,
                        onSendOtpClick = onSendOtpClick,
                        onResendOtpClick = onResendOtpClick,
                        onVerifyOtpClick = onVerifyOtpClick,
                        onResetOtpState = onResetOtpState,
                        focusManager = focusManager
                    )
                }
            }
        }
    }

    if (adminGateState !is AdminGateState.Hidden) {
        AdminGateSheet(
            gateState = adminGateState,
            loginUiState = loginUiState,
            authState = authState,
            canUseBiometric = remember(context) { canAuthenticateWithBiometrics(context) },
            onDismiss = onLockAdminGate,
            onSubmitPin = { pin -> onSubmitGatePin(pin, canAuthenticateWithBiometrics(context)) },
            onBiometricSuccess = onBiometricSuccess,
            onBiometricFailed = onBiometricFailed,
            onIdentifierChange = onIdentifierChange,
            onPasswordChange = onPasswordChange,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            onAdminLoginClick = onAdminLoginClick
        )
    }
}

@Composable
private fun LoginModeTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandRed else Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun PhoneOtpLoginView(
    subtitle: String,
    loginUiState: LoginUiState,
    authState: AuthState,
    onPhoneNumberChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResetOtpState: () -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!loginUiState.isOtpSent) {
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = loginUiState.phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                placeholder = {
                    Text(
                        text = "Enter 10-digit number",
                        color = Color.White.copy(alpha = 0.40f),
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    ) {
                        Text(
                            text = "🇮🇳 +91",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                },
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Black.copy(alpha = 0.32f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
                    focusedBorderColor = Color.White.copy(alpha = 0.80f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    cursorColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (loginUiState.phoneNumber.length == 10 && !loginUiState.isOtpSending) {
                            focusManager.clearFocus()
                            onSendOtpClick()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "OTP will be sent via SMS & WhatsApp",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSendOtpClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFFFFCDD2),
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.32f),
                    contentColor = Color.White
                ),
                enabled = loginUiState.phoneNumber.length == 10 && !loginUiState.isOtpSending
            ) {
                if (loginUiState.isOtpSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Login,
                            contentDescription = "Get OTP",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Get OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OTP sent to",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "+91 ${loginUiState.phoneNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                TextButton(
                    onClick = onResetOtpState,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit Number",
                        tint = Color(0xFFFFCDD2),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Change",
                        color = Color(0xFFFFCDD2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter 4-Digit OTP",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = loginUiState.otp,
                onValueChange = onOtpChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 16.sp,
                    color = Color.White
                ),
                placeholder = {
                    Text(
                        text = "••••",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Black.copy(alpha = 0.32f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
                    focusedBorderColor = Color.White.copy(alpha = 0.85f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                    cursorColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (loginUiState.otp.length >= 4 && authState !is AuthState.Loading) {
                            focusManager.clearFocus()
                            onVerifyOtpClick()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (loginUiState.resendCountdown > 0) {
                Text(
                    text = "Resend OTP in ${loginUiState.resendCountdown}s",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                TextButton(
                    onClick = onResendOtpClick,
                    enabled = !loginUiState.isOtpSending
                ) {
                    Text(
                        text = "Resend OTP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onVerifyOtpClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFFFFCDD2),
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.32f),
                    contentColor = Color.White
                ),
                enabled = loginUiState.otp.length >= 4 && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Login,
                            contentDescription = "Verify & Login",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verify & Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminGateSheet(
    gateState: AdminGateState,
    loginUiState: LoginUiState,
    authState: AuthState,
    canUseBiometric: Boolean,
    onDismiss: () -> Unit,
    onSubmitPin: (String) -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricFailed: (String) -> Unit,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onAdminLoginClick: () -> Unit
) {
    val context = LocalContext.current
    var gatePin by remember(gateState is AdminGateState.PinChallenge) { mutableStateOf("") }

    fun launchBiometricPrompt() {
        val activity = context.findFragmentActivity()
        if (activity == null || !canUseBiometric) {
            onBiometricSuccess()
            return
        }
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onBiometricSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        onBiometricFailed(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    onBiometricFailed("Biometric verification failed. Try again.")
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Identity")
            .setSubtitle("Confirm biometric to unlock authorized sign-in")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
        prompt.authenticate(promptInfo)
    }

    LaunchedEffect(gateState) {
        if (gateState is AdminGateState.BiometricChallenge) {
            launchBiometricPrompt()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF2F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (gateState) {
                            is AdminGateState.LockedOut -> "Access Temporarily Locked"
                            is AdminGateState.PinChallenge -> "Restricted Access Gate"
                            is AdminGateState.BiometricChallenge -> "Biometric Verification"
                            is AdminGateState.Unlocked -> {
                                if (gateState.tier.equals("SUB_ADMIN", ignoreCase = true)) {
                                    "Sub-Admin Login"
                                } else {
                                    "Admin Login"
                                }
                            }
                            else -> "Access Verification"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (gateState) {
                is AdminGateState.LockedOut -> {
                    val mins = gateState.remainingSeconds / 60L
                    val secs = gateState.remainingSeconds % 60L
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = gateState.message,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "Cooldown: %02d:%02d", mins, secs),
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandRed,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                is AdminGateState.PinChallenge -> {
                    Text(
                        text = "Enter your Gate PIN (personal or tier PIN, min 4 chars).",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = gatePin,
                        onValueChange = { if (it.length <= 32) gatePin = it },
                        label = { Text("Gate PIN") },
                        leadingIcon = { Icon(Icons.Outlined.Pin, contentDescription = null, tint = BrandRed) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (gatePin.trim().length >= 4 && !gateState.isVerifying) {
                                    onSubmitPin(gatePin)
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            focusedLabelColor = BrandRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    if (!gateState.error.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = gateState.error,
                            fontSize = 12.sp,
                            color = BrandRed,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onSubmitPin(gatePin) },
                        enabled = gatePin.trim().length >= 4 && !gateState.isVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        if (gateState.isVerifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verify Access PIN", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                is AdminGateState.BiometricChallenge -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Fingerprint,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Confirm your device biometric to continue.",
                            fontSize = 14.sp,
                            color = Color(0xFF374151),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Button(
                                onClick = { launchBiometricPrompt() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Verify Biometric", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is AdminGateState.Unlocked -> {
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (gateState.tier.equals("SUB_ADMIN", ignoreCase = true)) {
                                "Sub-Admin Login"
                            } else {
                                "Admin Login"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginUiState.identifier,
                        onValueChange = onIdentifierChange,
                        label = { Text("Username or Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            focusedLabelColor = BrandRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loginUiState.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (loginUiState.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    painter = painterResource(
                                        id = if (loginUiState.passwordVisible) {
                                            R.drawable.ic_visibility_off
                                        } else {
                                            R.drawable.ic_visibility
                                        }
                                    ),
                                    contentDescription = if (loginUiState.passwordVisible) "Hide password" else "Show password",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onAdminLoginClick() }),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            focusedLabelColor = BrandRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    if (!gateState.loginError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = gateState.loginError,
                            fontSize = 12.sp,
                            color = BrandRed,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAdminLoginClick,
                        enabled = authState !is AuthState.Loading &&
                            loginUiState.identifier.isNotBlank() &&
                            loginUiState.password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
