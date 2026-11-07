package towerbell.www.template

class AddTuneReplacements {
  val replacements: Map<String, String>
    get() = mapOf(
      "\$ADD_OR_EDIT" to "Add New",
      "\$ADD_OR_UPDATE" to "Add",
      "\$ADD_OR_UPDATE_FUNCTION_NAME" to "onAdd",
      "\$RENDERED_IMAGE_TAG" to "",
      "\$FINAL_SCRIPT" to getFinalScriptForAdd())

  private fun getFinalScriptForAdd(): String = buildString {
    appendLine("document.getElementById('temponotevalue').value = 4;")
    appendLine("document.getElementById('beatsperminute').value = 80;")
  }
}