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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnMappable;
import com.effektif.workflow.api.mapper.BpmnTypeAttribute;
import com.effektif.workflow.api.mapper.TypeName;
import com.effektif.workflow.api.mapper.Writable;
import com.effektif.workflow.api.mapper.Writer;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.mapper.deprecated.SubclassMapping;
import com.effektif.workflow.impl.mapper.deprecated.TypeField;


/**
 * @author Tom Baeyens
 */
public class Mappings {

  Map<Class<?>, SubclassMapping> subclassMappings = new HashMap<>();
  Map<Class<?>, TypeField> typeFields = new HashMap<>();
  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappings = new HashMap<>();
  Map<Class<?>, BpmnMappingsImpl> bpmnNameMappings = new HashMap<>();

  public Mappings() {
    registerBpmnMappable(Transition.class);
  }

  public void registerBaseClass(Class<?> baseClass) {
    registerBaseClass(baseClass, "type");
  }

  public void registerBaseClass(Class<?> baseClass, String typeField) {
    SubclassMapping subclassMapping = new SubclassMapping(typeField);
    subclassMappings.put(baseClass, subclassMapping);
  }

  public void registerSubClass(Class<?> subClass) {
    TypeName typeName = subClass.getAnnotation(TypeName.class);
    if (typeName==null) {
      throw new RuntimeException(subClass.getName()+" must declare "+TypeName.class.toString());
    }
    registerSubClass(subClass, typeName.value(), subClass);
    if (Activity.class.isAssignableFrom(subClass)) {
      BpmnElement bpmnElement = subClass.getAnnotation(BpmnElement.class);
      if (bpmnElement!=null) {
        BpmnTypeMapping bpmnTypeMapping = new BpmnTypeMapping();
        bpmnTypeMapping.setBpmnElementName(bpmnElement.value());
        Annotation[] annotations = subClass.getAnnotations();
        for (Annotation annotation: annotations) {
          if (annotation instanceof BpmnTypeAttribute) {
            BpmnTypeAttribute bpmnTypeAttribute = (BpmnTypeAttribute) annotation;
            bpmnTypeMapping.addBpmnTypeAttribute(bpmnTypeAttribute.attribute(), bpmnTypeAttribute.value());
          }
        }
        bpmnTypeMappings.put(subClass, bpmnTypeMapping);
      } else {
        // throw new RuntimeException("No bpmn element specified on "+subclass);
      }
    }
    if (BpmnMappable.class.isAssignableFrom(subClass)) {
      registerBpmnMappable(subClass);
    }
  }

  protected void registerBpmnMappable(Class< ? > bpmnMappableType) {
    try {
      BpmnMappable bpmnMappable = (BpmnMappable) bpmnMappableType.newInstance();
      BpmnMappingsImpl bpmnMappings = new BpmnMappingsImpl();
      bpmnMappable.initializeBpmnMapping(bpmnMappings);
      bpmnNameMappings.put(bpmnMappableType, bpmnMappings);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public BpmnMappingsImpl getBpmnNameMappings(Class<?> type) {
    return bpmnNameMappings.get(type);
  } 

  public void registerSubClass(Class<?> baseClass, String typeName, Class<?> subClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    if (subclassMapping!=null) {
      subclassMapping.registerSubclass(typeName, subClass);
      typeFields.put(subClass, new TypeField(subclassMapping.getTypeField(), typeName));
    }
    Class< ? > superClass = baseClass.getSuperclass();
    if (superClass!=null) {
      registerSubClass(superClass, typeName, subClass);
    }
    for (Class<?> i: baseClass.getInterfaces()) {
      registerSubClass(i, typeName, subClass);
    }
  }

  public <T> Class<T> getConcreteClass(Map<String,Object> jsonObject, Class<T> baseClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    return subclassMapping!=null ? (Class<T>) subclassMapping.getSubclass(jsonObject) : baseClass;
  }

  public BpmnTypeMapping getBpmnTypeMapping(Class<?> subClass) {
    return bpmnTypeMappings.get(subClass);
  }

  public void writeTypeField(Writer writer, Writable o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      writer.writeString(typeField.getTypeField(), typeField.getTypeName());
    }
  }
}
