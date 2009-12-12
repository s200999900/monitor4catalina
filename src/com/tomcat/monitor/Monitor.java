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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.tomcat.monitor.jmx.obj.bean.MServer;

public class Monitor {

   protected final static Logger log = Logger.getLogger(Monitor.class);
   private static final MServer mserver = new MServer();

   public HashMap<String, String> getJMXValues() throws IOException {

      HashMap<String, String> resultSet = new HashMap<String, String>();

      try {
         resultSet.clear();

         SystemProperty sysProp = new SystemProperty();

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

      } catch (Exception ex) {
         ex.printStackTrace();
      }
      ;
      return resultSet;
   }
}
