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
package towerbell.configuration;

import towerbell.ColumnUpdater;
import towerbell.ipc.DatabaseUpdateListener;
import towerbell.Proto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationManager {
  private final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
  private final Proto.FixedConfig fixedConfig;
  private DatabaseUpdateListener databaseUpdateListener;

  /** Sane defaults for the case of a missing configuration row. */
  private String webAuthenticationUsername = "username";
  private String webAuthenticationPassword = "password";
  private int ringDurationMillis = 3000;
  private int defaultSilenceDurationMillis = 3000;

  public ConfigurationManager(Proto.FixedConfig fixedConfig) {
    this.fixedConfig = fixedConfig;
    reload();
  }

  public synchronized void setDatabaseUpdateListener(DatabaseUpdateListener databaseUpdateListener) {
    this.databaseUpdateListener = databaseUpdateListener;
  }


  public synchronized String getWebAuthenticationUsername() {
    return webAuthenticationUsername;
  }

  public synchronized String getWebAuthenticationPassword() {
    return webAuthenticationPassword;
  }

  public synchronized int getRingDurationMillis() {
    return ringDurationMillis;
  }

  public synchronized int getDefaultSilenceDurationMillis() {
    return defaultSilenceDurationMillis;
  }

  public synchronized void reload() {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery("SELECT * FROM Configuration WHERE ConfigurationId = 1")) {
        boolean success = false;
        if (rs.next()) {
          webAuthenticationUsername = rs.getString("WebAuthenticationUsername");
          webAuthenticationPassword = rs.getString("WebAuthenticationPassword");
          ringDurationMillis = rs.getInt("RingDurationMillis");
          defaultSilenceDurationMillis = rs.getInt("DefaultSilenceDurationMillis");
          success = true;
          if (databaseUpdateListener != null) {
            databaseUpdateListener.onDatabaseUpdate();
          }
        }
        if (!success) {
          logger.warning("No configuration row with ConfigurationId = 1 found in database");
        }
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  public synchronized boolean update(String webAuthenticationUsername,
      String webAuthenticationPassword, int ringDurationMillis,
      int defaultSilenceDurationMillis) {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url)) {
      ColumnUpdater updater = new ColumnUpdater(conn, "Configuration", "ConfigurationId", 1);

      if (!Objects.equals(webAuthenticationUsername, this.webAuthenticationUsername)) {
        updater.updateColumn("WebAuthenticationUsername", webAuthenticationUsername);
      }
      if (!Objects.equals(webAuthenticationPassword, this.webAuthenticationPassword)) {
        updater.updateColumn("WebAuthenticationPassword", webAuthenticationPassword);
      }
      if (ringDurationMillis != this.ringDurationMillis) {
        updater.updateColumn("RingDurationMillis", ringDurationMillis);
      }
      if (defaultSilenceDurationMillis != this.defaultSilenceDurationMillis) {
        updater.updateColumn("DefaultSilenceDurationMillis", defaultSilenceDurationMillis);
      }
      reload();
      return true;
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
  }
}
