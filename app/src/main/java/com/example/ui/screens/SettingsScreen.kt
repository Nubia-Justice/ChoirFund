package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedRedWarning
import com.example.ui.viewmodel.ChoirUiState
import com.example.ui.viewmodel.UserRole

@Composable
fun SettingsScreen(
    state: ChoirUiState,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onUpdateChoirIdentity: (choirName: String, parishName: String, treasurerName: String) -> Unit,
    onChangePassword: () -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean) -> Unit) -> Unit,
    onSignOut: () -> Unit,
    onOpenSaturdayReminders: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFr = state.language == "FR"
    val isTreasurer = state.session.role == UserRole.TREASURER

    var choirNameInput by remember(state.settings.choirName) { mutableStateOf(state.settings.choirName) }
    var parishNameInput by remember(state.settings.parishName) { mutableStateOf(state.settings.parishName) }
    var treasurerNameInput by remember(state.settings.treasurerName) { mutableStateOf(state.settings.treasurerName) }

    var importJsonText by remember { mutableStateOf("") }
    var showImportField by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App / User Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.session.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTreasurer) {
                                if (isFr) "Trésorier (Administrateur)" else "Treasurer (Admin)"
                            } else {
                                if (isFr) "Choriste" else "Choir Member"
                            },
                            fontSize = 11.sp,
                            color = ForestGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Everyone: Appearance & Language
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Préférences d'affichage" else "Preferences",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleLanguage() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = if (isFr) "Langue de l'application" else "App Language", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = if (isFr) "Français" else "English", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = if (isFr) "Basculer en English" else "Passer en Français",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 6.dp))

                    // Theme toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTheme() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = if (isFr) "Thème" else "Theme", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = if (state.isDarkTheme) (if (isFr) "Sombre" else "Dark") else (if (isFr) "Clair" else "Light"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = state.isDarkTheme,
                            onCheckedChange = { onToggleTheme() }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 6.dp))

                    // Change Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangePassword() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = if (isFr) "Mon mot de passe" else "My Password", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = if (isFr) "Modifier mon mot de passe de connexion" else "Change my sign-in password", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = if (isFr) "Modifier" else "Change",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }
                }
            }
        }

        // Treasurer: Choir Identity
        if (isTreasurer) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFr) "Identité de la chorale" else "Choir Identity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isFr) "Affiché sur la page de connexion et sur les reçus" else "Shown on the sign-in page and official receipts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = choirNameInput,
                            onValueChange = { choirNameInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isFr) "Nom de la chorale" else "Choir Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = parishNameInput,
                            onValueChange = { parishNameInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isFr) "Paroisse / Église" else "Parish / Church Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = treasurerNameInput,
                            onValueChange = { treasurerNameInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isFr) "Nom du trésorier" else "Treasurer Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onUpdateChoirIdentity(choirNameInput, parishNameInput, treasurerNameInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFr) "Enregistrer l'identité" else "Save Identity")
                        }
                    }
                }
            }

            // Treasurer: Saturday Reminders Configuration & Testing
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isFr) "Rappels automatiques du samedi" else "Automated Saturday Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isFr)
                                "Alerte automatique programmée chaque samedi à 09h00 pour identifier les cotisations non payées (min. 200 FCFA) et envoyer les rappels WhatsApp / SMS."
                            else
                                "Automated push notification scheduled every Saturday at 9:00 AM to check unpaid weekly dues (min. 200 FCFA) and send WhatsApp / SMS reminders.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    com.example.util.SaturdayReminderScheduler.triggerImmediateTest(context)
                                    Toast.makeText(
                                        context,
                                        if (isFr) "Notification test envoyée !" else "Test notification triggered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isFr) "Tester alerte" else "Test Alert", fontSize = 11.sp)
                            }

                            if (onOpenSaturdayReminders != null) {
                                Button(
                                    onClick = onOpenSaturdayReminders,
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isFr) "Ouvrir rappels" else "Open Reminders", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Treasurer: Backup & Restore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFr) "Sauvegarde & Restauration" else "Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isFr) "Exportez ou importez vos données en JSON" else "Export or restore choir records as JSON",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onExportBackup { json ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Choir Funds Backup", json)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(
                                            context,
                                            if (isFr) "Sauvegarde copiée dans le presse-papiers !" else "Backup JSON copied to clipboard!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFr) "Exporter" else "Export", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showImportField = !showImportField },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFr) "Restaurer" else "Restore", fontSize = 12.sp)
                            }
                        }

                        if (showImportField) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = importJsonText,
                                onValueChange = { importJsonText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (isFr) "Coller le JSON de sauvegarde ici" else "Paste backup JSON here") },
                                maxLines = 5,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (importJsonText.isNotBlank()) {
                                        onImportBackup(importJsonText) { success ->
                                            if (success) {
                                                importJsonText = ""
                                                showImportField = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isFr) "Confirmer la restauration" else "Confirm Restore")
                            }
                        }
                    }
                }
            }
        }

        // Sign Out Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignOut() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = MutedRedWarning)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isFr) "Déconnexion" else "Sign Out",
                            fontWeight = FontWeight.Bold,
                            color = MutedRedWarning,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
