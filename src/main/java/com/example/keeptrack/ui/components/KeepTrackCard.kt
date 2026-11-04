package com.example.keeptrack.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun KeepTrackCard(
    title: String,
    subtitle: String? = null,
    category: String? = null,
    trailingText: String? = null,

    /*
     * Allows a custom UI element such as:
     *
     * Switch
     * Icon
     * Button
     * Progress indicator
     *
     * to appear on the right side of the card.
     */
    trailingContent: (@Composable () -> Unit)? = null,

    onClick: () -> Unit = {},

    modifier: Modifier = Modifier
) {

    Card(
        onClick = onClick,

        modifier =
            modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            // =====================================================
            // LEFT CONTENT
            // =====================================================

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = title,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )


                if (subtitle != null) {

                    Text(
                        text = subtitle,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                if (category != null) {

                    Text(
                        text = category,

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }


            // =====================================================
            // RIGHT CONTENT
            // =====================================================

            when {

                /*
                 * Custom content gets priority.
                 *
                 * Used by Dark Theme for the Switch.
                 */

                trailingContent != null -> {

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    trailingContent()
                }


                /*
                 * Existing text-based trailing content.
                 *
                 * Keeps all your existing cards working.
                 */

                trailingText != null -> {

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Text(
                        text = trailingText,

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }
        }
    }
}