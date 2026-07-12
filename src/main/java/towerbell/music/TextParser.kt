/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package towerbell.music

object TextParser {
  private val tempoRegex = Regex("""\\tempo\s+\d+\s*=\s*(\d+)""")
  private val notePattern = Regex("""^([a-g])(flat|sharp)?([',]*)(\d+)?(\.*)$""")
  private val elementNotePattern = Regex("""^([a-g])(sharp|flat)?([',]*)$""")
  private val restPattern = Regex("""^r(\d+)?(\.*)$""")
  private val durationDotsPattern = Regex("""^(\d+)?(\.*)$""")
  private val letterOrder = listOf(
    PitchLetter.C, PitchLetter.D, PitchLetter.E, PitchLetter.F,
    PitchLetter.G, PitchLetter.A, PitchLetter.B
  )

  fun parse(source: String, title: String = "Untitled", defaultTempoBpm: Int = 120): Song {
    var text = stripComments(source)
    val tempo = tempoRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: defaultTempoBpm
    text = text.replace(tempoRegex, " ")

    val tokens = tokenize(text)
    val voices = TokenParser(tokens).parse()
    return Song(title, tempo, voices)
  }

  private fun stripComments(source: String): String =
    source
      .replace(Regex("""%\{[\s\S]*?%}"""), " ")
      .replace(Regex("""%[^\n]*"""), " ")

  private fun tokenize(source: String): List<String> {
    val spaced = source
      .replace("{", " { ")
      .replace("}", " } ")
    return spaced.split(Regex("\\s+")).filter { it.isNotBlank() }
  }

  private class TokenParser(private val tokens: List<String>) {
    private var pos = 0
    private fun peek(): String? = tokens.getOrNull(pos)
    private fun next(): String = tokens[pos++]
    private fun expect(t: String) {
      require(peek() == t) { "Expected '$t' but found '${peek()}' at token $pos" }
      pos++
    }

    fun parse(): List<Voice> {
      if (peek() == "{") next()
      val voices = listOf(Voice("", ElementParser().parseUntil(setOf("}"))))
      if (peek() == "}") next()
      return voices
    }

    private inner class ElementParser(initialRelativeRef: Pitch? = null) {
      private var currentDurationNumber = 4
      private var currentDotted = false
      private var relativeRef: Pitch? = initialRelativeRef

      fun parseUntil(stop: Set<String>): MutableList<MusicalElement> {
        val elements = mutableListOf<MusicalElement>()
        while (peek() != null && peek() !in stop) {
          val token = peek()!!
          when {
            token == "\\relative" -> {
              next() // \relative
              val (l, a, o) = parsePitchToken(next())
              val savedRef = relativeRef
              relativeRef = buildAbsolutePitch(l, a, o)
              expect("{")
              elements += parseUntil(setOf("}"))
              expect("}")
              relativeRef = savedRef // restore outer context
            }

            token.startsWith("<") -> {
              elements += parseChord()
            }

            token.startsWith("r") && restPattern.matches(token) -> {
              next()
              val m = restPattern.matchEntire(token)!!
              applyDuration(m.groupValues[1], m.groupValues[2])
              elements += Rest(Duration(4.0 / currentDurationNumber, currentDotted))
            }

            else -> {
              next()
              var t = token
              val tied = t.endsWith("~")
              if (tied) t = t.dropLast(1)
              val m = notePattern.matchEntire(t) ?: error("Unrecognized token: $t")
              val (letterStr, accStr, octaveMarks, durStr, dots) = m.destructured
              applyDuration(durStr, dots)
              val pitch = nextPitch(letterStr, accStr, octaveMarks)
              elements += Note(pitch, Duration(4.0 / currentDurationNumber, currentDotted), tied)
            }
          }
        }
        return elements
      }

      private fun parsePitchToken(token: String): Triple<String, String, String> {
        val m = elementNotePattern.matchEntire(token)
          ?: error("Unrecognized pitch: $token")
        return Triple(m.groupValues[1], m.groupValues[2], m.groupValues[3])
      }

      private fun buildAbsolutePitch(letterStr: String, accStr: String, octaveMarks: String): Pitch {
        val letter = PitchLetter.valueOf(letterStr.uppercase())
        val octave = 3 + octaveMarks.count { it == '\'' } - octaveMarks.count { it == ',' }
        return Pitch(letter, accidentalFor(accStr), octave)
      }

      private fun parseChord(): Chord {
        val pitchTokens = mutableListOf<String>()
        var durStr = ""
        var dots = ""
        var tied = false

        val first = next().removePrefix("<")

        fun consumeClosing(piece: String) {
          val idx = piece.indexOf('>')
          val pitchPart = piece.substring(0, idx)
          if (pitchPart.isNotEmpty()) pitchTokens += pitchPart
          var rest = piece.substring(idx + 1)
          tied = rest.endsWith("~")
          if (tied) rest = rest.dropLast(1)
          val dm = durationDotsPattern.matchEntire(rest) ?: error("Bad chord duration: $rest")
          durStr = dm.groupValues[1]
          dots = dm.groupValues[2]
        }

        if (first.contains(">")) {
          consumeClosing(first)
        } else {
          if (first.isNotEmpty()) pitchTokens += first
          while (true) {
            val t = next()
            if (t.contains(">")) {
              consumeClosing(t)
              break
            } else {
              pitchTokens += t
            }
          }
        }

        applyDuration(durStr, dots)
        val pitches = pitchTokens.map { pt ->
          val (l, a, o) = parsePitchToken(pt)
          nextPitch(l, a, o)
        }
        return Chord(pitches, Duration(4.0 / currentDurationNumber, currentDotted), tied)
      }

      private fun buildRelativePitch(
        letterStr: String, accStr: String, octaveMarks: String, reference: Pitch
      ): Pitch {
        val letter = PitchLetter.valueOf(letterStr.uppercase())
        val prevIdx = letterOrder.indexOf(reference.letter)
        val newIdx = letterOrder.indexOf(letter)

        var octave = reference.octave
        fun stepNumber(idx: Int, oct: Int) = oct * 7 + idx
        var diff = stepNumber(newIdx, octave) - stepNumber(prevIdx, reference.octave)
        while (diff > 3) { octave--; diff -= 7 }
        while (diff < -3) { octave++; diff += 7 }

        octave += octaveMarks.count { it == '\'' } - octaveMarks.count { it == ',' }
        return Pitch(letter, accidentalFor(accStr), octave)
      }

      private fun accidentalFor(accStr: String): Accidental = when (accStr) {
        "sharp" -> Accidental.SHARP
        "flat" -> Accidental.FLAT
        else -> Accidental.NATURAL
      }

      private fun applyDuration(durStr: String, dots: String) {
        if (durStr.isNotEmpty()) currentDurationNumber = durStr.toInt()
        currentDotted = dots.isNotEmpty()
      }

      private fun nextPitch(letterStr: String, accStr: String, octaveMarks: String): Pitch {
        val ref = relativeRef
        val pitch = if (ref != null) {
          buildRelativePitch(letterStr, accStr, octaveMarks, ref)
        } else {
          buildAbsolutePitch(letterStr, accStr, octaveMarks)
        }
        relativeRef = pitch
        return pitch
      }
    }
  }
}