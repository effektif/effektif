/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.impl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


/**
 * @author Tom Baeyens
 */
public class JsonService implements Brewable {
  
  // private static final Logger log = LoggerFactory.getLogger(JsonService.class);
  
  protected JsonFactory jsonFactory;
  protected ObjectMapper objectMapper;

  public void brew(Brewery brewery) {
    this.objectMapper = brewery.get(ObjectMapper.class);
    this.jsonFactory = brewery.get(JsonFactory.class);
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
  
  
  
  public void deserializeTriggerInstance(TriggerInstance triggerInstance, WorkflowImpl workflow) {
    Map<String, Object> triggerValues = triggerInstance.getData();
    if (triggerValues!=null) {
      for (String triggerKey: triggerValues.keySet()) {
        Object jsonValue = triggerValues.get(triggerKey);
        DataType<?> dataType = workflow.trigger!=null ? workflow.trigger.getDataTypeForTriggerKey(triggerKey) : null;
        if (dataType==null) {
          VariableImpl variable = workflow.findVariableByIdLocal(triggerKey);
          dataType = variable!=null ? variable.type : null; 
        }
        if (dataType!=null) {
          Object value = dataType.convertJsonToInternalValue(jsonValue);
          triggerValues.put(triggerKey, value);
        }
      }
    }
  }
  
  public void deserializeMessage(Message message, WorkflowImpl workflow) {
    deserializeVariableValues(message.getData(), workflow);
  }

  protected void deserializeVariableValues(Map<String,Object> variableValues, WorkflowImpl workflow) {
    if (variableValues!=null) {
      for (String variableId: variableValues.keySet()) {
        Object jsonValue = variableValues.get(variableId);
        VariableImpl variable = workflow.findVariableByIdLocal(variableId);
        if (variable!=null && variable.type!=null) {
          Object value = variable.type.convertJsonToInternalValue(jsonValue);
          variableValues.put(variableId, value);
        }
      }
    }
  }

}
