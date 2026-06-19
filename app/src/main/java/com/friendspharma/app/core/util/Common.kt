package com.friendspharma.app.core.util

import java.math.BigDecimal
import java.math.RoundingMode


object Common {

    fun isValidMobile(mobile: String): Boolean {

        return mobile.isNotEmpty() && mobile.length == 11 && !mobile.startsWith(
            "011"
        ) && !mobile.startsWith("012") && mobile.startsWith("01")
    }

    fun isNumeric(string: String): Boolean {
        return string.toDoubleOrNull() != null
    }

}

/**
 * Formats a percentage for display WITHOUT forcing a whole number.
 * Keeps up to 2 decimals and strips trailing zeros:
 *   17.0 -> "17", 12.5 -> "12.5", 12.75 -> "12.75"
 */
fun Double.formatPercent(): String =
    BigDecimal(this)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()