package com.willowtree.energychart.model

data class EnergyAverageUsage(
    val average: Float,
    val averageRangeLow: Float,
    val averageRangeHigh: Float,
    val unit: String
)
