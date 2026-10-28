package towerbell.www.template

class JavaScriptFactory {
  private fun Any?.toJsLiteral(): String = when (this) {
    null -> "null"
    is Boolean, is Number -> toString()
    else -> "'${toString()}'"
  }

  fun setPropertyScript(property: String, fields: Map<String, Any?>): String =
    fields.entries.joinToString(separator = "") { (id, value) ->
      "document.getElementById('$id').$property = ${value.toJsLiteral()};\n"
    }
}