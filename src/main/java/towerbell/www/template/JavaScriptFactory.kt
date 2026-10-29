package towerbell.www.template

class JavaScriptFactory {
  private fun Any?.toJsLiteral(): String = when (this) {
    null -> "null"
    is Boolean, is Number -> toString()
    else -> "'${toString().escapeJs()}'"
  }

  fun setPropertyScript(property: String, fields: Map<String, Any?>): String =
    fields.entries.joinToString(separator = "") { (id, value) ->
      "document.getElementById('$id').$property = ${value.toJsLiteral()};\n"
    }

  private fun String.escapeJs(): String = buildString {
    for (c in this@escapeJs) {
      when (c) {
        '\\' -> append("\\\\")
        '\'' -> append("\\'")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '<' -> append("\\u003C")   // prevents "</script>" from closing the script block
        else -> append(c)
      }
    }
  }
}