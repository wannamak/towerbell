package towerbell.www.template

import towerbell.configuration.ConfigurationManager
import towerbell.schedule.Schedule
import towerbell.tune.Tune
import towerbell.www.ScheduleFormatter
import java.time.LocalTime

class EditTuneReplacements() {
  private val jsFactory = JavaScriptFactory()

  fun getReplacements(tune: Tune) = mapOf(
      "\$ADD_OR_EDIT" to "Edit",
      "\$ADD_OR_UPDATE" to "Update",
      "\$ADD_OR_UPDATE_FUNCTION_NAME" to "onUpdate",
      "\$RENDERED_IMAGE_TAG" to "<img id='renderedscore' src='/api/tune/render/${tune.tuneId}'>",
      "\$FINAL_SCRIPT" to getFinalScriptForEdit(tune))

  private fun getFinalScriptForEdit(tune: Tune): String {
    val valueMap = buildMap {
      put("tuneid", tune.tuneId.toString())
      put("tunename", tune.tuneName)
      put("temponotevalue", tune.tempoNoteValue.toString())
      put("beatsperminute", tune.beatsPerMinute.toString())
      put("keysignaturenote", tune.keySignatureNote)
      put("keysignatureaccidental", tune.keySignatureAccidental)
      put("keysignaturemode", tune.keySignatureMode)
      put("lilypondrelativenote", tune.lilyPondRelativeNote)
      put("tunenotes", tune.tuneNotes)
    }

    return jsFactory.setPropertyScript("value", valueMap)
  }
}