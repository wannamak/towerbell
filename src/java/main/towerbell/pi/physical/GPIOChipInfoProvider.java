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
package towerbell.pi.physical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

/**
 * Provides a way to figure out which /dev/gpiochipN the program should use.
 * The label is set in the configuration.
 * Some manual tools use the default label below.
 */
public class GPIOChipInfoProvider {
  public static final String DEFAULT_RASPBERRY_PI_DEVICE_LABEL = "pinctrl-bcm2711";

  public static class GPIOChipInfo {
    public final String name;
    public final String label;
    public final int numLines;

    public GPIOChipInfo(String name, String label, int numLines) {
      this.name = name;
      this.label = label;
      this.numLines = numLines;
    }

    @Override
    public String toString() {
      return String.format("name:[%s] label:[%s] numLines:[%d]", name, label, numLines);
    }
  }

  /**
   * Given a label, iterates all /dev/gpiochip devices trying to find the one which matches.
   * @return the matching device or null
   */
  public Path getDevicePathForLabel(String targetLabel) throws IOException {
    Path devDirectory = new File("/dev").toPath();
    final PathMatcher filter = devDirectory.getFileSystem().getPathMatcher("glob:**/gpiochip*");
    try (Stream<Path> stream = Files.list(devDirectory)) {
      for (Path path : stream.filter(filter::matches).toList()) {
        GPIOChipInfo info = getGPIOChipInfo(path);
        if (info != null && info.label.equals(targetLabel)) {
          return path;
        }
      }
    }
    return null;
  }

  public GPIOChipInfo getGPIOChipInfo(Path devicePath) {
    return getGPIOChipInfoInternal(devicePath.toString());
  }

  native private GPIOChipInfo getGPIOChipInfoInternal(String devicePath);
}
