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

import java.nio.file.Files;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import towerbell.Proto;
import towerbell.TowerBell;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.schedule.Schedule;
import towerbell.schedule.ScheduleManager;
import towerbell.schedule.ScheduledRing;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

public class HtmlServlet extends HttpServlet {
  private final Logger logger = Logger.getLogger(HtmlServlet.class.getName());
  private String mainTemplate;
  private String ringTemplate;
  private String scheduleTemplate;
  private String configTemplate;
  private String headerInclude;
  private final String templateDirectory;
  private final boolean developmentMode;
  private final ScheduleManager scheduleManager;
  private final ConfigurationManager configurationManager;
  private final SilenceManager silenceManager;
  private final ScheduleFormatter scheduleFormatter = new ScheduleFormatter();
  private final TimeFormatter timeFormatter = new TimeFormatter();
  private final PathUtil pathUtil = new PathUtil();

  public HtmlServlet(Proto.FixedConfig fixedConfig, ScheduleManager scheduleManager,
      ConfigurationManager configurationManager, SilenceManager silenceManager) {
    templateDirectory = fixedConfig.getTemplateDirectory();
    developmentMode = fixedConfig.getDevelopmentMode();
    this.scheduleManager = scheduleManager;
    this.configurationManager = configurationManager;
    this.silenceManager = silenceManager;
  }

  @Override
  public void init() throws ServletException {
    try {
      readTemplates();
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  private void readTemplates() throws IOException {
    mainTemplate = readTemplate("main-template.html");
    ringTemplate = readTemplate("ring-template.html");
    scheduleTemplate = readTemplate("schedule-template.html");
    configTemplate = readTemplate("config-template.html");
    headerInclude = readTemplate("header.inc");
  }

  private String readTemplate(String templateName) throws IOException {
    File templateFile = new File(templateDirectory, templateName);
    if (!templateFile.exists()) {
      logger.warning("Could not find template file " + templateFile.getAbsolutePath());
    }
    return Files.readString(templateFile.toPath(), StandardCharsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (developmentMode) {
      readTemplates();
    }
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();
    String path = req.getServletPath();

    if (path != null && path.equals("/add")) {
      out.println(expandAddTemplate());
    } else if (path != null && path.startsWith("/update/")) {
      Integer scheduleId = pathUtil.parseIdFromPathSuffix(path);
      Schedule schedule;
      if (scheduleId == null
          || (schedule = scheduleManager.getSchedule(scheduleId)) == null) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      out.println(expandUpdateTemplate(schedule));
    } else if (path != null && path.equals("/ring")) {
      out.println(expandRingTemplate());
    } else if (path != null && path.equals("/config")) {
      out.println(expandConfigTemplate());
    } else {
      out.println(expandMainTemplate());
    }
  }

  private String expandAddTemplate() {
    return scheduleTemplate
        .replace("$1_ADD_OR_EDIT", "Add New")
        .replace("$2_ADD_OR_UPDATE", "Add")
        .replace("$3_ADD_OR_UPDATE_FUNCTION_NAME", "onAdd")
        .replace("$4_FINAL_SCRIPT", getFinalScriptForAdd())
        .replace("$HEADER", headerInclude);
  }

  private String expandUpdateTemplate(Schedule schedule) {
    return scheduleTemplate
        .replace("$1_ADD_OR_EDIT", "Edit")
        .replace("$2_ADD_OR_UPDATE", "Update")
        .replace("$3_ADD_OR_UPDATE_FUNCTION_NAME", "onUpdate")
        .replace("$4_FINAL_SCRIPT", getFinalScriptForEdit(schedule))
        .replace("$HEADER", headerInclude);
  }

  private String expandRingTemplate() {
    return expandInfoBox(ringTemplate)
        .replace("$HEADER", headerInclude);
  }

  private String expandMainTemplate() {
    StringBuilder allSchedules = new StringBuilder();
    for (Schedule schedule : scheduleManager.getSchedules()) {
      allSchedules.append(scheduleFormatter.format(schedule));
    }

    return expandInfoBox(mainTemplate)
        .replace("$SCHEDULE", allSchedules.toString())
        .replace("$VERSION", String.format("Version %s", TowerBell.VERSION))
        .replace("$SILENCE_CHECKED", silenceManager.getLastSilenced() != null ? "checked" : "")
        .replace("$HEADER", headerInclude);
  }

  private String expandConfigTemplate() {
    return configTemplate
        .replace("$FINAL_SCRIPT", getFinalScriptForConfig())
        .replace("$HEADER", headerInclude);
  }

  private String expandInfoBox(String template) {
    ZonedDateTime now = ZonedDateTime.now();
    ScheduledRing nextScheduledRing = scheduleManager.getNextRing();
    ZonedDateTime nextRing = (nextScheduledRing != null && nextScheduledRing.ringTime() != null) ?
        nextScheduledRing.ringTime() : null;
    String nextRingOverrideString = getNextRingOverrideString(nextScheduledRing);

    return template
        .replace("$ISO_NOW",
            now.truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .replace("$ISO_NEXT_RING_MAYBE_EMPTY",
            nextRing == null ? "" :
                nextRing.truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .replace("$NEXT_RING_OVERRIDE_STRING_MAYBE_EMPTY",
            nextRingOverrideString == null ? "" : nextRingOverrideString);
  }

  private String getFinalScriptForConfig() {
    StringBuilder result = new StringBuilder();
    result.append(
        String.format("document.getElementById('webauthenticationusername').value = '%s';\n",
            configurationManager.getWebAuthenticationUsername()));
    result.append(
        String.format("document.getElementById('webauthenticationpassword').value = '%s';\n",
            configurationManager.getWebAuthenticationPassword()));
    result.append(
        String.format("document.getElementById('ringdurationmillis').value = '%s';\n",
            configurationManager.getRingDurationMillis()));
    result.append(
        String.format("document.getElementById('defaultsilencedurationmillis').value = '%s';\n",
            configurationManager.getDefaultSilenceDurationMillis()));
    return result.toString();
  }

  private String getFinalScriptForAdd() {
    return String.format("document.getElementById('silence').value = '%s';\n",
        ScheduleFormatter.formatMilliseconds(configurationManager.getDefaultSilenceDurationMillis()));
  }

  private String getFinalScriptForEdit(Schedule schedule) {
    StringBuilder result = new StringBuilder();
    for (DayOfWeek dayOfWeek : schedule.getDaysOfWeek()) {
      result.append(
          String.format("document.getElementById('%s').checked = true;\n",
              dayOfWeek.toString().toLowerCase()));
    }

    if (schedule.isHourlyRing()) {
      result.append("document.getElementById('ishourly').checked = true;\n");
    } else if (schedule.getNumRings() > 0) {
      result.append("document.getElementById('isspecified').checked = true;\n");
      result.append(
          String.format("document.getElementById('numrings').value = '%d';\n",
              schedule.getNumRings()));
    }

    if (schedule.getPeriodMinutes() > 0) {
      result.append(
          String.format("document.getElementById('periodminutes').value = '%d';\n",
              schedule.getPeriodMinutes()));
    }

    setTimeFields(result, schedule.getStartTime(),
        "starthour", "startminute", "startampm");
    setTimeFields(result, schedule.getEndTime(),
        "endhour", "endminute", "endampm");

    result.append(
        String.format("document.getElementById('silence').value = '%s';\n",
            ScheduleFormatter.formatMilliseconds(
                schedule.getSilenceDurationMillis())));

    result.append(
        String.format("document.getElementById('scheduleid').value = '%d';\n",
            schedule.getScheduleId()));

    return result.toString();
  }

  private void setTimeFields(StringBuilder builder, LocalTime time, String hourField,
      String minuteField, String ampmField) {
    if (time == null) {
      return;
    }
    boolean isAm = time.getHour() < 12;
    int hour = time.getHour() % 12;
    builder.append(
        String.format("document.getElementById('%s').value = '%02d';\n", hourField, hour));
    builder.append(
        String.format("document.getElementById('%s').value = '%02d';\n", minuteField,
            time.getMinute()));
    builder.append(
        String.format("document.getElementById('%s').value = '%s';\n", ampmField,
            isAm ? "am" : "pm"));
  }

  private String getNextRingOverrideString(ScheduledRing nextRing) {
    ZonedDateTime lastSilenced = silenceManager.getLastSilenced();
    if (lastSilenced != null) {
      return "Silenced since " + timeFormatter.formatZonedDateTime(lastSilenced);
    }
    if (nextRing == null || nextRing.ringTime() == null) {
      return "No rings scheduled";
    } else {
      return null;
    }
  }
}
