package towerbell.tune

import towerbell.Proto
import java.io.File
import java.util.logging.Logger

class RenderCache(val fixedConfig: Proto.FixedConfig, val tuneRenderer: TuneRenderer) {
  private val logger: Logger = Logger.getLogger(RenderCache::class.java.name)
  private val renderedScoreDirectory: File = File(fixedConfig.renderedScoreDirectory);

  init {
    renderedScoreDirectory.mkdirs()
  }

  fun render(tune: Tune): String {
    val existingSvg = renderedScoreDirectory.resolve("${tune.scoreHash}.svg")
    return if (existingSvg.exists()) {
      existingSvg.readText()
    } else {
      tuneRenderer.render(tune, renderedScoreDirectory)
    }
  }
}