package towerbell.www.template;

import towerbell.Proto.PitchConfiguration;
import towerbell.configuration.SilenceManager
import towerbell.schedule.ScheduleManager

class RingReplacements(
  val pitchConfigs: List<PitchConfiguration>,
  scheduleManager: ScheduleManager,
  silenceManager: SilenceManager) : InfoBoxReplacements(scheduleManager, silenceManager) {

  override val replacements: Map<String, String>
    get() = super.replacements + mapOf(
      "\$PITCH_NAMES" to getPitchNamesString())

  private fun getPitchNamesString(): String =
    pitchConfigs
      .map { it.pitchName }
      .joinToString(separator = "") { name -> "<option value=\"$name\">$name</option>\n" }
}
