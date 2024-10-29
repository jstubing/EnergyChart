package com.willowtree.energychart.model

import androidx.annotation.StringRes
import com.willowtree.energychart.R

enum class EnergyUsageType(@StringRes val nameResId: Int) {
    DAY(R.string.day),
    WEEK(R.string.week),
    MONTH(R.string.month),
    YEAR(R.string.year)
}
