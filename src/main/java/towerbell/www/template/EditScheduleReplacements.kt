package towerbell.www.template

import towerbell.configuration.ConfigurationManager
import towerbell.schedule.Schedule
import towerbell.www.ScheduleFormatter
import java.time.LocalTime

class EditScheduleReplacements(val configurationManager: ConfigurationManager) {
  private val jsFactory = JavaScriptFactory()

  fun getReplacements(schedule: Schedule) = mapOf(
      "\$ADD_OR_EDIT" to "Edit",
      "\$ADD_OR_UPDATE" to "Update",
      "\$ADD_OR_UPDATE_FUNCTION_NAME" to "onUpdate",
      "\$FINAL_SCRIPT" to getFinalScriptForEdit(schedule))

  private fun getFinalScriptForEdit(schedule: Schedule): String {
    // These values aren't quoted in the JavaScript.
    val checkedMap = buildMap {
      putAll(schedule.daysOfWeek.associate { it.name.lowercase() to true })
      if (schedule.isHourlyRing) {
        put("ishourly", true)
      } else if (schedule.numRings > 0) {
        put("isspecified", true)
      }
    }

    // These values are quoted in the JavaScript.
    val valueMap = buildMap {
      if (!schedule.isHourlyRing && schedule.numRings > 0) {
        put("numrings", schedule.numRings.toString())
      }

      if (schedule.periodMinutes > 0) {
        put("periodminutes", schedule.periodMinutes.toString())
      }

      schedule.startTime?.let { putTime("start", it) }
      schedule.endTime?.let { putTime("end", it) }

      put("silence", ScheduleFormatter.formatMilliseconds(schedule.silenceDurationMillis))
      put("scheduleid", schedule.scheduleId.toString())
      put("isenabled", if (schedule.isEnabled) "1" else "0")
    }

    return jsFactory.setPropertyScript("checked", checkedMap) +
        jsFactory.setPropertyScript("value", valueMap)
  }

  private fun MutableMap<String, Any>.putTime(prefix: String, time: LocalTime) {
    val isAm = time.hour < 12
    val hour = (time.hour % 12).takeIf { it != 0 } ?: 12
    put("${prefix}hour", hour.toString())
    put("${prefix}minute", String.format("%02d", time.minute))
    put("${prefix}ampm", if (isAm) "am" else "pm")
  }
}