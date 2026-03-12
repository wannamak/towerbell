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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScheduleFactory {
  public Schedule createFromResultSet(ResultSet rs) throws SQLException {
    int scheduleId = rs.getInt("ScheduleId");
    Set<DayOfWeek> daysOfWeek = new LinkedHashSet<>();
    if (rs.getInt("Sunday") > 0) {
      daysOfWeek.add(DayOfWeek.SUNDAY);
    }
    if (rs.getInt("Monday") > 0) {
      daysOfWeek.add(DayOfWeek.MONDAY);
    }
    if (rs.getInt("Tuesday") > 0) {
      daysOfWeek.add(DayOfWeek.TUESDAY);
    }
    if (rs.getInt("Wednesday") > 0) {
      daysOfWeek.add(DayOfWeek.WEDNESDAY);
    }
    if (rs.getInt("Thursday") > 0) {
      daysOfWeek.add(DayOfWeek.THURSDAY);
    }
    if (rs.getInt("Friday") > 0) {
      daysOfWeek.add(DayOfWeek.FRIDAY);
    }
    if (rs.getInt("Saturday") > 0) {
      daysOfWeek.add(DayOfWeek.SATURDAY);
    }

    int rawStartTime = rs.getInt("StartTime");
    LocalTime startTime = LocalTime.of(rawStartTime / 100, rawStartTime % 100);

    int rawEndTime = rs.getInt("EndTime");
    LocalTime endTime = rawEndTime == -1 ? null : LocalTime.of(rawEndTime / 100, rawEndTime % 100);

    int periodMinutes = rs.getInt("PeriodMinutes");

    int numRings = rs.getInt("NumRings");
    boolean isHourlyRing = rs.getInt("IsHourlyRing") > 0;

    int ringDurationMillis = rs.getInt("RingDurationMillis");
    int silenceDurationMillis = rs.getInt("SilenceDurationMillis");

    return new Schedule(scheduleId, daysOfWeek, startTime, endTime,
        periodMinutes, numRings, isHourlyRing, ringDurationMillis, silenceDurationMillis);
  }

  public Schedule createFromFields(
      int scheduleId,
      Set<DayOfWeek> daysOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      int periodMinutes,  // -1 for no period
      int numRings,  // 0 if isHourlyRing
      boolean isHourlyRing,
      int ringDurationMillis,
      int silenceDurationMillis) {
    return new Schedule(scheduleId, daysOfWeek, startTime, endTime, periodMinutes, numRings,
        isHourlyRing, ringDurationMillis, silenceDurationMillis);
  }
}
