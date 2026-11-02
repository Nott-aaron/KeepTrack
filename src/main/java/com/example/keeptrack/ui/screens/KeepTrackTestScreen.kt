package com.example.keeptrack.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.keeptrack.viewmodel.RecordViewModel

@Composable
fun KeepTrackTestScreen(
    recordViewModel: RecordViewModel
) {
    Text(
        text = "KeepTrack Local Database Connected"
    )
}