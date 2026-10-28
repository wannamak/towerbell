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

CREATE TABLE Tune
(
  TuneId INTEGER PRIMARY KEY,
  TuneName VARCHAR(255) NOT NULL,
  TempoNoteValue INTEGER NOT NULL,
  BeatsPerMinute INTEGER NOT NULL,
  KeySignatureNote VARCHAR(1) NOT NULL CHECK (KeySignatureNote IN ('C', 'D', 'E', 'F', 'G', 'A', 'B')),
  KeySignatureSuffix VARCHAR(5) NOT NULL CHECK (KeySignatureSuffix IN ('minor', 'major', '')),
  LilyPondRelativeNote VARCHAR(5),
  TuneNotes VARCHAR(1024) NOT NULL
);

INSERT INTO Tune (TuneId, TuneName, TempoNoteValue, BeatsPerMinute, KeySignatureNote, KeySignatureSuffix, LilyPondRelativeNote, TuneNotes)
  VALUES (0, "Westminster First Quarter", 4, 60, "E", "major", "g'", "gs4 fs4 e4 b2.");
