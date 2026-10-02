package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Member
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun AddEditMemberDialog(
    initialMember: Member? = null,
    language: String,
    onSave: (name: String, phone: String, email: String, password: String, notes: String, isActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isFr = language == "FR"
    val isEdit = initialMember != null

    var name by remember { mutableStateOf(initialMember?.fullName ?: "") }
    var phone by remember { mutableStateOf(initialMember?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(initialMember?.email ?: "") }
    var password by remember { mutableStateOf(if (isEdit) "" else "1234") }
    var notes by remember { mutableStateOf(initialMember?.notes ?: "") }
    var isActive by remember { mutableStateOf(initialMember?.isActive ?: true) }

    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) {
                                if (isFr) "Modifier le membre" else "Edit Member"
                            } else {
                                if (isFr) "Ajouter un membre" else "Add New Member"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFr) "Informations de contact et compte" else "Contact info & account details",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_name"),
                    label = { Text(if (isFr) "Nom complet *" else "Full Name *") },
                    placeholder = { Text("e.g. Pauline Ngono") },
                    isError = isError && name.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone number (main contact)
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_phone"),
                    label = { Text(if (isFr) "Numéro de téléphone (Contact principal) *" else "Phone Number (Main Contact) *") },
                    placeholder = { Text("e.g. 677123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = isError && phone.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Email (optional)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isFr) "Adresse e-mail (Optionnel)" else "Email Address (Optional)") },
                    placeholder = { Text("e.g. member@gmail.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password (starting password or reset password)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            if (isEdit) {
                                if (isFr) "Réinitialiser le mot de passe (Laisser vide pour ne pas changer)" else "Reset Password (Leave blank to keep unchanged)"
                            } else {
                                if (isFr) "Mot de passe initial *" else "Starting Password *"
                            }
                        )
                    },
                    placeholder = { Text("e.g. 1234") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isFr) "Remarques (Optionnel)" else "Notes (Optional)") },
                    placeholder = { Text("e.g. Ancienne chorale, paroisse...") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                if (isEdit) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFr) "Membre actif" else "Active Member",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isFr) "Annuler" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                isError = true
                            } else {
                                onSave(name, phone, email, password, notes, isActive)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_save_member")
                    ) {
                        Text(
                            if (isEdit) {
                                if (isFr) "Enregistrer" else "Save Changes"
                            } else {
                                if (isFr) "Ajouter le membre" else "Add Member"
                            }
                        )
                    }
                }
            }
        }
    }
}
