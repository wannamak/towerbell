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

import java.io.IOException;
import java.util.logging.Logger;

public class NoOpBellRinger extends BellRinger {
  private final Logger logger = Logger.getLogger(NoOpBellRinger.class.getName());

  public NoOpBellRinger(
      Proto.FixedConfig fixedConfig,
      ConfigurationManager configurationManager,
      SilenceManager silenceManager)
      throws IOException {
    super(fixedConfig, configurationManager, silenceManager);
  }

  @Override
  protected void beginRingSequence() {
  }

  @Override
  protected void beginRing() {
    logger.info("beginRing");
  }

  @Override
  protected void endRing() {
    logger.info("endRing");
  }

  @Override
  protected void endRingSequence() {
  }
}
