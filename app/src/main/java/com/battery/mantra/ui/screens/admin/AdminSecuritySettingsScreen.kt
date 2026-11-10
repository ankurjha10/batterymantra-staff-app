package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.GateConfigResponse
import com.battery.mantra.data.models.UpdateGatePinRequest
import com.battery.mantra.data.repository.AuthRepository
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.ui.theme.BrandRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminSecurityViewModel(
    private val authRepository: AuthRepository,
    isSuperAdmin: Boolean
) : ViewModel() {

    val biometricEnabled: StateFlow<Boolean> = authRepository.adminBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _gateConfigState = MutableStateFlow<AdminDataState<GateConfigResponse>>(
        if (isSuperAdmin) AdminDataState.Loading else AdminDataState.Idle
    )
    val gateConfigState: StateFlow<AdminDataState<GateConfigResponse>> = _gateConfigState.asStateFlow()

    init {
        if (isSuperAdmin) {
            fetchGateConfig()
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setAdminBiometricEnabled(enabled)
        }
    }

    fun fetchGateConfig() {
        viewModelScope.launch {
            _gateConfigState.value = AdminDataState.Loading
            authRepository.getGateConfig()
                .onSuccess { _gateConfigState.value = AdminDataState.Success(it) }
                .onFailure {
                    _gateConfigState.value = AdminDataState.Error(it.message ?: "Failed to load gate config")
                }
        }
    }

    fun updateGatePin(
        targetRole: String,
        newPin: String,
        currentPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            authRepository.updateGatePin(
                UpdateGatePinRequest(
                    targetRole = targetRole,
                    newPin = newPin,
                    currentPassword = currentPassword
                )
            ).onSuccess { updated ->
                _gateConfigState.value = AdminDataState.Success(updated)
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to update gate PIN")
            }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            isSuperAdmin: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminSecurityViewModel(authRepository, isSuperAdmin) as T
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSecuritySettingsScreen(
    role: String,
    viewModel: AdminSecurityViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isSuperAdmin = role.equals("ADMIN", ignoreCase = true)
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val gateConfigState by viewModel.gateConfigState.collectAsState()

    val biometricAvailability = remember(context) {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
    }
    val canUseBiometrics = biometricAvailability == BiometricManager.BIOMETRIC_SUCCESS

    val biometricNote = when (biometricAvailability) {
        BiometricManager.BIOMETRIC_SUCCESS ->
            "Require Fingerprint / Face Lock after entering the hidden gate PIN on this device."
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            "No fingerprint or face enrolled in Phone Settings. Biometric check is skipped automatically."
        else ->
            "No biometric sensor available on this device. Biometric check is skipped automatically."
    }

    var selectedTargetRole by remember { mutableStateOf("ADMIN") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var currentAdminPassword by remember { mutableStateOf("") }
    var isUpdatingPin by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Security", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundSurface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Biometric Toggle (Optional & Hardware-aware)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Biometric Gate Lock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = if (canUseBiometrics) "Optional second check on this phone" else "Unavailable on this device",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        Switch(
                            checked = biometricEnabled && canUseBiometrics,
                            onCheckedChange = { enabled ->
                                viewModel.setBiometricEnabled(enabled)
                            },
                            enabled = canUseBiometrics,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandRed
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = biometricNote,
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            // Gate PIN Management (Strictly Super Admin only)
            if (isSuperAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mobile App Gate PINs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin and Sub-Admin use separate Gate PINs. Updating here also reflects across the Website Admin Panel.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (val cfg = gateConfigState) {
                            is AdminDataState.Loading -> {
                                CircularProgressIndicator(
                                    color = BrandRed,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            is AdminDataState.Success -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GateStatusChip("Admin PIN", cfg.data.adminPinConfigured)
                                    GateStatusChip("Sub-Admin PIN", cfg.data.subAdminPinConfigured)
                                }
                            }
                            is AdminDataState.Error -> {
                                Text(text = cfg.message, fontSize = 12.sp, color = BrandRed)
                            }
                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Select Role PIN to Update",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ADMIN" to "Super Admin PIN", "SUB_ADMIN" to "Sub-Admin PIN").forEach { (roleKey, label) ->
                                val selected = selectedTargetRole == roleKey
                                Surface(
                                    onClick = { selectedTargetRole = roleKey },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) Color(0xFFFEE2E2) else Color(0xFFF9FAFB),
                                    border = BorderStroke(1.dp, if (selected) BrandRed else Color(0xFFD1D5DB)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color(0xFFB91C1C) else Color(0xFF374151),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            focusedLabelColor = BrandRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { newPin = it.filter { ch -> ch.isDigit() }.take(12) },
                            label = { Text("New Gate PIN (min 4 digits) *") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it.filter { ch -> ch.isDigit() }.take(12) },
                            label = { Text("Confirm New PIN *") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = currentAdminPassword,
                            onValueChange = { currentAdminPassword = it },
                            label = { Text("Current Super Admin Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (newPin.length < 4) {
                                    Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPin != confirmPin) {
                                    Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (currentAdminPassword.isBlank()) {
                                    Toast.makeText(context, "Enter your current password", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isUpdatingPin = true
                                viewModel.updateGatePin(
                                    targetRole = selectedTargetRole,
                                    newPin = newPin,
                                    currentPassword = currentAdminPassword,
                                    onSuccess = {
                                        isUpdatingPin = false
                                        newPin = ""
                                        confirmPin = ""
                                        currentAdminPassword = ""
                                        Toast.makeText(context, "Gate PIN updated", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isUpdatingPin = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isUpdatingPin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                        ) {
                            Text(
                                text = if (isUpdatingPin) "Updating..." else "Update Gate PIN",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GateStatusChip(label: String, configured: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (configured) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
    ) {
        Text(
            text = "$label: ${if (configured) "Configured" else "Default"}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (configured) Color(0xFF047857) else Color(0xFFB45309),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

