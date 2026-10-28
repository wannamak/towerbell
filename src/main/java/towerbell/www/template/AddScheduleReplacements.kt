package towerbell.www.template

import towerbell.configuration.ConfigurationManager
import towerbell.www.ScheduleFormatter

class AddScheduleReplacements(val configurationManager: ConfigurationManager) {
  val replacements: Map<String, String>
    get() = mapOf(
      "\$ADD_OR_EDIT" to "Add New",
      "\$ADD_OR_UPDATE" to "Add",
      "\$ADD_OR_UPDATE_FUNCTION_NAME" to "onAdd",
      "\$FINAL_SCRIPT" to getFinalScriptForAdd())

  private fun getFinalScriptForAdd(): String = buildString {
    val silence = ScheduleFormatter.formatMilliseconds(configurationManager.defaultSilenceDurationMillis)
    appendLine("document.getElementById('silence').value = '$silence';")
    appendLine("document.getElementById('isenabled').value = '1';")
  }
}