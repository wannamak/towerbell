package towerbell.pi.physical

import towerbell.Proto

class PCF8574Controller(val fixedConfig: Proto.FixedConfig) {
  val bus = SystemManagementBus()
  var state = 0

  fun initialize() {
    bus.initialize(fixedConfig.multiNoteConfig.pcf8574Address)
  }

  private fun flush() {
    val data = state.inv() and 0xFF
    bus.writeByte(data)
  }
}