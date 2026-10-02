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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import com.example.data.model.Expense
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedRedWarning
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.ChoirUiState
import com.example.ui.viewmodel.UserRole

@Composable
fun ExpensesScreen(
    state: ChoirUiState,
    onCategoryFilterChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFr = state.language == "FR"
    val isTreasurer = state.session.role == UserRole.TREASURER

    val categories = listOf(
        "ALL",
        "SHEET_MUSIC",
        "SOUND_EQUIPMENT",
        "REFRESHMENTS",
        "UNIFORMS",
        "TRANSPORT",
        "HONORARIUM",
        "WELFARE",
        "OTHER"
    )

    val filteredList = state.expenses.filter {
        state.expenseCategoryFilter == "ALL" || it.category == state.expenseCategoryFilter
    }

    val totalSpent = filteredList.sumOf { it.amount }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (isTreasurer) {
                FloatingActionButton(
                    onClick = onAddExpense,
                    containerColor = MutedRedWarning,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("fab_add_expense")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Expense")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category comparison bars
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFr) "Dépenses par catégorie" else "Expenses by Category",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Total: ${ChoirStrings.formatFcfa(state.totalExpenses)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MutedRedWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val activeCategories = state.expenses.groupBy { it.category }
                    val allExpensesTotal = state.totalExpenses.coerceAtLeast(1L)

                    if (activeCategories.isEmpty()) {
                        Text(
                            text = if (isFr) "Aucune dépense enregistrée" else "No expenses logged",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        activeCategories.forEach { (cat, list) ->
                            val sum = list.sumOf { it.amount }
                            val fraction = (sum.toDouble() / allExpensesTotal.toDouble()).toFloat().coerceIn(0f, 1f)
                            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = ChoirStrings.getExpenseCategoryLabel(cat, state.language),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${ChoirStrings.formatFcfa(sum)} (${(fraction * 100).toInt()}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp)),
                                    color = MutedRedWarning,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val label = if (cat == "ALL") (if (isFr) "Tout" else "All")
                    else ChoirStrings.getExpenseCategoryLabel(cat, state.language)
                    FilterChip(
                        selected = state.expenseCategoryFilter == cat,
                        onClick = { onCategoryFilterChange(cat) },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(6.dp)
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
                        text = if (isFr) "Aucune dépense dans cette catégorie" else "No expenses found",
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
                    items(filteredList, key = { it.id }) { expense ->
                        ExpenseRowCard(
                            expense = expense,
                            isTreasurer = isTreasurer,
                            language = state.language,
                            onEdit = { onEditExpense(expense) },
                            onDelete = { onDeleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRowCard(
    expense: Expense,
    isTreasurer: Boolean,
    language: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isFr = language == "FR"
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = expense.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${ChoirStrings.getExpenseCategoryLabel(expense.category, language)} • ${ChoirStrings.getPaymentMethodLabel(expense.paymentMethod, language)} • ${ChoirStrings.formatDate(expense.date, language)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${ChoirStrings.formatFcfa(expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MutedRedWarning
                )

                if (isTreasurer) {
                    Spacer(modifier = Modifier.width(4.dp))
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
        }
    }
}
