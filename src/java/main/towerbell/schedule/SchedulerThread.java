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
package towerbell.schedule;

import towerbell.ipc.DatabaseUpdateListener;
import towerbell.ringer.BellRinger;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SchedulerThread extends Thread implements DatabaseUpdateListener {
  private final Logger logger = Logger.getLogger(SchedulerThread.class.getName());

  private final ScheduleManager scheduleManager;
  private final BellRinger bellRinger;

  private final Object lock = new Object();
  private final AtomicBoolean isRescheduled = new AtomicBoolean(false);

  public SchedulerThread(ScheduleManager scheduleManager, BellRinger bellRinger) {
    this.scheduleManager = scheduleManager;
    this.bellRinger = bellRinger;
  }

  @Override
  public void onDatabaseUpdate() {
    // scheduleManager's schedules (and thus scheduleManager.getNextRing)
    //  are guaranteed to be updated at this point.
    isRescheduled.set(true);
    synchronized (lock) {
      lock.notifyAll();
    }
  }

  @Override
  public void run() {
    while (true) {
      ScheduledRing nextRing = scheduleManager.getNextRing();

      if (nextRing == null) {
        synchronized (lock) {
          logger.info("No schedules found.  Waiting forever.");
          try {
            lock.wait();
          } catch (InterruptedException e) {
            //
          }
        }
        continue;
      }

      logger.fine("Next ring: " + nextRing.ringTime() + " for " + nextRing.schedule());

      long millisUntilNextRing = ZonedDateTime.now().until(
          nextRing.ringTime(), java.time.temporal.ChronoUnit.MILLIS);

      if (millisUntilNextRing > 0) {
        try {
          logWaitTime(millisUntilNextRing);
          synchronized (lock) {
            lock.wait(millisUntilNextRing);
          }
        } catch (InterruptedException ignored) {
          //
        }
      }

      if (isRescheduled.getAndSet(false)) {
        // The schedule changed.
        logger.fine("The schedule changed; reload.");
        continue;
      }

      logger.fine("The schedule did not change.  Ring.");

      bellRinger.ring(nextRing);
    }
  }

  private void logWaitTime(long millis) {
    Duration d = Duration.ofMillis(millis);
    long days    = d.toDaysPart();
    long hours   = d.toHoursPart();
    long minutes = d.toMinutesPart();
    long seconds = d.toSecondsPart();
    String result = String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
    logger.fine("Waiting " + result + " to ring");
  }
}
