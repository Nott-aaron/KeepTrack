package com.example.keeptrack.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keeptrack.ui.components.KeepTrackButton

@Composable
fun ProfileSetupScreen(
    initialName: String = "",
    initialEmail: String = "",
    initialPhone: String = "",
    initialGender: String = "",
    isEditing: Boolean = false,
    onContinue: (
        name: String,
        email: String,
        phone: String,
        gender: String
    ) -> Unit,
    onCancel: () -> Unit = {}
) {

    var name by remember {
        mutableStateOf(initialName)
    }

    var email by remember {
        mutableStateOf(initialEmail)
    }

    var phone by remember {
        mutableStateOf(initialPhone)
    }

    var gender by remember {
        mutableStateOf(initialGender)
    }

    var genderMenuExpanded by remember {
        mutableStateOf(false)
    }

    val genderOptions = listOf(
        "Male",
        "Female",
        "Other",
        "Prefer not to say"
    )

    val isFormValid =
        name.isNotBlank() &&
                email.isNotBlank() &&
                phone.isNotBlank() &&
                gender.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        // =====================================================
        // TITLE
        // =====================================================

        Text(
            text = if (isEditing) {
                "Edit Profile"
            } else {
                "Let's set up your profile"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // =====================================================
        // DESCRIPTION
        // =====================================================

        Text(
            text = if (isEditing) {
                "Update your personal information."
            } else {
                "Your information will be stored only on your device."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // NAME
        // =====================================================

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(
                    text = "Name"
                )
            },
            placeholder = {
                Text(
                    text = "Enter your name"
                )
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // =====================================================
        // EMAIL
        // =====================================================

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            ),
            label = {
                Text(
                    text = "Email"
                )
            },
            placeholder = {
                Text(
                    text = "Enter your email"
                )
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // =====================================================
        // PHONE
        // =====================================================

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            ),
            label = {
                Text(
                    text = "Phone Number"
                )
            },
            placeholder = {
                Text(
                    text = "Enter your phone number"
                )
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // =====================================================
        // GENDER
        // =====================================================

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            TextButton(
                onClick = {
                    genderMenuExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = if (gender.isEmpty()) {
                        "Select Gender"
                    } else {
                        gender
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = genderMenuExpanded,
                onDismissRequest = {
                    genderMenuExpanded = false
                }
            ) {

                genderOptions.forEach { option ->

                    DropdownMenuItem(

                        text = {
                            Text(
                                text = option,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },

                        onClick = {

                            gender = option

                            genderMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // =====================================================
        // SAVE / CONTINUE
        // =====================================================

        KeepTrackButton(
            text = if (isEditing) {
                "Save Changes"
            } else {
                "Continue"
            },

            enabled = isFormValid,

            onClick = {

                onContinue(
                    name.trim(),
                    email.trim(),
                    phone.trim(),
                    gender
                )
            }
        )

        // =====================================================
        // CANCEL
        // =====================================================

        if (isEditing) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}