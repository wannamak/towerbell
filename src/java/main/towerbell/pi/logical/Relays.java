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

import java.io.IOException;
import java.util.Arrays;

public abstract class Relays {
  protected Relay[] relays;

  public abstract void initialize() throws IOException;

  public Relay[] getRelays() {
    return relays;
  }

  public int length() {
    return relays.length;
  }

  public Relay get(int index) {
    return relays[index];
  }

  @Override
  public String toString() {
    return "Relays";
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(relays);
  }

  @Override
  public boolean equals(Object that) {
    if (!(that instanceof Relays)) {
      return false;
    }
    return Arrays.equals(((Relays) that).relays, relays);
  }
}
