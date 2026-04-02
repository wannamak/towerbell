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

import java.io.IOException;

public class MCP23017RelayImpl implements Relay {
  private final MCP23017Controller controller;
  private final int pin;

  public MCP23017RelayImpl(MCP23017Controller controller, int pin) {
    this.controller = controller;
    this.pin = pin;
  }

  @Override
  public void close() {
    controller.set(pin, MCP23017Controller.Value.LOW);
  }

  @Override
  public void open() {
    controller.set(pin, MCP23017Controller.Value.HIGH);
  }

  @Override
  public boolean isClosed() throws IOException {
    return controller.get(pin) == MCP23017Controller.Value.LOW;
  }

  @Override
  public void initialize() throws IOException {

  }
}
