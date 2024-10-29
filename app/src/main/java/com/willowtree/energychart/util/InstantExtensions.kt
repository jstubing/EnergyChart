package com.willowtree.energychart.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private const val DefaultTimeFormat = "h:mm a"

fun Instant.occursIn30MinutesOrLess(now: Instant = Instant.now()): Boolean {
    val thirtyMinutes = 30L
    val duration = Duration.between(now, this).toMinutes()
    return duration in 0L..thirtyMinutes
}

fun Instant.plusMinutes(minutes: Long): Instant = plus(Duration.ofMinutes(minutes))
fun Instant.plusHours(hours: Long): Instant = plus(Duration.ofHours(hours))
fun Instant.plusDays(days: Long): Instant = plus(Duration.ofDays(days))
fun Instant.minusMinutes(minutes: Long): Instant = minus(Duration.ofMinutes(minutes))
fun Instant.minusHours(hours: Long): Instant = minus(Duration.ofHours(hours))
fun Instant.minusDays(days: Long): Instant = minus(Duration.ofDays(days))

fun Instant.toLocalDate(): LocalDate = atZone(ZoneId.systemDefault()).toLocalDate()

/**
 * Determines how many days a given [Instant] is from today.
 * @return number of days between [Instant] and today.
 */
fun Instant.daysLeft(now: Instant = Instant.now()) =
    Period.between(now.toLocalDate(), this.toLocalDate()).days

/**
 * Determines if given [Instant] occurs today.
 * @return true if [Instant] occurs today, false otherwise.
 */
fun Instant.occursToday(
    today: Instant = Instant.now()
): Boolean = toLocalDate() == today.toLocalDate()

/**
 * Formats a given [Instant] to a string using the given pattern.
 * @param pattern the pattern to use when formatting the [Instant].
 * @return a string representation of the [Instant] using the given pattern.
 */
fun Instant.format(pattern: String): String = DateTimeFormatter.ofPattern(pattern)
    .withZone(ZoneId.systemDefault())
    .format(this)

/**
 * Provides the formatted time of a given [Instant].
 * @return a string representation of the time of the [Instant].
 */
val Instant.formattedTime: String
    get() = format(DefaultTimeFormat).lowercase()

/**
 * Formats a given [Instant] to a short time of day string, the format of which depends on the
 * user's locale. e.g. 11:45 PM or 23:45
 */
val Instant.shortTimeOfDay: String
    get() {
        val localDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
        val dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return localDateTime.format(dateTimeFormatter)
    }

/**
 * Provides the formatted time of a given date range [Pair<Instant, Instant>].
 * @return a string representation of the time of the [Pair<Instant, Instant>].
 */
val Pair<Instant, Instant>.formattedTimeRange: String
    get() {
        val startDatePattern = when {
            (first.isInAM && second.isInAM) || (first.isInPM && second.isInPM) -> "h:mm"
            else -> DefaultTimeFormat
        }
        return "${first.format(startDatePattern)} - ${second.format(DefaultTimeFormat)}".lowercase()
    }
private val Instant.isInAM
    get() = ZonedDateTime.ofInstant(this, ZoneId.systemDefault()).hour < 12
private val Instant.isInPM
    get() = ZonedDateTime.ofInstant(this, ZoneId.systemDefault()).hour >= 12

val Instant.convertToYYYYMMDD: String?
    get() {
        val endDate = this.minus(1, ChronoUnit.DAYS)
        val localDateTime = LocalDateTime.ofInstant(endDate, ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.BASIC_ISO_DATE)
    }

fun Instant.convertToMMMdhhmm(is24HourFormat: Boolean = false): String {
    val localDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    return localDateTime.format(
        DateTimeFormatter.ofPattern(
            if (is24HourFormat) {
                "MMM d, HH:mm"
            } else {
                "MMM d, hh:mm a"
            }
        )
    )
}

fun Instant.convertToHHmm(is24HourFormat: Boolean = false): String {
    val localDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    return localDateTime.format(
        DateTimeFormatter.ofPattern(
            if (is24HourFormat) {
                "HH:mm"
            } else {
                DefaultTimeFormat
            }
        )
    )
}
