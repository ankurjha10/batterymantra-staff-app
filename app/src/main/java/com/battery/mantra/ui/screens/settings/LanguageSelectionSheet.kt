package com.battery.mantra.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = (context.applicationContext as com.battery.mantra.BatteryMantraApp).container.tokenManager
    var selectedLanguage by remember { mutableStateOf(tokenManager.getLanguage()) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 8.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.select_language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LanguageOption(
                title = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.english),
                subtitle = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.english_subtitle),
                isSelected = selectedLanguage == "en",
                onClick = { selectedLanguage = "en" }
            )
            
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            LanguageOption(
                title = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.hindi),
                subtitle = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.hindi_subtitle),
                isSelected = selectedLanguage == "hi",
                onClick = { selectedLanguage = "hi" }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.kotlinx.coroutines.launch {
                        tokenManager.saveLanguage(selectedLanguage)
                        
                        // Change Language
                        val locale = java.util.Locale(selectedLanguage)
                        java.util.Locale.setDefault(locale)
                        val config = context.resources.configuration
                        config.setLocale(locale)
                        context.resources.updateConfiguration(config, context.resources.displayMetrics)
                        
                        android.widget.Toast.makeText(context, context.getString(com.battery.mantra.R.string.language_saved), android.widget.Toast.LENGTH_LONG).show()
                        onDismiss()
                        
                        // Recreate Activity to apply changes
                        (context as? android.app.Activity)?.recreate()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.apply_language), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LanguageOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFD32F2F),
                unselectedColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
        }
    }
}
