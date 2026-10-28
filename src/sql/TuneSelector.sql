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

CREATE TABLE TuneSelector
(
  TuneSelectorId INTEGER PRIMARY KEY,
  TuneSelectorName VARCHAR(255) NOT NULL,
  IsRandom INT NOT NULL DEFAULT 0,
  IsHourly INT NOT NULL DEFAULT 0,
  FirstQuarterTuneId INT,
  SecondQuarterTuneId INT,
  ThirdQuarterTuneId INT,
  HourlyTuneId INT,
  LastPlayedTuneId INT
);

INSERT INTO TuneSelector (TuneSelectorId, TuneSelectorName, IsRandom, IsHourly, FirstQuarterTuneId,
                          SecondQuarterTuneId, ThirdQuarterTuneId, HourlyTuneId, LastPlayedTuneId)
  VALUES (0, "Westminster Chimes", 0, 1, 0, NULL, NULL, NULL, NULL);
