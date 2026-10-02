package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Expense
import com.example.ui.theme.MutedRedWarning
import com.example.ui.util.ChoirStrings
import java.util.Calendar

@Composable
fun AddEditExpenseDialog(
    initialExpense: Expense? = null,
    language: String,
    onSave: (category: String, description: String, date: Long, amount: Long, paymentMethod: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isFr = language == "FR"
    val isEdit = initialExpense != null

    val categories = listOf(
        "SHEET_MUSIC",
        "SOUND_EQUIPMENT",
        "REFRESHMENTS",
        "UNIFORMS",
        "TRANSPORT",
        "HONORARIUM",
        "WELFARE",
        "OTHER"
    )
    var selectedCategory by remember {
        mutableStateOf(initialExpense?.category ?: categories[0])
    }

    var description by remember { mutableStateOf(initialExpense?.description ?: "") }
    var amountText by remember { mutableStateOf(initialExpense?.amount?.toString() ?: "5000") }

    val paymentMethods = listOf("CASH", "MTN_MOMO", "ORANGE_MONEY")
    var selectedPaymentMethod by remember {
        mutableStateOf(initialExpense?.paymentMethod ?: "CASH")
    }

    var dateTimestamp by remember { mutableLongStateOf(initialExpense?.date ?: System.currentTimeMillis()) }

    val calendar = Calendar.getInstance().apply { timeInMillis = dateTimestamp }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val newCal = Calendar.getInstance()
            newCal.set(y, m, d)
            dateTimestamp = newCal.timeInMillis
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
                                if (isFr) "Modifier la dépense" else "Edit Expense"
                            } else {
                                if (isFr) "Enregistrer une dépense" else "Log Choir Expense"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFr) "Sortie de caisse de la chorale" else "Choir fund disbursement",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_description"),
                    label = { Text(if (isFr) "Description de la dépense *" else "Expense Description *") },
                    placeholder = { Text(if (isFr) "e.g. Partitions, rafraîchissements..." else "e.g. Sheet music, water...") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount in FCFA
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount"),
                    label = { Text(if (isFr) "Montant (FCFA) *" else "Amount (FCFA) *") },
                    placeholder = { Text("5000") },
                    suffix = { Text("FCFA", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = if (isFr) "Catégorie" else "Category",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(ChoirStrings.getExpenseCategoryLabel(cat, language), fontSize = 12.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Card
                Text(
                    text = if (isFr) "Date" else "Date",
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
                                tint = MutedRedWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ChoirStrings.formatDate(dateTimestamp, language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = if (isFr) "Modifier" else "Change",
                            color = MutedRedWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method
                Text(
                    text = if (isFr) "Moyen de règlement" else "Payment Method",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method },
                            label = { Text(ChoirStrings.getPaymentMethodLabel(method, language), fontSize = 12.sp) },
                            shape = RoundedCornerShape(6.dp)
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
                            val parsedAmount = amountText.toLongOrNull() ?: 0L
                            if (description.isBlank() || parsedAmount <= 0) {
                                isError = true
                            } else {
                                onSave(selectedCategory, description, dateTimestamp, parsedAmount, selectedPaymentMethod)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MutedRedWarning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_save_expense")
                    ) {
                        Text(
                            if (isEdit) {
                                if (isFr) "Enregistrer" else "Save Changes"
                            } else {
                                if (isFr) "Enregistrer la dépense" else "Log Expense"
                            }
                        )
                    }
                }
            }
        }
    }
}
