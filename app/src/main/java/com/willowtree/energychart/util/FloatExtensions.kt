package com.willowtree.energychart.util

import android.icu.text.NumberFormat
import android.icu.util.Currency

/**
 * Converts a float value into a currency string suitable for display in the UI.
 */
fun Float.toCurrencyString(currencyCode: String): String {
    val currencyFormatter = NumberFormat.getCurrencyInstance()
    currencyFormatter.currency = Currency.getInstance(currencyCode)
    if (this == 0f) {
        currencyFormatter.maximumIntegerDigits = 0
    }
    return currencyFormatter.format(this)
}
