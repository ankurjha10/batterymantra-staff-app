package com.battery.mantra.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
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
 *
 * Characteristics:
 * - Left edge starts midway (~62% height)
 * - Curves down into a deep, round trough directly under the word "Sign" (~33% width, 96% height)
 * - Rises smoothly across the center to a shoulder (~70% width, 74% height)
 * - Swoops steeply upwards on the far right to ~38% height
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

                Spacer(modifier = Modifier.height(24.dp))

                // Centered Form Fields (Matching exact screenshot styling)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Label: Email
                    Text(
                        text = "Email",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Centered Email Pill Input
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
                                text = "Enter phone or email",
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

                    // Centered Pill "Sign In" Button with Glowing Gradient Border (Matching Screenshot)
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

                    // Centered "Forgot Password?" Link (No Social Logins)
                    TextButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please contact your Battery Mantra administrator to reset your password.")
                            }
                        }
                    ) {
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
        }
    }
}
