package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Member
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.SoftBeigeBorder
import com.example.ui.viewmodel.ChoirUiState

@Composable
fun AuthScreen(
    state: ChoirUiState,
    onSignInAsTreasurer: (String) -> Boolean,
    onSignInAsMember: (Long, String) -> Boolean,
    onCreateTreasurer: (String, String, String) -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFr = state.language == "FR"

    // If first-time setup needed (e.g. no treasurer created or hasCompletedFirstTimeSetup is false)
    val needsFirstTimeSetup = !state.settings.hasCompletedFirstTimeSetup

    var selectedAccountId by remember { mutableStateOf<Long?>(-1L) } // -1 for Treasurer
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // First time setup fields
    var setupName by remember { mutableStateOf("") }
    var setupPhone by remember { mutableStateOf("") }
    var setupPassword by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Language & Theme Toggle in top-right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onToggleLanguage) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Translate, contentDescription = "Language", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = state.language, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (state.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Theme",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Choir Hymn-book Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ForestGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Hymn Book",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Choir Name & Parish (shows placeholders or saved values)
            Text(
                text = state.settings.choirName,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = state.settings.parishName,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (needsFirstTimeSetup) {
                // FIRST TIME SETUP: Create the treasurer account
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isFr) "Créer le compte du trésorier" else "Create the Treasurer Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFr) "La première personne devient le trésorier administrateur"
                            else "The first person to complete this becomes the treasurer",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = setupName,
                            onValueChange = { setupName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isFr) "Nom du trésorier" else "Treasurer Name") },
                            placeholder = { Text("e.g. Jean-Paul Mbarga") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = setupPhone,
                            onValueChange = { setupPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isFr) "Numéro de téléphone" else "Phone Number") },
                            placeholder = { Text("e.g. 677123456") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = setupPassword,
                            onValueChange = { setupPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isFr) "Mot de passe" else "Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (setupName.isNotBlank() && setupPassword.isNotBlank()) {
                                    onCreateTreasurer(setupName, setupPhone, setupPassword)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isFr) "Créer et continuer" else "Create and Continue")
                        }
                    }
                }
            } else {
                // REGULAR SIGN-IN: Pick name from list + enter password
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isFr) "Connexion" else "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFr) "Sélectionnez votre nom et saisissez votre mot de passe"
                            else "Select your name and enter your password",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Account Picker Dropdown
                        Text(
                            text = if (isFr) "Votre nom" else "Your Name",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val activeMembers = state.activeMembers
                        val selectedLabel = if (selectedAccountId == -1L) {
                            "${state.settings.treasurerName.ifBlank { "Treasurer" }} (${if (isFr) "Trésorier" else "Treasurer"})"
                        } else {
                            val mem = activeMembers.find { it.id == selectedAccountId }
                            mem?.fullName ?: (if (isFr) "Sélectionner un nom" else "Select name")
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { accountDropdownExpanded = true }
                                    .testTag("auth_account_picker"),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = selectedLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = accountDropdownExpanded,
                                onDismissRequest = { accountDropdownExpanded = false }
                            ) {
                                // Treasurer option
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${state.settings.treasurerName.ifBlank { "Treasurer" }} (${if (isFr) "Trésorier / Admin" else "Treasurer / Admin"})",
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        selectedAccountId = -1L
                                        accountDropdownExpanded = false
                                        errorMessage = null
                                    }
                                )

                                // Active members
                                activeMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.fullName, fontSize = 13.sp) },
                                        onClick = {
                                            selectedAccountId = member.id
                                            accountDropdownExpanded = false
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            label = { Text(if (isFr) "Mot de passe" else "Password") },
                            placeholder = { Text("••••") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = errorMessage != null,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val success = if (selectedAccountId == -1L) {
                                    onSignInAsTreasurer(password)
                                } else {
                                    onSignInAsMember(selectedAccountId!!, password)
                                }
                                if (!success) {
                                    errorMessage = if (isFr) "Mot de passe incorrect" else "Incorrect password"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_btn_login"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isFr) "Se connecter" else "Sign In",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
