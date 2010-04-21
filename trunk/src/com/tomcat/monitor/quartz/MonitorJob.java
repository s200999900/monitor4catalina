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

package com.tomcat.monitor.quartz;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.tomcat.monitor.Monitor;
import com.tomcat.monitor.MonitorConfiguration;
import com.tomcat.monitor.zabbix.ZabbixSender;

public class MonitorJob implements Job {

   protected final static Logger log = Logger.getLogger(MonitorJob.class);

   /** Creates a new instance of MonitorJob */
   public MonitorJob() {
   }

   // execute() method must be implemented
   public void execute(JobExecutionContext context) throws JobExecutionException {

      log.info("MonitorJob --->" + new Date());

      try {
         // doRun(0); stands for "offline-automatic"-start
         // MServlet.doRun(0);

         Monitor mon = new Monitor();
         MonitorConfiguration sysProp = new MonitorConfiguration();
         HashMap<String, String> resultSet;
         
         ZabbixSender sender = new ZabbixSender();

         if (sysProp.getMonitorTypes().contains("getJMXValues")) {
            resultSet = mon.getJMXValues(Monitor.MODE_QUARTZ);

            if (sysProp.getMonitorBackendSender().contains("zabbix")) {
               Set<Map.Entry<String, String>> set = resultSet.entrySet();

               for (Map.Entry<String, String> me : set) {
                  log.info("MonitorJob ---> " + me.getKey() + "=" + me.getValue());
                  sender.send(sysProp.getZabbixServer(), sysProp.getZabbixPort(), sysProp.getZabbixTimeout(),
                        sysProp.getSystemHostname(), me.getKey().toLowerCase(), me.getValue());
               }
            }
         }
         else{
            log.error("MonitorJob ---> Sorry, one of the specified methods isn't supported jet!");
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}