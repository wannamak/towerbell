package towerbell.tune

data class Tune(
  val tuneId: Int,
  val tuneName: String,
  val tempoNoteValue: Int,
  val beatsPerMinute: Int,
  val keySignatureNote: String,
  val keySignatureAccidental: String,
  val keySignatureMode: String,
  val lilyPondRelativeNote: String,
  val tuneNotes: String)