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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnTypeAttribute;
import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.mapper.JsonWriter;
import com.effektif.workflow.api.mapper.TypeName;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.bpmn.Bpmn;
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
  Map<Class<?>, Map<String,Type>> fieldTypes = new HashMap<>();

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
        String elementName = bpmnElement.value();
        bpmnTypeMapping.setBpmnElementName(elementName);
        bpmnTypeMapping.setType(subClass);
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
  }

  public BpmnTypeMapping getBpmnTypeMapping(XmlElement activityXml, BpmnReaderImpl bpmnReaderImpl) {
    List<BpmnTypeMapping> typeMappings = bpmnTypeMappingsByElement.get(activityXml.getName());
    if (typeMappings!=null) {
      if (typeMappings.size()==1) {
        return typeMappings.get(0);
      } else if (!typeMappings.isEmpty()) {
        for (BpmnTypeMapping typeMapping: typeMappings) {
          if (isBpmnTypeAttributeMatch(activityXml, typeMapping, bpmnReaderImpl)) {
            return typeMapping;
          }
        }
      }
    }
    return null;
  }

  private boolean isBpmnTypeAttributeMatch(XmlElement xmlElement, BpmnTypeMapping typeMapping, BpmnReaderImpl bpmnReaderImpl) {
    Map<String, String> bpmnTypeAttributes = typeMapping.getBpmnTypeAttributes();
    if (bpmnTypeAttributes==null) {
      return true;
    }
    for (String localPart: bpmnTypeAttributes.keySet()) {
      String typeValue = bpmnTypeAttributes.get(localPart);
      // get the attribute value in the xml element
      String xmlValue = xmlElement.getAttribute(Bpmn.EFFEKTIF_URI, localPart);
      if (typeValue.equals(xmlValue)) {
        // only if there is a match we read (==remove) the the attribute from the xml element
        xmlElement.removeAttribute(Bpmn.EFFEKTIF_URI, localPart);
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

  public void writeTypeField(JsonWriter jsonWriter, JsonWritable o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      jsonWriter.writeString(typeField.getTypeField(), typeField.getTypeName());
    }
  }
  
  public synchronized Type getFieldType(Class< ? > clazz, String fieldName) {
    // could be cached in this mappings object
    Type fieldType = getFieldTypeFromCache(clazz, fieldName);
    if (fieldType!=null) {
      return fieldType;
    }
    Map<String,Type> fieldTypesForClass = fieldTypes.get(clazz);
    if (fieldTypesForClass==null) {
      fieldTypesForClass = new HashMap<>();
      fieldTypes.put(clazz, fieldTypesForClass);
    }
    fieldType = findFieldType(clazz, fieldName);
    if (fieldType==null) {
      throw new RuntimeException("Field "+clazz.getName()+"."+fieldName+" not found");
    }
    fieldTypesForClass.put(fieldName, fieldType);
    return fieldType;
  }

  private Type findFieldType(Class< ? > clazz, String fieldName) {
    try {
      for (Field field: clazz.getDeclaredFields()) {
        if (field.getName().equals(fieldName)) {
          return field.getGenericType();
        }
      }
      if (clazz.getSuperclass()!=Object.class) {
        return findFieldType(clazz.getSuperclass(), fieldName);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private Type getFieldTypeFromCache(Class< ? > type, String fieldName) {
    Map<String,Type> types = fieldTypes.get(type);
    if (types==null) {
      return null;
    }
    return types.get(fieldName);
  }
}
