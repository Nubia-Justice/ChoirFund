package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ChoirStrings {

    fun formatFcfa(amount: Long): String {
        val s = String.format(Locale.FRENCH, "%,d", amount).replace(',', ' ')
        return "$s FCFA"
    }

    fun formatDate(timestamp: Long, language: String): String {
        val locale = if (language == "FR") Locale.FRENCH else Locale.ENGLISH
        val pattern = if (language == "FR") "dd MMM yyyy" else "MMM dd, yyyy"
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long, language: String): String {
        val locale = if (language == "FR") Locale.FRENCH else Locale.ENGLISH
        val pattern = if (language == "FR") "dd/MM/yyyy HH:mm" else "MMM dd, yyyy h:mm a"
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timestamp))
    }

    fun isTodaySaturday(currentTime: Long = System.currentTimeMillis()): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = currentTime }
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
    }

    /**
     * Dues week rule: dues are collected every Saturday.
     * A dues week runs Saturday to Friday.
     * Returns the timestamp for Saturday 00:00:00 of the current dues week.
     */
    fun getDuesWeekStart(currentTime: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Saturday = 7
        val daysSinceSaturday = if (dayOfWeek == Calendar.SATURDAY) 0 else (dayOfWeek % 7)
        cal.add(Calendar.DAY_OF_YEAR, -daysSinceSaturday)
        return cal.timeInMillis
    }

    /**
     * Cameroon phone normalization for WhatsApp.
     * If 9 digits (e.g. 6XXXXXXXX), prepend +237.
     */
    fun normalizeCameroonPhone(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return when {
            digits.length == 9 -> "+237$digits"
            digits.length == 12 && digits.startsWith("237") -> "+$digits"
            digits.length == 13 && digits.startsWith("+237") -> digits
            else -> if (digits.isNotBlank()) "+237$digits" else ""
        }
    }

    // Bilingual payment method labels
    fun getPaymentMethodLabel(code: String, language: String): String {
        return when (code) {
            "CASH" -> if (language == "FR") "Espèces" else "Cash"
            "MTN_MOMO" -> "MTN Mobile Money"
            "ORANGE_MONEY" -> "Orange Money"
            else -> code
        }
    }

    // Bilingual contribution type labels
    fun getContributionTypeLabel(code: String, language: String): String {
        return when (code) {
            "DUES" -> if (language == "FR") "Cotisation" else "Dues"
            "VOLUNTARY_GIFT" -> if (language == "FR") "Don volontaire" else "Voluntary gift"
            "DONATION" -> if (language == "FR") "Donation" else "Donation"
            else -> code
        }
    }

    // Bilingual expense category labels
    fun getExpenseCategoryLabel(code: String, language: String): String {
        return when (code) {
            "SHEET_MUSIC" -> if (language == "FR") "Partitions et Livres" else "Sheet Music & Books"
            "SOUND_EQUIPMENT" -> if (language == "FR") "Matériel sonore" else "Sound Equipment"
            "REFRESHMENTS" -> if (language == "FR") "Rafraîchissements" else "Refreshments"
            "UNIFORMS" -> if (language == "FR") "Uniformes et Tenues" else "Uniforms & Vestments"
            "TRANSPORT" -> if (language == "FR") "Déplacements" else "Transport"
            "HONORARIUM" -> if (language == "FR") "Honoraires" else "Honorarium"
            "WELFARE" -> if (language == "FR") "Œuvres sociales" else "Welfare & Charity"
            "OTHER" -> if (language == "FR") "Autre" else "Other"
            else -> code
        }
    }

    // WhatsApp Receipt Text Generator
    fun buildWhatsAppReceipt(
        choirName: String,
        parishName: String,
        donorName: String,
        amount: Long,
        type: String,
        paymentMethod: String,
        date: Long,
        notes: String,
        language: String
    ): String {
        val typeLabel = getContributionTypeLabel(type, language)
        val methodLabel = getPaymentMethodLabel(paymentMethod, language)
        val amountStr = formatFcfa(amount)
        val dateStr = formatDateTime(date, language)

        return if (language == "FR") {
            """
            *REÇU OFFICIEL - $choirName*
            ⛪ $parishName
            ------------------------------------
            👤 *Membre / Donateur :* $donorName
            💰 *Montant :* $amountStr
            🏷️ *Type :* $typeLabel
            💳 *Moyen :* $methodLabel
            📅 *Date :* $dateStr
            ${if (notes.isNotBlank()) "📝 *Note :* $notes\n" else ""}------------------------------------
            _Merci pour votre soutien précieux à la chorale !_
            _Que Dieu vous bénisse abondamment._
            """.trimIndent()
        } else {
            """
            *OFFICIAL RECEIPT - $choirName*
            ⛪ $parishName
            ------------------------------------
            👤 *Member / Donor:* $donorName
            💰 *Amount:* $amountStr
            🏷️ *Type:* $typeLabel
            💳 *Method:* $methodLabel
            📅 *Date:* $dateStr
            ${if (notes.isNotBlank()) "📝 *Note:* $notes\n" else ""}------------------------------------
            _Thank you for your faithful contribution to the choir!_
            _May God bless you abundantly._
            """.trimIndent()
        }
    }

    /**
     * Formats WhatsApp dues reminder message.
     */
    fun buildWhatsAppDuesReminder(
        memberName: String,
        choirName: String,
        parishName: String,
        minAmount: Long = 200L,
        language: String
    ): String {
        val minStr = formatFcfa(minAmount)
        return if (language == "FR") {
            """
            🔔 *RAPPEL COTISATION DU SAMEDI*
            🎶 *$choirName*
            ⛪ $parishName
            ------------------------------------
            Bonjour $memberName,
            C'est aujourd'hui samedi, jour de collecte de notre cotisation hebdomadaire (minimum $minStr).

            💳 Modes de paiement acceptés :
            • MTN Mobile Money
            • Orange Money
            • Espèces à la répétition

            Merci pour votre fidélité et votre précieux engagement !
            Que Dieu bénisse votre week-end. 🙏
            """.trimIndent()
        } else {
            """
            🔔 *SATURDAY DUES REMINDER*
            🎶 *$choirName*
            ⛪ $parishName
            ------------------------------------
            Hello $memberName,
            Today is Saturday, our weekly choir dues collection day (minimum $minStr).

            💳 Payment options:
            • MTN Mobile Money
            • Orange Money
            • Cash at rehearsal

            Thank you for your faithfulness and devoted commitment to the choir!
            May God bless your weekend. 🙏
            """.trimIndent()
        }
    }

    /**
     * Formats SMS dues reminder message (concise for SMS character limit).
     */
    fun buildSmsDuesReminder(
        memberName: String,
        choirName: String,
        minAmount: Long = 200L,
        language: String
    ): String {
        val minStr = formatFcfa(minAmount)
        return if (language == "FR") {
            "Rappel $choirName: Bonjour $memberName, n'oubliez pas votre cotisation hebdomadaire de ce samedi (min $minStr). Paiement: MoMo, OM ou especes. Merci et bon samedi!"
        } else {
            "Reminder $choirName: Hi $memberName, friendly reminder for your weekly choir dues this Saturday (min $minStr). Pay via MoMo, OM, or cash. Thank you and blessed Saturday!"
        }
    }
}
