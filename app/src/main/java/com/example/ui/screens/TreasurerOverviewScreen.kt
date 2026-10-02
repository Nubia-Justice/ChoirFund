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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contribution
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MtnYellow
import com.example.ui.theme.MutedRedWarning
import com.example.ui.theme.OrangeBrand
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.ChoirNavTab
import com.example.ui.viewmodel.ChoirUiState

@Composable
fun TreasurerOverviewScreen(
    state: ChoirUiState,
    onTabSelect: (ChoirNavTab) -> Unit,
    onAddContribution: () -> Unit,
    onAddMember: () -> Unit,
    onAddExpense: () -> Unit,
    onAddCampaign: () -> Unit,
    onOpenWhatsAppReceipt: (Contribution) -> Unit,
    onOpenSaturdayReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFr = state.language == "FR"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top 4 Metrics Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Main Balance in Hand
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreenPrimary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isFr) "SOLDE EN CAISSE" else "BALANCE IN HAND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ChoirStrings.formatFcfa(state.balanceInHand),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${state.activeMemberCount} ${if (isFr) "membres actifs inscrits" else "active choir members"}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Saturday Dues Collection & Reminders Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSaturdayReminders() }
                        .testTag("card_saturday_reminders"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isTodaySaturday)
                            ForestGreenLight.copy(alpha = 0.35f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isTodaySaturday) ForestGreenPrimary else MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (state.isTodaySaturday) {
                                            if (isFr) "Samedi : Collecte des cotisations" else "Saturday : Dues Collection Day"
                                        } else {
                                            if (isFr) "Cotisations hebdomadaires" else "Weekly Dues Reminders"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (state.isTodaySaturday) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ForestGreenPrimary)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isFr) "Aujourd'hui" else "Today",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (state.unpaidMembersCount > 0) {
                                        if (isFr)
                                            "${state.unpaidMembersCount} membre(s) non à jour (min. 200 FCFA)"
                                        else
                                            "${state.unpaidMembersCount} member(s) unpaid (min. 200 FCFA)"
                                    } else {
                                        if (isFr)
                                            "Tous les membres sont à jour ! 🎉"
                                        else
                                            "All members have paid! 🎉"
                                    },
                                    fontSize = 11.sp,
                                    color = if (state.unpaidMembersCount > 0) MutedRedWarning else ForestGreenPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = onOpenSaturdayReminders,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier.testTag("btn_send_saturday_reminders")
                        ) {
                            Text(if (isFr) "Rappels" else "Remind", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Funds Collected & Total Expenses row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Funds Collected
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isFr) "Fonds collectés" else "Funds Collected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ChoirStrings.formatFcfa(state.totalContributions),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${state.contributions.size} ${if (isFr) "versements" else "receipts"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Total Expenses
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isFr) "Total dépenses" else "Total Expenses",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedRedWarning
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ChoirStrings.formatFcfa(state.totalExpenses),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${state.expenses.size} ${if (isFr) "dépenses" else "expenses"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick action buttons
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
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onAddContribution, modifier = Modifier.testTag("overview_btn_contrib")) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFr) "Cotiser" else "Contribute", fontSize = 12.sp)
                    }
                    TextButton(onClick = onAddMember, modifier = Modifier.testTag("overview_btn_member")) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFr) "Membre" else "Member", fontSize = 12.sp)
                    }
                    TextButton(onClick = onAddExpense, modifier = Modifier.testTag("overview_btn_expense")) {
                        Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFr) "Dépense" else "Expense", fontSize = 12.sp)
                    }
                }
            }
        }

        // Financial comparison: bars comparing funds collected with total expenses
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Comparaison financière" else "Financial Comparison",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val totalFlow = (state.totalContributions + state.totalExpenses).coerceAtLeast(1L)
                    val inFraction = (state.totalContributions.toDouble() / totalFlow.toDouble()).toFloat()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${if (isFr) "Entrées :" else "Collected:"} ${ChoirStrings.formatFcfa(state.totalContributions)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "${if (isFr) "Dépenses :" else "Expenses:"} ${ChoirStrings.formatFcfa(state.totalExpenses)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedRedWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { inFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ForestGreenPrimary,
                        trackColor = MutedRedWarning.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Breakdown by payment method with bars (Cash, MTN MoMo, Orange Money)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Répartition par moyen de paiement" else "Breakdown by Payment Method",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val methods = listOf("CASH", "MTN_MOMO", "ORANGE_MONEY")
                    val totalIn = state.totalContributions.coerceAtLeast(1L)

                    methods.forEach { method ->
                        val sum = state.contributions.filter { it.paymentMethod == method }.sumOf { it.amount }
                        val fraction = (sum.toDouble() / totalIn.toDouble()).toFloat().coerceIn(0f, 1f)
                        val barColor = when (method) {
                            "MTN_MOMO" -> MtnYellow
                            "ORANGE_MONEY" -> OrangeBrand
                            else -> ForestGreenPrimary
                        }

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ChoirStrings.getPaymentMethodLabel(method, state.language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${ChoirStrings.formatFcfa(sum)} (${(fraction * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Progress of each campaign
        if (state.campaigns.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFr) "Campagnes & Objectifs" else "Campaigns & Budgets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            TextButton(onClick = { onTabSelect(ChoirNavTab.CAMPAIGNS) }) {
                                Text(if (isFr) "Tout voir" else "View All", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        state.campaigns.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = item.campaign.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        text = "${ChoirStrings.formatFcfa(item.amountRaised)} / ${ChoirStrings.formatFcfa(item.campaign.targetAmount)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { item.percentageReached / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = ForestGreenPrimary,
                                    trackColor = ForestGreenPrimary.copy(alpha = 0.15f)
                                )
                                Text(
                                    text = "${item.percentageReached}% ${if (isFr) "atteint" else "reached"} • ${ChoirStrings.formatFcfa(item.amountRemaining)} ${if (isFr) "restant" else "remaining"}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Contributions with WhatsApp receipt action
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFr) "Derniers versements" else "Recent Contributions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { onTabSelect(ChoirNavTab.CONTRIBUTIONS) }) {
                        Text(if (isFr) "Tout voir" else "View All", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val recentList = state.contributions.take(5)
                if (recentList.isEmpty()) {
                    Text(
                        text = if (isFr) "Aucun versement enregistré" else "No contributions recorded yet",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    recentList.forEach { contribution ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contribution.donorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${ChoirStrings.getContributionTypeLabel(contribution.type, state.language)} • ${ChoirStrings.getPaymentMethodLabel(contribution.paymentMethod, state.language)} • ${ChoirStrings.formatDate(contribution.date, state.language)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ChoirStrings.formatFcfa(contribution.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onOpenWhatsAppReceipt(contribution) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "WhatsApp Receipt",
                                            tint = ForestGreenPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
