package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedRedWarning
import com.example.ui.theme.SoftBeigeBorder
import com.example.ui.util.ChoirStrings
import com.example.ui.viewmodel.MemberDuesStatus
import com.example.util.SaturdayReminderScheduler
import java.net.URLEncoder

@Composable
fun SaturdayDuesReminderDialog(
    unpaidMembers: List<MemberDuesStatus>,
    allMembersCount: Int,
    choirName: String,
    parishName: String,
    language: String,
    remindedMemberIds: Set<Long>,
    onMemberReminded: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isFr = language == "FR"
    val isSaturday = remember { ChoirStrings.isTodaySaturday() }

    // Channel selection: 0 = WhatsApp, 1 = SMS
    var selectedChannel by remember { mutableStateOf(0) }
    var showPreview by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("dialog_saturday_reminders"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSaturday) ForestGreenPrimary else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isSaturday) Color.White else ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isFr) "Rappels cotisations du samedi" else "Saturday Dues Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isSaturday) {
                                    if (isFr) "Aujourd'hui c'est samedi • Min. 200 FCFA" else "Today is Saturday • Min. 200 FCFA"
                                } else {
                                    if (isFr) "Semaine en cours • Min. 200 FCFA" else "Current Dues Week • Min. 200 FCFA"
                                },
                                fontSize = 11.sp,
                                color = if (isSaturday) ForestGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSaturday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unpaidMembers.isNotEmpty())
                            MutedRedWarning.copy(alpha = 0.1f)
                        else
                            ForestGreenLight.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (unpaidMembers.isNotEmpty()) {
                                    if (isFr) "${unpaidMembers.size} membre(s) non à jour" else "${unpaidMembers.size} member(s) unpaid"
                                } else {
                                    if (isFr) "Tous les membres sont à jour !" else "All members paid up!"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (unpaidMembers.isNotEmpty()) MutedRedWarning else ForestGreenPrimary
                            )
                            Text(
                                text = if (isFr)
                                    "Sur un total de $allMembersCount membres actifs"
                                else
                                    "Out of $allMembersCount active members",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Test Notification button
                        OutlinedButton(
                            onClick = {
                                SaturdayReminderScheduler.triggerImmediateTest(context)
                                Toast.makeText(
                                    context,
                                    if (isFr) "Notification test envoyée !" else "Test notification sent!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFr) "Tester alerte" else "Test alert", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs: WhatsApp vs SMS
                TabRow(
                    selectedTabIndex = selectedChannel,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = ForestGreenPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedChannel == 0,
                        onClick = { selectedChannel = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedChannel == 1,
                        onClick = { selectedChannel = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SMS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Message Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPreview = !showPreview }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showPreview)
                            (if (isFr) "Masquer l'aperçu du message ▲" else "Hide message preview ▲")
                        else
                            (if (isFr) "Voir le modèle de message ▼" else "View message template ▼"),
                        fontSize = 11.sp,
                        color = ForestGreenPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (selectedChannel == 0) "Format WhatsApp" else "Format SMS",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showPreview) {
                    val sampleName = if (unpaidMembers.isNotEmpty()) unpaidMembers.first().member.fullName else "Jean Dupont"
                    val sampleText = if (selectedChannel == 0) {
                        ChoirStrings.buildWhatsAppDuesReminder(
                            memberName = sampleName,
                            choirName = choirName,
                            parishName = parishName,
                            language = language
                        )
                    } else {
                        ChoirStrings.buildSmsDuesReminder(
                            memberName = sampleName,
                            choirName = choirName,
                            language = language
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, SoftBeigeBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = sampleText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Dues Reminder", sampleText))
                                        Toast.makeText(
                                            context,
                                            if (isFr) "Message copié !" else "Message copied!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isFr) "Copier" else "Copy", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                // List of Unpaid Members
                if (unpaidMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isFr)
                                    "Félicitations ! Toutes les cotisations sont à jour pour cette semaine."
                                else
                                    "Wonderful! All weekly dues are fully paid up for this week.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unpaidMembers, key = { it.member.id }) { item ->
                            val member = item.member
                            val isReminded = remindedMemberIds.contains(member.id)
                            val normalizedPhone = ChoirStrings.normalizeCameroonPhone(member.phoneNumber)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isReminded)
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Member details
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isReminded) ForestGreenLight.copy(alpha = 0.3f)
                                                    else MaterialTheme.colorScheme.primaryContainer
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.fullName.firstOrNull()?.uppercase() ?: "M",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isReminded) ForestGreenPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = member.fullName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (normalizedPhone.isNotBlank()) normalizedPhone else (if (isFr) "Sans numéro" else "No phone"),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (isReminded) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = ForestGreenPrimary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isFr) "Rappel envoyé" else "Reminded",
                                                        fontSize = 10.sp,
                                                        color = ForestGreenPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = if (item.duesThisWeekAmount > 0)
                                                        "${ChoirStrings.formatFcfa(item.duesThisWeekAmount)} / 200 FCFA"
                                                    else
                                                        (if (isFr) "0 FCFA payé cette semaine" else "0 FCFA paid this week"),
                                                    fontSize = 10.sp,
                                                    color = MutedRedWarning,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    // Action button
                                    Button(
                                        onClick = {
                                            if (selectedChannel == 0) {
                                                // WhatsApp
                                                val message = ChoirStrings.buildWhatsAppDuesReminder(
                                                    memberName = member.fullName,
                                                    choirName = choirName,
                                                    parishName = parishName,
                                                    language = language
                                                )
                                                sendWhatsApp(context, normalizedPhone, message)
                                            } else {
                                                // SMS
                                                val message = ChoirStrings.buildSmsDuesReminder(
                                                    memberName = member.fullName,
                                                    choirName = choirName,
                                                    language = language
                                                )
                                                sendSms(context, normalizedPhone, message)
                                            }
                                            onMemberReminded(member.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isReminded)
                                                MaterialTheme.colorScheme.secondary
                                            else
                                                ForestGreenPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (selectedChannel == 0) Icons.Default.Share else Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isReminded) {
                                                if (isFr) "Renvoyer" else "Resend"
                                            } else {
                                                if (selectedChannel == 0) "WhatsApp" else "SMS"
                                            },
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isFr) "Fermer" else "Close", fontSize = 12.sp)
                    }

                    val firstUnreminded = unpaidMembers.firstOrNull { !remindedMemberIds.contains(it.member.id) }
                    if (firstUnreminded != null) {
                        Button(
                            onClick = {
                                val member = firstUnreminded.member
                                val phone = ChoirStrings.normalizeCameroonPhone(member.phoneNumber)
                                if (selectedChannel == 0) {
                                    val msg = ChoirStrings.buildWhatsAppDuesReminder(
                                        memberName = member.fullName,
                                        choirName = choirName,
                                        parishName = parishName,
                                        language = language
                                    )
                                    sendWhatsApp(context, phone, msg)
                                } else {
                                    val msg = ChoirStrings.buildSmsDuesReminder(
                                        memberName = member.fullName,
                                        choirName = choirName,
                                        language = language
                                    )
                                    sendSms(context, phone, msg)
                                }
                                onMemberReminded(member.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFr)
                                    "Envoyer suivant (${firstUnreminded.member.fullName.take(12)})"
                                else
                                    "Send Next (${firstUnreminded.member.fullName.take(12)})",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun sendWhatsApp(context: Context, rawPhone: String, message: String) {
    try {
        val encoded = URLEncoder.encode(message, "UTF-8")
        val digits = rawPhone.filter { it.isDigit() }
        val url = if (digits.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$digits&text=$encoded"
        } else {
            "https://api.whatsapp.com/send?text=$encoded"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Reminder"))
    }
}

private fun sendSms(context: Context, rawPhone: String, message: String) {
    try {
        val digits = rawPhone.filter { it.isDigit() || it == '+' }
        val uri = if (digits.isNotBlank()) Uri.parse("smsto:$digits") else Uri.parse("smsto:")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Send SMS"))
    }
}
