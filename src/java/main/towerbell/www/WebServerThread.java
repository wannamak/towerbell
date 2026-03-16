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

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import towerbell.Proto;
import towerbell.configuration.ConfigurationManager;
import towerbell.configuration.SilenceManager;
import towerbell.ringer.BellRinger;
import towerbell.schedule.ScheduleManager;

public class WebServerThread extends Server {
  public static final String AUTH_REALM = "TowerBellRealm";
  public static final String[] AUTH_ROLE = new String[] { "user" };

  public WebServerThread(Proto.FixedConfig fixedConfig, ScheduleManager scheduleManager,
      ConfigurationManager configurationManager, BellRinger bellRinger,
      SilenceManager silenceManager) {
    super(fixedConfig.getServerPort());

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    ServletHolder staticHolder = new ServletHolder("static", DefaultServlet.class);
    staticHolder.setInitParameter("resourceBase", fixedConfig.getStaticFileDirectory());
    staticHolder.setInitParameter("dirAllowed", "false");
    context.addServlet(staticHolder, "/static/*");
    ServletHolder htmlServlet = new ServletHolder(
        new HtmlServlet(fixedConfig, scheduleManager, configurationManager, silenceManager));
    context.addServlet(htmlServlet, "/");
    context.addServlet(htmlServlet, "/add");
    context.addServlet(htmlServlet, "/ring");
    context.addServlet(htmlServlet, "/settings");
    context.addServlet(htmlServlet, "/update");
    HashLoginService loginService = new HashLoginService(AUTH_REALM);
    context.addServlet(new ServletHolder(
        new ApiServlet(scheduleManager, configurationManager, silenceManager, loginService,
            bellRinger)),
        "/api/*");

    // --- Basic auth ---
    UserStore userStore = new UserStore();
    userStore.addUser(
        configurationManager.getWebAuthenticationUsername(),
        Credential.getCredential(configurationManager.getWebAuthenticationPassword()),
        AUTH_ROLE);
    loginService.setUserStore(userStore);
    loginService.setHotReload(true);
    addBean(loginService);

    Constraint constraint = new Constraint();
    constraint.setName(Constraint.__DIGEST_AUTH);
    constraint.setRoles(AUTH_ROLE);
    constraint.setAuthenticate(true);
    constraint.setDataConstraint(Constraint.DC_NONE);

    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setConstraint(constraint);
    mapping.setPathSpec("/*");

    ConstraintSecurityHandler security = new ConstraintSecurityHandler();
    security.setAuthenticator(new DigestAuthenticator());
    security.setRealmName(AUTH_REALM);
    security.addConstraintMapping(mapping);
    security.setLoginService(loginService);
    security.setHandler(context);

    setHandler(security);
  }
}
