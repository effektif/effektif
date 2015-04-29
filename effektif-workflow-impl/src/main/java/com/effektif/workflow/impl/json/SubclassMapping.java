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

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.deprecated.triggers.FormTrigger;
import com.effektif.workflow.api.workflow.Trigger;

/**
 * A mapping from a ‘base class’, e.g. {@link Trigger}, to its subclasses (e.g. {@link FormTrigger}).
 *
 * @author Tom Baeyens
 */
public class SubclassMapping {

  Class<?> baseClass;
  String typeField;
  Map<String,Class<?>> subclasses = new HashMap<>();
  
  public SubclassMapping(Class<?> baseClass, String typeField) {
    this.baseClass = baseClass;
    this.typeField = typeField;
  }
  
  public void registerSubclass(String typeName, Class<?> subclass) {
    subclasses.put(typeName, subclass);
  }
  
  public Class<?> getSubclass(Map<String,Object> jsonObject) {
    String typeName = (String) jsonObject.get(typeField);
    Class< ? > subclass = subclasses.get(typeName);
    if (subclass==null) {
      throw new RuntimeException("Unknown subclass "+typeField+":"+typeName+" of "+baseClass.getName()+": object is "+jsonObject);
    }
    return subclass;
  }

  public Class<?> getSubclass(BpmnReader r) {
    String typeName = r.readStringAttributeEffektif(typeField);
    return subclasses.get(typeName);
  }

  public String getTypeField() {
    return typeField;
  }

}
