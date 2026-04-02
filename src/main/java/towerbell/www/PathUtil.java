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

import java.util.logging.Logger;

public class PathUtil {
  private final Logger logger = Logger.getLogger(PathUtil.class.getName());

  Integer parseIdFromPathSuffix(String path) {
    int index = path.lastIndexOf('/');
    if (index == -1) {
      return null;
    }
    if (path.length() <= index + 1) {
      return null;
    }
    String id = path.substring(path.lastIndexOf('/') + 1);
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      logger.warning("Invalid schedule id in path: " + path);
      return null;
    }
  }
}
