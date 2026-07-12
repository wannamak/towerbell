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

enum class PitchLetter { A, B, C, D, E, F, G }

enum class Accidental(val symbol: String) {
  DOUBLE_FLAT("bb"),
  FLAT("b"),
  NATURAL(""),
  SHARP("#"),
  DOUBLE_SHARP("x");
}

data class Pitch(
  val letter: PitchLetter,
  val accidental: Accidental = Accidental.NATURAL,
  val octave: Int = 4
) {
  override fun toString() = "$letter${accidental.symbol}$octave"
}

data class Duration(val beats: Double, val dotted: Boolean = false) {
  val totalBeats: Double
    get() = if (dotted) beats * 1.5 else beats

  companion object {
    val WHOLE = Duration(4.0)
    val HALF = Duration(2.0)
    val QUARTER = Duration(1.0)
    val EIGHTH = Duration(0.5)
    val SIXTEENTH = Duration(0.25)
  }
}

sealed class MusicalElement {
  abstract val duration: Duration
  abstract val tiedToNext: Boolean
}

data class Rest(
  override val duration: Duration,
  override val tiedToNext: Boolean = false) : MusicalElement()

data class Note(
  val pitch: Pitch,
  override val duration: Duration,
  override val tiedToNext: Boolean = false
) : MusicalElement() {
  constructor(pitchLetter: PitchLetter, duration: Duration) : this(Pitch(pitchLetter), duration)
}

data class Chord(
  val pitches: List<Pitch>,
  override val duration: Duration,
  override val tiedToNext: Boolean = false
) : MusicalElement()

data class Voice(
  val name: String,
  val elements: List<MusicalElement>
) {
  val totalBeats: Double
    get() = elements.sumOf { it.duration.totalBeats }
}

data class Song(
  val title: String,
  val tempoBpm: Int,
  val voices: List<Voice>
)
