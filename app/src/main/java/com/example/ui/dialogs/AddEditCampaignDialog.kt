package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Campaign
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.util.ChoirStrings
import java.util.Calendar

@Composable
fun AddEditCampaignDialog(
    initialCampaign: Campaign? = null,
    language: String,
    onSave: (name: String, targetAmount: Long, deadline: Long, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isFr = language == "FR"
    val isEdit = initialCampaign != null

    var name by remember { mutableStateOf(initialCampaign?.name ?: "") }
    var targetAmountText by remember { mutableStateOf(initialCampaign?.targetAmount?.toString() ?: "500000") }
    var description by remember { mutableStateOf(initialCampaign?.description ?: "") }
    var deadlineTimestamp by remember {
        mutableLongStateOf(initialCampaign?.deadline ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = deadlineTimestamp }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val newCal = Calendar.getInstance()
            newCal.set(y, m, d)
            deadlineTimestamp = newCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
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
                                if (isFr) "Modifier la campagne" else "Edit Campaign"
                            } else {
                                if (isFr) "Nouvelle campagne / Projet" else "New Campaign / Target Budget"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFr) "Objectif financier de la chorale" else "Target budget for choir needs",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isFr) "Nom du projet / Campagne *" else "Campaign Name *") },
                    placeholder = { Text("e.g. New Speakers / Nouveaux Haut-parleurs") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Amount in FCFA
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isFr) "Montant cible (FCFA) *" else "Target Amount (FCFA) *") },
                    placeholder = { Text("500000") },
                    suffix = { Text("FCFA", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Deadline
                Text(
                    text = if (isFr) "Date limite" else "Target Deadline",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ChoirStrings.formatDate(deadlineTimestamp, language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = if (isFr) "Modifier" else "Change",
                            color = ForestGreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isFr) "Description / Objectif" else "Description / Objective") },
                    placeholder = { Text(if (isFr) "Détails du projet..." else "Project details...") },
                    shape = RoundedCornerShape(8.dp)
                )

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
                            val parsedAmount = targetAmountText.toLongOrNull() ?: 0L
                            if (name.isBlank() || parsedAmount <= 0) {
                                isError = true
                            } else {
                                onSave(name, parsedAmount, deadlineTimestamp, description)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (isEdit) {
                                if (isFr) "Enregistrer" else "Save Changes"
                            } else {
                                if (isFr) "Créer la campagne" else "Create Campaign"
                            }
                        )
                    }
                }
            }
        }
    }
}
