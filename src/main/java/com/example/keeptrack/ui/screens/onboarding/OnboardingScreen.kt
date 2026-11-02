package com.example.keeptrack.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.keeptrack.ui.components.KeepTrackButton

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Everything in one place",
        description = "Store your receipts, warranties, rental agreements, insurance and important documents in one organized app.",
        icon = Icons.Default.Description
    ),
    OnboardingPage(
        title = "Never miss an expiry",
        description = "KeepTrack tracks warranties, agreements, insurance and service dates and reminds you before they expire.",
        icon = Icons.Default.Notifications
    ),
    OnboardingPage(
        title = "Find it when you need it",
        description = "Search and access your products and documents without going through folders, emails or physical files.",
        icon = Icons.Default.Search
    )
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {

    var currentPage by remember {
        mutableIntStateOf(0)
    }

    val page = onboardingPages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {

            if (currentPage > 0) {

                TextButton(
                    onClick = {
                        currentPage--
                    }
                ) {
                    Text(
                        text = "Back",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(90.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            onboardingPages.forEachIndexed { index, _ ->

                Text(
                    text = if (index == currentPage) {
                        "●"
                    } else {
                        "○"
                    },
                    color = if (index == currentPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        KeepTrackButton(
            text = if (currentPage == onboardingPages.lastIndex) {
                "Get Started"
            } else {
                "Continue"
            },
            onClick = {

                if (currentPage < onboardingPages.lastIndex) {

                    currentPage++

                } else {

                    onGetStarted()
                }
            }
        )
    }
}