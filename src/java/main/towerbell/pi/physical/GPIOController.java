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

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Controls GPIO via /dev/gpiochipN, which is the 'new' (and only) way to control GPIO
 * in Ubuntu 24 (kernel 6.8.0-1018).
 */
public class GPIOController {
  private static final Logger logger = Logger.getLogger(GPIOController.class.getName());

  public enum Value {
    ACTIVE,
    INACTIVE
  }

  public enum Direction {
    IN,
    OUT
  }

  private final Path devicePath;
  private final int logicalPin;
  private final Direction direction;
  private long context;

  public GPIOController(Path devicePath, int logicalPin, Direction direction) {
    this.devicePath = devicePath;
    this.logicalPin = logicalPin;
    this.direction = direction;
  }

  public synchronized void initialize() {
    if (direction == Direction.OUT) {
      context = initializeOutput(devicePath.toString(), logicalPin, true);
    } else {
      if (direction != Direction.IN) {
        throw new IllegalStateException();
      }
      context = initializeInput(devicePath.toString(), logicalPin);
    }
    if (context == 0) {
      throw new IllegalStateException("Failed to initialize GPIO pin " + logicalPin);
    }
  }

  /**
   * Returns an opaque context for the GPIO pin.
   */
  private native long initializeOutput(String devicePath, int pin, boolean isActiveLow);
  private native long initializeInput(String devicePath, int pin);

  public synchronized void set(Value value) {
    if (direction != Direction.OUT) {
      throw new IllegalStateException();
    }
    int result = setInternal(context, value.equals(Value.ACTIVE));
    if (result != 0) {
      throw new IllegalStateException("Failed to set GPIO pin " + logicalPin);
    }
  }

  /**
   * Returns -1 on error, else 0.
   */
  private native int setInternal(long context, boolean value);

  public synchronized Value get() {
    boolean result = getInternal(context);
    return result ? Value.ACTIVE : Value.INACTIVE;
  }

  private native boolean getInternal(long context);

  @Override
  public String toString() {
    return String.format("GPIO pin %d:%s %s", logicalPin, direction.toString(), devicePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(logicalPin, direction, devicePath);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GPIOController that)) {
      return false;
    }
    return Objects.equals(this.logicalPin, that.logicalPin)
        && Objects.equals(this.direction, that.direction)
        && Objects.equals(this.devicePath, that.devicePath);
  }
}
