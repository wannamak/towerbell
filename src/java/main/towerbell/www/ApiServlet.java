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

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Credential;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.ringer.BellRinger;
import towerbell.schedule.Schedule;
import towerbell.schedule.ScheduleFactory;
import towerbell.schedule.ScheduleManager;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static towerbell.www.WebServerThread.AUTH_ROLE;

public class ApiServlet extends HttpServlet {
  private final Logger logger = Logger.getLogger(ApiServlet.class.getName());

  private final ScheduleManager scheduleManager;
  private final ConfigurationManager configurationManager;
  private final SilenceManager silenceManager;
  private final HashLoginService loginService;
  private final BellRinger bellRinger;
  private final ScheduleFactory scheduleFactory;

  public ApiServlet(
      ScheduleManager scheduleManager,
      ConfigurationManager configurationManager,
      SilenceManager silenceManager,
      HashLoginService loginService,
      BellRinger bellRinger) {
    this.scheduleManager = scheduleManager;
    this.configurationManager = configurationManager;
    this.silenceManager = silenceManager;
    this.loginService = loginService;
    this.bellRinger = bellRinger;
    this.scheduleFactory = new ScheduleFactory();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    try {
      if (req.getPathInfo().equals("/delete")) {
        handleDelete(req, res);
      } else if (req.getPathInfo().equals("/add")) {
        handleAddOrUpdate(req, res, true);
      } else if (req.getPathInfo().equals("/update")) {
        handleAddOrUpdate(req, res, false);
      } else if (req.getPathInfo().equals("/ring")) {
        handleRing(req, res);
      } else if (req.getPathInfo().equals("/config")) {
        handleConfigUpdate(req, res);
      } else if (req.getPathInfo().equals("/silence")) {
        handleSilence(req, res);
      } else if (req.getPathInfo().equals("/enable")) {
        handleEnabled(req, res);
      } else {
        logger.warning("Unhandled path: " + req.getPathInfo());
        sendBadRequest(res, "An unexpected error occurred.", List.of());
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      sendBadRequest(res, "An unexpected error occurred.", List.of());
    }
  }

  private void handleDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
    JSONObject obj = new JSONObject(new JSONTokener(req.getInputStream()));
    int id = obj.getInt("id");
    scheduleManager.deleteSchedule(id);
    res.setStatus(HttpServletResponse.SC_OK);
  }

  private void handleRing(HttpServletRequest req, HttpServletResponse res) throws IOException {
    BellRinger.Result result = bellRinger.singleRing();
    if (result.success) {
      res.setStatus(HttpServletResponse.SC_OK);
    } else {
      sendBadRequest(res, result.errorMessage, List.of());
    }
  }

  private void handleAddOrUpdate(HttpServletRequest req, HttpServletResponse res,
      boolean isAdd) throws IOException {
    JSONObject obj = new JSONObject(new JSONTokener(req.getInputStream()));
    ValidationResult<Schedule> validationResult = validateSchedule(obj);
    if (validationResult.object != null) {
      if (isAdd) {
        scheduleManager.addNewSchedule(validationResult.object);
      } else {
        if (!scheduleManager.updateSchedule(validationResult.object)) {
          sendBadRequest(res, "An unexpected error occurred.", List.of());
          return;
        }
      }
      res.setStatus(HttpServletResponse.SC_OK);
    } else {
      sendBadRequest(res, validationResult.errorMessage, validationResult.errorFields);
    }
  }

  private record BellConfig(
      String webAuthenticationUsername,
      String webAuthenticationPassword,
      int ringDurationMillis,
      int defaultSilenceDurationMillis) { }

  private void handleConfigUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
    JSONObject obj = new JSONObject(new JSONTokener(req.getInputStream()));

    ValidationResult<BellConfig> validationResult = validateConfig(obj);
    if (validationResult.object != null) {
      BellConfig config = validationResult.object;
      boolean isAuthChange =
          !config.webAuthenticationUsername.equals(configurationManager.getWebAuthenticationUsername()) ||
          !config.webAuthenticationPassword.equals(configurationManager.getWebAuthenticationPassword());
      if (!configurationManager.update(
          config.webAuthenticationUsername,
          config.webAuthenticationPassword,
          config.ringDurationMillis,
          config.defaultSilenceDurationMillis)) {
        sendBadRequest(res, "An unexpected error occurred.", List.of());
        return;
      }

      if (isAuthChange) {
        UserStore userStore = new UserStore();
        userStore.addUser(
            configurationManager.getWebAuthenticationUsername(),
            Credential.getCredential(configurationManager.getWebAuthenticationPassword()),
            AUTH_ROLE);
        loginService.setUserStore(userStore);
      }

      res.setStatus(HttpServletResponse.SC_OK);
    } else {
      sendBadRequest(res, validationResult.errorMessage, validationResult.errorFields);
    }
  }

  private void handleSilence(HttpServletRequest req, HttpServletResponse res) throws IOException {
    JSONObject obj = new JSONObject(new JSONTokener(req.getInputStream()));
    try {
      boolean isSilenced = obj.getBoolean("silence");
      silenceManager.update(isSilenced);
      res.setStatus(HttpServletResponse.SC_OK);
    } catch (JSONException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      sendBadRequest(res, "An unexpected error occurred.", List.of());
    }
  }

  private void handleEnabled(HttpServletRequest req, HttpServletResponse res) throws IOException {
    JSONObject obj = new JSONObject(new JSONTokener(req.getInputStream()));
    int scheduleId = obj.getInt("scheduleId");
    boolean isEnabled = obj.getBoolean("isEnabled");
    if (scheduleManager.updateEnabled(scheduleId, isEnabled)) {
      res.setStatus(HttpServletResponse.SC_OK);
    } else {
      sendBadRequest(res, "An unexpected error occurred.", List.of());
    }
  }

  private void sendBadRequest(HttpServletResponse res, String errorMessage, List<String> errorFields)
      throws IOException {
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    JSONObject response = new JSONObject();
    response.put("errorFields", errorFields);
    response.put("errorMessage", errorMessage);

    res.setContentType("application/json");
    res.getWriter().write(response.toString());
  }

  private static class ValidationResult<T> {
    T object;
    List<String> errorFields;
    String errorMessage;

    static <T> ValidationResult<T> success(T object) {
      ValidationResult<T> result = new ValidationResult<>();
      result.object = object;
      return result;
    }

    static <T> ValidationResult<T> failure(String errorMessage, String... errorFields) {
      ValidationResult<T> result = new ValidationResult<>();
      result.errorFields = errorFields == null ? new ArrayList<>() : List.of(errorFields);
      result.errorMessage = errorMessage;
      return result;
    }

    boolean isFailure() {
      return errorMessage != null;
    }

    <S> ValidationResult<S> asFailure() {
      ValidationResult<S> result = new ValidationResult<>();
      result.errorFields = errorFields;
      result.errorMessage = errorMessage;
      return result;
    }
  }

  private ValidationResult<Schedule> validateSchedule(JSONObject obj) {
    try {
      Set<DayOfWeek> daysOfWeek = new HashSet<>();
      for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
        if (obj.has(dayOfWeek.toString().toLowerCase())
            && obj.getString(dayOfWeek.toString().toLowerCase()).equals("on")) {
          daysOfWeek.add(dayOfWeek);
        }
      }

      if (daysOfWeek.isEmpty()) {
        return ValidationResult.failure("Please select at least one day of the week.",
            "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday");
      }

      ValidationResult<LocalTime> startTimeResult = buildLocalTime(obj, "starthour", "startminute", "startampm");
      if (startTimeResult.isFailure()) {
        return startTimeResult.asFailure();
      } else if (startTimeResult.object == null) {
        return ValidationResult.failure("Please specify a start time.", "starthour", "startminute", "startampm");
      }

      ValidationResult<LocalTime> endTimeResult = buildLocalTime(obj, "endhour", "endminute", "endampm");
      if (endTimeResult.object != null && !endTimeResult.object.isAfter(startTimeResult.object)) {
        return ValidationResult.failure("End time must be after start time.", "endhour", "endminute", "endampm");
      }

      String periodMinutesStr = obj.getString("periodminutes");
      int periodMinutes = toInt(periodMinutesStr, 0);

      String numRingsStr = obj.getString("numrings");
      int numRings = toInt(numRingsStr, 0);

      boolean isHourlyRing = obj.has("ishourly") && obj.getString("ishourly").equals("on");

      if (!isHourlyRing && numRings == 0) {
        return ValidationResult.failure(
            "Please specify either an hourly ring or number of rings.",
            "ishourly", "numrings");
      }

      int silenceDurationMillis = toMillis(obj.getString("silence"));
      if (silenceDurationMillis == 0) {
        return ValidationResult.failure(
            "Please specify a silence duration.",
            "silence");
      }

      int scheduleId = 0;
      if (obj.has("scheduleid")) {
        scheduleId = toInt(obj.getString("scheduleid"), 0);
      }

      boolean isEnabled = obj.has("isenabled") && obj.getString("isenabled").equals("1");

      return ValidationResult.success(scheduleFactory.createFromFields(scheduleId,
          daysOfWeek, startTimeResult.object, endTimeResult.object, periodMinutes, numRings,
          isHourlyRing, configurationManager.getRingDurationMillis(), silenceDurationMillis,
          isEnabled));
    } catch (JSONException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return ValidationResult.failure("An unexpected error occurred.", "");
    }
  }

  private ValidationResult<LocalTime> buildLocalTime(
      JSONObject obj, String hourKey, String minuteKey, String ampmKey) {
    if (!obj.has(hourKey) && !obj.has(minuteKey) && !obj.has(ampmKey)) {
      return ValidationResult.success(null);
    }
    if (obj.getString(hourKey).isEmpty() && obj.getString(minuteKey).isEmpty()
      && obj.getString(ampmKey).isEmpty()) {
      return ValidationResult.success(null);
    }
    int hour = toInt(obj.getString(hourKey), -1);
    if (hour == -1) {
      return ValidationResult.failure("Invalid hour value", hourKey);
    }
    int minute = toInt(obj.getString(minuteKey), -1);
    if (minute == -1) {
      return ValidationResult.failure("Invalid minute value", minuteKey);
    }
    String ampm = obj.getString(ampmKey);
    if (!(ampm.equals("am") || ampm.equals("pm"))) {
      return ValidationResult.failure("Invalid am/pm value", ampmKey);
    }
    if (ampm.equals("pm") && hour < 12) {
      hour += 12;
    }
    return ValidationResult.success(LocalTime.of(hour, minute));
  }

  private ValidationResult<BellConfig> validateConfig(JSONObject obj) {
    try {
      String username = obj.getString("webauthenticationusername");
      if (username == null || username.isEmpty()) {
        return ValidationResult.failure(
            "Please specify a username.",
            "webauthenticationusername");
      }
      String password = obj.getString("webauthenticationpassword");
      if (password == null || password.isEmpty()) {
        return ValidationResult.failure(
            "Please specify a password.",
            "webauthenticationpassword");
      }

      String ringDurationMillisStr = obj.getString("ringdurationmillis");
      int ringDurationMillis = toInt(ringDurationMillisStr, 0);
      if (ringDurationMillis <= 0 || ringDurationMillis > 30000) {
        return ValidationResult.failure(
            "Invalid ring duration",
            "ringdurationmillis");
      }

      String defaultSilenceDurationMillisStr = obj.getString("defaultsilencedurationmillis");
      int defaultSilenceDurationMillis = toInt(defaultSilenceDurationMillisStr, 0);
      if (defaultSilenceDurationMillis == 0) {
        return ValidationResult.failure(
            "Please specify a default silence duration.",
            "defaultsilencedurationmillis");
      }
      return ValidationResult.success(new BellConfig(username, password, ringDurationMillis,
          defaultSilenceDurationMillis));
    } catch (JSONException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return ValidationResult.failure("An unexpected error occurred.", "");
    }
  }

  private int toInt(String s, int defaultValue) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  // Returns 0 on error.
  private int toMillis(String s) {
    if (s == null || s.isEmpty()) {
      return 0;
    }
    try {
      float silenceDurationSecs = Float.parseFloat(s);
      if (silenceDurationSecs <= 0) {
        return 0;
      }
      return (int) (silenceDurationSecs * 1000);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
