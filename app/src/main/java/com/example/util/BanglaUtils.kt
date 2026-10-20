package com.example.util

import com.example.data.model.PaymentMethod
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object BanglaUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toBangla(input: Any): String {
        val str = input.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglish(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = banglaDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats currency with South Asian comma separator and Taka symbol (টাকা)
     * e.g., 784139.0 -> "৭,৮৪,১৩৯ টাকা" or "৳ ৭,৮৪,১৩৯"
     */
    fun formatTaka(amount: Double, useSymbol: Boolean = false, inBangla: Boolean = true): String {
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        val formattedNumber = formatSouthAsianNumber(absAmount)
        val numberString = if (inBangla) toBangla(formattedNumber) else formattedNumber
        val prefix = if (isNegative) "-" else ""

        return if (useSymbol) {
            "$prefix৳ $numberString"
        } else {
            "$prefix$numberString টাকা"
        }
    }

    /**
     * Converts a number to South Asian comma format (12,34,567.89)
     */
    private fun formatSouthAsianNumber(amount: Double): String {
        val longVal = amount.toLong()
        val decimalPart = if (amount % 1.0 != 0.0) {
            String.format(Locale.US, "%.2f", amount % 1.0).substring(1)
        } else {
            ""
        }

        val s = longVal.toString()
        if (s.length <= 3) return s + decimalPart

        val last3 = s.substring(s.length - 3)
        val remaining = s.substring(0, s.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(',')
            }
        }
        return sb.reverse().toString() + "," + last3 + decimalPart
    }

    /**
     * Compact taka representation e.g. 30000 -> "30K", 24500 -> "24.5K", 239 -> "239"
     */
    fun formatCompactAmount(amount: Double): String {
        return if (amount >= 1000.0) {
            val inK = amount / 1000.0
            if (inK % 1.0 == 0.0) {
                "${inK.toLong()}K"
            } else {
                val formatted = DecimalFormat("#.##").format(inK)
                "${formatted}K"
            }
        } else {
            if (amount % 1.0 == 0.0) amount.toLong().toString() else DecimalFormat("#.##").format(amount)
        }
    }

    /**
     * Formats transaction code as used in the store ledger:
     * e.g. 30K (B 915/689) or 9.5K (N 4010/689) or 78K
     */
    fun generateTransactionCode(
        amount: Double,
        method: PaymentMethod,
        sender: String,
        receiver: String
    ): String {
        val compactAmt = formatCompactAmount(amount)
        val cleanSender = sender.trim()
        val cleanReceiver = receiver.trim()

        return if (method == PaymentMethod.CASH && cleanSender.isEmpty() && cleanReceiver.isEmpty()) {
            compactAmt
        } else if (cleanSender.isNotEmpty() || cleanReceiver.isNotEmpty() || method != PaymentMethod.CASH) {
            val prefix = method.codePrefix
            val accounts = when {
                cleanSender.isNotEmpty() && cleanReceiver.isNotEmpty() -> "$cleanSender/$cleanReceiver"
                cleanSender.isNotEmpty() -> cleanSender
                cleanReceiver.isNotEmpty() -> cleanReceiver
                else -> ""
            }
            if (accounts.isNotEmpty()) {
                "$compactAmt ($prefix $accounts)"
            } else {
                "$compactAmt ($prefix)"
            }
        } else {
            compactAmt
        }
    }

    /**
     * Helper to parse shorthand code like "30K (B 915/689)" or "9.5K (N 4010/689)"
     */
    data class ParsedCode(
        val amount: Double?,
        val method: PaymentMethod?,
        val sender: String?,
        val receiver: String?
    )

    fun parseShorthandCode(code: String): ParsedCode? {
        try {
            val clean = toEnglish(code).trim()
            // Regex for e.g. "30K (B 915/689)" or "12K (N 2050//2774)" or "239 (B 4010/2774)" or "78K"
            val pattern = Pattern.compile("(?i)^([0-9.]+)\\s*(K)?(?:\\s*\\(([BNbna-z]+)\\s*([0-9a-zA-Z]+)?(?:[/]+([0-9a-zA-Z]+))?\\))?")
            val matcher = pattern.matcher(clean)
            if (matcher.find()) {
                val numStr = matcher.group(1) ?: return null
                var num = numStr.toDoubleOrNull() ?: return null
                val isK = matcher.group(2) != null
                if (isK) {
                    num *= 1000.0
                }

                val methodStr = matcher.group(3)?.uppercase(Locale.US)
                val method = when (methodStr) {
                    "B" -> PaymentMethod.BKASH
                    "N" -> PaymentMethod.NAGAD
                    "C" -> PaymentMethod.CASH
                    else -> if (methodStr != null) PaymentMethod.BKASH else null
                }

                val sender = matcher.group(4) ?: ""
                val receiver = matcher.group(5) ?: ""

                return ParsedCode(num, method, sender, receiver)
            }
        } catch (_: Exception) {}
        return null
    }

    fun getCurrentDateBangla(): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("bn", "BD"))
        return try {
            sdf.format(Date())
        } catch (_: Exception) {
            val simple = SimpleDateFormat("d MMM yyyy", Locale.US).format(Date())
            toBangla(simple)
        }
    }

    fun getCurrentDateEnglish(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun formatDateToBangla(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("bn", "BD"))
        return try {
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            toBangla(SimpleDateFormat("d MMM yyyy", Locale.US).format(Date(timestamp)))
        }
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return toBangla(sdf.format(Date(timestamp)))
    }
}
