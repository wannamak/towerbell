package towerbell.www.template;

import towerbell.configuration.ConfigurationManager

class ConfigReplacements(private val configurationManager: ConfigurationManager) {
  private val jsFactory = JavaScriptFactory()

  val replacements: Map<String, String>
    get() = mapOf("\$FINAL_SCRIPT" to getFinalScriptForConfig())

  private fun getFinalScriptForConfig(): String {
    val valueMap = mapOf(
      "webauthenticationusername" to configurationManager.webAuthenticationUsername,
      "webauthenticationpassword" to configurationManager.webAuthenticationPassword,
      "ringdurationmillis" to configurationManager.ringDurationMillis,
      "defaultsilencedurationmillis" to configurationManager.defaultSilenceDurationMillis,
    )

    return jsFactory.setPropertyScript("value", valueMap)
  }
}
