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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScheduleFormatterTest {
  @Test
  public void testFormatPeriodMinutes() {
    ScheduleFormatter formatter = new ScheduleFormatter();
    assertEquals("", formatter.formatPeriodMinutes(0));
    assertEquals("1 minute", formatter.formatPeriodMinutes(1));
    assertEquals("5 minutes", formatter.formatPeriodMinutes(5));
    assertEquals("1 hour", formatter.formatPeriodMinutes(60));
    assertEquals("1 hour, 1 minute", formatter.formatPeriodMinutes(61));
    assertEquals("1 hour, 5 minutes", formatter.formatPeriodMinutes(65));
    assertEquals("2 hours", formatter.formatPeriodMinutes(120));
    assertEquals("2 hours, 1 minute", formatter.formatPeriodMinutes(121));
    assertEquals("2 hours, 5 minutes", formatter.formatPeriodMinutes(125));
  }

  @Test
  public void testFormatMilliseconds() {
    assertEquals("0", ScheduleFormatter.formatMilliseconds(0));
    assertEquals("1", ScheduleFormatter.formatMilliseconds(1000));
    assertEquals("0.25", ScheduleFormatter.formatMilliseconds(250));
    assertEquals("0.125", ScheduleFormatter.formatMilliseconds(125));
    assertEquals("0.5", ScheduleFormatter.formatMilliseconds(500));
    assertEquals("2", ScheduleFormatter.formatMilliseconds(2000));
  }
}