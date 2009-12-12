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

import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class ApplicationStartup implements ServletContextListener {
   /**
    * @see javax.servlet.ServletContextListener#void
    *      (javax.servlet.ServletContextEvent)
    */
   
   protected final static Logger log = Logger.getLogger(ApplicationStartup.class);
   
   public void contextDestroyed(ServletContextEvent arg0) {
      log.info("JMX-Servlet ---> THE APPLICATION STOPPED");
   }

   /**
    * @see javax.servlet.ServletContextListener#void
    *      (javax.servlet.ServletContextEvent)
    */
   public void contextInitialized(ServletContextEvent arg0) {
      try {
         SchedulerFactory schedulerFactory = new StdSchedulerFactory();
         Scheduler scheduler = schedulerFactory.getScheduler();

         JobDetail jobDetail = new JobDetail("jobDetail-s1", "jobDetailGroup-s1", MonitorJob.class);

         long startTime = System.currentTimeMillis() + (5L * 1000L);

         SimpleTrigger simpleTrigger = new SimpleTrigger("simpleTrigger", "triggerGroup-s1", new Date(startTime), null,
               SimpleTrigger.REPEAT_INDEFINITELY, 120L * 1000L);
         scheduler.scheduleJob(jobDetail, simpleTrigger);
         scheduler.start();
      } catch (SchedulerException se) {
         log.error(se);
      } catch (Exception e) {
         log.error(e);
      }
   }
}