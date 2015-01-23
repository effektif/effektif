/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.workflow.impl.configuration;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.effektif.workflow.impl.WorkflowInstanceEventListener;


public class WorkflowEngineConfiguration {

  protected String id;
  public List<WorkflowInstanceEventListener> listeners = new CopyOnWriteArrayList<>();

  public String getId() {
    if (id==null) {
      id = createDefaultId();
    }
    return id; 
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  protected static String createDefaultId() {
    String id = null;
    try {
      id = InetAddress.getLocalHost().getHostAddress();
      try {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        int atIndex = processName.indexOf('@');
        if (atIndex > 0) {
          id += ":" + processName.substring(0, atIndex);
        }
      } catch (Exception e) {
        id += ":?";
      }
    } catch (UnknownHostException e1) {
      id = UUID.randomUUID().toString();
    }
    return id;
  }
  
  public synchronized void addListener(WorkflowInstanceEventListener listener) {
    listeners.add(listener);
  }

  public synchronized void removeListener(WorkflowInstanceEventListener listener) {
    listeners.remove(listener);
  }
  
  public synchronized List<WorkflowInstanceEventListener> getListeners() {
    return listeners;
  }
}
