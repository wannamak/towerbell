package towerbell.tune

import towerbell.Proto
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.readText

class TuneRenderer(val fixedConfig: Proto.FixedConfig) {
  private val logger: Logger = Logger.getLogger(TuneRenderer::class.java.name)

  fun render(tune: Tune, directory: File): String {
    val source = generateLilyPondSource(tune)
    logger.fine("Rendering tune $tune with source:\n$source")
    val outputSvgWithoutSuffix = directory.toPath().resolve(tune.scoreHash)
    renderLilyPondSvg(source, outputSvgWithoutSuffix)
    return outputSvgWithoutSuffix.resolveSibling("${tune.scoreHash}.svg").readText()
  }

  private fun generateLilyPondSource(tune: Tune): String {
    val keyModifier = when (tune.keySignatureAccidental) {
      "sharp" -> "s"
      "flat" -> "f"
      else -> ""
    }
    return """
      \version "2.24.0"
      \language "english"
      \paper {
        indent = 0\mm
        oddFooterMarkup = ##f
        oddHeaderMarkup = ##f
        bookTitleMarkup = ##f
        scoreTitleMarkup = ##f
      }

      global = {
        \key ${tune.keySignatureNote.lowercase()}${keyModifier} \${tune.keySignatureMode}
        \time 4/${tune.tempoNoteValue}
      }

      \new Staff {
        \global
        \relative ${tune.lilyPondRelativeNote} {
        ${tune.tuneNotes}
        }
      }
    """.trimIndent()
  }

  fun renderLilyPondSvg(lilypondSource: String, outputPathWithoutSuffix: Path) {
    val process = ProcessBuilder(
      fixedConfig.lilypond,
      "-dbackend=svg",
      "-dno-point-and-click",
      "-dcrop",
      "-o", outputPathWithoutSuffix.absolutePathString(),
      "-",
    )
      .redirectErrorStream(true)
      .start()

    process.outputStream.bufferedWriter().use { it.write(lilypondSource) }
    val log = process.inputStream.bufferedReader().readText()

    Files.writeString(File("/tmp/source.ly").toPath(), lilypondSource)

    if (!process.waitFor(30, TimeUnit.SECONDS)) {
      process.destroyForcibly()
      error("lilypond timed out")
    }
    if (process.exitValue() != 0) {
      logger.warning("lilypond failed:\n$log")
    } else {
      val svg = outputPathWithoutSuffix.resolveSibling("${outputPathWithoutSuffix.fileName}.svg")
      val croppedSvg = outputPathWithoutSuffix.resolveSibling("${outputPathWithoutSuffix.fileName}.cropped.svg")

      if (croppedSvg.exists()) {
        croppedSvg.moveTo(svg, overwrite = true)
      }
    }
  }
}