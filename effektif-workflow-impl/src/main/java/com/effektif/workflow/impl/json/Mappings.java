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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
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
import com.effektif.workflow.api.json.JsonFieldName;
import com.effektif.workflow.api.json.JsonIgnore;
import com.effektif.workflow.api.json.JsonPropertyOrder;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.bpmn.Bpmn;
import com.effektif.workflow.impl.bpmn.BpmnReaderImpl;
import com.effektif.workflow.impl.bpmn.BpmnTypeMapping;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.types.BeanMapper;
import com.effektif.workflow.impl.json.types.BooleanMapper;
import com.effektif.workflow.impl.json.types.ListMapper;
import com.effektif.workflow.impl.json.types.MapMapper;
import com.effektif.workflow.impl.json.types.NumberMapper;
import com.effektif.workflow.impl.json.types.StringMapper;
import com.effektif.workflow.impl.mapper.SubclassMapping;
import com.effektif.workflow.impl.mapper.TypeField;
import com.effektif.workflow.impl.util.Lists;

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

  Map<Class<?>, JsonTypeMapper> jsonTypeMappers = new HashMap<>();

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
    registerTypeMapper(new ListMapper());
    
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
    registerSubClass(StartEvent.class);

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

  public void registerTypeMapper(JsonTypeMapper jsonTypeMapper) {
    jsonTypeMappers.put(jsonTypeMapper.getMappedClass(), jsonTypeMapper);
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

  public void writeTypeField(JsonWriter jsonWriter, Object o) {
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
          JsonTypeMapper jsonTypeMapper = getTypeMapper(fieldType);
          FieldMapping fieldMapping = new FieldMapping(field, jsonTypeMapper);

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

  private static final Set<String> NUMBERTYPENAMES = new HashSet<>(
          Lists.of("short", "int", "long", "float", "double"));
  public JsonTypeMapper getTypeMapper(Class< ? > clazz) {
    JsonTypeMapper jsonTypeMapper = jsonTypeMappers.get(clazz);
    if (jsonTypeMapper!=null) {
      return jsonTypeMapper;
    }
    if (String.class==clazz) {
      jsonTypeMapper = StringMapper.INSTANCE;
    } else if (List.class.isAssignableFrom(clazz)) {
      jsonTypeMapper = ListMapper.INSTANCE;
    } else if (Map.class.isAssignableFrom(clazz)) {
      jsonTypeMapper = MapMapper.INSTANCE;
    } else if (Number.class.isAssignableFrom(clazz)
               || NUMBERTYPENAMES.contains(clazz.getName())) {
      jsonTypeMapper = NumberMapper.INSTANCE;
    } else if ( Boolean.class==clazz
                || boolean.class==clazz ) {
      jsonTypeMapper = BooleanMapper.INSTANCE;
    } else {
      jsonTypeMapper = BeanMapper.INSTANCE;
    }
    jsonTypeMappers.put(clazz, jsonTypeMapper);
    return jsonTypeMapper;
  }

  public JsonTypeMapper getTypeMapper(Object jsonValue, Type type) {
    return getTypeMapper((Class)type);
  }
}