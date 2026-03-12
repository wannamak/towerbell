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
package towerbell.pi.physical;

import java.util.logging.Logger;

public class MCP23017Controller {

  private final Logger logger = Logger.getLogger(MCP23017Controller.class.getName());
  private final SystemManagementBus bus;

  // default
  private static final int MCP_23017_EXPANDER_BOARD_DEVICE_ID = 0x27;

  private static final int NUM_OUTPUTS = 16;

  // https://ww1.microchip.com/downloads/en/devicedoc/20001952c.pdf
  private static final int MCP23017_IODIRECTION = 0x00;
  private static final int MCP23017_IPOL = 0x02;
  private static final int MCP23017_GPINTEN = 0x04;
  private static final int MCP23017_DEFVAL = 0x06;
  private static final int MCP23017_INTCON = 0x08;
  private static final int MCP23017_IOCON = 0x0A;
  private static final int MCP23017_GPPU = 0x0C;
  private static final int MCP23017_INTF = 0x0E;
  private static final int MCP23017_INTCAP = 0x10;
  private static final int MCP23017_GPIO = 0x12;
  private static final int MCP23017_OLATCH = 0x14;

  private static final int MCP23017_REGISTER_A_BIT = 0x00;
  private static final int MCP23017_REGISTER_B_BIT = 0x01;

  private static class Output {
    final int latchRegister;
    final int gpioRegister;
    final int directionRegister;
    final int bitmask;

    Output(int registerBit, int bitmask) {
      this.latchRegister = MCP23017_OLATCH | registerBit;
      this.gpioRegister = MCP23017_GPIO | registerBit;
      this.directionRegister = MCP23017_IODIRECTION | registerBit;
      this.bitmask = bitmask;
    }

    int update(int original, boolean value) {
      if (value) {
        return original | bitmask;
      } else {
        return original & ~bitmask;
      }
    }

    boolean isSet(int registerValue) {
      return (registerValue & bitmask) > 0;
    }
  }

  private static final Output[] OUTPUTS = new Output[] {
      new Output(MCP23017_REGISTER_A_BIT, 1),
      new Output(MCP23017_REGISTER_A_BIT, 2),
      new Output(MCP23017_REGISTER_A_BIT, 4),
      new Output(MCP23017_REGISTER_A_BIT, 8),
      new Output(MCP23017_REGISTER_A_BIT, 16),
      new Output(MCP23017_REGISTER_A_BIT, 32),
      new Output(MCP23017_REGISTER_A_BIT, 64),
      new Output(MCP23017_REGISTER_A_BIT, 128),

      new Output(MCP23017_REGISTER_B_BIT, 1),
      new Output(MCP23017_REGISTER_B_BIT, 2),
      new Output(MCP23017_REGISTER_B_BIT, 4),
      new Output(MCP23017_REGISTER_B_BIT, 8),
      new Output(MCP23017_REGISTER_B_BIT, 16),
      new Output(MCP23017_REGISTER_B_BIT, 32),
      new Output(MCP23017_REGISTER_B_BIT, 64),
      new Output(MCP23017_REGISTER_B_BIT, 128),
  };

  public MCP23017Controller() {
    this.bus = new SystemManagementBus();
  }

  public void initialize() {
    bus.initialize(MCP_23017_EXPANDER_BOARD_DEVICE_ID);
    initializeRegisters();
    //initializeDirectionRegister();
  }

  public enum Value {
    HIGH,
    LOW
  }

  public void set(int pin, Value value) {
    Output output = OUTPUTS[pin];
    int bitmap = bus.readByte(output.latchRegister);
    bitmap = output.update(bitmap, value == Value.HIGH);
    bus.writeByte(output.gpioRegister, bitmap);
  }

  public Value get(int pin) {
    Output output = OUTPUTS[pin];
    return output.isSet(bus.readByte(output.latchRegister)) ? Value.HIGH : Value.LOW;
  }

  private void initializeRegisters() {
    for (int register = 0; register < 22; register++) {
      if (register == 20 || register == 21) {
        bus.writeByte(register, 0xff);
      } else {
        bus.writeByte(register, 0);
      }
    }
  }

  private void initializeDirectionRegister() {
    for (Output output : OUTPUTS) {
      int value = bus.readByte(output.directionRegister);
      value = output.update(value, false /* output */);
      bus.writeByte(output.directionRegister, value);
    }
  }
}
