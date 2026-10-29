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
package towerbell.www

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import towerbell.Proto.FixedConfig
import towerbell.configuration.ConfigurationManager
import towerbell.configuration.SilenceManager
import towerbell.schedule.Schedule
import towerbell.schedule.ScheduleManager
import towerbell.tune.Tune
import towerbell.tune.TuneManager
import towerbell.www.template.TemplateEngine
import java.util.logging.Logger

class HtmlServlet(
  fixedConfig: FixedConfig,
  private val scheduleManager: ScheduleManager,
  private val tuneManager: TuneManager,
  configurationManager: ConfigurationManager,
  silenceManager: SilenceManager
) : HttpServlet() {
  private val logger: Logger? = Logger.getLogger(HtmlServlet::class.java.name)
  private val developmentMode: Boolean = fixedConfig.developmentMode
  private val pathUtil = PathUtil()
  private val templateEngine = TemplateEngine(fixedConfig, scheduleManager, silenceManager, configurationManager)

  override fun init() {
    templateEngine.readTemplates()
  }

  override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
    if (developmentMode) {
      templateEngine.readTemplates()
    }
    res.contentType = "text/html;charset=UTF-8"
    val path = req.servletPath.orEmpty()

    val html = when {
      path == "/schedule/add" -> templateEngine.getAddScheduleHtml()
      path.startsWith("/schedule/update/") -> {
        val schedule = lookupSchedule(path)
        if (schedule == null) {
          res.sendError(HttpServletResponse.SC_NOT_FOUND)
          return
        }
        templateEngine.getEditScheduleHtml(schedule)
      }
      path == "/tune/add" -> templateEngine.getAddTuneHtml()
      path.startsWith("/tune/update/") -> {
        val tune = lookupTune(path)
        if (tune == null) {
          res.sendError(HttpServletResponse.SC_NOT_FOUND)
          return
        }
        templateEngine.getEditTuneHtml(tune)
      }
      path == "/ring" -> templateEngine.getRingHtml()
      path == "/config" -> templateEngine.getConfigHtml()
      else -> templateEngine.getHomeHtml()
    }
    res.writer.println(html)
  }

  private fun lookupSchedule(path: String): Schedule? =
    pathUtil.parseIdFromPathSuffix(path)?.let { scheduleManager.getSchedule(it) }

  private fun lookupTune(path: String): Tune? =
    pathUtil.parseIdFromPathSuffix(path)?.let { tuneManager.getTune(it) }
}
