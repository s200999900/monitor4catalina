/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @author Peter Rossbach <pr@objektpark.de>
 * @author Mario Kleinsasser <mario.kleinsasser@gmail.com>
 * @author Bernhard Rausch <rausch.bernhard@gmail.com>
 */

package com.tomcat.monitor.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.tomcat.monitor.Monitor;
import com.tomcat.monitor.MonitorConfiguration;

public class MServlet extends HttpServlet {

   protected final static Logger log = Logger.getLogger(MServlet.class);

   private static final long serialVersionUID = -2331215009660461241L;

   /**
    * Antwort auf einen GET HTTP Request
    * 
    * @param request
    *           Anfrage
    * @param response
    *           Antwort
    * @throws IOException
    *            Fehlerhaftes IO
    * @throws ServletException
    *            Hier gibt es einen Servlet Anwendungsfehler
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

      response.setContentType("text/html");
      PrintWriter writer = response.getWriter();

      try {
         writer.println("JMX-Values:</br></br>");
         MonitorConfiguration sysProp = new MonitorConfiguration();
         Monitor mon = new Monitor();

         log.info(sysProp.getSystemHostname() + sysProp.getZabbixServer() + sysProp.getZabbixPort().toString()
               + sysProp.getZabbixTimeout().toString() + sysProp.getCatalinaHost()
               + sysProp.getCatalinaPath().toString() + sysProp.getMonitorGRPName());

         HashMap<String, String> resultSet = mon.getJMXValues(Monitor.MODE_SERVLET);

         Set<Map.Entry<String, String>> set = resultSet.entrySet();

         for (Map.Entry<String, String> me : set) {
            writer.println(me.getKey() + "=" + me.getValue() + "<br>");
         }

         writer.println("</br>System-Properties:</br></br>");
         Properties props = System.getProperties();
         Enumeration<?> enprop = props.propertyNames();
         String key = "";
         while (enprop.hasMoreElements()) {
            key = (String) enprop.nextElement();
            writer.println(key + " = " + props.getProperty(key) + "</br>");
         }

      } catch (Exception ex) {
      }
      ;
   }
}