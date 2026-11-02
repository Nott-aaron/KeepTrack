package com.example.keeptrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keeptrack.ui.theme.KeepTrackBlue
import com.example.keeptrack.ui.theme.KeepTrackLightBlue

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status.lowercase()) {
        "active" -> KeepTrackLightBlue
        "expiring soon" -> KeepTrackLightBlue
        "expired" -> KeepTrackLightBlue
        else -> KeepTrackLightBlue
    }

    Text(
        text = status,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
        color = KeepTrackBlue
    )
}