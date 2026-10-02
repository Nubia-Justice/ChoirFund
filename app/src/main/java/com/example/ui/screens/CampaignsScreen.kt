package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.Campaign
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedRedWarning
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.CampaignWithStats
import com.example.ui.viewmodel.ChoirUiState
import com.example.ui.viewmodel.UserRole

@Composable
fun CampaignsScreen(
    state: ChoirUiState,
    onAddCampaign: () -> Unit,
    onEditCampaign: (Campaign) -> Unit,
    onDeleteCampaign: (Campaign) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFr = state.language == "FR"
    val isTreasurer = state.session.role == UserRole.TREASURER

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (isTreasurer) {
                FloatingActionButton(
                    onClick = onAddCampaign,
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("fab_add_campaign")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Campaign")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (isFr) "Campagnes & Projets budgétaires" else "Target Budgets & Campaigns",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFr) "Objectifs de collecte pour les besoins de la chorale" else "Financial targets for choir instruments and projects",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.campaigns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFr) "Aucune campagne active" else "No active campaigns",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.campaigns, key = { it.campaign.id }) { item ->
                        CampaignCard(
                            item = item,
                            isTreasurer = isTreasurer,
                            language = state.language,
                            onEdit = { onEditCampaign(item.campaign) },
                            onDelete = { onDeleteCampaign(item.campaign) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignCard(
    item: CampaignWithStats,
    isTreasurer: Boolean,
    language: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isFr = language == "FR"
    val camp = item.campaign
    var menuExpanded by remember { mutableStateOf(false) }
    var contributorsExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = camp.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (camp.description.isNotBlank()) {
                        Text(
                            text = camp.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isTreasurer) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
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

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { item.percentageReached / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ForestGreenPrimary,
                trackColor = ForestGreenPrimary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row: amount raised, percentage reached, amount remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isFr) "Collecté" else "Raised",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ChoirStrings.formatFcfa(item.amountRaised),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFr) "Objectif" else "Target",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ChoirStrings.formatFcfa(camp.targetAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isFr) "Restant" else "Remaining",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ChoirStrings.formatFcfa(item.amountRemaining),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.amountRemaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else ForestGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.percentageReached}% ${if (isFr) "atteint" else "reached"} • ${if (isFr) "Échéance :" else "Deadline:"} ${ChoirStrings.formatDate(camp.deadline, language)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Contributor list toggle: Treasurer can see individual entries, members see totals only
                if (isTreasurer && item.contributions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clickable { contributorsExpanded = !contributorsExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.contributions.size} ${if (isFr) "dons" else "entries"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Icon(
                            imageVector = if (contributorsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded contributor list for Treasurer only
            if (isTreasurer && contributorsExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isFr) "Détail des versements pour cette campagne :" else "Contributions for this campaign:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    item.contributions.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${c.donorName} (${ChoirStrings.formatDate(c.date, language)})", fontSize = 11.sp)
                            Text(text = ChoirStrings.formatFcfa(c.amount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }
                    }
                }
            }
        }
    }
}
