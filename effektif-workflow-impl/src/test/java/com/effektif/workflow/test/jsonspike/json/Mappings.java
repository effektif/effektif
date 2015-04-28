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
package com.effektif.workflow.test.jsonspike.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.condition.And;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.Contains;
import com.effektif.workflow.api.condition.ContainsIgnoreCase;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.condition.EqualsIgnoreCase;
import com.effektif.workflow.api.condition.GreaterThan;
import com.effektif.workflow.api.condition.GreaterThanOrEqual;
import com.effektif.workflow.api.condition.HasNoValue;
import com.effektif.workflow.api.condition.HasValue;
import com.effektif.workflow.api.condition.IsFalse;
import com.effektif.workflow.api.condition.IsTrue;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.condition.LessThanOrEqual;
import com.effektif.workflow.api.condition.Not;
import com.effektif.workflow.api.condition.NotContains;
import com.effektif.workflow.api.condition.NotContainsIgnoreCase;
import com.effektif.workflow.api.condition.NotEquals;
import com.effektif.workflow.api.condition.NotEqualsIgnoreCase;
import com.effektif.workflow.api.condition.Or;
import com.effektif.workflow.api.serialization.bpmn.BpmnElement;
import com.effektif.workflow.api.serialization.bpmn.BpmnReader;
import com.effektif.workflow.api.serialization.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.serialization.bpmn.BpmnWriter;
import com.effektif.workflow.api.serialization.bpmn.XmlElement;
import com.effektif.workflow.api.serialization.json.JsonIgnore;
import com.effektif.workflow.api.serialization.json.JsonPropertyOrder;
import com.effektif.workflow.api.serialization.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.mapper.Bpmn;
import com.effektif.workflow.impl.mapper.BpmnReaderImpl;
import com.effektif.workflow.impl.mapper.BpmnTypeMapping;
import com.effektif.workflow.impl.mapper.SubclassMapping;
import com.effektif.workflow.impl.mapper.TypeField;
import com.effektif.workflow.test.jsonspike.json.typemappers.BeanTypeMapper;
import com.effektif.workflow.test.jsonspike.json.typemappers.ListTypeMapper;
import com.effektif.workflow.test.jsonspike.json.typemappers.StringMapper;

/**
 * Registry for API model classes, used to determine their serialisations.
 *
 * @author Tom Baeyens
 */
public class Mappings {
  
  public String getTypeField(Class<?> clazz) {
    SubclassMapping subclassMapping = null;
    while (subclassMapping==null && clazz!=null) {
      subclassMapping = subclassMappings.get(clazz);
      if (subclassMapping==null) {
        clazz = clazz.getSuperclass();
      }
    }
    return subclassMapping.getTypeField();
  }

  Map<Class<?>, TypeMapper> typeMappers = new HashMap<>();

  /** Maps registered base classes (e.g. <code>Trigger</code> to their subclass mappings. */
  Map<Class<?>, SubclassMapping> subclassMappings = new HashMap<>();
  Map<Class<?>, List<FieldMapping>> fieldMappings = new HashMap<>();

  Map<Class<?>, TypeField> typeFields = new HashMap<>();
  Map<Class<?>, Map<String,java.lang.reflect.Type>> fieldTypes = new HashMap<>();

  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappingsByClass = new HashMap<>();
  Map<String, List<BpmnTypeMapping>> bpmnTypeMappingsByElement = new HashMap<>();

  /** Maps, for each registered class, the Java field name to the corresponding JSON field name. */
  Map<Class<?>,Map<String,String>> jsonFieldNames = new HashMap<>();

  public Mappings() {
    
    registerTypeMapper(new StringMapper());
    registerTypeMapper(new ListTypeMapper());
    
    registerBaseClass(Type.class, "name");
    registerBaseClass(Trigger.class);

    registerBaseClass(Activity.class);
    registerSubClass(Call.class);
    registerSubClass(EmbeddedSubprocess.class);
    registerSubClass(EndEvent.class);
    registerSubClass(ExclusiveGateway.class);
    registerSubClass(HttpServiceTask.class);
    registerSubClass(JavaServiceTask.class);
    registerSubClass(NoneTask.class);
    registerSubClass(ParallelGateway.class);
    registerSubClass(ReceiveTask.class);

    registerBaseClass(Condition.class);
    registerSubClass(And.class);
    registerSubClass(Or.class);
    registerSubClass(Not.class);
    registerSubClass(Contains.class);
    registerSubClass(NotContains.class);
    registerSubClass(ContainsIgnoreCase.class);
    registerSubClass(NotContainsIgnoreCase.class);
    registerSubClass(EqualsIgnoreCase.class);
    registerSubClass(NotEqualsIgnoreCase.class);
    registerSubClass(Equals.class);
    registerSubClass(NotEquals.class);
    registerSubClass(GreaterThan.class);
    registerSubClass(GreaterThanOrEqual.class);
    registerSubClass(LessThan.class);
    registerSubClass(LessThanOrEqual.class);
    registerSubClass(HasNoValue.class);
    registerSubClass(HasValue.class);
    registerSubClass(IsFalse.class);
    registerSubClass(IsTrue.class);

    registerBaseClass(JobType.class);
  }

  public void registerTypeMapper(TypeMapper typeMapper) {
    typeMappers.put(typeMapper.getMappedClass(), typeMapper);
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
    if (typeName!=null) {
      registerSubClass(subClass, typeName.value(), subClass);
      if (Activity.class.isAssignableFrom(subClass) || Condition.class.isAssignableFrom(subClass)) {
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
    } else {
      for (Class<?> baseClass: subclassMappings.keySet()) {
        if (baseClass.isAssignableFrom(subClass)) {
          throw new RuntimeException(subClass.getName()+" does not declare "+TypeName.class.toString());
        }
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

  public <T> Class<T> getConcreteClass(BpmnReader bpmnReader, Class<T> baseClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    return subclassMapping!=null ? (Class<T>) subclassMapping.getSubclass(bpmnReader) : baseClass;
  }

  public BpmnTypeMapping getBpmnTypeMapping(Class<?> subClass) {
    if (!bpmnTypeMappingsByClass.containsKey(subClass)) {
      throw new IllegalArgumentException("No BPMN type mapping defined for " + subClass.getName());
    }
    return bpmnTypeMappingsByClass.get(subClass);
  }

  public void writeTypeField(JsonFieldWriter jsonWriter, Object o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      jsonWriter.writeFieldName(typeField.getTypeField());
      jsonWriter.writeString(typeField.getTypeName());
    }
  }
  
  public void writeTypeAttribute(BpmnWriter bpmnWriter, Object o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      bpmnWriter.writeStringAttributeEffektif(typeField.getTypeField(), typeField.getTypeName());
    }
  }
  
  public <T> FieldMapping<T> findFieldMapping(Class<?> clazz, String fieldName) {
    // TODO  
    // use getFieldMappings(Class<?> clazz) // this will automatically scan the class if that's not yet done.
    return null;
  }

  /**
   * Returns the JSON field name for the given model class field name, used for (de)serialisation.
   */
  public boolean definesJsonFieldName(Class<?> modelClass, String fieldName) {
    return jsonFieldNames.containsKey(modelClass) && jsonFieldNames.get(modelClass).containsKey(fieldName);
  }

  /**
   * Returns the JSON field name for the given model class field name, used for (de)serialisation.
   */
  public String getJsonFieldName(Class<?> modelClass, String fieldName) {
    if (!jsonFieldNames.containsKey(modelClass)) {
      throw new IllegalArgumentException("No field mappings registered for class " + modelClass.getName());
    }
    if (!jsonFieldNames.get(modelClass).containsKey(fieldName)) {
      throw new IllegalArgumentException(String.format("No mapping for field %s in class %s", fieldName, modelClass.getName()));
    }
    // TODO update scanFields below, add annotation support @JsonFieldName("_id") (annotation already created in this package)
    return jsonFieldNames.get(modelClass).get(fieldName);
  }

  /**
   * Sets the mapping from the given model class field name to a JSON field name, used for (de)serialisation.
   */
  public void setJsonFieldName(Class<?> modelClass, String fieldName, String jsonFieldName) {
    if (!jsonFieldNames.containsKey(modelClass)) {
      jsonFieldNames.put(modelClass, new HashMap<String, String>());
    }
    jsonFieldNames.get(modelClass).put(fieldName, jsonFieldName);
  }

  public synchronized java.lang.reflect.Type getFieldType(Class< ? > clazz, String fieldName) {
    // could be cached in this mappings object
    java.lang.reflect.Type fieldType = getFieldTypeFromCache(clazz, fieldName);
    if (fieldType!=null) {
      return fieldType;
    }
    Map<String,java.lang.reflect.Type> fieldTypesForClass = fieldTypes.get(clazz);
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

  private java.lang.reflect.Type findFieldType(Class< ? > clazz, String fieldName) {
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

  private java.lang.reflect.Type getFieldTypeFromCache(Class< ? > type, String fieldName) {
    Map<String,java.lang.reflect.Type> types = fieldTypes.get(type);
    if (types==null) {
      return null;
    }
    return types.get(fieldName);
  }

  public List<FieldMapping> getFieldMappings(Class<?> clazz) {
    List<FieldMapping> classFieldMappings = fieldMappings.get(clazz);
    if (classFieldMappings!=null) {
      return classFieldMappings;
    }
    classFieldMappings = new ArrayList<>();
    scanFields(classFieldMappings, clazz);
    
    JsonPropertyOrder jsonPropertyOrder = clazz.getAnnotation(JsonPropertyOrder.class);
    if (jsonPropertyOrder!=null) {
      String[] fieldNamesOrder = jsonPropertyOrder.value();
      for (int i=fieldNamesOrder.length-1; i>=0; i--) {
        String fieldName = fieldNamesOrder[i];
        FieldMapping fieldMapping = removeField(classFieldMappings, fieldName);
        if (fieldMapping!=null) {
          classFieldMappings.add(0, fieldMapping);
        }
      }
    }
    
    fieldMappings.put(clazz, classFieldMappings);
    return classFieldMappings;
  }

  private FieldMapping removeField(List<FieldMapping> fieldMappings, String fieldName) {
    Iterator<FieldMapping> iterator = fieldMappings.iterator();
    while (iterator.hasNext()) {
      FieldMapping fieldMapping = iterator.next();
      if (fieldMapping.getFieldName().equals(fieldName)) {
        iterator.remove();
        return fieldMapping;
      }
    }
    return null;
  }

  public void scanFields(List<FieldMapping> fieldMappings, Class< ? > clazz) {
    Field[] declaredFields = clazz.getDeclaredFields();
    if (declaredFields!=null) {
      for (Field field: declaredFields) {
        boolean includeInJsonSerialisation = field.getAnnotation(JsonIgnore.class) == null;
        if (!Modifier.isStatic(field.getModifiers()) && includeInJsonSerialisation) {
          field.setAccessible(true);
          Class< ? > fieldType = field.getType();
          TypeMapper typeMapper = getTypeMapper(fieldType);
          FieldMapping fieldMapping = new FieldMapping(field, typeMapper);

          // Annotation-based field name override.
          JsonFieldName jsonFieldNameAnnotation = field.getAnnotation(JsonFieldName.class);
          if (jsonFieldNameAnnotation != null) {
            fieldMapping.setJsonFieldName(jsonFieldNameAnnotation.value());
          }

          // Programmatic field name override.
          // TODO test @JsonFieldName to see if this works
          if (definesJsonFieldName(fieldType, fieldMapping.getFieldName())) {
            fieldMapping.setJsonFieldName(getJsonFieldName(fieldType, fieldMapping.getFieldName()));
          }

          fieldMappings.add(fieldMapping);
        }
      }
    }
    Class<? > superclass = clazz.getSuperclass();
    if (Object.class!=superclass) {
      scanFields(fieldMappings, superclass);
    }
  }

  public TypeMapper getTypeMapper(Class< ? > clazz) {
    TypeMapper typeMapper = typeMappers.get(clazz);
    if (typeMapper!=null) {
      return typeMapper;
    }
    if (List.class.isAssignableFrom(clazz)) {
      typeMapper = ListTypeMapper.INSTANCE;
    } else {
      typeMapper = BeanTypeMapper.INSTANCE;
    }
    typeMappers.put(clazz, typeMapper);
    return typeMapper;
  }

  public TypeMapper getTypeMapper(Object jsonValue, Type type) {
    return getTypeMapper((Class)type);
  }
}