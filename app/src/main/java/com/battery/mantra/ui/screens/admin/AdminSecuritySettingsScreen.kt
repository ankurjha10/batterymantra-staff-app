package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.battery.mantra.data.models.GateUserPinStatus
import com.battery.mantra.data.models.UpdateGatePinRequest
import com.battery.mantra.data.models.UpdateUserGatePinRequest
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
    private val isSuperAdmin: Boolean
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
        if (!isSuperAdmin) return
        viewModelScope.launch {
            if (_gateConfigState.value !is AdminDataState.Success) {
                _gateConfigState.value = AdminDataState.Loading
            }
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
            ).onSuccess {
                fetchGateConfig()
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to update fallback gate PIN")
            }
        }
    }

    fun updateUserGatePin(
        userId: String,
        newPin: String,
        currentPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            authRepository.updateUserGatePin(
                userId = userId,
                request = UpdateUserGatePinRequest(
                    newPin = newPin,
                    currentPassword = currentPassword
                )
            ).onSuccess {
                fetchGateConfig()
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to update personal gate PIN")
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
    var isUpdatingFallback by remember { mutableStateOf(false) }

    var editingUserPin by remember { mutableStateOf<GateUserPinStatus?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        focusedLabelColor = BrandRed,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

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

            // Gate PIN Management (Strictly Super Admin only; hidden for SUB_ADMIN)
            if (isSuperAdmin) {
                // Personal Admin & Sub-Admin Gate PINs
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personal Admin & Sub-Admin PINs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Assign individual mobile Gate PINs (4–32 chars) per staff member.",
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
                            is AdminDataState.Error -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = cfg.message, fontSize = 12.sp, color = BrandRed)
                                    OutlinedButton(onClick = { viewModel.fetchGateConfig() }) {
                                        Text("Retry", color = BrandRed)
                                    }
                                }
                            }
                            is AdminDataState.Success -> {
                                val users = cfg.data.users
                                if (users.isEmpty()) {
                                    Text(
                                        text = "No Admin / Sub-Admin accounts found.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        users.forEach { user ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF9FAFB),
                                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = user.username?.takeIf { it.isNotBlank() }
                                                                    ?: user.email
                                                                    ?: user.userId.take(8),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Color(0xFF111827)
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(50),
                                                                color = if (user.role.equals("ADMIN", true)) {
                                                                    Color(0xFFFEE2E2)
                                                                } else {
                                                                    Color(0xFFDBEAFE)
                                                                }
                                                            ) {
                                                                Text(
                                                                    text = user.role ?: "STAFF",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (user.role.equals("ADMIN", true)) {
                                                                        Color(0xFFB91C1C)
                                                                    } else {
                                                                        Color(0xFF1D4ED8)
                                                                    },
                                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                        if (!user.email.isNullOrBlank()) {
                                                            Text(
                                                                text = user.email,
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF6B7280)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = if (user.personalPinConfigured) {
                                                                "Personal PIN: Configured"
                                                            } else {
                                                                "Personal PIN: Not set"
                                                            },
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (user.personalPinConfigured) {
                                                                Color(0xFF047857)
                                                            } else {
                                                                Color(0xFFB45309)
                                                            }
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    OutlinedButton(
                                                        onClick = { editingUserPin = user },
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, BrandRed),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = if (user.personalPinConfigured) "Change PIN" else "Set PIN",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = BrandRed
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }

                // Fallback Tier Gate PINs
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
                                text = "Fallback Tier Gate PINs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Shared fallback PINs per role tier (used when a user has no personal PIN).",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val cfg = gateConfigState
                        if (cfg is AdminDataState.Success) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GateStatusChip("Admin Fallback", cfg.data.adminPinConfigured)
                                GateStatusChip("Sub-Admin Fallback", cfg.data.subAdminPinConfigured)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Select Fallback Role Tier",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ADMIN" to "ADMIN Tier", "SUB_ADMIN" to "SUB_ADMIN Tier").forEach { (roleKey, label) ->
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

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 32) newPin = it },
                            label = { Text("New Fallback PIN (4–32 chars) *") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { if (it.length <= 32) confirmPin = it },
                            label = { Text("Confirm New PIN *") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                                val trimmedPin = newPin.trim()
                                if (trimmedPin.length !in 4..32) {
                                    Toast.makeText(context, "PIN must be 4–32 characters", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (trimmedPin != confirmPin.trim()) {
                                    Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (currentAdminPassword.isBlank()) {
                                    Toast.makeText(context, "Enter your current password", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isUpdatingFallback = true
                                viewModel.updateGatePin(
                                    targetRole = selectedTargetRole,
                                    newPin = trimmedPin,
                                    currentPassword = currentAdminPassword,
                                    onSuccess = {
                                        isUpdatingFallback = false
                                        newPin = ""
                                        confirmPin = ""
                                        currentAdminPassword = ""
                                        Toast.makeText(context, "Fallback Gate PIN updated", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isUpdatingFallback = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isUpdatingFallback,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                        ) {
                            Text(
                                text = if (isUpdatingFallback) "Updating..." else "Update Fallback PIN",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    editingUserPin?.let { user ->
        UpdateUserPersonalPinDialog(
            user = user,
            onDismiss = { editingUserPin = null },
            onSubmit = { pinValue, adminPass, onDone, onErr ->
                viewModel.updateUserGatePin(
                    userId = user.userId,
                    newPin = pinValue,
                    currentPassword = adminPass,
                    onSuccess = {
                        onDone()
                        editingUserPin = null
                        Toast.makeText(context, "Personal Gate PIN updated", Toast.LENGTH_SHORT).show()
                    },
                    onError = onErr
                )
            }
        )
    }
}

@Composable
private fun UpdateUserPersonalPinDialog(
    user: GateUserPinStatus,
    onDismiss: () -> Unit,
    onSubmit: (newPin: String, currentPassword: String, onDone: () -> Unit, onErr: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var personalPin by remember { mutableStateOf("") }
    var confirmPersonalPin by remember { mutableStateOf("") }
    var superAdminPassword by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        focusedLabelColor = BrandRed,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color.White,
        title = {
            Column {
                Text("Personal Gate PIN", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = user.username ?: user.email ?: user.userId,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = personalPin,
                    onValueChange = { if (it.length <= 32) personalPin = it },
                    label = { Text("New Personal PIN (4–32 chars) *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = confirmPersonalPin,
                    onValueChange = { if (it.length <= 32) confirmPersonalPin = it },
                    label = { Text("Confirm Personal PIN *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = superAdminPassword,
                    onValueChange = { superAdminPassword = it },
                    label = { Text("Super Admin Password *") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = personalPin.trim()
                    if (trimmed.length !in 4..32) {
                        Toast.makeText(context, "PIN must be 4–32 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (trimmed != confirmPersonalPin.trim()) {
                        Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (superAdminPassword.isBlank()) {
                        Toast.makeText(context, "Enter your Super Admin password", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    onSubmit(
                        trimmed,
                        superAdminPassword,
                        { isSaving = false },
                        { err ->
                            isSaving = false
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                Text(if (isSaving) "Saving..." else "Save PIN", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
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
