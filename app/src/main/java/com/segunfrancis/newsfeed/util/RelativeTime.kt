package com.segunfrancis.newsfeed.util

import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * RelativeTime.kt
 *
 * Converts ISO 8601 date strings (e.g. "2026-06-18T00:08:24Z") into
 * human-readable relative time strings ("5 minutes ago", "2 days ago", etc.)
 *
 * REQUIREMENTS
 * ─────────────────────────────────────────────────────────────────────────────
 * java.Time is used throughout (no third-party dependencies).
 *
 * Android: requires API 26+ OR core library desugaring enabled:
 *   // build.gradle.kts
 *   android { compileOptions { isCoreLibraryDesugaringEnabled = true } }
 *   dependencies { coreLibraryDesugaring("com.android.tools.build:r8:...") }
 *
 *
 * USAGE
 * ─────────────────────────────────────────────────────────────────────────────
 *   // Simple UI use — always returns a display string
 *   val label: String = apiDateString.toRelativeTimeString()
 *
 *   // Programmatic use — inspect success/failure explicitly
 *   when (val result = apiDateString.toRelativeTimeResult()) {
 *       is RelativeTimeResult.Success -> show(result.label)
 *       is RelativeTimeResult.InvalidFormat -> logError(result.input)
 *       is RelativeTimeResult.Empty         -> showPlaceholder()
 *   }
 */

// ─────────────────────────────────────────────────────────────────────────────
// Sealed result type — for callers that need to act on failure modes
// ─────────────────────────────────────────────────────────────────────────────

sealed class RelativeTimeResult {
    /** Parsing and calculation succeeded. */
    data class Success(val label: String) : RelativeTimeResult()

    /** Input was null, empty, or entirely whitespace. */
    object Empty : RelativeTimeResult()

    /** Input was non-empty but could not be parsed as ISO 8601. */
    data class InvalidFormat(val input: String) : RelativeTimeResult()
}

// ─────────────────────────────────────────────────────────────────────────────
// Primary extension — always returns a display-safe String
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Converts a nullable ISO 8601 date string into a relative time label.
 *
 * Returns a plain display string in all cases — never throws.
 * Use [toRelativeTimeResult] if you need to distinguish failure modes.
 *
 * Examples:
 *   "2026-06-18T00:08:24Z".toRelativeTimeString() → "5 minutes ago"
 *   null.toRelativeTimeString()                    → "Unknown time"
 *   "not-a-date".toRelativeTimeString()            → "Invalid date"
 *   "".toRelativeTimeString()                      → "Unknown time"
 *
 * @param fallbackForEmpty   Returned when input is null/blank. Default: "Unknown time"
 * @param fallbackForInvalid Returned when input cannot be parsed. Default: "Invalid date"
 */
fun String?.toRelativeTimeString(
    fallbackForEmpty: String = "",
    fallbackForInvalid: String = "",
): String = when (val result = toRelativeTimeResult()) {
    is RelativeTimeResult.Success -> result.label
    is RelativeTimeResult.Empty -> fallbackForEmpty
    is RelativeTimeResult.InvalidFormat -> fallbackForInvalid
}

// ─────────────────────────────────────────────────────────────────────────────
// Secondary extension — structured result for programmatic handling
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Converts a nullable ISO 8601 date string into a [RelativeTimeResult].
 *
 * Lets callers distinguish between an empty input and a malformed one,
 * and access the raw invalid input for logging/analytics.
 */
fun String?.toRelativeTimeResult(): RelativeTimeResult {
    // ── Guard: null / blank ──────────────────────────────────────────────────
    if (isNullOrBlank()) return RelativeTimeResult.Empty

    val trimmed = trim()

    // ── Parse ────────────────────────────────────────────────────────────────
    val instant: Instant = try {
        Instant.parse(trimmed)
    } catch (e: DateTimeParseException) {
        e.printStackTrace()
        return RelativeTimeResult.InvalidFormat(input = trimmed)
    } catch (e: Exception) {
        e.printStackTrace()
        // Catch-all: e.g. platform oddities — treated the same as invalid format
        return RelativeTimeResult.InvalidFormat(input = trimmed)
    }

    // ── Calculate delta ──────────────────────────────────────────────────────
    val now = Instant.now()
    val diffSeconds = ChronoUnit.SECONDS.between(instant, now) // negative = future
    val absDiff = abs(diffSeconds)

    // ── Format ───────────────────────────────────────────────────────────────
    val label = when {
        // Threshold logic mirrors how apps like Twitter/Google News handle it:
        // < 45 s   → "just now"  (avoids the jarring "0 minutes ago")
        // 45 s–89 s  → "1 minute ago" / "in 1 minute"
        // 45–89 min  → "1 hour ago"   / "in 1 hour"
        // 22–35 h    → "1 day ago"    / "in 1 day"
        // etc.
        absDiff < 45 -> "just now"
        absDiff < 2_700 -> plural(absDiff / 60, "minute", diffSeconds)   // up to 44 min
        absDiff < 5_400 -> suffix(1, "hour", diffSeconds)                 // 45–89 min → "1 hour"
        absDiff < 79_200 -> plural(absDiff / 3_600, "hour", diffSeconds)  // up to 21 h
        absDiff < 129_600 -> suffix(1, "day", diffSeconds)                  // 22–35 h   → "1 day"
        absDiff < 604_800 -> plural(absDiff / 86_400, "day", diffSeconds)  // up to 6 d
        absDiff < 1_209_600 -> suffix(1, "week", diffSeconds)              // 7–13 d    → "1 week"
        absDiff < 2_592_000 -> plural(absDiff / 604_800, "week", diffSeconds) // up to 3 w
        absDiff < 7_776_000 -> suffix(1, "month", diffSeconds)             // ~1–2 mo   → "1 month"
        absDiff < 31_536_000 -> plural(absDiff / 2_592_000, "month", diffSeconds) // up to 11 mo
        absDiff < 63_072_000 -> suffix(1, "year", diffSeconds)             // 1–1.9 yr  → "1 year"
        else -> plural(absDiff / 31_536_000, "year", diffSeconds)
    }

    return RelativeTimeResult.Success(label)
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Builds "X unit ago" or "in X units", handling singular/plural automatically.
 *
 * @param count       The numeric quantity (already divided to the right unit)
 * @param unit        Singular unit name, e.g. "minute", "hour", "day"
 * @param rawDelta    Raw seconds delta (negative = future, positive = past)
 */
private fun plural(count: Long, unit: String, rawDelta: Long): String {
    val unitLabel = if (count == 1L) unit else "${unit}s"
    return suffix(count, unitLabel, rawDelta)
}

/**
 * Wraps a count+unit with the right tense ("X unit ago" vs "in X unit").
 */
private fun suffix(count: Long, unit: String, rawDelta: Long): String =
    if (rawDelta >= 0) "$count $unit ago" else "in $count $unit"
