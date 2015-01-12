/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.plugin.PluginService;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.util.Exceptions;


public class SimpleServiceRegistry implements ServiceRegistry {

  Map<String,Object> services = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> serviceInterface) {
    Exceptions.checkNotNullParameter(serviceInterface, "serviceInterface");
    return (T) getService(serviceInterface.getName());
  }

  public synchronized Object getService(String serviceTypeName) {
    Object service = (Object) services.get(serviceTypeName);
    if (service==null) {
      throw new RuntimeException("Service "+serviceTypeName+" is not registered");
    }
    return service;
  }

  public SimpleServiceRegistry registerService(Object service) {
    registerService(service, service.getClass());
    return this;
  }

  protected void registerService(Object service, Class<?>... serviceTypes) {
    if (serviceTypes!=null) {
      for (Class<?> serviceType: serviceTypes) {
        services.put(serviceType.getName(), service);
        Class< ? > superclass = serviceType.getSuperclass();
        if (superclass!=null && superclass!=Object.class) {
          registerService(service, superclass);
        }
        registerService(service, serviceType.getInterfaces());
      }
    }
  }

  @Override
  public void prepare(WorkflowEngineConfiguration configuration) {
    PluginService pluginService = getService(PluginService.class);
    for (Initializable initializable: configuration.initializables) {
      initializable.initialize(this, configuration);
    }
    for (Class<?> javaBeanType: configuration.javaBeanTypes) {
      pluginService.registerJavaBeanType(javaBeanType);
    }
    for (DataType dataType: configuration.dataTypes) {
      pluginService.registerDataType(dataType);
    }
    for (ActivityType activityType: configuration.activityTypes) {
      pluginService.registerActivityType(activityType);
    }
    for (Class<? extends JobType> jobTypeClass: configuration.jobTypeClasses) {
      pluginService.registerJobType(jobTypeClass);
    }
  }
}
