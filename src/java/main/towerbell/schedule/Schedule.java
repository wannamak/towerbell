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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public class Schedule {
  private final int scheduleId;
  private final Set<DayOfWeek> daysOfWeek;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final int periodMinutes;
  private final int numRings;
  private final boolean isHourlyRing;
  private final int ringDurationMillis;
  private final int silenceDurationMillis;
  private final boolean isEnabled;

  public int getScheduleId() {
    return scheduleId;
  }

  public Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  // null for no end time.
  // inclusive.
  public LocalTime getEndTime() {
    return endTime;
  }

  // 0 for no period.
  public int getPeriodMinutes() {
    return periodMinutes;
  }

  // could be 0.
  public int getNumRings() {
    return numRings;
  }

  public boolean isHourlyRing() {
    return isHourlyRing;
  }

  public int getRingDurationMillis() {
    return ringDurationMillis;
  }

  public int getSilenceDurationMillis() {
    return silenceDurationMillis;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  Schedule(int scheduleId, Set<DayOfWeek> daysOfWeek, LocalTime startTime,
      LocalTime endTime, int periodMinutes,
      int numRings, boolean isHourlyRing,
      int ringDurationMillis, int silenceDurationMillis, boolean isEnabled) {
    this.scheduleId = scheduleId;
    this.daysOfWeek = daysOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.periodMinutes = periodMinutes;
    this.numRings = numRings;
    this.isHourlyRing = isHourlyRing;
    this.ringDurationMillis = ringDurationMillis;
    this.silenceDurationMillis = silenceDurationMillis;
    this.isEnabled = isEnabled;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Schedule schedule = (Schedule) o;
    return scheduleId == schedule.scheduleId
        && periodMinutes == schedule.periodMinutes
        && numRings == schedule.numRings
        && isHourlyRing == schedule.isHourlyRing
        && ringDurationMillis == schedule.ringDurationMillis
        && silenceDurationMillis == schedule.silenceDurationMillis
        && Objects.equals(daysOfWeek, schedule.daysOfWeek)
        && Objects.equals(startTime, schedule.startTime)
        && Objects.equals(endTime, schedule.endTime)
        && isEnabled == schedule.isEnabled;
  }

  @Override
  public int hashCode() {
    return Objects.hash(scheduleId, daysOfWeek, startTime, endTime,
        periodMinutes, numRings, isHourlyRing, ringDurationMillis,
        silenceDurationMillis, isEnabled);
  }

  @Override
  public String toString() {
    return "Schedule{" +
        "scheduleId=" + scheduleId +
        ", daysOfWeek=" + daysOfWeek +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", periodMinutes=" + periodMinutes +
        ", numRings=" + numRings +
        ", isHourlyRing=" + isHourlyRing +
        ", ringDurationMillis=" + ringDurationMillis +
        ", silenceDurationMillis=" + silenceDurationMillis +
        ", isEnabled=" + isEnabled +
        '}';
  }
}
