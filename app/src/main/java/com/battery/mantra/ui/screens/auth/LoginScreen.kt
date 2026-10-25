package com.battery.mantra.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.R
import kotlinx.coroutines.launch

/**
 * Custom Organic Wave Shape for the Top Header.
 * Recreates the fluid organic wave from the reimagined design.
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
            lineTo(size.width, size.height * 0.42f)
            // Swoop down towards right-center
            cubicTo(
                size.width * 0.90f, size.height * 0.80f,
                size.width * 0.65f, size.height * 0.72f,
                size.width * 0.48f, size.height * 0.92f
            )
            // Smooth dip lobe on left-center rising towards top-left
            cubicTo(
                size.width * 0.32f, size.height * 1.05f,
                size.width * 0.12f, size.height * 0.82f,
                0f, size.height * 0.62f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Secondary subtle wave shape behind the main wave to add depth and elevation
 */
class OrganicWaveAccentShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.46f)
            cubicTo(
                size.width * 0.92f, size.height * 0.84f,
                size.width * 0.67f, size.height * 0.76f,
                size.width * 0.48f, size.height * 0.96f
            )
            cubicTo(
                size.width * 0.32f, size.height * 1.09f,
                size.width * 0.12f, size.height * 0.86f,
                0f, size.height * 0.66f
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
        // Rich Red Gradient Background (Replacing the Black in the reference)
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE53935), // Vibrant Red
                            Color(0xFFD32F2F), // Brand Red
                            Color(0xFFB71C1C), // Deep Crimson
                            Color(0xFF7F1D1D)  // Rich Dark Burgundy
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Organic White Wave Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    // Accent wave shadow layer for depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(OrganicWaveAccentShape())
                            .background(Color.White.copy(alpha = 0.22f))
                    )

                    // Main crisp white wave
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(OrganicWaveHeaderShape())
                            .background(Color.White)
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Top Row: Battery Mantra Logo (No Sign Up on right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Battery Mantra",
                                    modifier = Modifier
                                        .height(38.dp)
                                        .width(150.dp),
                                    contentScale = ContentScale.Fit,
                                    alignment = Alignment.CenterStart
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFD32F2F).copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = "PORTAL",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Large Bold "Sign In" Headline (Matches reference)
                            Text(
                                text = "Sign In",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E293B),
                                letterSpacing = (-1.5).sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Text(
                                text = "Internal Partner & Engineer Access",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Input Fields Section (Pill-shaped, sleek dark-crimson inputs matching reference layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                ) {
                    // Label: Email / Phone
                    Text(
                        text = "Phone Number or Email",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    // Phone/Email Pill Input
                    OutlinedTextField(
                        value = loginUiState.identifier,
                        onValueChange = onIdentifierChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, CircleShape),
                        placeholder = {
                            Text(
                                text = "Enter phone or email",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.70f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
                            focusedBorderColor = Color.White.copy(alpha = 0.85f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Label: Password
                    Text(
                        text = "Password",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    // Password Pill Input
                    OutlinedTextField(
                        value = loginUiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, CircleShape),
                        placeholder = {
                            Text(
                                text = "Enter your password",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.70f),
                                modifier = Modifier.size(20.dp)
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
                            focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
                            focusedBorderColor = Color.White.copy(alpha = 0.85f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                            cursorColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Sign In Pill Button (Clean, high-contrast, modern action button)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onLoginClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFD32F2F)
                        ),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color(0xFFD32F2F),
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
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign In",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Forgot Password / Support Footer
                    TextButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please contact your Battery Mantra administrator to reset your password.")
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(id = R.string.forgot_password),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
