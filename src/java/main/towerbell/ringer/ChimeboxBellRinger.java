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
package towerbell.ringer;

import towerbell.Proto;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.pi.logical.RaspberryRelays;
import towerbell.pi.logical.Relays;
import towerbell.pi.physical.GPIOChipInfoProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class ChimeboxBellRinger extends BellRinger {
  private final Logger logger = Logger.getLogger(ChimeboxBellRinger.class.getName());

  private Relays relays;
  private final int startNote = 3;
  private final int endNote = 7;
  private int currentNote = 3 + new SecureRandom().nextInt(4);
  private static final int POWER_RELAY_INDEX = 0;

  public ChimeboxBellRinger(
      Proto.FixedConfig fixedConfig,
      ConfigurationManager configurationManager,
      SilenceManager silenceManager) throws IOException {
    super(fixedConfig, configurationManager, silenceManager);
    if (!fixedConfig.getDisableNative()) {
      System.loadLibrary("towerbell");

      GPIOChipInfoProvider gpioManager = new GPIOChipInfoProvider();
      Path gpioDevicePath = gpioManager.getDevicePathForLabel(fixedConfig.getGpioLabel());
      //Preconditions.checkNotNull(
      //    gpioDevicePath, "No device for label " + fixedConfig.getGpioLabel());

      relays = new RaspberryRelays(gpioDevicePath);
      relays.initialize();
    } else {
      logger.info("Native library disabled");
    }
  }

  @Override
  protected void beginRingSequence() {
    relays.get(POWER_RELAY_INDEX).close();
  }

  @Override
  protected void beginRing() {
    relays.get(currentNote).close();
  }

  @Override
  protected void endRing() {
    relays.get(currentNote).open();
    currentNote++;
    if (currentNote > endNote) {
      currentNote = startNote;
    }
  }

  @Override
  protected void endRingSequence() {
    relays.get(POWER_RELAY_INDEX).open();
  }
}
