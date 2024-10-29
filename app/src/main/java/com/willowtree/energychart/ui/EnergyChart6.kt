package com.willowtree.energychart.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val chartData = listOf(
    0 to 7.5f,
    1 to 0.5f,
    2 to 14.5f,
    3 to 5f,
    4 to 10f
)

private val average = 7.5f
private val averageLow = 5f
private val averageHigh = 10f

@Preview(showBackground = true)
@Composable
private fun EnergyChart6() {
    val averageBoxColor = Color(0xFFF8F0FF)
    val averageBoxCornerRadius = 8.dp
    val averageBoxPaddingStart = 8.dp

    val lineColorAverage = Color(0xFF8F4EC6)
    val lineColorAboveAverage = Color(0xFFFFCA49)
    val lineColorBelowAverage = Color(0xFF43AE0C)
    val lineStrokeWidth = 4.dp

    val gridlineColor = Color(0xFF9A9A9D)
    val gridlineStrokeWidth = 1.dp
    val gridlineDashWidth = 2.dp

    // Hard coded x and y axis labels
    val xAxisLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val yAxisLabels = listOf("0", "5", "10", "15")

    // Measure size of x and y axis labels
    val textMeasurer = rememberTextMeasurer()
    val measuredXAxisLabels = textMeasurer.measureLabels(xAxisLabels)
    val measuredYAxisLabels = textMeasurer.measureLabels(yAxisLabels)

    var coordinates = emptyList<Offset>()

    MaterialTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(16f / 9f)
                    .background(Color.White)

            ) {
                val xAxisLabelPaddingTop = 12.dp.toPx()
                val yAxisLabelPaddingBottom = 4.dp.toPx()
                val yAxisLabelPaddingEnd = 16.dp.toPx()

                val xAxisLabelHeight =
                    measuredXAxisLabels.firstOrNull()?.size?.height?.toFloat() ?: 0f
                val firstXAxisLabelWidth =
                    measuredXAxisLabels.firstOrNull()?.size?.width?.toFloat() ?: 0f
                val lastXAxisLabelWidth =
                    measuredXAxisLabels.lastOrNull()?.size?.width?.toFloat() ?: 0f
                val widestYAxisLabelWidth = measuredYAxisLabels.dropLast(1).maxOf { it.size.width }

                // Calculate x and y axis tick positions
                //
                // x-axis tick positions will sit in the center of the x-axis labels
                // y-axis tick positions will sit slightly below the y-axis labels
                //
                // The tick positions need to take measured label size into account since not all
                // labels will be the same width and a user may bump up their font size which changes
                // the position of the ticks.
                val firstXAxisTickX =
                    maxOf(widestYAxisLabelWidth + yAxisLabelPaddingEnd, firstXAxisLabelWidth / 2)
                val lastXAxisTickX = size.width - lastXAxisLabelWidth / 2

                val xAxisTickPositions = getTickPositions(
                    firstTickPosition = firstXAxisTickX,
                    lastTickPosition = lastXAxisTickX,
                    totalTickPositions = measuredXAxisLabels.size
                )

                val yAxisLabelHeight =
                    measuredYAxisLabels.firstOrNull()?.size?.height?.toFloat() ?: 0f

                val firstYAxisTickY =
                    size.height - xAxisLabelHeight - xAxisLabelPaddingTop - (gridlineStrokeWidth.toPx() / 2)
                val lastYAxisTickY = yAxisLabelHeight + yAxisLabelPaddingBottom

                val yAxisTickPositions = getTickPositions(
                    firstTickPosition = firstYAxisTickY,
                    lastTickPosition = lastYAxisTickY,
                    totalTickPositions = measuredYAxisLabels.size
                )

                // Knowing the tick positions, we can create a Rectangle defining the bounds of the
                // area where the line will be drawn. This rectangle gives us quick access to the
                // start, top, end, and bottom positions of the drawing area.
                val plotArea = Rect(
                    offset = Offset(firstXAxisTickX, lastYAxisTickY),
                    size = Size(lastXAxisTickX - firstXAxisTickX, firstYAxisTickY - lastYAxisTickY)
                )

                // Extract the drawing of the x and y axis labels to their own functions.
                //
                // These are extensions on DrawScope which allow us to access functions such
                // as drawText.
                //
                // We pass in the tick positions that we calculated earlier so that we can
                // draw the text in a position relative to the tick positions.
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

                val averageBox = getAverageBox(
                    plotArea = plotArea,
                    averageBoxStart = widestYAxisLabelWidth + averageBoxPaddingStart.toPx()
                )

                drawAverageBox(
                    averageBox = averageBox,
                    color = averageBoxColor,
                    cornerRadius = averageBoxCornerRadius
                )

                drawGridlines(
                    yAxisTickPositions = yAxisTickPositions,
                    strokeColor = gridlineColor,
                    strokeWidth = gridlineStrokeWidth,
                    dashWidth = gridlineDashWidth
                )

                coordinates = getCoordinates(plotArea)

                val path = drawChartLine(
                    coordinates = coordinates,
                    strokeColor = lineColorAverage,
                    strokeWidth = lineStrokeWidth
                )

                // Construct a rectangle representing the bounds of of the drawing area for the
                // above average region of the chart line.
                val aboveAverageLineDrawArea = Rect(
                    offset = Offset(averageBox.left, 0f),
                    size = Size(size.width - averageBox.left, averageBox.top)
                )

                // Draw the chart line path but clip anything that falls outside of the bounds of
                // the above rectangle.
                drawClippedLine(
                    path = path,
                    drawArea = aboveAverageLineDrawArea,
                    strokeColor = lineColorAboveAverage,
                    strokeWidth = lineStrokeWidth
                )

                // Construct a rectangle representing the bounds of of the drawing area for the
                // below average region of the chart line.
                val belowAverageLineDrawArea = Rect(
                    offset = Offset(averageBox.left, averageBox.bottom),
                    size = Size(size.width - averageBox.left, size.height - averageBox.bottom)
                )

                // Draw the chart line path but clip anything that falls outside of the bounds of
                // the above rectangle.
                drawClippedLine(
                    path = path,
                    drawArea = belowAverageLineDrawArea,
                    strokeColor = lineColorBelowAverage,
                    strokeWidth = lineStrokeWidth
                )
            }
        }
    }
}

@Composable
private fun TextMeasurer.measureLabels(
    labels: List<String>
) = labels.map { label ->
    measure(
        text = label,
        style = MaterialTheme.typography.labelSmall
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

private fun DrawScope.getAverageBox(
    plotArea: Rect,
    averageBoxStart: Float
): Rect {
    val energyAverageUsage = average

    val averageBoxTop = getYCoordinate(
        value = averageHigh,
        minValue = 0f,
        maxValue = 15f,
        plotArea = plotArea
    )

    val averageBoxBottom = getYCoordinate(
        value = averageLow,
        minValue = 0f,
        maxValue = 15f,
        plotArea = plotArea
    )

    return Rect(
        offset = Offset(averageBoxStart, averageBoxTop),
        size = Size(size.width - averageBoxStart, averageBoxBottom - averageBoxTop)
    )
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

private fun getCoordinates(plotArea: Rect): List<Offset> {
    return chartData.map { dataPair ->
        val x = getXCoordinate(
            value = dataPair.first,
            minValue = 0,
            maxValue = 4,
            plotArea = plotArea
        )

        val y = getYCoordinate(
            value = dataPair.second,
            minValue = 0f,
            maxValue = 15f,
            plotArea = plotArea
        )

        Offset(x, y)
    }
}

private fun getXCoordinate(
    value: Int,
    minValue: Int,
    maxValue: Int,
    plotArea: Rect
): Float {
    return plotArea.left +
        (value - minValue).toFloat() /
        (maxValue - minValue).toFloat() *
        plotArea.width
}

private fun getYCoordinate(
    value: Float,
    minValue: Float,
    maxValue: Float,
    plotArea: Rect
): Float {
    return plotArea.bottom - (value - minValue) / (maxValue - minValue) * plotArea.height
}
