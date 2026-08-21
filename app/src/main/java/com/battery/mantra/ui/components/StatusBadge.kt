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
        "UNASSIGNED", "PENDING" -> Pair(Color(0xFFFDE8E8), Color(0xFFC81E1E))
        "CONFIRMED" -> Pair(StatusConfirmed.copy(alpha = 0.15f), StatusConfirmed)
        "DISPATCHED" -> Pair(Color(0xFFE1E8F0), Color(0xFF1E293B))
        "COMPLETED" -> Pair(StatusCompleted.copy(alpha = 0.15f), StatusCompleted)
        "CANCELLED", "FAILED" -> Pair(StatusCancelled.copy(alpha = 0.15f), StatusCancelled)
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
