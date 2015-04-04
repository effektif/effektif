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
package com.effektif.workflow.impl.mapper;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class BpmnTypeMapping {

  String bpmnElementName;
  Map<String,String> bpmnTypeAttributes;
  Class<?> type;
  BpmnFieldMappingsImpl bpmnFieldMappings;

  public String getBpmnElementName() {
    return bpmnElementName;
  }
  
  public void setBpmnElementName(String bpmnElementName) {
    this.bpmnElementName = bpmnElementName;
  }
  
  public Class< ? > getType() {
    return type;
  }

  public void setType(Class< ? > type) {
    this.type = type;
  }

  public Map<String, String> getBpmnTypeAttributes() {
    return bpmnTypeAttributes;
  }
  
  public void addBpmnTypeAttribute(String attribute, String value) {
    if (bpmnTypeAttributes==null) {
      bpmnTypeAttributes = new HashMap<String, String>();
    }
    bpmnTypeAttributes.put(attribute, value);
  }

  public Object instantiate() {
    try {
      return type.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public BpmnFieldMappingsImpl getBpmnFieldMappings() {
    return bpmnFieldMappings;
  }

  
  public void setBpmnFieldMappings(BpmnFieldMappingsImpl bpmnFieldMappings) {
    this.bpmnFieldMappings = bpmnFieldMappings;
  }
}
