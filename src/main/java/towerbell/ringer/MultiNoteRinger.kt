package towerbell.ringer

import towerbell.Proto
import towerbell.configuration.ConfigurationManager
import towerbell.configuration.SilenceManager
import towerbell.pi.physical.PCF8574Relays
import java.util.logging.Logger

class MultiNoteRinger(fixedConfig: Proto.FixedConfig,
                      configurationManager: ConfigurationManager,
                      silenceManager: SilenceManager
) : BellRinger(fixedConfig, configurationManager, silenceManager) {
  val relays: PCF8574Relays
  private val logger: Logger = Logger.getLogger(TowerBellRinger::class.java.name)

  init {
    System.loadLibrary("towerbell")
    logger.info("Init device 0x" + Integer.toHexString(fixedConfig.multiNoteConfig.pcf8574Address))
    relays = PCF8574Relays(fixedConfig.multiNoteConfig.pcf8574Address)
  }

  override fun beginRingSequence() {
  }

  override fun beginRing() {
    relays.set(0, PCF8574Relays.Value.HIGH)
  }

  override fun endRing() {
    relays.set(0, PCF8574Relays.Value.LOW)
  }

  override fun endRingSequence() {
  }
}