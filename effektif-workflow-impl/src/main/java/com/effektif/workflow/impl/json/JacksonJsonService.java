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
package com.effektif.workflow.impl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.command.AbstractCommand;
import com.effektif.workflow.api.command.VariableValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.plugin.DataType;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.PluginService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.types.ObjectTypeImpl;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;


public class JacksonJsonService implements JsonService, Initializable<WorkflowEngineConfiguration> {
  
  protected JsonFactory jsonFactory;
  protected ObjectMapper objectMapper;
  protected PluginService pluginService; 
  protected WorkflowEngineImpl workflowEngine; 

  @Override
  public void initialize(ServiceRegistry serviceRegistry, WorkflowEngineConfiguration configuration) {
    this.objectMapper = serviceRegistry.getService(ObjectMapper.class);
    this.jsonFactory = serviceRegistry.getService(JsonFactory.class);
    this.pluginService = serviceRegistry.getService(PluginService.class);
    this.workflowEngine = serviceRegistry.getService(WorkflowEngineImpl.class);
    
    objectMapper
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
      .setSerializationInclusion(Include.NON_EMPTY);
    
    SimpleModule module = new SimpleModule();
    module.addSerializer(new LocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    objectMapper.registerModule(module);
  }
  
  public void registerSubtype(Class<?> subtype) {
    this.objectMapper.registerSubtypes(subtype);
  }

  public String objectToJsonString(Object object) {
    StringWriter stringWriter = new StringWriter();
    objectToJson(object, stringWriter, objectMapper.writer());
    return stringWriter.toString();
  }

  public String objectToJsonStringPretty(Object object) {
    StringWriter stringWriter = new StringWriter();
    objectToJson(object, stringWriter, objectMapper.writerWithDefaultPrettyPrinter());
    return stringWriter.toString();
  }

  public void objectToJson(Object object, Writer writer) {
    objectToJson(object, writer, objectMapper.writer());
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> objectToJsonMap(Object object) {
    return objectMapper.convertValue(object, Map.class);
  }
  
  protected void objectToJson(Object object, Writer writer, ObjectWriter objectWriter) {
    try {
      objectWriter
        .writeValue(writer, object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T jsonToObject(String json, Class<T> type) {
    try {
      return jsonToObject(jsonFactory.createParser(json), type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T jsonToObject(Reader reader, Class<T> type) {
    try {
      return jsonToObject(jsonFactory.createParser(reader), type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T jsonMapToObject(Map<String,Object> jsonMap, Class<T> type) {
    return objectMapper.convertValue(jsonMap, type);
  }

  protected <T> T jsonToObject(JsonParser jsonParser, Class<T> type) throws IOException {
    T object = objectMapper
      .reader(type)
      .readValue(jsonParser);
    return object;
  }

  
  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  public void setJsonFactory(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /** Ensures types are added to the untyped values so this model can be deserialized.
   * Jackson can serialize fields of type object.  But to deserialize, sometimes 
   * the right type is needed such as for binding values.  This method adds
   * the type information to the serialized form so deserialization can use it. */
  @Override
  public Workflow serializeWorkflow(Workflow workflow) {
    serializeScope(workflow);
    return workflow;
  }

  @Override
  public Workflow deserializeWorkflow(Workflow workflow) {
    deserializeScope(workflow);
    return workflow;
  }
  
  protected void serializeScope(Scope scopeApi) {
    List<Activity> activitiesApi = scopeApi.getActivities();
    if (activitiesApi!=null) {
      for (Activity activityApi: activitiesApi) {
        ObjectTypeImpl serializer = pluginService.getActivityTypeSerializer(activityApi.getClass());
        if (serializer!=null && serializer.isSerializeRequired()) {
          serializer.serialize(activityApi);
        }
        serializeScope(activityApi);
      }
    }
  }

  protected void deserializeScope(Scope scopeApi) {
    List<Activity> activitiesApi = scopeApi.getActivities();
    if (activitiesApi!=null) {
      for (Activity activityApi: activitiesApi) {
        ObjectTypeImpl serializer = pluginService.getActivityTypeSerializer(activityApi.getClass());
        if (serializer!=null && serializer.isSerializeRequired()) {
          serializer.deserialize(activityApi);
        }
        deserializeScope(activityApi);
      }
    }
  }

  @Override
  public WorkflowInstance deserializeWorkflowInstance(WorkflowInstance workflowInstance) {
    deserializeScopeInstance(workflowInstance);
    return workflowInstance;
  }

  protected void deserializeScopeInstance(ScopeInstance scopeInstance) {
    if (scopeInstance.getVariableInstances()!=null) {
      for (VariableInstance variableInstance: scopeInstance.getVariableInstances()) {
        deserializeVariableInstance(variableInstance);
      }
    }
    if (scopeInstance.getActivityInstances()!=null) {
      for (ActivityInstance activityInstance: scopeInstance.getActivityInstances()) {
        deserializeScopeInstance(activityInstance);
      }
    }
  }

  protected void deserializeVariableInstance(VariableInstance variableInstance) {
    Object value = variableInstance.getValue();
    Type type = variableInstance.getType();
    if (value!=null && type!=null) {
      DataType dataType = pluginService.createDataType(type);
      Object deserializedValue = dataType.deserialize(value);
      variableInstance.setValue(deserializedValue);
    }
  }

  @Override
  public <T extends AbstractCommand> T serializeCommand(T command) {
    if (command==null || command.getVariableValues()==null) {
      return command;
    }
    for (VariableValue variableValue: command.getVariableValues()) {
      Object value = variableValue.getValue();
      if (value!=null && variableValue.getType()==null) {
        Type type = pluginService.getTypeByValue(value);
        variableValue.type(type);
      }
    }
    return command;
  }

  @Override
  public <T extends AbstractCommand> T deserializeCommand(T command) {
    if (command==null || command.getVariableValues()==null) {
      return command;
    }
    for (VariableValue variableValue: command.getVariableValues()) {
      Object value = variableValue.getValue();
      if (value!=null && variableValue.getType()!=null) {
        DataType dataType = pluginService.createDataType(variableValue.getType());
        Object deserializedValue = dataType.deserialize(value);
        variableValue.value(deserializedValue);
      }
    }
    return command;
  }
}
