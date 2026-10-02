package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Campaign
import com.example.data.model.Contribution
import com.example.data.model.Member
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.util.ChoirStrings
import java.util.Calendar

@Composable
fun AddEditContributionDialog(
    members: List<Member>,
    campaigns: List<Campaign>,
    initialContribution: Contribution? = null,
    preselectedMemberId: Long? = null,
    language: String,
    onSave: (
        memberId: Long?,
        outsideDonorName: String,
        campaignId: Long?,
        type: String,
        amount: Long,
        date: Long,
        paymentMethod: String,
        notes: String
    ) -> Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isFr = language == "FR"
    val isEdit = initialContribution != null

    // Contributor source: Member or Outside Donor
    var isMemberSource by remember {
        mutableStateOf(initialContribution == null || !initialContribution.isOutsideDonor)
    }

    val defaultSelectedMember = if (initialContribution != null && initialContribution.memberId != null) {
        members.find { it.id == initialContribution.memberId }
    } else if (preselectedMemberId != null) {
        members.find { it.id == preselectedMemberId }
    } else {
        members.firstOrNull { it.isActive } ?: members.firstOrNull()
    }

    var selectedMember by remember { mutableStateOf(defaultSelectedMember) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    var outsideDonorName by remember {
        mutableStateOf(if (initialContribution?.isOutsideDonor == true) initialContribution.donorName else "")
    }

    // Types: DUES, VOLUNTARY_GIFT, DONATION
    val types = listOf("DUES", "VOLUNTARY_GIFT", "DONATION")
    var selectedType by remember {
        mutableStateOf(initialContribution?.type ?: "DUES")
    }

    // Amount: starts at 200 for DUES
    var amountText by remember {
        mutableStateOf(
            if (initialContribution != null) {
                initialContribution.amount.toString()
            } else if (selectedType == "DUES") {
                "200"
            } else {
                "1000"
            }
        )
    }

    // Payment methods: CASH, MTN_MOMO, ORANGE_MONEY
    val paymentMethods = listOf("CASH", "MTN_MOMO", "ORANGE_MONEY")
    var selectedPaymentMethod by remember {
        mutableStateOf(initialContribution?.paymentMethod ?: "CASH")
    }

    // Optional Campaign Link
    var selectedCampaignId by remember {
        mutableStateOf<Long?>(initialContribution?.campaignId)
    }
    var campaignDropdownExpanded by remember { mutableStateOf(false) }

    var notes by remember { mutableStateOf(initialContribution?.notes ?: "") }
    var dateTimestamp by remember { mutableLongStateOf(initialContribution?.date ?: System.currentTimeMillis()) }

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

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f),
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
                                if (isFr) "Modifier la contribution" else "Edit Contribution"
                            } else {
                                if (isFr) "Enregistrer une contribution" else "Record Contribution"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFr) "Cotisation, don ou libéralité" else "Dues, voluntary gift or donation",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contribution Type selection (Dues, Voluntary gift, Donation)
                Text(
                    text = if (isFr) "Type de versement" else "Contribution Type",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = {
                                selectedType = t
                                if (t == "DUES" && (amountText.toLongOrNull() ?: 0L) < 200L) {
                                    amountText = "200"
                                }
                                errorMessage = null
                            },
                            label = { Text(ChoirStrings.getContributionTypeLabel(t, language), fontSize = 12.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contributor Source: Choir Member vs Outside Donor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isMemberSource,
                        onClick = { isMemberSource = true },
                        label = { Text(if (isFr) "Membre de la chorale" else "Choir Member", fontSize = 12.sp) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isMemberSource,
                        onClick = { isMemberSource = false },
                        label = { Text(if (isFr) "Donateur extérieur" else "Outside Donor", fontSize = 12.sp) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isMemberSource) {
                    // Member Dropdown Picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { memberDropdownExpanded = true },
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
                                    Text(
                                        text = selectedMember?.fullName ?: (if (isFr) "Choisir un membre..." else "Select member..."),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = memberDropdownExpanded,
                            onDismissRequest = { memberDropdownExpanded = false }
                        ) {
                            members.filter { it.isActive }.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.fullName, fontSize = 13.sp) },
                                    onClick = {
                                        selectedMember = member
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Outside donor name input
                    OutlinedTextField(
                        value = outsideDonorName,
                        onValueChange = { outsideDonorName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isFr) "Nom du donateur extérieur *" else "Outside Donor Name *") },
                        placeholder = { Text("e.g. Papa Tabi Joseph") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Campaign link
                if (campaigns.isNotEmpty()) {
                    Text(
                        text = if (isFr) "Lier à une campagne / projet (Optionnel)" else "Link to Campaign / Project (Optional)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val activeCampaign = campaigns.find { it.id == selectedCampaignId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { campaignDropdownExpanded = true },
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
                                Text(
                                    text = activeCampaign?.name ?: (if (isFr) "Aucune campagne liée" else "None (General choir fund)"),
                                    fontSize = 13.sp,
                                    color = if (activeCampaign != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = campaignDropdownExpanded,
                            onDismissRequest = { campaignDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isFr) "Aucune (Fonds général de la chorale)" else "None (General choir fund)") },
                                onClick = {
                                    selectedCampaignId = null
                                    campaignDropdownExpanded = false
                                }
                            )
                            campaigns.forEach { camp ->
                                DropdownMenuItem(
                                    text = { Text("${camp.name} (Obj: ${ChoirStrings.formatFcfa(camp.targetAmount)})") },
                                    onClick = {
                                        selectedCampaignId = camp.id
                                        campaignDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Amount in FCFA
                Text(
                    text = if (isFr) "Montant en FCFA" else "Amount in FCFA",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedType == "DUES") {
                    Text(
                        text = if (isFr) "Cotisation hebdomadaire : minimum 200 FCFA" else "Weekly dues: minimum 200 FCFA",
                        fontSize = 11.sp,
                        color = ForestGreenPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contribution_amount"),
                    placeholder = { Text(if (selectedType == "DUES") "200" else "1000") },
                    suffix = { Text("FCFA", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Quick preset chips
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = if (selectedType == "DUES") listOf(200L, 500L, 1000L, 2000L, 5000L)
                    else listOf(1000L, 2500L, 5000L, 10000L, 25000L, 50000L)
                    presets.forEach { preset ->
                        SuggestionChip(
                            onClick = { amountText = preset.toString() },
                            label = { Text("${preset} FCFA", fontSize = 11.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Card
                Text(
                    text = if (isFr) "Date du versement" else "Payment Date",
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
                                text = ChoirStrings.formatDate(dateTimestamp, language),
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

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method: Cash, MTN MoMo, Orange Money
                Text(
                    text = if (isFr) "Mode de paiement" else "Payment Method",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isFr) "Note / Détails (Optionnel)" else "Note (Optional)") },
                    placeholder = { Text(if (isFr) "e.g. Cotisation samedi répétition" else "e.g. Saturday dues") },
                    singleLine = true,
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
                            val parsedAmount = amountText.toLongOrNull() ?: 0L
                            if (selectedType == "DUES" && parsedAmount < 200L) {
                                errorMessage = if (isFr) {
                                    "La cotisation hebdomadaire doit être d'au moins 200 FCFA"
                                } else {
                                    "Weekly dues must be at least 200 FCFA"
                                }
                                return@Button
                            }
                            if (parsedAmount <= 0) {
                                errorMessage = if (isFr) "Veuillez entrer un montant valide" else "Please enter a valid amount"
                                return@Button
                            }

                            val memberId = if (isMemberSource) selectedMember?.id else null
                            val donor = if (!isMemberSource) outsideDonorName.trim() else ""

                            if (isMemberSource && selectedMember == null) {
                                errorMessage = if (isFr) "Veuillez choisir un membre" else "Please select a member"
                                return@Button
                            }
                            if (!isMemberSource && outsideDonorName.isBlank()) {
                                errorMessage = if (isFr) "Veuillez indiquer le nom du donateur" else "Please enter donor name"
                                return@Button
                            }

                            val success = onSave(
                                memberId,
                                donor,
                                selectedCampaignId,
                                selectedType,
                                parsedAmount,
                                dateTimestamp,
                                selectedPaymentMethod,
                                notes
                            )
                            if (!success) {
                                errorMessage = if (isFr) "Erreur de validation" else "Validation error"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_save_contribution")
                    ) {
                        Text(
                            if (isEdit) {
                                if (isFr) "Mettre à jour" else "Update"
                            } else {
                                if (isFr) "Enregistrer le versement" else "Save Contribution"
                            }
                        )
                    }
                }
            }
        }
    }
}
