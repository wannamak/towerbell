package towerbell.www.template;

import towerbell.TowerBell
import towerbell.configuration.SilenceManager
import towerbell.schedule.ScheduleManager
import towerbell.www.ScheduleFormatter

class HomeReplacements(
  val scheduleManager: ScheduleManager,
  val silenceManager: SilenceManager) : InfoBoxReplacements(scheduleManager, silenceManager) {

  val scheduleFormatter = ScheduleFormatter()

  override val replacements: Map<String, String>
    get() = super.replacements + mapOf(
      "\$SCHEDULE" to getFormattedSchedule(),
      "\$VERSION" to "Version ${TowerBell.VERSION}",
      "\$SILENCE_CHECKED" to if (silenceManager.lastSilenced != null) "checked" else "")

  private fun getFormattedSchedule(): String =
    scheduleManager.schedules.joinToString(separator = "") { scheduleFormatter.format(it) }
}
