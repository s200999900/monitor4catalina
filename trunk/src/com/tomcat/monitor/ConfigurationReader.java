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

package com.tomcat.monitor;

import java.util.ResourceBundle;

public class ConfigurationReader {
   
   public static final String KEY_MONITOR_SERVLET = "servletMonitoringEnabled";
   public static final String KEY_MONITOR_SERVLET_PROTOCOL = "servletMonitorProtocol";
   public static final String KEY_MONITOR_SERVLET_HOST = "servletMonitorHost";
   public static final String KEY_MONITOR_SERVLET_PORT = "servletMonitorPort";
   public static final String KEY_MONITOR_SERVLET_PATH = "servletMonitorPath";
   public static final String KEY_MONITOR_SERVLET_PARAMETER = "servletMonitorParameter";
   public static final String KEY_MONITOR_SERVLET_TIMEOUT = "servletMonitorTimeout";
   
   private static ResourceBundle bundle;

   private static ResourceBundle getBundle() {
      if (bundle == null){
         bundle = ResourceBundle.getBundle("com.tomcat.monitor.System"); 
      }
      
      return bundle;
   }
   
   public static String get(String key){
      return getBundle().getString(key);
   }
   
   public static boolean isServletMonitoringEnabled(){
      String val = get(KEY_MONITOR_SERVLET);
      return ("1".equals(val.trim()) || "true".equals(val.trim()));
   }
   
   public static int getServletMonitorTimeout(){
      int retVal;
      
      try{
         retVal = Integer.parseInt(get(KEY_MONITOR_SERVLET_TIMEOUT));
      } catch (NumberFormatException ex) {
         retVal = 1000;
      }
      
      return retVal;
      
   }
}
