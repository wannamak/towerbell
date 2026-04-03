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

public class SystemManagementBus {
  private final Logger logger = Logger.getLogger(SystemManagementBus.class.getName());
  private int fd;

  public synchronized void initialize(int deviceId) {
    fd = initializeFileDescriptor("/dev/i2c-1", deviceId);
    if (fd < 0) {
      logger.warning("Unable to initialize bus for device " + deviceId);
    }
  }

  public synchronized int readByteData(int register) {
    if (fd < 0) {
      logger.warning("Uninitialized read of register " + register);
      return 0;
    }
    return readByteData(fd, register);
  }

  public synchronized void writeByteData(int register, int value) {
    if (fd < 0) {
      logger.warning("Uninitialize write of register " + register);
      return;
    }
    if (writeByteData(fd, register, value) < 0) {
      logger.warning("Error writing register " + register);
    }
  }

  public synchronized int readByte() {
    if (fd < 0) {
      logger.warning("Uninitialized read");
      return 0;
    }
    return readByteData(fd);
  }

  public synchronized void writeByte(int value) {
    if (fd < 0) {
      logger.warning("Uninitialize write");
      return;
    }
    if (writeByte(fd, value) < 0) {
      logger.warning("Error writing 0x" + Integer.toHexString(value));
    }
  }

  private native int readByteData(int fd, int register);
  private native int writeByteData(int fd, int register, int value);
  private native int readByte(int fd);
  private native int writeByte(int fd, int value);
  private native int initializeFileDescriptor(String devicePath, int deviceId);
}
