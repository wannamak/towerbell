-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

CREATE TABLE Schedule
(
  ScheduleId INTEGER PRIMARY KEY,
  Sunday INT NOT NULL DEFAULT 0,
  Monday INT NOT NULL DEFAULT 0,
  Tuesday INT NOT NULL DEFAULT 0,
  Wednesday INT NOT NULL DEFAULT 0,
  Thursday INT NOT NULL DEFAULT 0,
  Friday INT NOT NULL DEFAULT 0,
  Saturday INT NOT NULL DEFAULT 0,
  StartTime INT NOT NULL,
  EndTime INT NOT NULL DEFAULT -1,
  PeriodMinutes INT NOT NULL DEFAULT 0,
  NumRings INT NOT NULL DEFAULT 1,
  IsHourlyRing INT NOT NULL DEFAULT 0,
  RingDurationMillis INT NOT NULL,
  SilenceDurationMillis INT NOT NULL,
  LastModifiedTime timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  IsEnabled INT NOT NULL DEFAULT 1
);

INSERT INTO Schedule (Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday,
 StartTime, EndTime, PeriodMinutes, NumRings, IsHourlyRing,
 RingDurationMillis, SilenceDurationMillis)
VALUES (0, 1, 1, 1, 1, 1, 1, 0900, 1800, 60, 0, 1, 3000, 3000),
       (0, 1, 1, 1, 1, 1, 1, 0930, 1730, 60, 1, 0, 3000, 3000),
       (1, 0, 0, 0, 0, 0, 0, 1300, 1800, 60, 0, 1, 3000, 3000),
       (1, 0, 0, 0, 0, 0, 0, 1330, 1730, 60, 1, 0, 3000, 3000),
       (1, 0, 0, 0, 0, 0, 0, 0820,   -1,  0, 3, 0, 3000, 3000),
       (1, 0, 0, 0, 0, 0, 0, 1050,   -1,  0, 3, 0, 3000, 3000);

-- ALTER TABLE Schedule ADD COLUMN IsEnabled INT NOT NULL DEFAULT 1;
