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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.tomcat.monitor.jmx.obj.bean.MServer;

public class Monitor {

   public static final int MODE_QUARTZ = 0;
   public static final int MODE_SERVLET = 1;
   
   private static final String ATTRIB_ACTIVE_CONNS = "numActive";
   private static final String ATTRIB_IDLE_CONNS = "numIdle";
   
   private static final String TYPE_DATA_SOURCE = "DataSource";

   protected final static Logger log = Logger.getLogger(Monitor.class);
   private static final MServer mserver = new MServer();

   public HashMap<String, String> getJMXValues(int mode) {

      HashMap<String, String> resultSet = new HashMap<String, String>();

      try {
         resultSet.clear();

         MonitorConfiguration sysProp = new MonitorConfiguration();

         log.info(sysProp.getSystemHostname() + sysProp.getZabbixServer() + sysProp.getZabbixPort().toString()
               + sysProp.getZabbixTimeout().toString() + sysProp.getCatalinaHost()
               + sysProp.getCatalinaPath().toString() + sysProp.getMonitorGRPName());

         HashMap<String, String> attributes = new HashMap<String, String>();

         Map<String, Object> values = mserver.values(new ObjectName("java.lang:type=Threading"), attributes);

         // activeSessions for given catalinaPath's
         for (String n : sysProp.getCatalinaPath()) {
            attributes.clear();
            attributes.put("activeSessions", null);
            values = mserver.values(new ObjectName("Catalina:type=Manager,path=" + n + ",host="
                  + sysProp.getCatalinaHost()), attributes);
            resultSet.put("tomcat.activeSessions." + n.substring(1), values.get("activeSessions").toString());
         }

         // HeapMemoryUsage.used
         attributes.clear();
         attributes.put("HeapMemoryUsage", "used");
         values = mserver.values(new ObjectName("java.lang:type=Memory"), attributes);
         resultSet.put("java.HeapMemoryUsage.used", values.get("HeapMemoryUsage.used").toString());

         // NonHeapMemoryUsage.used
         attributes.clear();
         attributes.put("NonHeapMemoryUsage", "used");
         values = mserver.values(new ObjectName("java.lang:type=Memory"), attributes);
         resultSet.put("java.NonHeapMemoryUsage.used", values.get("NonHeapMemoryUsage.used").toString());

         // NonHeapMemory
         attributes.clear();
         attributes.put("requestCount", null);
         values = mserver.values(new ObjectName("Catalina:type=GlobalRequestProcessor,name="
               + sysProp.getMonitorGRPName()), attributes);
         resultSet.put("tomcat.requestCount", values.get("requestCount").toString());

         
         // Connections
         String nameStr;
         String attribkey;
         StringBuilder urlStr;
         for (String n : sysProp.getCatalinaPath()) {
            if (ConfigurationReader.isServletMonitoringEnabled()) {
               
               HttpURLConnection conn = null;
               BufferedReader in = null;
               try {
                  String monitorAttribs = ConfigurationReader.get(ConfigurationReader.KEY_MONITOR_SERVLET_PARAMETER);
                  urlStr = new StringBuilder();
                  urlStr.append(ConfigurationReader.get(ConfigurationReader.KEY_MONITOR_SERVLET_PROTOCOL));
                  urlStr.append("://");
                  urlStr.append(ConfigurationReader.get(ConfigurationReader.KEY_MONITOR_SERVLET_HOST));
                  String port = ConfigurationReader.get(ConfigurationReader.KEY_MONITOR_SERVLET_PORT);
                  if (port != null && !"".equals(port.trim())) {
                     urlStr.append(":");
                     urlStr.append(port);
                  }
                  urlStr.append(n);
                  urlStr.append("/");
                  urlStr.append(ConfigurationReader.get(ConfigurationReader.KEY_MONITOR_SERVLET_PATH));
                  urlStr.append("?attrib=");
                  urlStr.append(monitorAttribs);

                  URL url = new URL(urlStr.toString());
                  conn = (HttpURLConnection) url.openConnection();
                  conn.setConnectTimeout(ConfigurationReader.getServletMonitorTimeout());
                  in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                  String inputLine;

                  List<String> lines = new LinkedList<String>(); 
                  while ((inputLine = in.readLine()) != null)
                     lines.add(inputLine);
                  
                  StringTokenizer tokenizer = new StringTokenizer(monitorAttribs);
                  String attrib;
                  String value = "0";
                  while (tokenizer.hasMoreTokens()){
                     attrib = tokenizer.nextToken();
                     for (String line : lines){
                        if (line.contains(attrib)){
                           value=line.replace(attrib+"=", "").trim();
                           break;
                        }
                     }
                     resultSet.put("tomcat.monitor." + n.substring(1)+"."+attrib, value);
                  }
                  
               } catch (IOException ex) {
                  log.error("servletmonitoring", ex);
                  if (mode == MODE_SERVLET)
                     resultSet.put("tomcat.monitor." + n.substring(1)+"(Servlet only)", ex.getMessage());
               } finally {
                  try{
                     if (in!= null)
                        in.close();
                  } catch (IOException ex){
                     log.error("Reader close", ex);
                  }
                  if (conn != null)
                     conn.disconnect();
               }
            }
            
            Set<ObjectName> onames = mserver.getMserver().queryNames(new ObjectName("Catalina:type=" + TYPE_DATA_SOURCE
                  + ",path=" + n + ",host=" + sysProp.getCatalinaHost() + ",*"),
                  null);

            for (ObjectName objectName : onames) {
               attributes.clear();
               attributes.put(ATTRIB_ACTIVE_CONNS, null);
               values = mserver.values(objectName, attributes);
               
               nameStr = objectName.toString();
               attribkey = "tomcat.activeConnections." + n.substring(1)+"."+nameStr.substring(nameStr.indexOf("name=")+5).replace("\"", "");
               resultSet.put(attribkey, values.get(ATTRIB_ACTIVE_CONNS).toString());
               
               attributes.clear();
               attributes.put(ATTRIB_IDLE_CONNS, null);
               values = mserver.values(objectName, attributes);
               
               attribkey = attribkey.replace("activeConnections", "idleConnections");
               resultSet.put(attribkey, values.get(ATTRIB_IDLE_CONNS).toString());
            }
         }

      } catch (MalformedObjectNameException ex) {
         log.error("monitor", ex);
      }

      return resultSet;
   }
}
