package com.willowtree.energychart.ui

import com.willowtree.energychart.model.EnergyAverageUsage
import com.willowtree.energychart.model.EnergyChartState
import com.willowtree.energychart.model.EnergyCostSnapshot
import com.willowtree.energychart.model.EnergyUsageSnapshot
import com.willowtree.energychart.model.EnergyUsageSummary
import com.willowtree.energychart.model.EnergyUsageType
import com.willowtree.energychart.model.TimeSeriesEntry
import java.time.Instant

val energyChartState = EnergyChartState(
    timeSeriesEntries = listOf(
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690156800),
            generateEnergyUsageSummary(4.53f, 15.43f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690243200),
            generateEnergyUsageSummary(11.75f, 6.89f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690329600),
            generateEnergyUsageSummary(16.07f, 10.47f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690416000),
            generateEnergyUsageSummary(6.54f, 5.73f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690502400),
            generateEnergyUsageSummary(18.19f, 11.29f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690588800),
            generateEnergyUsageSummary(4.81f, 9.91f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690675200),
            generateEnergyUsageSummary(15.93f, 0.5f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690761600),
            generateEnergyUsageSummary(16.57f, 8.46f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690848000),
            generateEnergyUsageSummary(20.06f, 18.56f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1690934400),
            generateEnergyUsageSummary(13.30f, 9.21f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691020800),
            generateEnergyUsageSummary(2.68f, 9.14f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691107200),
            generateEnergyUsageSummary(8.99f, 2.33f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691193600),
            generateEnergyUsageSummary(0.55f, 7.65f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691280000),
            generateEnergyUsageSummary(16.42f, 8.7f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691366400),
            generateEnergyUsageSummary(7.30f, 10.48f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691452800),
            generateEnergyUsageSummary(10.25f, 8.44f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691539200),
            generateEnergyUsageSummary(2.74f, 7.4f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691625600),
            generateEnergyUsageSummary(9.28f, 15.35f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691712000),
            generateEnergyUsageSummary(19.20f, 10.71f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691798400),
            generateEnergyUsageSummary(2.73f, 8.38f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691884800),
            generateEnergyUsageSummary(18.72f, 15.23f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1691971200),
            generateEnergyUsageSummary(8.06f, 15.91f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692057600),
            generateEnergyUsageSummary(16.12f, 9.07f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692144000),
            generateEnergyUsageSummary(19.36f, 7.68f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692230400),
            generateEnergyUsageSummary(2.44f, 6.32f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692316800),
            generateEnergyUsageSummary(20.37f, 7.0f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692403200),
            generateEnergyUsageSummary(20.46f, 7.04f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692489600),
            generateEnergyUsageSummary(6.66f, 6.28f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692576000),
            generateEnergyUsageSummary(19.03f, 10.18f)
        ),
        TimeSeriesEntry(
            Instant.ofEpochSecond(1692662400),
            generateEnergyUsageSummary(11.42f, 10.20f)
        )
    ),
    energyAverageUsage = EnergyAverageUsage(
        average = 10.5f,
        averageRangeLow = 8.5f,
        averageRangeHigh = 12.5f,
        unit = "kWh"
    ),
    energyUsageType = EnergyUsageType.MONTH
)

private fun generateEnergyUsageSummary(
    energyUsage: Float,
    energyCost: Float
): EnergyUsageSummary {
    return EnergyUsageSummary(
        usage = EnergyUsageSnapshot(
            amount = energyUsage,
            unit = "kWh"
        ),
        cost = EnergyCostSnapshot(
            amount = energyCost,
            currencyCode = "USD"
        )
    )
}
