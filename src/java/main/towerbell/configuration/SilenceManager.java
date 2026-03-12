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

import towerbell.Proto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SilenceManager {
  private final Logger logger = Logger.getLogger(SilenceManager.class.getName());
  private final Proto.FixedConfig fixedConfig;

  private ZonedDateTime lastSilenced = null;

  public SilenceManager(Proto.FixedConfig fixedConfig) {
    this.fixedConfig = fixedConfig;
    reload();
  }

  public synchronized ZonedDateTime getLastSilenced() {
    return lastSilenced;
  }

  public synchronized boolean isSilenced() {
    return lastSilenced != null;
  }

  public synchronized void reload() {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(
          "SELECT LastSilencedZonedDateTime FROM Silence WHERE SilenceId = 1")) {
        if (rs.next()) {
          String dataStr = rs.getString("LastSilencedZonedDateTime");
          if (dataStr == null) {
            lastSilenced = null;
          } else {
            lastSilenced = ZonedDateTime.parse(dataStr);
          }
        }
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  public synchronized boolean update(boolean isSilenced) {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url)) {
      String sql = "UPDATE Silence SET LastSilencedZonedDateTime = ? WHERE SilenceId = 1";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        if (isSilenced) {
          stmt.setString(1, ZonedDateTime.now().toString());
        } else {
          stmt.setNull(1, java.sql.Types.VARCHAR);
        }
        stmt.executeUpdate();
      }
      reload();
      return true;
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
  }
}
