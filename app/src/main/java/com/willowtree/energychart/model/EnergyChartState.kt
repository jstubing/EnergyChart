package com.willowtree.energychart.model

import com.willowtree.energychart.util.format
import com.willowtree.energychart.util.formattedString
import com.willowtree.energychart.util.shortTimeOfDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.ceil

data class EnergyChartState(
    val timeSeriesEntries: List<TimeSeriesEntry>,
    val energyAverageUsage: EnergyAverageUsage,
    val energyUsageType: EnergyUsageType
) {

    val minTimestamp = timeSeriesEntries.firstOrNull()?.timestamp
    val maxTimestamp = timeSeriesEntries.lastOrNull()?.timestamp

    val minXAxisValue = minTimestamp?.epochSecond ?: 0L
    val maxXAxisValue = maxTimestamp?.epochSecond ?: 0L

    val minYAxisValue = 0

    val maxYAxisValue = if (timeSeriesEntries.any { it.summary.usage.amount > 0f }) {
        (ceil(timeSeriesEntries.maxOf { it.summary.usage.amount } / 3) * 3).toInt()
    } else {
        3
    }

    val xAxisLabels: ImmutableList<String>
        get() {
            return when (energyUsageType) {
                EnergyUsageType.DAY -> {
                    if (timeSeriesEntries.size >= 2) {
                        listOf(
                            timeSeriesEntries.first().xAxisLabel,
                            timeSeriesEntries.last().xAxisLabel
                        )
                    } else {
                        timeSeriesEntries.map { it.xAxisLabel }
                    }
                }

                EnergyUsageType.WEEK, EnergyUsageType.YEAR -> {
                    timeSeriesEntries.map { it.xAxisLabel }
                }

                EnergyUsageType.MONTH -> {
                    if (timeSeriesEntries.size > MaxXAxisLabels) {
                        listOf(
                            timeSeriesEntries.first().xAxisLabel,
                            timeSeriesEntries.last().xAxisLabel
                        )
                    } else {
                        timeSeriesEntries.map { it.xAxisLabel }
                    }
                }
            }.toImmutableList()
        }

    val yAxisLabels: ImmutableList<String>
        get() {
            val step = (maxYAxisValue - minYAxisValue) / 3
            var yAxisLabel = minYAxisValue

            return buildList {
                repeat(GridlineCount) { index ->
                    if (index < GridlineCount - 1) {
                        add(yAxisLabel.toString())
                    } else {
                        add("$yAxisLabel ${energyAverageUsage.unit}")
                    }

                    yAxisLabel += step
                }
            }.toImmutableList()
        }

    private val TimeSeriesEntry.xAxisLabel: String
        get() {
            return when (energyUsageType) {
                EnergyUsageType.DAY -> timestamp.shortTimeOfDay
                EnergyUsageType.WEEK -> timestamp.format("EEE")
                EnergyUsageType.MONTH -> timestamp.format("MMM d")
                EnergyUsageType.YEAR -> timestamp.format("MMM")
            }
        }

    fun getScrubberTooltipText(timeSeriesEntry: TimeSeriesEntry): String {
        return """
            ${timeSeriesEntry.xAxisLabel}
            ${timeSeriesEntry.summary.usage.formattedString}
            ${timeSeriesEntry.summary.cost.formattedString}
        """.trimIndent()
    }

    companion object {
        private const val GridlineCount = 4
        private const val MaxXAxisLabels = 8
    }
}
