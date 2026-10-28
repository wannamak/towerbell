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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import towerbell.Proto;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.schedule.Schedule;
import towerbell.schedule.ScheduleManager;
import towerbell.www.template.TemplateEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class HtmlServlet extends HttpServlet {
  private final Logger logger = Logger.getLogger(HtmlServlet.class.getName());
  private final boolean developmentMode;
  private final ScheduleManager scheduleManager;
  private final PathUtil pathUtil = new PathUtil();
  private final TemplateEngine templateEngine;

  public HtmlServlet(Proto.FixedConfig fixedConfig, ScheduleManager scheduleManager,
      ConfigurationManager configurationManager, SilenceManager silenceManager) {
    this.developmentMode = fixedConfig.getDevelopmentMode();
    this.scheduleManager = scheduleManager;
    this.templateEngine = new TemplateEngine(fixedConfig, scheduleManager, silenceManager, configurationManager);
  }

  @Override
  public void init() throws ServletException {
    templateEngine.readTemplates();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (developmentMode) {
      templateEngine.readTemplates();
    }
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();
    String path = req.getServletPath();

    if (path != null && path.equals("/add")) {
      out.println(templateEngine.getAddScheduleHtml());
    } else if (path != null && path.startsWith("/update/")) {
      Integer scheduleId = pathUtil.parseIdFromPathSuffix(path);
      Schedule schedule;
      if (scheduleId == null
          || (schedule = scheduleManager.getSchedule(scheduleId)) == null) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      out.println(templateEngine.getEditScheduleHtml(schedule));
    } else if (path != null && path.equals("/ring")) {
      out.println(templateEngine.getRingHtml());
    } else if (path != null && path.equals("/config")) {
      out.println(templateEngine.getConfigHtml());
    } else {
      out.println(templateEngine.getHomeHtml());
    }
  }
}
