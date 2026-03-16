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
package towerbell;

import com.google.protobuf.TextFormat;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.ringer.BellRinger;
import towerbell.ringer.TowerBellRinger;
import towerbell.schedule.ScheduleManager;
import towerbell.schedule.SchedulerThread;
import towerbell.www.WebServerThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;

public class TowerBell {
  private final Logger logger = Logger.getLogger(TowerBell.class.getName());
  private final towerbell.Proto.FixedConfig fixedConfig;

  public static final String VERSION = "1.3";

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Specify path to config.txt");
      System.exit(-1);
    }
    new TowerBell(args[0]).run();
  }

  public TowerBell(String pathToConfig) throws IOException {
    Proto.FixedConfig.Builder fixedConfigBuilder = Proto.FixedConfig.newBuilder();
    logger.info("Reading config from " + pathToConfig);
    try (BufferedReader br = new BufferedReader(new FileReader(pathToConfig))) {
      TextFormat.merge(br, fixedConfigBuilder);
    }
    this.fixedConfig = fixedConfigBuilder.build();
  }

  public void run() throws Exception {
    ConfigurationManager configurationManager = new ConfigurationManager(fixedConfig);
    ScheduleManager scheduleManager = new ScheduleManager(fixedConfig);
    SilenceManager silenceManager = new SilenceManager(fixedConfig);
    BellRinger bellRinger = instantiateRingerClass(configurationManager, silenceManager);

    SchedulerThread schedulerThread = new SchedulerThread(scheduleManager, bellRinger);
    schedulerThread.start();

    configurationManager.setDatabaseUpdateListener(schedulerThread);
    scheduleManager.setDatabaseUpdateListener(schedulerThread);

    WebServerThread webServerThread = new WebServerThread(fixedConfig, scheduleManager,
        configurationManager, bellRinger, silenceManager);
    webServerThread.start();

    schedulerThread.join();  // wait forever
  }

  private BellRinger instantiateRingerClass(
      ConfigurationManager configurationManager, SilenceManager silenceManager) throws Exception {
    String ringerClassname = fixedConfig.getRingerClassname();
    if (ringerClassname.isEmpty()) {
      ringerClassname = TowerBellRinger.class.getName();
    }
    Class<?> clazz = Class.forName(ringerClassname);
    Constructor<?> constructor = clazz.getConstructor(
        Proto.FixedConfig.class,
        ConfigurationManager.class,
        SilenceManager.class
    );
    return (BellRinger) constructor.newInstance(
        fixedConfig, configurationManager, silenceManager);
  }
}
