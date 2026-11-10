package com.battery.mantra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.ui.theme.StatusCancelled
import com.battery.mantra.ui.theme.StatusCompleted
import com.battery.mantra.ui.theme.StatusConfirmed
import com.battery.mantra.ui.theme.StatusDispatched
import com.battery.mantra.ui.theme.StatusPending

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "UNASSIGNED", "PENDING" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "CONFIRMED", "PROCESSING", "ASSIGNED", "ACCEPTED" -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        "SHIPPED", "OUT_FOR_DELIVERY", "DISPATCHED" -> Pair(Color(0xFFF3E8FF), Color(0xFF7E22CE))
        "DELIVERED", "INSTALLED", "COMPLETED" -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
        "CANCELLED", "FAILED", "RETURNED" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        else -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray)
    }

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
