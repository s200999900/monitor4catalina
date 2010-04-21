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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.tomcat.monitor.jmx.obj.bean.MServer;

public class MonitorConfiguration {
   
   private List<String> monitorExpressCatalinaContext;
   private List<String> monitorSuppressCatalinaContext;

   private String monitorGRPName;
   private List<String> monitorBackendSender;
   private List<String> monitorTypes;

   private String zabbixServer;
   private Integer zabbixPort;
   private Integer zabbixTimeout;

   private String systemHostname;

   private List<String> catalinaPath = new ArrayList<String>();
   private String catalinaHost;

   public MonitorConfiguration() {
      super();

      final MServer mserver = new MServer();
      InetAddress localMachine;
      try {
         

         String expressContext = ConfigurationReader.get("monitor.expressCatalinaContext");

         if (expressContext != null && !"".equals(expressContext.trim())) {
            StringTokenizer tokenizer = new StringTokenizer(expressContext, ",");
            this.monitorExpressCatalinaContext = new ArrayList<String>();

            while (tokenizer.hasMoreTokens()) {
               this.monitorExpressCatalinaContext.add(tokenizer.nextToken().trim());
            }
         }

         String suppressContext = ConfigurationReader.get("monitor.suppressCatalinaContext");

         if (suppressContext != null && !"".equals(suppressContext.trim())) {
            StringTokenizer tokenizer = new StringTokenizer(suppressContext, ",");
            this.monitorSuppressCatalinaContext = new ArrayList<String>();

            while (tokenizer.hasMoreTokens()) {
               this.monitorSuppressCatalinaContext.add(tokenizer.nextToken().trim());
            }
         }

         // if no expressCatalinaContext is given - read the catalinaPath's from
         // running system
         if (this.monitorExpressCatalinaContext == null) {
            // if no suppressCatalinaContext is given - read the catalinaPath's
            // from running system
            if (this.monitorSuppressCatalinaContext == null) {
               Set<ObjectName> onames = mserver.getMserver()
                     .queryNames(new ObjectName("Catalina:type=Manager,*"), null);
               Iterator<ObjectName> it = onames.iterator();
               it = onames.iterator();
               while (it.hasNext()) {
                  ObjectName oname = (ObjectName) it.next();
                  String path = oname.getKeyProperty("path");
                  this.catalinaHost = oname.getKeyProperty("host");
                  if (this.catalinaHost != null) {
                     this.catalinaPath.add(path);
                  }
               }
            }
            // if suppressCatalinaContext is set - only write the not suppressed
            // (to be monitored) catalinaPath's to catalinaPath
            else {
               Set<ObjectName> onames = mserver.getMserver()
                     .queryNames(new ObjectName("Catalina:type=Manager,*"), null);
               Iterator<ObjectName> it = onames.iterator();
               it = onames.iterator();
               while (it.hasNext()) {
                  ObjectName oname = (ObjectName) it.next();
                  String path = oname.getKeyProperty("path");
                  this.catalinaHost = oname.getKeyProperty("host");
                  if (this.catalinaHost != null) {
                     boolean write = true;
                     for (String n : this.monitorSuppressCatalinaContext) {
                        if (path.equals(n)) {
                           write = false;
                        }
                     }
                     if (write) {
                        this.catalinaPath.add(path);
                     }
                  }
               }
            }
         }
         // if expressCatalinaContext is set (to be monitored) - write them to
         // catalinaPath
         else {
            Set<ObjectName> onames = mserver.getMserver().queryNames(new ObjectName("Catalina:type=Manager,*"), null);
            Iterator<ObjectName> it = onames.iterator();
            it = onames.iterator();
            this.catalinaPath = this.monitorExpressCatalinaContext;
            while (it.hasNext()) {
               ObjectName oname = (ObjectName) it.next();
               this.catalinaHost = oname.getKeyProperty("host");
               System.out.println();
            }
         }

         String helperCatalinaHostname = ConfigurationReader.get("zabbix.catalinaHostname").trim();
         // if no zabbix.catalinaHostname is given read it from inetAddress
         if (helperCatalinaHostname.isEmpty()) {
            localMachine = InetAddress.getLocalHost();
            String hostname = localMachine.toString();
            String[] cutted = hostname.split("/");
            this.systemHostname = cutted[0];
         }
         // if zabbix.catalinaHostname is given
         else {
            // if zabbix.catalinaHostname is set by javavm attribute
            if (helperCatalinaHostname.startsWith("-D")) {
               Properties sysProperties = System.getProperties();
               this.systemHostname = sysProperties.getProperty(helperCatalinaHostname.substring(2));
            }
            // if zabbix.catalinaHostname is set
            else {
               this.systemHostname = helperCatalinaHostname;
            }

         }

         String Types = ConfigurationReader.get("monitor.types");

         if (Types != null && !"".equals(Types.trim())) {
            StringTokenizer tokenizer = new StringTokenizer(Types, ",");
            this.monitorTypes = new ArrayList<String>();

            while (tokenizer.hasMoreTokens()) {
               this.monitorTypes.add(tokenizer.nextToken().trim());
            }
         }

         String BackendSender = ConfigurationReader.get("monitor.backendSender");

         if (Types != null && !"".equals(BackendSender.trim())) {
            StringTokenizer tokenizer = new StringTokenizer(BackendSender, ",");
            this.monitorBackendSender = new ArrayList<String>();

            while (tokenizer.hasMoreTokens()) {
               this.monitorBackendSender.add(tokenizer.nextToken().trim());
            }
         }
        
         this.zabbixServer = ConfigurationReader.get("zabbix.server").trim();
         this.zabbixPort = new Integer(ConfigurationReader.get("zabbix.port").trim());
         this.zabbixTimeout = new Integer(ConfigurationReader.get("zabbix.timeout").trim());
         this.monitorGRPName = ConfigurationReader.get("monitor.globalRequestProcessorName").trim();
         // this.monitorBackendSender =
         // ConfigurationReader.get("monitor.backendSender").trim();

      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (MalformedObjectNameException e) {
         e.printStackTrace();
      } catch (NullPointerException e) {
         e.printStackTrace();
      }
   }
         
   //named GETTER
   public List<String> getMonitorBackendSender() {
      return monitorBackendSender;
   }

   public String getMonitorGRPName() {
      return monitorGRPName;
   }

   public List<String> getMonitorExpressContext() {
      return monitorExpressCatalinaContext;
   }

   public List<String> getMonitorSuppressContext() {
      return monitorSuppressCatalinaContext;
   }

   public String getZabbixServer() {
      return zabbixServer;
   }

   public Integer getZabbixPort() {
      return zabbixPort;
   }

   public Integer getZabbixTimeout() {
      return zabbixTimeout;
   }

   public String getSystemHostname() {
      return systemHostname;
   }

   public List<String> getCatalinaPath() {
      return catalinaPath;
   }

   public String getCatalinaHost() {
      return catalinaHost;
   }

   public List<String> getMonitorTypes() {
      return monitorTypes;
   }
}
