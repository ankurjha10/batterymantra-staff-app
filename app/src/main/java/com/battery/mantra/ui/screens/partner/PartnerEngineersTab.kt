package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.ui.theme.TextGray
import com.battery.mantra.ui.theme.BrandRed

@Composable
fun PartnerEngineersTab() {
    val engineers = listOf(
        mapOf("name" to "Vikram Singh", "phone" to "+91 9876543210", "status" to "ON_DUTY"),
        mapOf("name" to "Suresh Kumar", "phone" to "+91 8765432109", "status" to "OFF_DUTY")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { /* Show AddEngineerDialog */ },
            modifier = Modifier.padding(16.dp).align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
        ) {
            Text("+ Add Engineer")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(engineers) { engineer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = engineer["name"] ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = engineer["phone"] ?: "", color = TextGray, fontSize = 14.sp)
                        }
                        
                        val isOnline = engineer["status"] == "ON_DUTY"
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            color = if (isOnline) Color(0xFF388E3C) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
