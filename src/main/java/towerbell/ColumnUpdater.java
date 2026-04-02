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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ColumnUpdater {
  private final Logger logger = Logger.getLogger(ColumnUpdater.class.getName());

  private final Connection connection;
  private final String tableName;
  private final String idColumnName;
  private final int id;

  public ColumnUpdater(Connection connection, String tableName, String idColumnName, int id) {
    this.connection = connection;
    this.tableName = tableName;
    this.idColumnName = idColumnName;
    this.id = id;
  }

  public void updateColumn(String columnName, String value) throws SQLException {
    String sql = String.format(
        "UPDATE %s SET %s = ?, LastModifiedTime = CURRENT_TIMESTAMP WHERE %s = %d",
        tableName, columnName, idColumnName, id);
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, value);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  public void updateColumn(String columnName, int value) throws SQLException {
    String sql = String.format(
        "UPDATE %s SET %s = ?, LastModifiedTime = CURRENT_TIMESTAMP WHERE %s = %d",
        tableName, columnName, idColumnName, id);
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, value);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  public void updateColumn(String columnName, boolean value) throws SQLException {
    updateColumn(columnName, value ? 1 : 0);
  }
}
