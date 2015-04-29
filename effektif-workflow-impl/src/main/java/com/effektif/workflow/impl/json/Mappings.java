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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.json.JsonFieldName;
import com.effektif.workflow.api.json.JsonIgnore;
import com.effektif.workflow.api.json.JsonPropertyOrder;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.bpmn.Bpmn;
import com.effektif.workflow.impl.bpmn.BpmnReaderImpl;
import com.effektif.workflow.impl.bpmn.BpmnTypeMapping;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.types.BeanMapper;
import com.effektif.workflow.impl.json.types.BooleanMapper;
import com.effektif.workflow.impl.json.types.ListMapper;
import com.effektif.workflow.impl.json.types.MapMapper;
import com.effektif.workflow.impl.json.types.NumberMapper;
import com.effektif.workflow.impl.json.types.StringMapper;
import com.effektif.workflow.impl.json.types.ValueMapper;
import com.effektif.workflow.impl.util.Lists;

/**
 * Registry for API model classes, used to determine their serialisations.
 *
 * @author Tom Baeyens
 */
public class Mappings {
  
  private static final Logger log = LoggerFactory.getLogger(Mappings.class);
  
  Map<Class<?>, JsonTypeMapper> jsonTypeMappers = new HashMap<>();

  /** Maps registered base classes (e.g. <code>Trigger</code> to their subclass mappings. */
  Map<Class<?>, SubclassMapping> subclassMappings = new HashMap<>();
  Map<Class<?>, List<FieldMapping>> fieldMappings = new HashMap<>();

  Map<Class<?>, TypeField> typeFields = new HashMap<>();
  Map<Class<?>, Map<String,java.lang.reflect.Type>> fieldTypes = new HashMap<>();

  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappingsByClass = new HashMap<>();
  Map<String, List<BpmnTypeMapping>> bpmnTypeMappingsByElement = new HashMap<>();

  public Mappings() {
    registerBaseClass(Activity.class);
    ServiceLoader<ActivityType> activityTypeLoader = ServiceLoader.load(ActivityType.class);
    for (ActivityType activityType: activityTypeLoader) {
      registerSubClass(activityType.getActivityApiClass());
    }

    registerBaseClass(Type.class, "name");
    ServiceLoader<DataType> dataTypeLoader = ServiceLoader.load(DataType.class);
    for (DataType dataType: dataTypeLoader) {
      registerSubClass(dataType.getApiClass());
    }

    registerBaseClass(Condition.class);
    ServiceLoader<ConditionImpl> conditionLoader = ServiceLoader.load(ConditionImpl.class);
    for (ConditionImpl condition: conditionLoader) {
      registerSubClass(condition.getApiType());
    }

    registerBaseClass(Trigger.class);
    registerBaseClass(JobType.class);
  }

  public void registerTypeMapper(JsonTypeMapper jsonTypeMapper) {
    jsonTypeMappers.put(jsonTypeMapper.getMappedClass(), jsonTypeMapper);
  }

  public void registerBaseClass(Class<?> baseClass) {
    registerBaseClass(baseClass, "type");
  }

  public void registerBaseClass(Class<?> baseClass, String typeField) {
    SubclassMapping subclassMapping = new SubclassMapping(baseClass, typeField);
    subclassMappings.put(baseClass, subclassMapping);
  }

  public void registerSubClass(Class<?> subClass) {
    if (subClass==null) {
      return;
    }
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
   * Sets the mapping from the given model class field name to a JSON field name, used for (de)serialisation.
   */
  public void setJsonFieldName(Class<?> modelClass, String fieldName, String jsonFieldName) {
    FieldMapping fieldMapping = findFieldMapping(modelClass, fieldName);
    if (fieldMapping!=null) {
      fieldMapping.jsonFieldName = jsonFieldName;
    }
  }

  private FieldMapping findFieldMapping(Class< ? > modelClass, String fieldName) {
    List<FieldMapping> fieldMappings = getFieldMappings(modelClass);
    if (fieldMappings!=null) {
      for (FieldMapping fieldMapping: fieldMappings) {
        if (fieldMapping.field.getName().equals(fieldName)) {
          return fieldMapping;
        }
      }
    }
    return null;
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
          Type fieldType = field.getGenericType();
          JsonTypeMapper jsonTypeMapper = getTypeMapper(fieldType);
          
          log.debug(clazz.getSimpleName()+"."+field.getName()+" --> "+jsonTypeMapper);

          
          FieldMapping fieldMapping = new FieldMapping(field, jsonTypeMapper);

          // Annotation-based field name override.
          JsonFieldName jsonFieldNameAnnotation = field.getAnnotation(JsonFieldName.class);
          if (jsonFieldNameAnnotation != null) {
            fieldMapping.setJsonFieldName(jsonFieldNameAnnotation.value());
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
  
  public JsonTypeMapper getTypeMapper(Object jsonValue, Type type) {
    return getTypeMapper((Class)type);
  }

  public JsonTypeMapper getTypeMapper(Type type) {
    if (type==null || type==Object.class) {
      return ValueMapper.INSTANCE;
    }

    JsonTypeMapper jsonTypeMapper = jsonTypeMappers.get(type);
    if (jsonTypeMapper!=null) {
      return jsonTypeMapper;
    }
    
    Class<?> clazz = type instanceof Class ? (Class<?>) type : null;

    if (String.class==type) {
      jsonTypeMapper = StringMapper.INSTANCE;
    
    } else if ( Boolean.class==type
                || boolean.class==type ) {
      jsonTypeMapper = BooleanMapper.INSTANCE;
    
    } else if (isNumberClass(clazz)) {
      jsonTypeMapper = NumberMapper.INSTANCE;

    } else {
      
      if (clazz==null) {
        if (type instanceof ParameterizedType) {
          ParameterizedType parameterizedType = (ParameterizedType) type;
          clazz = (Class< ? >) parameterizedType.getRawType();
        } else if (type instanceof WildcardType) {
          WildcardType wildcardType = (WildcardType) type;
          clazz = (Class< ? >) wildcardType.getUpperBounds()[0];
        }
      }
      
      if (List.class.isAssignableFrom(clazz)) {
        Type elementType = getTypeArg(type, 0);
        JsonTypeMapper elementMapper = getTypeMapper(elementType);
        jsonTypeMapper = new ListMapper(elementMapper); 
        
      } else if (Map.class.isAssignableFrom(clazz)) {
        Type valuesType = getTypeArg(type, 1);
        JsonTypeMapper valuesMapper = getTypeMapper(valuesType);
        jsonTypeMapper = new MapMapper(valuesMapper);
        
      } else {
        jsonTypeMapper = new BeanMapper(type);
      }
    }

    jsonTypeMappers.put(clazz, jsonTypeMapper);
    return jsonTypeMapper;
  }

  private Type getTypeArg(Type type, int i) {
    if (type instanceof ParameterizedType) {
      Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
      if (typeArgs!=null && i<typeArgs.length) {
        return typeArgs[i];
      }
    }
    return null;
  }

  private static final Set<String> NUMBERTYPENAMES = new HashSet<>(
          Lists.of("byte", "short", "int", "long", "float", "double"));
  private boolean isNumberClass(Class< ? > clazz) {
    if (clazz==null) {
      return false;
    }
    return Number.class.isAssignableFrom(clazz)
      || NUMBERTYPENAMES.contains(clazz.getName());
  }

}