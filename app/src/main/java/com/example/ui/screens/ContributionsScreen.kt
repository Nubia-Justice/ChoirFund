package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contribution
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MtnYellow
import com.example.ui.theme.MutedRedWarning
import com.example.ui.theme.OrangeBrand
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.ChoirUiState

@Composable
fun ContributionsScreen(
    state: ChoirUiState,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onAddContribution: () -> Unit,
    onEditContribution: (Contribution) -> Unit,
    onDeleteContribution: (Contribution) -> Unit,
    onOpenWhatsAppReceipt: (Contribution) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFr = state.language == "FR"
    val types = listOf("ALL", "DUES", "VOLUNTARY_GIFT", "DONATION")

    val filteredList = state.contributions.filter { c ->
        val matchesType = state.contributionTypeFilter == "ALL" || c.type == state.contributionTypeFilter
        val query = state.searchQuery.trim().lowercase()
        val matchesQuery = query.isEmpty() ||
                c.donorName.lowercase().contains(query) ||
                c.notes.lowercase().contains(query) ||
                c.campaignName.lowercase().contains(query)

        matchesType && matchesQuery
    }

    val filteredTotal = filteredList.sumOf { it.amount }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContribution,
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("fab_add_contribution")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contribution")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_contributions"),
                    placeholder = { Text(if (isFr) "Rechercher par nom, campagne, note..." else "Search by name, campaign, note...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Type Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { t ->
                        val label = if (t == "ALL") (if (isFr) "Tout" else "All")
                        else ChoirStrings.getContributionTypeLabel(t, state.language)
                        FilterChip(
                            selected = state.contributionTypeFilter == t,
                            onClick = { onTypeFilterChange(t) },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} ${if (isFr) "versements enregistrés" else "records"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${ChoirStrings.formatFcfa(filteredTotal)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFr) "Aucune contribution trouvée" else "No contributions found",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { contribution ->
                        ContributionRowCard(
                            contribution = contribution,
                            language = state.language,
                            onEdit = { onEditContribution(contribution) },
                            onDelete = { onDeleteContribution(contribution) },
                            onOpenWhatsAppReceipt = { onOpenWhatsAppReceipt(contribution) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionRowCard(
    contribution: Contribution,
    language: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenWhatsAppReceipt: () -> Unit
) {
    val isFr = language == "FR"
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contribution.donorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (contribution.isOutsideDonor) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isFr) "Donateur ext." else "Outside donor",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${ChoirStrings.getContributionTypeLabel(contribution.type, language)} • ${ChoirStrings.getPaymentMethodLabel(contribution.paymentMethod, language)} • ${ChoirStrings.formatDate(contribution.date, language)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (contribution.campaignName.isNotBlank()) {
                        Text(
                            text = "${if (isFr) "Campagne :" else "Campaign:"} ${contribution.campaignName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForestGreenPrimary
                        )
                    }

                    if (contribution.notes.isNotBlank()) {
                        Text(
                            text = "“${contribution.notes}”",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = ChoirStrings.formatFcfa(contribution.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ForestGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // WhatsApp receipt action
                    IconButton(onClick = onOpenWhatsAppReceipt, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "WhatsApp",
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isFr) "Reçu WhatsApp" else "WhatsApp Receipt") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenWhatsAppReceipt()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isFr) "Modifier" else "Edit") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isFr) "Supprimer" else "Delete", color = MutedRedWarning) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MutedRedWarning) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
