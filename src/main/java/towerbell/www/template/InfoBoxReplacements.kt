package towerbell.www.template

import towerbell.configuration.SilenceManager
import towerbell.schedule.ScheduleManager
import towerbell.schedule.ScheduledRing
import towerbell.www.TimeFormatter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

open class InfoBoxReplacements(
  private val scheduleManager: ScheduleManager,
  private val silenceManager: SilenceManager) {

  private val timeFormatter = TimeFormatter()

  open val replacements: Map<String, String>
    get() = getInfoBoxReplacements()

  private fun getInfoBoxReplacements(): Map<String, String> {
    val nextScheduledRing = scheduleManager.nextRing
    val nextRingTime = nextScheduledRing?.ringTime

    return mapOf(
      "\$ISO_NOW" to ZonedDateTime.now().toIsoString(),
      "\$ISO_NEXT_RING_MAYBE_EMPTY" to nextRingTime?.toIsoString().orEmpty(),
      "\$NEXT_RING_OVERRIDE_STRING_MAYBE_EMPTY" to
          getNextRingOverrideString(nextScheduledRing).orEmpty(),
      "\$NEXT_RING_NUM_RINGS" to
          getNextRingNumRingsString(nextScheduledRing).orEmpty()
    )
  }

  private fun getNextRingOverrideString(nextRing: ScheduledRing?): String? {
    val lastSilenced = silenceManager.lastSilenced
    return when {
      lastSilenced != null -> "Silenced since ${timeFormatter.formatZonedDateTime(lastSilenced)}"
      nextRing?.ringTime == null -> "No rings scheduled"
      else -> null
    }
  }

  private fun getNextRingNumRingsString(nextRing: ScheduledRing?): String? {
    val schedule = nextRing?.schedule ?: return null
    val ringTime = nextRing.ringTime

    val numRings = when {
      schedule.isHourlyRing && ringTime != null -> if (ringTime.hour % 12 == 0) 12 else ringTime.hour % 12
      schedule.numRings > 0 -> schedule.numRings
      else -> return null
    }

    return "$numRings ring${if (numRings == 1) "" else "s"} in "
  }

  private fun ZonedDateTime.toIsoString(): String =
    truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}