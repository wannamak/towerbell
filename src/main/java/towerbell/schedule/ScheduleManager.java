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
package towerbell.schedule;

import towerbell.ColumnUpdater;
import towerbell.ipc.DatabaseUpdateListener;
import towerbell.Proto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduleManager {
  private final Logger logger = Logger.getLogger(ScheduleManager.class.getName());
  private final Proto.FixedConfig fixedConfig;
  private final ScheduleFactory scheduleFactory = new ScheduleFactory();
  private DatabaseUpdateListener databaseUpdateListener;

  // ScheduleId -> unexpanded Schedule.  Refreshed on reload()
  private final Map<Integer, Schedule> schedules = new LinkedHashMap<>();

  public ScheduleManager(Proto.FixedConfig fixedConfig) {
    this.fixedConfig = fixedConfig;
    reload();
  }

  public synchronized void setDatabaseUpdateListener(DatabaseUpdateListener databaseUpdateListener) {
    this.databaseUpdateListener = databaseUpdateListener;
  }

  public synchronized ScheduledRing getNextRing() {
    return computeNextRing(ZonedDateTime.now());
  }

  public synchronized List<Schedule> getSchedules() {
    return new ArrayList<>(schedules.values());
  }

  public synchronized Schedule getSchedule(int scheduleId) {
    return schedules.get(scheduleId);
  }

  public synchronized void deleteSchedule(int scheduleId) {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    String sql = "DELETE FROM Schedule WHERE ScheduleId = ?";
    try (Connection conn = DriverManager.getConnection(url);
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, scheduleId);
      stmt.executeUpdate();
      reload();
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  // Returns true on success.
  public synchronized boolean updateSchedule(Schedule newSchedule) {
    int scheduleId = newSchedule.getScheduleId();
    Schedule oldSchedule = schedules.get(scheduleId);
    if (oldSchedule == null) {
      logger.warning("Schedule " + scheduleId + " not found");
      return false;
    }

    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url)) {
      ColumnUpdater updater = new ColumnUpdater(
          conn, "Schedule", "ScheduleId", scheduleId);

      for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
        if (oldSchedule.getDaysOfWeek().contains(dayOfWeek) ^
            newSchedule.getDaysOfWeek().contains(dayOfWeek)) {
          updater.updateColumn(dayOfWeek.name(), newSchedule.getDaysOfWeek().contains(dayOfWeek));
        }
      }

      if (!Objects.equals(oldSchedule.getStartTime(), newSchedule.getStartTime())) {
        updater.updateColumn("StartTime", newSchedule.getStartTime().getHour() * 100
            + newSchedule.getStartTime().getMinute());
      }
      if (!Objects.equals(oldSchedule.getEndTime(), newSchedule.getEndTime())) {
        if (newSchedule.getEndTime() == null) {
          updater.updateColumn("EndTime", -1);
        } else {
          updater.updateColumn("EndTime", newSchedule.getEndTime().getHour() * 100
              + newSchedule.getEndTime().getMinute());
        }
      }

      if (oldSchedule.getPeriodMinutes() != newSchedule.getPeriodMinutes()) {
        updater.updateColumn("PeriodMinutes", newSchedule.getPeriodMinutes());
      }
      if (oldSchedule.getNumRings() != newSchedule.getNumRings()) {
        updater.updateColumn("NumRings", newSchedule.getNumRings());
      }
      if (oldSchedule.isHourlyRing() != newSchedule.isHourlyRing()) {
        updater.updateColumn("IsHourlyRing", newSchedule.isHourlyRing());
      }
      if (oldSchedule.getRingDurationMillis() != newSchedule.getRingDurationMillis()) {
        updater.updateColumn("RingDurationMillis", newSchedule.getRingDurationMillis());
      }
      if (oldSchedule.getSilenceDurationMillis() != newSchedule.getSilenceDurationMillis()) {
        updater.updateColumn("SilenceDurationMillis", newSchedule.getSilenceDurationMillis());
      }
      if (oldSchedule.isEnabled() != newSchedule.isEnabled()) {
        updater.updateColumn("IsEnabled", newSchedule.isEnabled());
      }
      reload();
      return true;
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
  }

  // Returns true on success.
  public synchronized boolean updateEnabled(int scheduleId, boolean isEnabled) {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url)) {
      ColumnUpdater updater = new ColumnUpdater(
          conn, "Schedule", "ScheduleId", scheduleId);
      updater.updateColumn("IsEnabled", isEnabled);
      reload();
      return true;
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
  }

  public synchronized void addNewSchedule(Schedule schedule) {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    String sql = "INSERT INTO Schedule (Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, " +
        "Saturday, StartTime, EndTime, PeriodMinutes, NumRings, IsHourlyRing, " +
        "RingDurationMillis, SilenceDurationMillis, IsEnabled) VALUES (?, ?, ?, ?, ?, ?, " +
        "?, ?, ?, ?, ?, ?, " +
        "?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(url);
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      int index = 1;
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.SUNDAY));
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.MONDAY));
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.TUESDAY));
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.WEDNESDAY));
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.THURSDAY));
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.FRIDAY));
      stmt.setBoolean(index++, schedule.getDaysOfWeek().contains(DayOfWeek.SATURDAY));
      stmt.setInt(index++, schedule.getStartTime().getHour() * 100
          + schedule.getStartTime().getMinute());
      if (schedule.getEndTime() == null) {
        stmt.setInt(index++, -1);
      } else {
        stmt.setInt(index++, schedule.getEndTime().getHour() * 100
            + schedule.getEndTime().getMinute());
      }
      stmt.setInt(index++, schedule.getPeriodMinutes());
      stmt.setInt(index++, schedule.getNumRings());
      stmt.setBoolean(index++, schedule.isHourlyRing());
      stmt.setInt(index++, schedule.getRingDurationMillis());
      stmt.setInt(index++, schedule.getSilenceDurationMillis());
      stmt.setBoolean(index++, schedule.isEnabled());
      stmt.executeUpdate();
      reload();
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  private void reload() {
    String url = String.format("jdbc:sqlite:%s", fixedConfig.getDatabasePath());
    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery("SELECT * FROM Schedule")) {
        schedules.clear();
        while (rs.next()) {
          Schedule schedule = scheduleFactory.createFromResultSet(rs);
          schedules.put(schedule.getScheduleId(), schedule);
        }
      }
      if (databaseUpdateListener != null) {
        databaseUpdateListener.onDatabaseUpdate();
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  private ScheduledRing computeNextRing(ZonedDateTime now) {
    ZonedDateTime computedNextRing = null;
    Schedule computedNextRingSchedule = null;

    for (Schedule schedule : schedules.values()) {
      if (schedule.getDaysOfWeek().isEmpty() || !schedule.isEnabled()) {
        continue;
      }
      ZonedDateTime nextRing = computeNextRingOfDay(schedule, now);
      logger.fine("nextRing = " + nextRing + " for " + schedule + " at " + now);
      for (int i = 1; nextRing == null && i <= 7; i++) {
        nextRing = computeFirstRingOfDay(schedule, now.toLocalDate().plusDays(i), now.getZone());
      }
      if (nextRing != null) {
        if (computedNextRing == null) {
          computedNextRing = nextRing;
          computedNextRingSchedule = schedule;
          logger.fine("First next ring found at " + computedNextRing);
        } else {
          if (nextRing.isBefore(computedNextRing)) {
            computedNextRing = nextRing;
            computedNextRingSchedule = schedule;
            logger.fine("A sooner next ring found at " + computedNextRing);
          }
        }
      }
    }

    return new ScheduledRing(computedNextRing, computedNextRingSchedule);
  }

  private ZonedDateTime computeNextRingOfDay(Schedule schedule, ZonedDateTime now) {
    if (!schedule.getDaysOfWeek().contains(now.getDayOfWeek())) {
      return null;
    }
    LocalTime localTime = now.toLocalTime();
    int minutesAdded = 0;
    while (schedule.getEndTime() == null
        || !schedule.getStartTime().plusMinutes(minutesAdded).isAfter(schedule.getEndTime())) {
      LocalTime modifiedStart = schedule.getStartTime().plusMinutes(minutesAdded);
      if (!localTime.isAfter(modifiedStart)) {
        return ZonedDateTime.of(now.toLocalDate(), modifiedStart, now.getZone());
      }
      if (schedule.getEndTime() == null || schedule.getPeriodMinutes() <= 0) {
        return null;
      }
      minutesAdded += schedule.getPeriodMinutes();
    }
    return null;
  }

  private ZonedDateTime computeFirstRingOfDay(Schedule schedule, LocalDate today, ZoneId zoneId) {
    if (!schedule.getDaysOfWeek().contains(today.getDayOfWeek())) {
      return null;
    }
    return ZonedDateTime.of(today, schedule.getStartTime(), zoneId);
  }
}
