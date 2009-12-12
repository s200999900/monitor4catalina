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
 */

package com.tomcat.monitor.jmx.obj.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Dies Klasse erleichert den lesenden Zugang zu den lokalen MBeans. Es werde
 * die Werte aus den OpenMbeans (java.lang) geholt und es können Listen dieser
 * Werte erstellt werden.
 * 
 * @author Peter Rossbach (pr@objektpark.de)
 * @version $Revision:$ $Date:$
 */
public class MServer {

   // ----------------------------------------------------- Instance Variable

   /**
    * Logging device for this class
    */
   protected Log logger = LogFactory.getLog(getClass());

   /**
    * Nutze Plattform MBeanserver
    */
   protected final MBeanServer mserver;

   /**
    * Erzeuge Zugang zu dem lokalen Mbean server
    */
   public MServer() {
      super();
      mserver = getMBeanServer();
   }

   /**
    * @return the mserver
    */
   public MBeanServer getMserver() {
      return mserver;
   }

   /**
    * Zugriff zu dem lokalen Platform Server oder erzeuge neuen.
    */
   protected MBeanServer getMBeanServer() {
      MBeanServer server;
      server = ManagementFactory.getPlatformMBeanServer();
      if (server == null)
         server = MBeanServerFactory.createMBeanServer();
      return (server);
   }

   /**
    * Zugriff auf ausgewählte Menge von MBean Attribute
    * 
    * @param accessorMbean
    *           ObjectName of mbean
    * @param attributes
    *           Map von Attribute und evtl. Path Zugriffs ausdrücken (Separator
    *           ";") see accessValue
    */
   public Map<String, Object> values(ObjectName accessorMbean, Map<String, String> attributes) {

      String[] attrNames = new String[attributes.size()];
      attributes.keySet().toArray(attrNames);
      AttributeList list = null;
      try {
         list = mserver.getAttributes(accessorMbean, attrNames);
      } catch (Exception e) {
         logger.error(e);
      }
      Map<String, Object> values = new HashMap<String,Object>();
      if (list != null) {
         if (logger.isDebugEnabled())
            logger.debug("access: " + accessorMbean + " attr=" + list.toString());
         for (Iterator<Object> iter = list.iterator(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            Object value = attr.getValue();
            String attributePath = attributes.get(attr.getName());
            if (attributePath != null) {
               StringTokenizer st = new StringTokenizer(attributePath, ";");
               while (st.hasMoreTokens()) {
                  String path = st.nextToken();
                  Object pathvalue = accessValue(path, value);
                  values.put(attr.getName() + "." + path, pathvalue);
               }
            } else
               values.put(attr.getName(), value);
         }
      }
      return values;
   }

   /**
    * getValue form OpenMBean Value FIXME: SimpleType Umwandlung in skalare
    * Datentypen FIXME: Suche eine elegante Moeglichkeit an den Wert eines
    * Attributes in einem OpenMBean heranzukommen (BeanUtils...)?
    * <p/>
    * <p/>
    * Beispiele: attribute1 [key1] attribute1.attribute2 [key].attribute1
    * [key1].[key2] [key1].attribute1.[key2].attribute2
    * 
    * @param attributePath
    *           path to Mbean OpenMbean attribute
    * @param bean
    *           current value
    */
   public Object accessValue(String attributePath, Object bean) {
      if (attributePath == null || "".equals(attributePath))
         return null;
      if (bean instanceof CompositeDataSupport) {
         CompositeDataSupport data = (CompositeDataSupport) bean;
         // FIXME This only supports direct key access
         return data.get(attributePath);
      } else if (bean instanceof TabularDataSupport) {
         TabularDataSupport data = (TabularDataSupport) bean;
         if (attributePath.startsWith("[")) {
            int endIndex = attributePath.indexOf("]");
            if (endIndex > 1) {
               String attributeKey = attributePath.substring(1, endIndex);
               CompositeData valuedata = data.get(new Object[] { attributeKey });
               Object value = valuedata.get("value");
               OpenType<?> type = valuedata.getCompositeType().getType("value");
               if (type instanceof SimpleType) {
                  // FIXME Es fehlt noch die Prfung ob attributePath
                  // nicht false ist, sprich das nach [].<xxxx> gefragt
                  // wahr.
                  return value;
               } else {
                  String nextAttributePath = attributePath.substring(endIndex);
                  if (nextAttributePath.startsWith(".")) {
                     // FIXME stimmt das oder endIndex
                     return accessValue(attributePath.substring(endIndex + 1), value);
                  }
               }
            }
         }
      }
      return null;
   }

}