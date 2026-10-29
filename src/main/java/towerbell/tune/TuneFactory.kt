package towerbell.tune

import java.sql.ResultSet

class TuneFactory {
  fun createFromResultSet(rs: ResultSet): Tune {
    val tuneId = rs.getInt("TuneId")
    val tuneName = rs.getString("TuneName")
    val tempoNoteValue = rs.getInt("TempoNoteValue")
    val beatsPerMinute = rs.getInt("BeatsPerMinute")
    val keySignatureNote = rs.getString("KeySignatureNote")
    val keySignatureAccidental = rs.getString("KeySignatureAccidental")
    val keySignatureMode = rs.getString("KeySignatureMode")
    val lilyPondRelativeNote = rs.getString("LilyPondRelativeNote")
    val tuneNotes = rs.getString("TuneNotes")

    return Tune(
      tuneId, tuneName, tempoNoteValue, beatsPerMinute, keySignatureNote,
      keySignatureAccidental, keySignatureMode, lilyPondRelativeNote, tuneNotes)
  }
}