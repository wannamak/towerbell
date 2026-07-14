package towerbell.ringer

import towerbell.Proto
import towerbell.configuration.ConfigurationManager
import towerbell.configuration.SilenceManager
import towerbell.music.Pitch
import towerbell.pi.physical.PCF8574Relays
import java.util.logging.Logger

data class RelayAddress(val relay: PCF8574Relays, val pin: Int)

class MultiNoteBellRinger(fixedConfig: Proto.FixedConfig,
                          configurationManager: ConfigurationManager,
                          silenceManager: SilenceManager
) : BellRinger(fixedConfig, configurationManager, silenceManager) {
  val relays = mutableMapOf<Int, PCF8574Relays>()
  val pitchToStrikeRelay = mutableMapOf<Pitch, RelayAddress>()
  val pitchToRetractRelay = mutableMapOf<Pitch, RelayAddress>()
  private val logger: Logger = Logger.getLogger(TowerBellRinger::class.java.name)

  init {
    System.loadLibrary("towerbell")

    fun relayFor(address: Int): PCF8574Relays =
      relays.getOrPut(address) { PCF8574Relays(address) }

    for (pitchConfig in fixedConfig.pitchConfigList) {
      val pitch = Pitch.fromString(pitchConfig.pitchName)
      pitchToStrikeRelay[pitch] =
          RelayAddress(relayFor(pitchConfig.strike.address), pitchConfig.strike.pin)
      pitchToRetractRelay[pitch] =
          RelayAddress(relayFor(pitchConfig.retract.address), pitchConfig.retract.pin)
    }
  }

  override fun beginRingSequence() {
  }

  override fun beginStrike(pitch: Pitch) {
    val relayAddress = pitchToStrikeRelay[pitch] ?: run {
      logger.info("No relay for pitch $pitch strike")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.HIGH)
  }

  override fun endStrike(pitch: Pitch) {
    val relayAddress = pitchToStrikeRelay[pitch] ?: run {
      logger.info("No relay for pitch $pitch strike")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.LOW)
  }

  override fun beginRetract(pitch: Pitch) {
    val relayAddress = pitchToRetractRelay[pitch] ?: run {
      logger.info("No relay for pitch $pitch retract")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.HIGH)
  }

  override fun endRetract(pitch: Pitch) {
    val relayAddress = pitchToRetractRelay[pitch] ?: run {
      logger.info("No relay for pitch $pitch retract")
      return
    }
    relayAddress.relay.set(relayAddress.pin, PCF8574Relays.Value.LOW)
  }

  override fun endRingSequence() {
  }
}