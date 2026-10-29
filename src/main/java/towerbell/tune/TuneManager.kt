package towerbell.tune

import towerbell.Proto
import towerbell.schedule.Schedule
import java.sql.DriverManager
import java.sql.SQLException
import java.time.DayOfWeek
import java.util.logging.Level
import java.util.logging.Logger

class TuneManager(val fixedConfig: Proto.FixedConfig) {
  private val logger: Logger = Logger.getLogger(TuneManager::class.java.name)
  val tuneFactory = TuneFactory()

  @Volatile var tunesById = emptyMap<Int, Tune>()

  init {
    reload()
  }

  fun getAllTunes(): List<Tune> = tunesById.values.toList()
  fun getTune(tuneId: Int): Tune? = tunesById[tuneId]

  fun addTune(tune: Tune) {
    val url = "jdbc:sqlite:${fixedConfig.databasePath}"
    val sql = "INSERT INTO Tune (TuneId, TuneName, TempoNoteValue, " +
        "BeatsPerMinute, KeySignatureNote, KeySignatureAccidental, " +
        "KeySignatureMode, LilyPondRelativeNote, TuneNotes) VALUES " +
        "(?, ?, ?, ?, ?, ?, ?, ?, ?)"
    try {
      DriverManager.getConnection(url).use { conn ->
        conn.prepareStatement(sql).use { stmt ->
          var index = 1
          stmt.setString(index++, tune.tuneName)
          stmt.setInt(index++, tune.tempoNoteValue)
          stmt.setInt(index++, tune.beatsPerMinute)
          stmt.setString(index++, tune.keySignatureNote)
          stmt.setString(index++, tune.keySignatureAccidental)
          stmt.setString(index++, tune.keySignatureMode)
          stmt.setString(index++, tune.lilyPondRelativeNote)
          stmt.setString(index++, tune.tuneNotes)
          stmt.executeUpdate()
          reload()
        }
      }
    } catch (e: SQLException) {
      logger.log(Level.WARNING, "Failed to save tune $tune", e)
    }
  }

  private fun reload() {
    val url = "jdbc:sqlite:${fixedConfig.databasePath}"
    try {
      DriverManager.getConnection(url).use { conn ->
        conn.createStatement().use { stmt ->
          stmt.executeQuery(
            "SELECT * FROM Tune ORDER BY TuneName").use { rs ->
            tunesById = buildMap {
              while (rs.next()) {
                val tune = tuneFactory.createFromResultSet(rs)
                put(tune.tuneId, tune)
              }
            }
          }
        }
      }
    } catch (e: SQLException) {
      logger.log(Level.WARNING, "Tune reload failed", e)
    }
  }
}