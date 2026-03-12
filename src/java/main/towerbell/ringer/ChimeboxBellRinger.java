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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ChimeboxBellRinger extends BellRinger {
  private final Logger logger = Logger.getLogger(ChimeboxBellRinger.class.getName());

  private Relays relays;
  private static final int POWER_RELAY_INDEX = 0;
  private static final List<Integer> NOTES = List.of(3, 4, 5, 6, 8, 9);
  private int currentNote = -1;
  private final List<Integer> currentNotes = new ArrayList<>();

  public ChimeboxBellRinger(
      Proto.FixedConfig fixedConfig,
      ConfigurationManager configurationManager,
      SilenceManager silenceManager) throws IOException {
    super(fixedConfig, configurationManager, silenceManager);
    if (!fixedConfig.getDisableNative()) {
      System.loadLibrary("towerbell");

      GPIOChipInfoProvider gpioManager = new GPIOChipInfoProvider();
      Path gpioDevicePath = gpioManager.getDevicePathForLabel(fixedConfig.getGpioLabel());
      if (gpioDevicePath == null) {
        throw new IOException("No device for label " + fixedConfig.getGpioLabel());
      }
      relays = new RaspberryRelays(gpioDevicePath);
      relays.initialize();
    } else {
      logger.info("Native library disabled");
    }
  }

  @Override
  protected void beginRingSequence() {
    relays.get(POWER_RELAY_INDEX).close();
    if (currentNotes.isEmpty()) {
      Collections.copy(currentNotes, NOTES);
    }
    currentNote = currentNotes.remove(new SecureRandom().nextInt(currentNotes.size()));
  }

  @Override
  protected void beginRing() {
    relays.get(currentNote).close();
  }

  @Override
  protected void endRing() {
    relays.get(currentNote).open();
  }

  @Override
  protected void endRingSequence() {
    relays.get(POWER_RELAY_INDEX).open();
  }
}
