package towerbell.www.template

import towerbell.Proto
import towerbell.configuration.ConfigurationManager
import towerbell.configuration.SilenceManager
import towerbell.schedule.Schedule
import towerbell.schedule.ScheduleManager
import towerbell.www.TimeFormatter
import java.io.File
import java.util.logging.Logger
import kotlin.collections.plus
import kotlin.text.orEmpty

class TemplateEngine(
  fixedConfig: Proto.FixedConfig,
  scheduleManager: ScheduleManager,
  silenceManager: SilenceManager,
  configurationManager: ConfigurationManager) {
  private val templateDirectory = File(fixedConfig.templateDirectory)
  private val placeholderRegex = Regex("""\$[A-Z_0-9]+""")

  private var ringTemplate = ""
  private val ringReplacements = RingReplacements(
    fixedConfig.pitchConfigList,
    scheduleManager,
    silenceManager)

  private var configTemplate = ""
  private val configReplacements = ConfigReplacements(configurationManager)

  private var homeTemplate = ""
  private val homeReplacements = HomeReplacements(scheduleManager, silenceManager)

  private var scheduleTemplate = ""
  private val addScheduleReplacements = AddScheduleReplacements(configurationManager)
  private val editScheduleReplacements = EditScheduleReplacements(configurationManager)

  private var commonReplacements = mapOf<String, String>()

  init {
    readTemplates()
  }

  fun getRingHtml() = expand(ringTemplate, ringReplacements.replacements + commonReplacements)
  fun getConfigHtml() = expand(configTemplate, configReplacements.replacements + commonReplacements)
  fun getHomeHtml() = expand(homeTemplate, homeReplacements.replacements + commonReplacements)
  fun getAddScheduleHtml() = expand(scheduleTemplate, addScheduleReplacements.replacements + commonReplacements)
  fun getEditScheduleHtml(schedule: Schedule) = expand(scheduleTemplate,
    editScheduleReplacements.getReplacements(schedule) + commonReplacements)

  public fun readTemplates() {
    ringTemplate = readFile("ring-template.html")
    configTemplate = readFile("config-template.html")
    homeTemplate = readFile("home-template.html")
    scheduleTemplate = readFile("schedule-template.html")
    commonReplacements = mapOf("\$HEADER" to readFile("header.inc"))
  }

  private fun expand(template: String, replacements: Map<String, String>): String {
    return template.replace(placeholderRegex) { replacements[it.value].orEmpty();  }
  }

  private fun readFile(fileName: String): String {
    val templateFile = File(templateDirectory, fileName)
    if (!templateFile.exists()) {
      logger.warning("Could not find file ${templateFile.absolutePath}")
    }
    return templateFile.readText()
  }

  private companion object {
    val logger: Logger = Logger.getLogger(TemplateEngine::class.java.name)
  }
}