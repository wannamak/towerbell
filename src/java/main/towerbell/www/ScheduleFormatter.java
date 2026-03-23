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
package towerbell.www;

import towerbell.schedule.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ScheduleFormatter {
  public static final List<DayOfWeek> DAYS_OF_WEEK = List.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY,
      DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
      DayOfWeek.SATURDAY);
  public static final DateTimeFormatter localTimeFormatter =
      DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

  public String format(Schedule schedule) {
    StringBuilder sb = new StringBuilder();
    sb.append("<div class=\"row\">\n");

    sb.append(
        String.format("  <div class=\"cell checkbox\">" +
                "<input type=\"checkbox\" %s onChange=\"onEnableChanged(%d, this);\"></div>\n",
            schedule.isEnabled() ? "checked" : "", schedule.getScheduleId()));

    for (DayOfWeek dayOfWeek : DAYS_OF_WEEK) {
      sb.append(
          String.format("  <div class=\"cell checkbox\">" +
                  "<input type=\"checkbox\" %s disabled></div>\n",
              schedule.getDaysOfWeek().contains(dayOfWeek) ? "checked" : ""));
    }

    sb.append("  <div class=\"cell\" style=\"text-align: right;\">");
    sb.append(formatLocalTime(schedule.getStartTime()));
    sb.append("</div>\n");

    sb.append("  <div class=\"cell\">");
    if (schedule.getNumRings() > 0) {
      int numRings = schedule.getNumRings();
      sb.append(numRings);
      sb.append(" ring");
      if (numRings > 1) {
        sb.append("s");
      }
    } else if (schedule.isHourlyRing()) {
      sb.append("hour-number");
    }
    sb.append("</div>\n");

    sb.append("  <div class=\"cell\">");
    if (schedule.getPeriodMinutes() > 0) {
      sb.append(formatPeriodMinutes(schedule.getPeriodMinutes()));
    }
    sb.append("</div>\n");

    sb.append("  <div class=\"cell\" style=\"text-align: right;\">");
    if (schedule.getEndTime() != null) {
      sb.append(formatLocalTime(schedule.getEndTime()));
    }
    sb.append("</div>\n");

    sb.append("  <div class=\"cell\">");
    sb.append(formatMilliseconds(schedule.getSilenceDurationMillis()));
    sb.append(" seconds");
    sb.append("</div>\n");

    sb.append("  <div class=\"cell\">");
    sb.append("<a onclick=\"onUpdate(" + schedule.getScheduleId() + ");\">");
    sb.append("<img alt=\"Update\" src=\"static/update.png\">");
    sb.append("</a>");
    sb.append("<a onclick=\"onDelete(" + schedule.getScheduleId() + ");\">");
    sb.append("<img alt=\"Delete\" src=\"static/delete.png\">");
    sb.append("</a>");
    sb.append("</div>\n");

    sb.append("</div>\n");
    return sb.toString();
  }

  protected String formatLocalTime(LocalTime localTime) {
    return localTime.format(localTimeFormatter).toLowerCase();
  }

  protected String formatPeriodMinutes(int periodMinutes) {
    if (periodMinutes == 0) {
      return "";
    } else if (periodMinutes == 1) {
      return "1 minute";
    } else if (periodMinutes < 60) {
      return periodMinutes + " minutes";
    } else {
      int numHours = periodMinutes / 60;
      periodMinutes %= 60;
      if (periodMinutes == 0) {
        return numHours + " hour" + (numHours == 1 ? "" : "s");
      } else {
        return numHours + " hour" + (numHours == 1 ? "" : "s") + ", "
            + periodMinutes + " minute" + (periodMinutes == 1 ? "" : "s");
      }
    }
  }

  protected static String formatMilliseconds(int millis) {
    String raw = String.format("%f", millis / 1000.0);
    return raw.replaceAll("0*$", "").replaceAll("\\.$", "");
  }
}
