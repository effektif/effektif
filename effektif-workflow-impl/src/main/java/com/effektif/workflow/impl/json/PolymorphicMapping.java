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
import java.util.Set;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.impl.bpmn.BpmnReaderImpl;

/**
 * A mapping from a ‘base class’, e.g. {@link DataType}, to its subclasses (e.g. {@link TextType}).
 *
 * @author Tom Baeyens
 */
public class PolymorphicMapping extends BeanMapping {

  Class<?> baseClass;
  String typeField;
  Map<String,TypeMapping> typeMappingsByName;
  Map<Class,TypeMapping> typeMappingsByClass;

  public PolymorphicMapping(Class<?> baseClass, String typeField) {
    this.baseClass = baseClass;
    this.typeField = typeField;
    this.typeMappingsByName = new HashMap<>();
    this.typeMappingsByClass = new HashMap<>();
  }

  public void registerSubtypeMapping(String typeName, Class<?> subClass, TypeMapping typeMapping) {
    typeMappingsByName.put(typeName, typeMapping);
    typeMappingsByClass.put(subClass, typeMapping);
  }

  public String getTypeField() {
    return typeField;
  }

  @Override
  public TypeMapping getTypeMapping(Map<String, Object> jsonObject) {
    String typeName = (String) jsonObject.remove(typeField);
    return getTypeMapping(typeName);
  }

  public TypeMapping getTypeMapping(BpmnReaderImpl bpmnReader) {
    String typeName = bpmnReader.readStringAttributeEffektif(typeField);
    return getTypeMapping(typeName);
  }

  public TypeMapping getTypeMapping(String typeName) {
    TypeMapping typeMapping = typeMappingsByName.get(typeName);
    if (typeMapping==null) {
      throw new RuntimeException("Unknown subclass " + typeField + " ‘" + typeName + "’ of " + baseClass);
    }
    return typeMapping;
  }
  
  public Set<Class> getSubClasses() {
    return typeMappingsByClass.keySet();
  }

  @Override
  public TypeMapping getTypeMapping(Class< ? > beanClass) {
    return typeMappingsByClass.get(beanClass);
  }

  public Class<?> getSubclass(BpmnReader r) {
    String typeName = r.readStringAttributeEffektif(typeField);
    return typeMappingsByName.get(typeName).getRawClass();
  }

  public Class< ? > getBaseClass() {
    return baseClass;
  }
}
