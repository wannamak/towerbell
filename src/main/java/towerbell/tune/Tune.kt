package towerbell.tune

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
data class Tune(
  val tuneId: Int,
  val tuneName: String,
  val tempoNoteValue: Int,
  val beatsPerMinute: Int,
  val keySignatureNote: String,
  val keySignatureAccidental: String,
  val keySignatureMode: String,
  val lilyPondRelativeNote: String,
  val tuneNotes: String) {
  val scoreHash: String by lazy {
    val digest = MessageDigest.getInstance("SHA-256")
    for (field in listOf(tempoNoteValue.toString(), beatsPerMinute.toString(),
      keySignatureNote, keySignatureAccidental, keySignatureMode,
      lilyPondRelativeNote, tuneNotes)) {
      digest.update(field.toByteArray(Charsets.UTF_8))
      digest.update(0)
    }
    digest.digest().toHexString()
  }
}