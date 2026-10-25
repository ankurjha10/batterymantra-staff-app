package com.battery.mantra.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.battery.mantra.R
import kotlinx.coroutines.launch

/**
 * Mathematically precise organic wave shape directly matching the reference screenshot.
 */
class OrganicWaveHeaderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            // Right edge: high cut at 38%
            lineTo(size.width, size.height * 0.38f)

            // Segment 1: Right edge (1.0, 0.38) down to shoulder (0.70, 0.74)
            cubicTo(
                size.width * 0.94f, size.height * 0.54f,
                size.width * 0.84f, size.height * 0.70f,
                size.width * 0.70f, size.height * 0.74f
            )

            // Segment 2: Shoulder (0.70, 0.74) down to deep trough under "Sign" (0.33, 0.96)
            cubicTo(
                size.width * 0.56f, size.height * 0.78f,
                size.width * 0.44f, size.height * 0.96f,
                size.width * 0.33f, size.height * 0.96f
            )

            // Segment 3: Trough (0.33, 0.96) up to left edge (0.0, 0.62)
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

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    authState: AuthState,
    loginUiState: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onPhoneNumberChange: (String) -> Unit = {},
    onOtpChange: (String) -> Unit = {},
    onSendOtpClick: () -> Unit = {},
    onResendOtpClick: () -> Unit = {},
    onVerifyOtpClick: () -> Unit = {},
    onResetOtpState: () -> Unit = {},
    onLoginModeChange: (LoginMode) -> Unit = {},
    onLoginSuccess: (role: String) -> Unit,
    onResetState: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val role = (authState as AuthState.Success).loginResponse.role
                onLoginSuccess(role)
                onResetState()
            }
            is AuthState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((authState as AuthState.Error).message)
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
        // Rich Battery Mantra Red Gradient Background
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE53935), // Vibrant Red
                            Color(0xFFD32F2F), // Brand Red
                            Color(0xFFB71C1C), // Deep Crimson
                            Color(0xFF7F1D1D)  // Dark Burgundy
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
                // Precise Organic White Wave Header
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
                        // Top Row: Logo on Left (Clean & without Sign Up button)
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
                                    .width(135.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Big Bold "Sign In" Headline centered inside the white bulb
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

                // Segmented Pill Tab Switch: Engineer Login (Default) vs Admin / Partner
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
                    // Engineer Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (loginUiState.loginMode == LoginMode.ENGINEER_OTP) Color.White else Color.Transparent
                            )
                            .clickable { onLoginModeChange(LoginMode.ENGINEER_OTP) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Engineer (OTP)",
                            fontSize = 13.sp,
                            fontWeight = if (loginUiState.loginMode == LoginMode.ENGINEER_OTP) FontWeight.Bold else FontWeight.Medium,
                            color = if (loginUiState.loginMode == LoginMode.ENGINEER_OTP) Color(0xFFD32F2F) else Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Admin / Partner Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (loginUiState.loginMode == LoginMode.ADMIN_PASSWORD) Color.White else Color.Transparent
                            )
                            .clickable { onLoginModeChange(LoginMode.ADMIN_PASSWORD) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Admin / Partner",
                            fontSize = 13.sp,
                            fontWeight = if (loginUiState.loginMode == LoginMode.ADMIN_PASSWORD) FontWeight.Bold else FontWeight.Medium,
                            color = if (loginUiState.loginMode == LoginMode.ADMIN_PASSWORD) Color(0xFFD32F2F) else Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Content View according to LoginMode
                AnimatedContent(
                    targetState = loginUiState.loginMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "loginModeTransition"
                ) { mode ->
                    when (mode) {
                        LoginMode.ENGINEER_OTP -> {
                            EngineerOtpView(
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
                        LoginMode.ADMIN_PASSWORD -> {
                            AdminPasswordView(
                                loginUiState = loginUiState,
                                authState = authState,
                                onIdentifierChange = onIdentifierChange,
                                onPasswordChange = onPasswordChange,
                                onTogglePasswordVisibility = onTogglePasswordVisibility,
                                onLoginClick = onLoginClick,
                                focusManager = focusManager,
                                onForgotPassword = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please contact your Battery Mantra administrator to reset your password.")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Engineer Login View using Phone Number + 4-digit OTP.
 */
@Composable
private fun EngineerOtpView(
    loginUiState: LoginUiState,
    authState: AuthState,
    onPhoneNumberChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResetOtpState: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!loginUiState.isOtpSent) {
            // STEP 1: Enter Phone Number
            Text(
                text = "Registered Mobile Number",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Phone Number Pill Input with +91 Prefix
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

            // "Get OTP" Button
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
            // STEP 2: Enter & Verify OTP
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

            // Centered 4-Digit OTP Field with large spacing
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

            // Resend Countdown
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

            // "Verify & Login" Button
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

/**
 * Admin & Partner Login View using Username/Email and Password.
 */
@Composable
private fun AdminPasswordView(
    loginUiState: LoginUiState,
    authState: AuthState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onForgotPassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered Label: Email / Username
        Text(
            text = "Username or Email",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Centered Username Pill Input
        OutlinedTextField(
            value = loginUiState.identifier,
            onValueChange = onIdentifierChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = Color.White
            ),
            placeholder = {
                Text(
                    text = "Enter username or email",
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 15.sp,
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
                focusedBorderColor = Color.White.copy(alpha = 0.80f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                cursorColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Centered Label: Password
        Text(
            text = "Password",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Centered Password Pill Input
        OutlinedTextField(
            value = loginUiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = Color.White
            ),
            placeholder = {
                Text(
                    text = "••••••••••••",
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = CircleShape,
            visualTransformation = if (loginUiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLoginClick()
                }
            ),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        painter = painterResource(id = if (loginUiState.passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = if (loginUiState.passwordVisible) "Hide password" else "Show password",
                        tint = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Black.copy(alpha = 0.32f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
                focusedBorderColor = Color.White.copy(alpha = 0.80f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                cursorColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(30.dp))

        // "Sign In" Button
        Button(
            onClick = {
                focusManager.clearFocus()
                onLoginClick()
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
            enabled = authState !is AuthState.Loading
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
                        contentDescription = "Sign In",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Centered "Forgot Password?" Link
        TextButton(onClick = onForgotPassword) {
            Text(
                text = stringResource(id = R.string.forgot_password),
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
