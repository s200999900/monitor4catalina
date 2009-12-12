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

package com.tomcat.monitor.zabbix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.apache.commons.codec.binary.Base64;

import org.apache.log4j.Logger;

public class ZabbixSender {

   protected final static Logger log = Logger.getLogger(ZabbixSender.class);

   public void send(final String zabbixServer, final int zabbixPort, int zabbixTimeout, final String host,
         final String key, final String value) throws IOException {

      final byte[] response = new byte[1024];

      final long start = System.currentTimeMillis();

      final int TIMEOUT = zabbixTimeout * 1000;

      final StringBuilder message = new StringBuilder("<req><host>");
      message.append(new String(Base64.encodeBase64(host.getBytes())));
      message.append("</host><key>");
      message.append(new String(Base64.encodeBase64(key.getBytes())));
      message.append("</key><data>");
      message.append(new String(Base64.encodeBase64(value.getBytes())));
      message.append("</data></req>");

      if (log.isDebugEnabled()) {
         log.debug("sending " + message);
      }

      Socket zabbix = null;
      OutputStreamWriter out = null;
      InputStream in = null;
      try {
         zabbix = new Socket(zabbixServer, zabbixPort);
         zabbix.setSoTimeout(TIMEOUT);

         out = new OutputStreamWriter(zabbix.getOutputStream());
         out.write(message.toString());
         out.flush();

         in = zabbix.getInputStream();
         final int read = in.read(response);
         if (log.isDebugEnabled()) {
            log.debug("received " + new String(response));
         }
         if (read != 2 || response[0] != 'O' || response[1] != 'K') {
            log.warn("received unexpected response '" + new String(response) + "' for key '" + key + "'");
         }
      } finally {
         if (in != null) {
            in.close();
         }
         if (out != null) {
            out.close();
         }
         if (zabbix != null) {
            zabbix.close();
         }
      }
      log.info("send() " + (System.currentTimeMillis() - start) + " ms");
   }

}
