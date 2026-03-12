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

CREATE TABLE Configuration
(
  -- Not serial because we only expect a single row.
  ConfigurationId INT NOT NULL PRIMARY KEY,
  WebAuthenticationUsername STRING NOT NULL,
  WebAuthenticationPassword STRING NOT NULL,
  RingDurationMillis INT NOT NULL,
  DefaultSilenceDurationMillis INT NOT NULL,
  LastModifiedTime timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO Configuration (ConfigurationId,
                           WebAuthenticationUsername,
                           WebAuthenticationPassword,
                           RingDurationMillis,
                           DefaultSilenceDurationMillis,
                           LastModifiedTime)
VALUES (1, 'username', 'password', 3000, 3000, CURRENT_TIMESTAMP);



