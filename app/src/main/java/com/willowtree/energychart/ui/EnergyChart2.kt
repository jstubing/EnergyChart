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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Chart data: Hard coded list of pairs representing the x and y axis data
// We will use the getXCoordinate and getYCoordinate functions to convert these to x and y pixel
// coordinates in the plot area.
private val chartData = listOf(
    0 to 7.5f,
    1 to 0.5f,
    2 to 14f,
    3 to 5f,
    4 to 10f
)

// Average low and high will be used to draw the average box
private val averageLow = 5f
private val averageHigh = 10f

@Preview(showBackground = true)
@Composable
private fun EnergyChart2() {
    val averageBoxColor = Color(0xFFF8F0FF)
    val averageBoxCornerRadius = 8.dp
    val averageBoxPaddingStart = 8.dp

    val gridlineStrokeWidth = 1.dp

    // Hard coded x and y axis labels
    val xAxisLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val yAxisLabels = listOf("0", "5", "10", "15")

    // Measure size of x and y axis labels
    val textMeasurer = rememberTextMeasurer()
    val measuredXAxisLabels = textMeasurer.measureLabels(xAxisLabels)
    val measuredYAxisLabels = textMeasurer.measureLabels(yAxisLabels)

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

                // Construct a rectangle which defines the bounds of the average box
                val averageBox = getAverageBox(
                    plotArea = plotArea,
                    averageBoxStart = widestYAxisLabelWidth + averageBoxPaddingStart.toPx()
                )

                // Draw the average box using the bounds/dimensions of the rectangle above
                drawAverageBox(
                    averageBox = averageBox,
                    color = averageBoxColor,
                    cornerRadius = averageBoxCornerRadius
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

// Maps a given data point x-value to an x-position in the plot area
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

// Maps a given data point y-value to a y-position in the plot area
private fun getYCoordinate(
    value: Float,
    minValue: Float,
    maxValue: Float,
    plotArea: Rect
): Float {
    return plotArea.bottom - (value - minValue) / (maxValue - minValue) * plotArea.height
}
