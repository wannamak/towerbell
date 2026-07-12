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

import towerbell.music.Accidental.SHARP
import towerbell.music.Duration.Companion.EIGHTH
import towerbell.music.Duration.Companion.QUARTER
import towerbell.music.Duration.Companion.SIXTEENTH
import towerbell.music.PitchLetter.*
import towerbell.music.TextParser.parse
import kotlin.test.Test
import kotlin.test.assertEquals

class TestParserTest {
  @Test
  fun testRelative() {
    val sampleText = """
        { \tempo 4 = 120
          \relative c' {
            c4 d8 e8 <c e g>2. r4 fsharp8.~ fsharp16
          }
          c4
        }
    """.trimIndent()
    val song = parse(sampleText, title = "Relative scope + ties")
    assertEquals(120, song.tempoBpm)
    assertEquals(
      listOf(
        Note(C, QUARTER),
        Note(D, EIGHTH),
        Note(E, EIGHTH),
        Chord(
          listOf(
            Pitch(C),
            Pitch(E),
            Pitch(G)
          ),
          Duration(2.0, dotted = true)),
        Rest(QUARTER),
        Note(Pitch(F, SHARP), Duration(0.5, dotted = true), tiedToNext = true),
        Note(Pitch(F, SHARP), SIXTEENTH),
        Note(Pitch(C, octave = 3), QUARTER)
        ),
      song.voices.first().elements
    )
  }

  @Test
  fun testNestedPolyphony() {
    val nestedPolyphony = """
        \tempo 4 = 100
        \relative c' {
          c4 d4 <e f>4 g4
        }
    """.trimIndent()
    val song = parse(nestedPolyphony, title = "Nested polyphony")
    assertEquals(100, song.tempoBpm)
    assertEquals(
      listOf(
        Note(Pitch(C, octave = 4), QUARTER),
        Note(Pitch(D, octave = 4), QUARTER),
        Chord(listOf(Pitch(E), Pitch(F)), QUARTER),
        Note(Pitch(G, octave = 4), QUARTER)
      ),
      song.voices.first().elements
    )
  }
}