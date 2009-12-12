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
 * @author Mario Kleinsasser <mario.kleinsasser@gmail.com>
 * @author Bernhard Rausch <rausch.bernhard@gmail.com>
 */

package com.tomcat.monitor.logging;

import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.xml.DOMConfigurator;

public class AppStartupLogger implements ServletContextListener {

   @Override
   public void contextInitialized(ServletContextEvent arg0) {
      try {
         // setzen des Pfads der log4j.xml
         String log4jConfigFile = "com/tomcat/monitor/logging/log4j.xml";

         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl != null) {
            URL retValue = null;
            String[] resArray = { log4jConfigFile };
            Enumeration<URL> en;
            for (int i = 0; i < resArray.length; i++) {
               en = cl.getResources(resArray[i]);
               if (en.hasMoreElements()) {
                  retValue = (URL) en.nextElement();
                  break;
               }
            }

            if (retValue != null)
               DOMConfigurator.configure(retValue);
            else
               throw new Exception("ConfigFile not found");
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

   }

   @Override
   public void contextDestroyed(ServletContextEvent arg0) {
      // TODO Auto-generated method stub

   }

}
