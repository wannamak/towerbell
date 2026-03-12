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
package towerbell.pi.logical;

import towerbell.pi.physical.GPIORelayImpl;
import towerbell.pi.physical.MCP23017Controller;
import towerbell.pi.physical.MCP23017RelayImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class RaspberryRelays extends Relays {
  private static final Logger logger = Logger.getLogger(RaspberryRelays.class.getName());

  private final Path gpioDevicePath;

  public RaspberryRelays(Path gpioDevicePath) {
    this.gpioDevicePath = gpioDevicePath;
  }

  @Override
  public void initialize() throws IOException {
    MCP23017Controller controller = new MCP23017Controller();
    controller.initialize();

    relays = new Relay[] {
        new GPIORelayImpl(gpioDevicePath, 12),  // chimes power supply
        new GPIORelayImpl(gpioDevicePath, 16),  // low AC power to chimes
        new GPIORelayImpl(gpioDevicePath, 20),  // high AC power to chimes
        new GPIORelayImpl(gpioDevicePath, 21),  // note 1
        new GPIORelayImpl(gpioDevicePath, 23),
        new GPIORelayImpl(gpioDevicePath, 24),
        new GPIORelayImpl(gpioDevicePath, 25),
        new GPIORelayImpl(gpioDevicePath, 26),
        new MCP23017RelayImpl(controller, 7),
        new MCP23017RelayImpl(controller, 6),
        new MCP23017RelayImpl(controller, 5),
        new MCP23017RelayImpl(controller, 4),
        new MCP23017RelayImpl(controller, 3),  // note 10
        new MCP23017RelayImpl(controller, 2),
        new MCP23017RelayImpl(controller, 1),
        new MCP23017RelayImpl(controller, 0),
        new MCP23017RelayImpl(controller, 15),
        new MCP23017RelayImpl(controller, 14),
        new MCP23017RelayImpl(controller, 13),
        new MCP23017RelayImpl(controller, 12),
        new MCP23017RelayImpl(controller, 11),
        new MCP23017RelayImpl(controller, 10),
        new MCP23017RelayImpl(controller, 9),  // note 20
        new MCP23017RelayImpl(controller, 8),  // note 21
    };
    for (int i = 0; i < relays.length; ++i) {
      logger.info(String.format("Initializing relay %d", i));
      relays[i].initialize();
    }
  }
}
