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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnMappable;
import com.effektif.workflow.api.mapper.BpmnTypeAttribute;
import com.effektif.workflow.api.mapper.TypeName;
import com.effektif.workflow.api.mapper.Writable;
import com.effektif.workflow.api.mapper.Writer;
import com.effektif.workflow.api.mapper.XmlElement;
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
  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappingsByClass = new HashMap<>();
  Map<String, List<BpmnTypeMapping>> bpmnTypeMappingsByElement = new HashMap<>();
  Map<Class<?>, BpmnFieldMappingsImpl> bpmnFieldMappings = new HashMap<>();

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
    BpmnFieldMappingsImpl bpmnFieldMappings = null;
    if (BpmnMappable.class.isAssignableFrom(subClass)) {
      bpmnFieldMappings = registerBpmnMappable(subClass);
    }
    if (Activity.class.isAssignableFrom(subClass)) {
      BpmnElement bpmnElement = subClass.getAnnotation(BpmnElement.class);
      if (bpmnElement!=null) {
        BpmnTypeMapping bpmnTypeMapping = new BpmnTypeMapping();
        String elementName = bpmnElement.value();
        bpmnTypeMapping.setBpmnElementName(elementName);
        bpmnTypeMapping.setType(subClass);
        bpmnTypeMapping.setBpmnFieldMappings(bpmnFieldMappings);
        Annotation[] annotations = subClass.getAnnotations();
        for (Annotation annotation: annotations) {
          if (annotation instanceof BpmnTypeAttribute) {
            BpmnTypeAttribute bpmnTypeAttribute = (BpmnTypeAttribute) annotation;
            bpmnTypeMapping.addBpmnTypeAttribute(bpmnTypeAttribute.attribute(), bpmnTypeAttribute.value());
          }
        }
        bpmnTypeMappingsByClass.put(subClass, bpmnTypeMapping);

        List<BpmnTypeMapping> typeMappings = bpmnTypeMappingsByElement.get(elementName);
        if (typeMappings==null) {
          typeMappings = new ArrayList<>();
          bpmnTypeMappingsByElement.put(elementName, typeMappings);
        }
        typeMappings.add(bpmnTypeMapping);
      } else {
        // throw new RuntimeException("No bpmn element specified on "+subclass);
      }
    }
    if (BpmnMappable.class.isAssignableFrom(subClass)) {
      registerBpmnMappable(subClass);
    }
  }

  protected BpmnFieldMappingsImpl registerBpmnMappable(Class< ? > bpmnMappableType) {
    try {
      BpmnMappable bpmnMappable = (BpmnMappable) bpmnMappableType.newInstance();
      BpmnFieldMappingsImpl bpmnFieldMappingsImpl = new BpmnFieldMappingsImpl();
      bpmnMappable.initializeBpmnFieldMappings(bpmnFieldMappingsImpl);
      this.bpmnFieldMappings.put(bpmnMappableType, bpmnFieldMappingsImpl);
      return bpmnFieldMappingsImpl;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public BpmnFieldMappingsImpl getBpmnNameMappings(Class<?> type) {
    return bpmnFieldMappings.get(type);
  }
  
  public BpmnTypeMapping getBpmnTypeMapping(XmlElement activityXml, BpmnReader bpmnReader) {
    List<BpmnTypeMapping> typeMappings = bpmnTypeMappingsByElement.get(activityXml.getName());
    if (typeMappings!=null) {
      if (typeMappings.size()==1) {
        return typeMappings.get(0);
      } else if (!typeMappings.isEmpty()) {
        for (BpmnTypeMapping typeMapping: typeMappings) {
          if (isBpmnTypeAttributeMatch(activityXml, typeMapping, bpmnReader)) {
            return typeMapping;
          }
        }
      }
    }
    return null;
  }

  private boolean isBpmnTypeAttributeMatch(XmlElement xmlElement, BpmnTypeMapping typeMapping, BpmnReader bpmnReader) {
    Map<String, String> bpmnTypeAttributes = typeMapping.getBpmnTypeAttributes();
    if (bpmnTypeAttributes==null) {
      return true;
    }
    for (String attributeName: bpmnTypeAttributes.keySet()) {
      String typeValue = bpmnTypeAttributes.get(attributeName);
      // get leaves the attribute value in the xml element
      String xmlValue = bpmnReader.getEffektifAttribute(attributeName);
      if (typeValue.equals(xmlValue)) {
        // only if there is a match we read (==remove) the the attribute from the xml element
        bpmnReader.readEffektifAttribute(attributeName);
        return true;
      }
    }
    return false;
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
    return bpmnTypeMappingsByClass.get(subClass);
  }

  public void writeTypeField(Writer writer, Writable o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      writer.writeString(typeField.getTypeField(), typeField.getTypeName());
    }
  }
}
