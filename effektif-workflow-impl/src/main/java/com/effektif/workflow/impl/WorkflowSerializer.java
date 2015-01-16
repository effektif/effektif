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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.plugin.PluginService;
import com.effektif.workflow.impl.plugin.Serializer.ObjectSerializer;
import com.effektif.workflow.impl.type.ObjectTypeImpl;


public class WorkflowSerializer {

  PluginService pluginService;
  
  public WorkflowSerializer(WorkflowEngineImpl workflowEngine) {
    this.pluginService = workflowEngine.getServiceRegistry().getService(PluginService.class);
  }
  
  public void serialize(Workflow workflowApi) {
    serializeScope(workflowApi);
  }
  
  public void serialize(Activity activityApi) {
    serializeScope(activityApi);
  }
  
  public void serializeScope(Scope scopeApi) {
    List<Activity> activitiesApi = scopeApi.getActivities();
    if (activitiesApi!=null) {
      for (Activity activityApi: activitiesApi) {
        serialize(activityApi);
        ObjectTypeImpl serializer = pluginService.getActivityTypeSerializer(activityApi.getClass());
        serializer.serialize(activityApi, this);
      }
    }
  }

//  public void serializeBinding(Binding binding) {
//    Object value = binding.getValue();
//    if (value!=null && binding.getType()==null) {
//      DataType dataType = pluginService.getDataTypeByValueClass(value.getClass());
//      if (dataType==null) {
//        throw new RuntimeException("No data type found for value "+value+" ("+value.getClass().getName()+")");
//      }
//      binding.type(dataType.getApiType());
//    }
//  }
}
