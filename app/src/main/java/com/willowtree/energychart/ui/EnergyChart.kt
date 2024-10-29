package com.willowtree.energychart.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willowtree.energychart.R
import com.willowtree.energychart.model.EnergyChartState
import com.willowtree.energychart.model.TimeSeriesEntry
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.abs

@Composable
internal fun EnergyChart(
    state: EnergyChartState,
    onAverageEnergyUsageInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrubberEnabled: Boolean = false,
    selectedTimeSeriesEntry: TimeSeriesEntry? = null,
    onTimeSeriesEntrySelect: (TimeSeriesEntry?) -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        EnergyChartCanvas(
            state = state,
            selectedTimeSeriesEntry = selectedTimeSeriesEntry,
            scrubberEnabled = scrubberEnabled,
            onTimeSeriesEntrySelect = onTimeSeriesEntrySelect,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        AverageEnergyUsageInfo(onClick = onAverageEnergyUsageInfoClick)
    }
}

@Composable
private fun EnergyChartCanvas(
    state: EnergyChartState,
    selectedTimeSeriesEntry: TimeSeriesEntry?,
    scrubberEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTimeSeriesEntrySelect: (TimeSeriesEntry?) -> Unit = {}
) {
    // TODO: Consider extracting these to function parameters so that the caller can customize them
    val averageBoxColor = Color(0xFFF8F0FF)
    val averageBoxCornerRadius = 8.dp
    val averageBoxPaddingStart = 8.dp

    val lineColorAverage = Color(0xFF8F4EC6)
    val lineColorAboveAverage = Color(0xFFFFCA49) // TODO: Is there an MDS color for this?
    val lineColorBelowAverage = Color(0xFF43AE0C)
    val lineStrokeWidth = 4.dp

    val gridlineColor = Color(0xFF9A9A9D)
    val gridlineStrokeWidth = 1.dp
    val gridlineDashWidth = 2.dp
    val labelColor = Color(0xFF57575B)

    val scrubberLineColor = Color(0xFF8F4EC6)
    val scrubberOuterCircleColor = Color(0xFF8F4EC6)
    val scrubberInnerCircleColor = Color(0xFFF8F0FF)
    val scrubberTooltipColor = Color(0xFF8F4EC6)
    val scrubberTooltipTextColor = Color.White

    val textMeasurer = rememberTextMeasurer()

    val measuredXAxisLabels = textMeasurer.measureLabels(
        labels = state.xAxisLabels,
        labelColor = labelColor
    )

    val measuredYAxisLabels = textMeasurer.measureLabels(
        labels = state.yAxisLabels,
        labelColor = labelColor
    )

    var coordinates = emptyList<Offset>()

    val measuredScrubberTooltipText = textMeasurer.measure(
        text = selectedTimeSeriesEntry?.let { state.getScrubberTooltipText(it) }.orEmpty(),
        style = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 15.sp,
            lineHeight = 21.sp,
        ).copy(color = scrubberTooltipTextColor)
    )

    // TODO: Avoid subsequent calls to onTimeSeriesEntrySelect when the selected data point isn't
    //       changing. We are getting excessive calls to onTimeSeriesEntrySelect, particularly
    //       when dragging over the chart.
    Canvas(
        modifier = modifier.then(
            if (scrubberEnabled && state.timeSeriesEntries.isNotEmpty()) {
                Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                onTimeSeriesEntrySelect(
                                    getSelectedTimeSeriesEntry(
                                        pointerOffset = offset,
                                        chartCoordinates = coordinates,
                                        energyChartState = state
                                    )
                                )
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, _ ->
                                onTimeSeriesEntrySelect(
                                    getSelectedTimeSeriesEntry(
                                        pointerOffset = change.position,
                                        chartCoordinates = coordinates,
                                        energyChartState = state
                                    )
                                )
                            }
                        )
                    }
            } else {
                Modifier
            }
        )
    ) {
        val xAxisLabelPaddingTop = 12.dp.toPx()
        val yAxisLabelPaddingBottom = 4.dp.toPx()
        val yAxisLabelPaddingEnd = 16.dp.toPx()

        val xAxisLabelHeight = measuredXAxisLabels.firstOrNull()?.size?.height?.toFloat() ?: 0f
        val firstXAxisLabelWidth = measuredXAxisLabels.firstOrNull()?.size?.width?.toFloat() ?: 0f
        val lastXAxisLabelWidth = measuredXAxisLabels.lastOrNull()?.size?.width?.toFloat() ?: 0f
        val widestYAxisLabelWidth = measuredYAxisLabels.dropLast(1).maxOf { it.size.width }

        val firstXAxisTickX =
            maxOf(widestYAxisLabelWidth + yAxisLabelPaddingEnd, firstXAxisLabelWidth / 2)
        val lastXAxisTickX = size.width - lastXAxisLabelWidth / 2

        val xAxisTickPositions = getTickPositions(
            firstTickPosition = firstXAxisTickX,
            lastTickPosition = lastXAxisTickX,
            totalTickPositions = measuredXAxisLabels.size
        )

        val yAxisLabelHeight = measuredYAxisLabels.firstOrNull()?.size?.height?.toFloat() ?: 0f

        val firstYAxisTickY =
            size.height - xAxisLabelHeight - xAxisLabelPaddingTop - (gridlineStrokeWidth.toPx() / 2)
        val lastYAxisTickY = yAxisLabelHeight + yAxisLabelPaddingBottom

        val yAxisTickPositions = getTickPositions(
            firstTickPosition = firstYAxisTickY,
            lastTickPosition = lastYAxisTickY,
            totalTickPositions = measuredYAxisLabels.size
        )

        val plotArea = Rect(
            offset = Offset(firstXAxisTickX, lastYAxisTickY),
            size = Size(lastXAxisTickX - firstXAxisTickX, firstYAxisTickY - lastYAxisTickY)
        )

        val averageBox = getAverageBox(
            energyChartState = state,
            plotArea = plotArea,
            averageBoxStart = widestYAxisLabelWidth + averageBoxPaddingStart.toPx()
        )

        drawAverageBox(
            averageBox = averageBox,
            color = averageBoxColor,
            cornerRadius = averageBoxCornerRadius
        )

        drawXAxisLabels(
            measuredXAxisLabels = measuredXAxisLabels,
            xAxisLabelHeight = xAxisLabelHeight,
            xAxisTickPositions = xAxisTickPositions,
            plotArea = plotArea
        )

        drawYAxisLabels(
            measuredYAxisLabels = measuredYAxisLabels,
            yAxisLabelPaddingBottom = yAxisLabelPaddingBottom,
            yAxisTickPositions = yAxisTickPositions
        )

        drawGridlines(
            yAxisTickPositions = yAxisTickPositions,
            strokeColor = gridlineColor,
            strokeWidth = gridlineStrokeWidth,
            dashWidth = gridlineDashWidth
        )

        coordinates = getCoordinates(
            energyChartState = state,
            plotArea = plotArea
        )

        val path = drawChartLine(
            coordinates = coordinates,
            strokeColor = lineColorAverage,
            strokeWidth = lineStrokeWidth
        )

        val aboveAverageLineDrawArea = Rect(
            offset = Offset(averageBox.left, 0f),
            size = Size(size.width - averageBox.left, averageBox.top)
        )

        drawClippedLine(
            path = path,
            drawArea = aboveAverageLineDrawArea,
            strokeColor = lineColorAboveAverage,
            strokeWidth = lineStrokeWidth
        )

        val belowAverageLineDrawArea = Rect(
            offset = Offset(averageBox.left, averageBox.bottom),
            size = Size(size.width - averageBox.left, size.height - averageBox.bottom)
        )

        drawClippedLine(
            path = path,
            drawArea = belowAverageLineDrawArea,
            strokeColor = lineColorBelowAverage,
            strokeWidth = lineStrokeWidth
        )

        selectedTimeSeriesEntry?.let { selectedTimeSeriesEntry ->
            val index = state.timeSeriesEntries.indexOf(selectedTimeSeriesEntry)

            if (index in coordinates.indices) {
                drawScrubber(
                    offset = coordinates[index],
                    plotArea = plotArea,
                    measuredTooltipText = measuredScrubberTooltipText,
                    lineColor = scrubberLineColor,
                    outerCircleColor = scrubberOuterCircleColor,
                    innerCircleColor = scrubberInnerCircleColor,
                    tooltipColor = scrubberTooltipColor
                )
            }
        }
    }
}

@Composable
private fun AverageEnergyUsageInfo(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .heightIn(min = 36.dp)
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8F0FF))
                .width(26.dp)
                .height(14.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.average_energy_usage),
            color = Color(0xFF57575B),
            style = TextStyle(
                fontWeight = FontWeight.W700,
                fontSize = 11.sp,
                lineHeight = 13.sp,
            )
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF6E6E73)
        )
    }
}

@Composable
private fun TextMeasurer.measureLabels(
    labels: ImmutableList<String>,
    labelColor: Color
) = labels.map { label ->
    measure(
        text = label,
        style = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 11.sp,
            lineHeight = 13.sp,
        ).copy(color = labelColor)
    )
}

private fun getTickPositions(
    firstTickPosition: Float,
    lastTickPosition: Float,
    totalTickPositions: Int
): List<Float> {
    val step = (lastTickPosition - firstTickPosition) / (totalTickPositions - 1)
    var tickPosition = firstTickPosition

    return buildList {
        repeat(totalTickPositions) {
            add(tickPosition)
            tickPosition += step
        }
    }
}

private fun getXCoordinate(
    value: Long,
    energyChartState: EnergyChartState,
    plotArea: Rect
): Float {
    if (energyChartState.timeSeriesEntries.size == 1) {
        return plotArea.left + plotArea.width / 2
    }

    val minXAxisValue = energyChartState.minXAxisValue
    val maxXAxisValue = energyChartState.maxXAxisValue

    return plotArea.left +
        (value - minXAxisValue).toFloat() /
        (maxXAxisValue - minXAxisValue).toFloat() *
        plotArea.width
}

private fun getYCoordinate(
    value: Float,
    energyChartState: EnergyChartState,
    plotArea: Rect
): Float {
    val minYAxisValue = energyChartState.minYAxisValue
    val maxYAxisValue = energyChartState.maxYAxisValue
    return plotArea.bottom - (value - minYAxisValue) / (maxYAxisValue - minYAxisValue) * plotArea.height
}

private fun getCoordinates(
    energyChartState: EnergyChartState,
    plotArea: Rect
): List<Offset> {
    val timeSeriesEntries = energyChartState.timeSeriesEntries

    return timeSeriesEntries.map { timeSeriesEntry ->
        val x = getXCoordinate(
            value = timeSeriesEntry.timestamp.epochSecond,
            energyChartState = energyChartState,
            plotArea = plotArea
        )

        val y = getYCoordinate(
            value = timeSeriesEntry.summary.usage.amount,
            energyChartState = energyChartState,
            plotArea = plotArea
        )

        Offset(x, y)
    }
}

private fun DrawScope.drawAverageBox(
    averageBox: Rect,
    color: Color,
    cornerRadius: Dp
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(averageBox.left, averageBox.top),
        size = Size(averageBox.width, averageBox.height),
        cornerRadius = CornerRadius(
            cornerRadius.toPx(),
            cornerRadius.toPx()
        )
    )
}

private fun DrawScope.drawXAxisLabels(
    measuredXAxisLabels: List<TextLayoutResult>,
    xAxisLabelHeight: Float,
    xAxisTickPositions: List<Float>,
    plotArea: Rect
) {
    val xAxisLabelY = size.height - xAxisLabelHeight

    xAxisTickPositions.forEachIndexed { index, position ->
        val xAxisLabel = measuredXAxisLabels[index]

        val xAxisLabelX = if (xAxisTickPositions.size == 1) {
            plotArea.left + plotArea.width / 2 - xAxisLabel.size.width.toFloat() / 2
        } else {
            position - xAxisLabel.size.width.toFloat() / 2
        }

        drawText(
            textLayoutResult = xAxisLabel,
            topLeft = Offset(xAxisLabelX, xAxisLabelY)
        )
    }
}

private fun DrawScope.drawYAxisLabels(
    measuredYAxisLabels: List<TextLayoutResult>,
    yAxisLabelPaddingBottom: Float,
    yAxisTickPositions: List<Float>,
) {
    yAxisTickPositions.forEachIndexed { index, position ->
        val yAxisLabel = measuredYAxisLabels[index]
        val yAxisLabelY = position - yAxisLabel.size.height - yAxisLabelPaddingBottom

        drawText(
            textLayoutResult = yAxisLabel,
            topLeft = Offset(0f, yAxisLabelY)
        )
    }
}

private fun DrawScope.drawGridlines(
    yAxisTickPositions: List<Float>,
    strokeColor: Color,
    strokeWidth: Dp,
    dashWidth: Dp
) {
    val dashPathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(
            dashWidth.toPx(),
            dashWidth.toPx()
        ),
        phase = 0f
    )

    yAxisTickPositions.forEach { position ->
        drawLine(
            color = strokeColor,
            start = Offset(0f, position),
            end = Offset(size.width, position),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = dashPathEffect
        )
    }
}

private fun DrawScope.drawChartLine(
    coordinates: List<Offset>,
    strokeColor: Color,
    strokeWidth: Dp
): Path {
    if (coordinates.isEmpty()) {
        return Path()
    }

    val controlPoints1 = mutableListOf<Offset>()
    val controlPoints2 = mutableListOf<Offset>()

    for (i in 1 until coordinates.size) {
        controlPoints1.add(
            Offset(
                x = (coordinates[i].x + coordinates[i - 1].x) / 2,
                y = coordinates[i - 1].y
            )
        )

        controlPoints2.add(
            Offset(
                x = (coordinates[i].x + coordinates[i - 1].x) / 2,
                y = coordinates[i].y
            )
        )
    }

    val path = if (coordinates.size == 1) {
        val chartLineLength = 96.dp.toPx()
        val chartLineStartX = coordinates.first().x - (chartLineLength / 2)
        val chartLineEndX = coordinates.first().x + (chartLineLength / 2)

        Path().apply {
            moveTo(chartLineStartX, coordinates.first().y)
            lineTo(chartLineEndX, coordinates.first().y)
        }
    } else {
        Path().apply {
            moveTo(coordinates.first().x, coordinates.first().y)

            for (i in 0 until coordinates.size - 1) {
                cubicTo(
                    x1 = controlPoints1[i].x,
                    y1 = controlPoints1[i].y,
                    x2 = controlPoints2[i].x,
                    y2 = controlPoints2[i].y,
                    x3 = coordinates[i + 1].x,
                    y3 = coordinates[i + 1].y
                )
            }
        }
    }

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
    )

    return path
}

private fun DrawScope.drawClippedLine(
    path: Path,
    drawArea: Rect,
    strokeColor: Color,
    strokeWidth: Dp
) {
    clipRect(
        left = drawArea.left,
        top = drawArea.top,
        right = drawArea.right,
        bottom = drawArea.bottom
    ) {
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

private fun DrawScope.drawScrubber(
    offset: Offset,
    plotArea: Rect,
    measuredTooltipText: TextLayoutResult,
    lineColor: Color,
    outerCircleColor: Color,
    innerCircleColor: Color,
    tooltipColor: Color,
) {
    val lineWidth = 1.dp

    val outerCircleRadius = 5.dp
    val innerCircleRadius = 2.dp

    val tooltipPaddingHorizontal = 8.dp
    val tooltipPaddingVertical = 4.dp
    val tooltipCornerRadius = 8.dp
    val tooltipToLineDistance = 8.dp

    drawLine(
        color = lineColor,
        start = Offset(offset.x, plotArea.bottom),
        end = Offset(offset.x, plotArea.top),
        strokeWidth = lineWidth.toPx()
    )

    drawCircle(
        color = outerCircleColor,
        radius = outerCircleRadius.toPx(),
        center = offset
    )

    drawCircle(
        color = innerCircleColor,
        radius = innerCircleRadius.toPx(),
        center = offset
    )

    val scrubberTooltipWidth = measuredTooltipText.size.width + tooltipPaddingHorizontal.toPx() * 2
    val scrubberTooltipHeight = measuredTooltipText.size.height + tooltipPaddingVertical.toPx() * 2

    var scrubberTooltipStart = offset.x -
        outerCircleRadius.toPx() -
        tooltipToLineDistance.toPx() -
        scrubberTooltipWidth

    if (scrubberTooltipStart < plotArea.left) {
        scrubberTooltipStart = offset.x +
            outerCircleRadius.toPx() +
            tooltipToLineDistance.toPx()
    }

    val scrubberTooltipRect = Rect(
        Offset(scrubberTooltipStart, plotArea.top),
        Size(scrubberTooltipWidth, scrubberTooltipHeight)
    )

    drawRoundRect(
        color = tooltipColor,
        topLeft = Offset(scrubberTooltipRect.left, scrubberTooltipRect.top),
        size = Size(scrubberTooltipRect.width, scrubberTooltipRect.height),
        cornerRadius = CornerRadius(
            tooltipCornerRadius.toPx(),
            tooltipCornerRadius.toPx()
        )
    )

    drawText(
        textLayoutResult = measuredTooltipText,
        topLeft = Offset(
            scrubberTooltipRect.left + tooltipPaddingHorizontal.toPx(),
            scrubberTooltipRect.top + tooltipPaddingVertical.toPx()
        )
    )
}

private fun DrawScope.getAverageBox(
    energyChartState: EnergyChartState,
    plotArea: Rect,
    averageBoxStart: Float
): Rect {
    val energyAverageUsage = energyChartState.energyAverageUsage

    val averageBoxTop = getYCoordinate(
        value = energyAverageUsage.averageRangeHigh,
        energyChartState = energyChartState,
        plotArea = plotArea
    )

    val averageBoxBottom = getYCoordinate(
        value = energyAverageUsage.averageRangeLow,
        energyChartState = energyChartState,
        plotArea = plotArea
    )

    return Rect(
        offset = Offset(averageBoxStart, averageBoxTop),
        size = Size(size.width - averageBoxStart, averageBoxBottom - averageBoxTop)
    )
}

private fun getSelectedTimeSeriesEntry(
    pointerOffset: Offset,
    chartCoordinates: List<Offset>,
    energyChartState: EnergyChartState
): TimeSeriesEntry {
    val xCoordinates = chartCoordinates.map { it.x }
    val index = xCoordinates.withIndex().minBy { abs(it.value - pointerOffset.x) }.index
    return energyChartState.timeSeriesEntries[index]
}

@Preview(showBackground = true)
@Composable
private fun EnergyChartPreview() {
    MaterialTheme {
        var selectedTimeSeriesEntry: TimeSeriesEntry? by remember { mutableStateOf(null) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { selectedTimeSeriesEntry = null }
        ) {
            EnergyChart(
                state = energyChartState,
                scrubberEnabled = true,
                selectedTimeSeriesEntry = selectedTimeSeriesEntry,
                onTimeSeriesEntrySelect = { selectedTimeSeriesEntry = it },
                onAverageEnergyUsageInfoClick = {},
                modifier = Modifier
                    .height(300.dp)
                    .padding(16.dp)
            )
        }
    }
}
