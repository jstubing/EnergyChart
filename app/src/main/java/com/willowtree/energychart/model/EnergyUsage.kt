package com.willowtree.energychart.model

import kotlinx.collections.immutable.ImmutableList
import java.time.Instant

data class EnergyUsage(
    val summary: EnergyUsageSummary,
    val historyStartDate: Instant,
    val historicalLow: Float,
    val historicalHigh: Float,
    val usageAverage: EnergyAverageUsage,
    val usageData: ImmutableList<TimeSeriesEntry>,
    val devices: ImmutableList<EnergyUsageDevice>,
)
