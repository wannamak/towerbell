package towerbell.pi.physical

class PCF8574Relays(deviceAddress: Int) {
  val bus = SystemManagementBus()
  var state = 0

  init {
    bus.initialize(deviceAddress)
  }

  private fun flush() {
    val data = state.inv() and 0xFF
    bus.writeByte(data)
  }

  enum class Value {
    HIGH,
    LOW
  }

  private fun checkPin(pin: Int) =
    require(pin in 0..7) { "Pin must be 0..7, got $pin" }

  fun set(pin: Int, value: Value) {
    checkPin(pin)
    state = if (value == Value.HIGH) {
      state or (1 shl pin)
    } else {
      state and (1 shl pin).inv()
    }
    flush()
  }

  fun get(pin: Int): Value {
    checkPin(pin)
    return if (state and (1 shl pin) != 0) Value.HIGH else Value.LOW
  }
}