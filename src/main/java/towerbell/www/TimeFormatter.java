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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TimeFormatter {
  public String timeBetween(ZonedDateTime start, ZonedDateTime end) {
    long months = ChronoUnit.MONTHS.between(start, end);
    start = start.plusMonths(months);
    long weeks = ChronoUnit.WEEKS.between(start, end);
    start = start.plusWeeks(weeks);
    long days = ChronoUnit.DAYS.between(start, end);
    start = start.plusDays(days);
    long hours = ChronoUnit.HOURS.between(start, end);
    start = start.plusHours(hours);
    long minutes = ChronoUnit.MINUTES.between(start, end);
    start = start.plusMinutes(minutes);
    long seconds = ChronoUnit.SECONDS.between(start, end);

    List<String> parts = new ArrayList<>();
    if (months > 0)  parts.add(months + " month" + (months > 1 ? "s" : ""));
    if (weeks > 0)   parts.add(weeks + " week" + (weeks > 1 ? "s" : ""));
    if (days > 0)    parts.add(days + " day" + (days > 1 ? "s" : ""));
    if (hours > 0)   parts.add(hours + " hour" + (hours > 1 ? "s" : ""));
    if (minutes > 0) parts.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
    if (seconds > 0) parts.add(seconds + " second" + (seconds > 1 ? "s" : ""));

    return String.join(", ", parts);
  }

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm:ssa z");

  public String formatZonedDateTime(ZonedDateTime zonedDateTime) {
    String formatted = zonedDateTime.format(formatter);
    return formatted.replace("AM", "am").replace("PM", "pm");
  }
}
