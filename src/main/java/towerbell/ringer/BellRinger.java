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
package towerbell.ringer;

import towerbell.Proto;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.music.Pitch;
import towerbell.schedule.ScheduleManager;
import towerbell.schedule.ScheduledRing;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BellRinger {
  private final Logger logger = Logger.getLogger(BellRinger.class.getName());

  protected final Proto.FixedConfig fixedConfig;
  private final ConfigurationManager configurationManager;
  private final SilenceManager silenceManager;
  private final ReentrantLock lock = new ReentrantLock();

  protected abstract void beginRingSequence();
  protected abstract void beginStrike(Pitch pitch);
  protected abstract void endStrike(Pitch pitch);
  protected abstract void beginRetract(Pitch pitch);
  protected abstract void endRetract(Pitch pitch);
  protected abstract void endRingSequence();

  public static class Result {
    public final boolean success;
    public final String errorMessage;

    private Result(boolean success, String errorMessage) {
      this.success = success;
      this.errorMessage = errorMessage;
    }

    public static Result success() {
      return new Result(true, null);
    }

    public static Result failure(String errorMessage) {
      return new Result(false, errorMessage);
    }
  }

  public BellRinger(
      Proto.FixedConfig fixedConfig,
      ConfigurationManager configurationManager,
      SilenceManager silenceManager)
      throws IOException {
    this.fixedConfig = fixedConfig;
    this.configurationManager = configurationManager;
    this.silenceManager = silenceManager;
  }

  public Result ring(ScheduledRing scheduledRing) {
    if (silenceManager.isSilenced()) {
      return Result.failure("The chimes are silenced.");
    }
    if (!lock.tryLock()) {
      return Result.failure("The chimes are already ringing.");
    }
    try {
      return ringInternal(scheduledRing);
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return Result.failure("An unexpected error occurred.");
    } finally {
      lock.unlock();
    }
  }

  public Result singleRing(Pitch pitch, int strikeDurationMs, int pauseDurationMs, int retractDurationMs) {
    if (silenceManager.isSilenced()) {
      return Result.failure("The chimes are silenced.");
    }
    if (!lock.tryLock()) {
      return Result.failure("The chimes are already ringing.");
    }
    try {
      return singleRingInternal(pitch, strikeDurationMs, pauseDurationMs, retractDurationMs);
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return Result.failure("An unexpected error occurred.");
    } finally {
      lock.unlock();
    }
  }

  private static final Pitch SINGLE_BELL_ARBITRARY_PITCH = Pitch.fromString("C1");

  private Result ringInternal(ScheduledRing scheduledRing) {
    logger.fine("Ring: " + scheduledRing);
    beginRingSequence();
    try {
      int numRings = getNumRings(scheduledRing);
      for (int i = 0; i < numRings; i++) {
        if (silenceManager.isSilenced()) {
          logger.fine("The chimes are silenced.");
          return Result.failure("The chimes are silenced.");
        }
        logger.finest("Begin ring " + (i + 1) + " of " + numRings);
        beginStrike(SINGLE_BELL_ARBITRARY_PITCH);
        threadSleep(scheduledRing.schedule().getRingDurationMillis());
        logger.finest("End ring " + (i + 1) + " of " + numRings);
        endStrike(SINGLE_BELL_ARBITRARY_PITCH);
        if (i < numRings - 1) {
          threadSleep(scheduledRing.schedule().getSilenceDurationMillis());
        }
      }
    } finally {
      endRingSequence();
    }
    return Result.success();
  }

  private int getNumRings(ScheduledRing scheduledRing) {
    if (scheduledRing.schedule().isHourlyRing()) {
      int hourOfDay = scheduledRing.ringTime().getHour();
      if (hourOfDay == 0) {
        return 12;
      } else if (hourOfDay > 12) {
        return hourOfDay % 12;
      } else {
        return hourOfDay;
      }
    } else {
      return scheduledRing.schedule().getNumRings();
    }
  }

  private Result singleRingInternal(Pitch pitch, int strikeDurationMs, int pauseDurationMs, int retractDurationMs) {
    logger.fine("Single ring");
    beginRingSequence();
    try {
      logger.finer("Begin single strike " + pitch);
      beginStrike(pitch);
      threadSleep(strikeDurationMs);

      logger.finer("End single strike " + pitch);
      endStrike(pitch);

      if (retractDurationMs > 0) {
        threadSleep(pauseDurationMs);

        logger.finer("Begin single retract " + pitch);
        beginRetract(pitch);
        threadSleep(retractDurationMs);

        logger.finer("End single retract " + pitch);
        endRetract(pitch);
      }
    } finally {
      endRingSequence();
    }
    return Result.success();
  }

  private void threadSleep(int millis) {
    try {
      logger.finest("Sleeping for " + millis + "ms");
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      logger.log(Level.WARNING, "Interrupted", e);
    }
  }
}
