package towerbell.ringer

import towerbell.Proto
import towerbell.configuration.ConfigurationManager
import towerbell.configuration.SilenceManager
import towerbell.pi.physical.PCF8574Relays
import java.util.logging.Logger

data class RelayAddress(val relay: PCF8574Relays, val pin: Int)

class MultiNoteBellRinger(fixedConfig: Proto.FixedConfig,
                          configurationManager: ConfigurationManager,
                          silenceManager: SilenceManager
) : BellRinger(fixedConfig, configurationManager, silenceManager) {
  val relays = mutableMapOf<Int, PCF8574Relays>()
  val pitchNamesToStrike = mutableMapOf<String, RelayAddress>()
  val pitchNamesToRetract = mutableMapOf<String, RelayAddress>()
  private val logger: Logger = Logger.getLogger(TowerBellRinger::class.java.name)

  init {
    System.loadLibrary("towerbell")

    fun relayFor(address: Int): PCF8574Relays =
      relays.getOrPut(address) { PCF8574Relays(address) }

    for (pitchConfig in fixedConfig.pitchConfigList) {
      pitchNamesToStrike[pitchConfig.pitchName] =
          RelayAddress(relayFor(pitchConfig.strike.address), pitchConfig.strike.pin)
      pitchNamesToRetract[pitchConfig.pitchName] =
          RelayAddress(relayFor(pitchConfig.retract.address), pitchConfig.retract.pin)
    }
  }

  override fun beginRingSequence() {
  }

  override fun beginStrike(pitchName: String) {
    val relayAddress = pitchNamesToStrike[pitchName] ?: run {
      logger.info("No relay for pitch $pitchName strike")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.HIGH)
  }

  override fun endStrike(pitchName: String) {
    val relayAddress = pitchNamesToStrike[pitchName] ?: run {
      logger.info("No relay for pitch $pitchName strike")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.LOW)
  }

  override fun beginRetract(pitchName: String) {
    val relayAddress = pitchNamesToRetract[pitchName] ?: run {
      logger.info("No relay for pitch $pitchName retract")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.HIGH)
  }

  override fun endRetract(pitchName: String) {
    val relayAddress = pitchNamesToRetract[pitchName] ?: run {
      logger.info("No relay for pitch $pitchName retract")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.LOW)
  }

  override fun endRingSequence() {
  }
}