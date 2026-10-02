package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedRedWarning
import com.example.ui.theme.SoftGreenBadgeBg
import com.example.ui.theme.SoftRedBadgeBg
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.ChoirUiState
import java.util.Calendar

@Composable
fun MemberDashboardScreen(
    state: ChoirUiState,
    modifier: Modifier = Modifier
) {
    val isFr = state.language == "FR"
    val memberId = state.session.memberId

    // Member's contributions
    val myContributions = state.contributions.filter { it.memberId == memberId }
    val totalAllTime = myContributions.sumOf { it.amount }

    // This year total
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val yearStart = cal.timeInMillis
    val totalThisYear = myContributions.filter { it.date >= yearStart }.sumOf { it.amount }

    // Dues this week (Saturday to Friday)
    val duesWeekStart = ChoirStrings.getDuesWeekStart()
    val thisWeekDuesAmount = myContributions
        .filter { it.type == "DUES" && it.date >= duesWeekStart }
        .sumOf { it.amount }
    val isPaidThisWeek = thisWeekDuesAmount >= 200L

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Member Greeting & Dues Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${if (isFr) "Bienvenue," else "Welcome,"} ${state.session.displayName}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isFr) "Tableau de bord personnel du choriste" else "Personal choir member dashboard",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dues status this week (Paid or Outstanding)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPaidThisWeek) SoftGreenBadgeBg else SoftRedBadgeBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isFr) "Cotisation de la semaine" else "Dues This Week",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPaidThisWeek) ForestGreenPrimary else MutedRedWarning
                            )
                            Text(
                                text = if (isPaidThisWeek) {
                                    if (isFr) "Payé (${ChoirStrings.formatFcfa(thisWeekDuesAmount)})" else "Paid (${ChoirStrings.formatFcfa(thisWeekDuesAmount)})"
                                } else {
                                    if (isFr) "En retard (Minimum 200 FCFA)" else "Outstanding (Minimum 200 FCFA)"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaidThisWeek) ForestGreenPrimary else MutedRedWarning
                            )
                        }

                        Icon(
                            imageVector = if (isPaidThisWeek) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isPaidThisWeek) ForestGreenPrimary else MutedRedWarning,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Total Contributed (All time & this year)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // All time
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isFr) "Total versé (Tout temps)" else "Total Contributed (All time)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ChoirStrings.formatFcfa(totalAllTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "${myContributions.size} ${if (isFr) "versements au total" else "contributions"}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // This year
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isFr) "Total versé (Cette année)" else "Total (This year)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ChoirStrings.formatFcfa(totalThisYear),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "${Calendar.getInstance().get(Calendar.YEAR)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // My Own Contribution History
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Mon historique de versements" else "My Contribution History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (myContributions.isEmpty()) {
                        Text(
                            text = if (isFr) "Aucun versement enregistré pour le moment" else "No contributions recorded yet",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        myContributions.forEach { contribution ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${ChoirStrings.getContributionTypeLabel(contribution.type, state.language)} • ${ChoirStrings.formatDate(contribution.date, state.language)}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = ChoirStrings.getPaymentMethodLabel(contribution.paymentMethod, state.language) +
                                                    if (contribution.notes.isNotBlank()) " • ${contribution.notes}" else "",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = ChoirStrings.formatFcfa(contribution.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ForestGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Choir Campaigns (Totals only, without seeing other members' names)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Campagnes & Objectifs de la chorale" else "Choir Campaigns & Target Budgets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFr) "Progression générale des projets en cours" else "Overview of ongoing choir target budgets",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.campaigns.isEmpty()) {
                        Text(
                            text = if (isFr) "Aucune campagne active" else "No active campaigns",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.campaigns.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = item.campaign.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        text = "${ChoirStrings.formatFcfa(item.amountRaised)} / ${ChoirStrings.formatFcfa(item.campaign.targetAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
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
                                    text = "${item.percentageReached}% ${if (isFr) "collecté" else "raised"} • ${ChoirStrings.formatFcfa(item.amountRemaining)} ${if (isFr) "restant" else "remaining"}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overview of Choir Expenses
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Utilisation des fonds de la chorale" else "How Choir Money is Used",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFr) "Aperçu des dépenses par catégorie" else "Overview of expenses by category",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val categories = state.expenses.groupBy { it.category }
                    val totalExpenses = state.totalExpenses.coerceAtLeast(1L)

                    if (categories.isEmpty()) {
                        Text(
                            text = if (isFr) "Aucune dépense enregistrée" else "No expenses recorded",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categories.forEach { (cat, list) ->
                            val sum = list.sumOf { it.amount }
                            val fraction = (sum.toDouble() / totalExpenses.toDouble()).toFloat().coerceIn(0f, 1f)

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = ChoirStrings.getExpenseCategoryLabel(cat, state.language),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = ChoirStrings.formatFcfa(sum),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MutedRedWarning,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
