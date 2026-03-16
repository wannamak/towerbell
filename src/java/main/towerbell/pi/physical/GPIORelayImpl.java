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

import towerbell.pi.logical.Relay;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Controls relays via /dev/gpiochipN, which is the 'new' (and only) way to control GPIO
 * in Ubuntu 24 (kernel 6.8.0-1018).
 */
public class GPIORelayImpl extends GPIOController implements Relay {
  private static final Logger logger = Logger.getLogger(GPIORelayImpl.class.getName());

  private boolean isClosed = false;

  public GPIORelayImpl(Path devicePath, int logicalPin, boolean isActiveLow) {
    super(devicePath, logicalPin, Direction.OUT, isActiveLow);
  }

  @Override
  public void close() {
    set(Value.ACTIVE);
    isClosed = true;
  }

  @Override
  public void open() {
    set(Value.INACTIVE);
    isClosed = false;
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }
}
