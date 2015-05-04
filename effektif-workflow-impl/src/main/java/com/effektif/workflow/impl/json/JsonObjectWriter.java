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
package com.effektif.workflow.impl.json;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Serialises to JSON using a {@link Map}-based JSON representation.
 */
public class JsonObjectWriter extends JsonWriter {
  
  public static class ObjectContext {
    List<Object> jsonList;
    Map<String,Object> jsonMap;
    String fieldName;
    public void setValue(Object value) {
      if (jsonMap!=null && fieldName!=null) {
        jsonMap.put(fieldName, value);
        fieldName = null;
      } else if (jsonList!=null) {
        jsonList.add(value);
      }
    }
  }
  
  JsonObjectMapper jsonObjectMapper;
  Stack<ObjectContext> objectContextStack = new Stack<>();
  Object result = null;
  
  public JsonObjectWriter(Mappings mappings, JsonObjectMapper jsonObjectMapper) {
    super(mappings);
    this.jsonObjectMapper = jsonObjectMapper;
  }

  @Override
  public void objectStart() {
    ObjectContext objectContext = new ObjectContext();
    objectContext.jsonMap = jsonObjectMapper.newObjectMap();
    objectContextStack.push(objectContext);
  }

  @Override
  public void objectEnd() {
    ObjectContext objectContext = objectContextStack.pop();
    writeValue(objectContext.jsonMap);
  }

  @Override
  public void writeFieldName(String fieldName) {
    objectContextStack.peek().fieldName = fieldName;
  }

  @Override
  public void arrayEnd() {
    ObjectContext objectContext = objectContextStack.pop();
    writeValue(objectContext.jsonList);
  }

  @Override
  public void arrayStart() {
    ObjectContext objectContext = new ObjectContext();
    objectContext.jsonList = jsonObjectMapper.newArray();
    objectContextStack.push(objectContext);
  }

  public void writeValue(Object value) {
    if (!objectContextStack.isEmpty()) {
      objectContextStack.peek().setValue(value);
    } else {
      result = value;
    }
  }

  @Override
  public void writeString(String s) {
    writeValue(s);
  }

  @Override
  public void writeBoolean(Boolean b) {
    writeValue(b);
  }
  
  @Override
  public void writeNumber(Number n) {
    writeValue(n);
  }

  @Override
  public void writeNull() {
  }
}