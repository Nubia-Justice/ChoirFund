package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedRedWarning
import com.example.ui.theme.SoftGreenBadgeBg
import com.example.ui.theme.SoftRedBadgeBg
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.ChoirUiState
import com.example.ui.viewmodel.MemberDuesStatus

@Composable
fun MembersScreen(
    state: ChoirUiState,
    onSearchChange: (String) -> Unit,
    onAddMember: () -> Unit,
    onEditMember: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onAddContributionForMember: (Long) -> Unit,
    onOpenSaturdayReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFr = state.language == "FR"

    val filteredList = state.memberDuesList.filter { item ->
        val query = state.searchQuery.trim().lowercase()
        query.isEmpty() ||
                item.member.fullName.lowercase().contains(query) ||
                item.member.phoneNumber.contains(query) ||
                item.member.email.lowercase().contains(query)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMember,
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("fab_add_member")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
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
                        .testTag("input_search_members"),
                    placeholder = { Text(if (isFr) "Rechercher par nom, téléphone, e-mail..." else "Search by name, phone, email...") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} ${if (isFr) "membres" else "members"} (${state.activeMemberCount} ${if (isFr) "actifs" else "active"})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isFr) "Cotisation : Samedi - Vendredi" else "Dues cycle: Sat - Fri",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ForestGreenPrimary
                    )
                }

                if (state.unpaidMembersCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSaturdayReminders() }
                            .testTag("banner_members_unpaid_remind"),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MutedRedWarning.copy(alpha = 0.08f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MutedRedWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFr)
                                        "${state.unpaidMembersCount} membre(s) sans cotisation ce samedi"
                                    else
                                        "${state.unpaidMembersCount} member(s) unpaid this Saturday",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isFr) "Rappeler ▶" else "Remind ▶",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                    }
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
                        text = if (isFr) "Aucun membre trouvé" else "No members found",
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
                    items(filteredList, key = { it.member.id }) { item ->
                        MemberRowCard(
                            item = item,
                            language = state.language,
                            onEdit = { onEditMember(item.member) },
                            onDelete = { onDeleteMember(item.member) },
                            onAddContribution = { onAddContributionForMember(item.member.id) },
                            onRemind = { onOpenSaturdayReminders() },
                            onCall = {
                                if (item.member.phoneNumber.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.member.phoneNumber}"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRowCard(
    item: MemberDuesStatus,
    language: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddContribution: () -> Unit,
    onRemind: (() -> Unit)? = null,
    onCall: () -> Unit
) {
    val isFr = language == "FR"
    val member = item.member
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("member_card_${member.id}"),
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
                // Name & phone
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.fullName.take(2).uppercase(),
                            color = ForestGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!member.isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isFr) "Inactif" else "Inactive",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = member.phoneNumber + if (member.email.isNotBlank()) " • ${member.email}" else "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dues Status badge (Paid or Outstanding)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (item.duesThisWeekPaid) SoftGreenBadgeBg else SoftRedBadgeBg)
                        .clickable(enabled = !item.duesThisWeekPaid && onRemind != null) {
                            onRemind?.invoke()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.duesThisWeekPaid) {
                            if (isFr) "Payé" else "Paid"
                        } else {
                            if (isFr) "En retard" else "Outstanding"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.duesThisWeekPaid) ForestGreenPrimary else MutedRedWarning
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
                            text = { Text(if (isFr) "Cotiser pour ce membre" else "Record Contribution") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAddContribution()
                            }
                        )
                        if (!item.duesThisWeekPaid && member.isActive && onRemind != null) {
                            DropdownMenuItem(
                                text = { Text(if (isFr) "Envoyer rappel cotisation" else "Send Dues Reminder") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = ForestGreenPrimary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRemind.invoke()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (isFr) "Modifier / Réinitialiser mot de passe" else "Edit / Reset Password") },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with Total Contributed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isFr) "Total versé : " else "Total Contributed: ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ChoirStrings.formatFcfa(item.totalContributed),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (member.phoneNumber.isNotBlank()) {
                        Text(
                            text = if (isFr) "Appeler" else "Call",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            modifier = Modifier
                                .clickable { onCall() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
