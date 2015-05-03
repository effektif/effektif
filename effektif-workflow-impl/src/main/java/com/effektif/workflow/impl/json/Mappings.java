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
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.json.GenericType;
import com.effektif.workflow.api.json.JsonFieldName;
import com.effektif.workflow.api.json.JsonIgnore;
import com.effektif.workflow.api.json.JsonPropertyOrder;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.bpmn.Bpmn;
import com.effektif.workflow.impl.bpmn.BpmnReaderImpl;
import com.effektif.workflow.impl.bpmn.BpmnTypeMapping;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.types.ObjectType;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.types.ArrayMapperFactory;
import com.effektif.workflow.impl.json.types.BeanMapper;
import com.effektif.workflow.impl.json.types.BindingMapperFactory;
import com.effektif.workflow.impl.json.types.BooleanMapper;
import com.effektif.workflow.impl.json.types.ClassMapper;
import com.effektif.workflow.impl.json.types.EnumMapperFactory;
import com.effektif.workflow.impl.json.types.ListMapperFactory;
import com.effektif.workflow.impl.json.types.MapMapperFactory;
import com.effektif.workflow.impl.json.types.NumberMapperFactory;
import com.effektif.workflow.impl.json.types.PolymorphicBeanMapper;
import com.effektif.workflow.impl.json.types.StringMapper;
import com.effektif.workflow.impl.json.types.TypedValueMapperFactory;
import com.effektif.workflow.impl.json.types.ValueMapper;
import com.effektif.workflow.impl.json.types.VariableInstanceMapperFactory;
import com.effektif.workflow.impl.util.Reflection;

/**
 * Registry for API model classes, used to determine their serialisations.
 *
 * @author Tom Baeyens
 */
public class Mappings {
  
  private static final Logger log = LoggerFactory.getLogger(Mappings.class);
  
  List<JsonTypeMapperFactory> jsonTypeMapperFactories = new ArrayList<>();
  /** Json type mappers are the SPI to plug in support for particular types */ 
  Map<Type, JsonTypeMapper> jsonTypeMappers = new HashMap<>();
  Map<Type,DataType> dataTypesByClass = new HashMap<>();

  /** Maps registered base classes (like e.g. <code>Activity</code>) to *unparameterized* polymorphic mappings.
   * Polymorphic parameterized types are not yet supported */
  Map<Class<?>, PolymorphicMapping> polymorphicMappings = new HashMap<>();
  /** Type mappings contain the field mappings for each type.  Types can be parameterized. */ 
  Map<Type, TypeMapping> typeMappings = new HashMap<>();

  Map<Class<?>, TypeField> typeFields = new HashMap<>();
  Map<Class<?>, Map<String,Type>> fieldTypes = new HashMap<>();

  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappingsByClass = new HashMap<>();
  Map<String, List<BpmnTypeMapping>> bpmnTypeMappingsByElement = new HashMap<>();

  public Mappings() {
    registerBaseClass(Trigger.class);
    registerBaseClass(JobType.class);
    registerBaseClass(Activity.class);
    registerBaseClass(Condition.class);
    registerBaseClass(DataType.class, "name");
    
    registerTypeMapperFactory(new ValueMapper());
    registerTypeMapperFactory(new StringMapper());
    registerTypeMapperFactory(new BooleanMapper());
    registerTypeMapperFactory(new ClassMapper());
    registerTypeMapperFactory(new NumberMapperFactory());
    registerTypeMapperFactory(new VariableInstanceMapperFactory());
    registerTypeMapperFactory(new TypedValueMapperFactory());
    registerTypeMapperFactory(new EnumMapperFactory());
    registerTypeMapperFactory(new ArrayMapperFactory());
    registerTypeMapperFactory(new ListMapperFactory());
    registerTypeMapperFactory(new MapMapperFactory());
    registerTypeMapperFactory(new BindingMapperFactory());
    
    ServiceLoader<ActivityType> activityTypeLoader = ServiceLoader.load(ActivityType.class);
    for (ActivityType activityType: activityTypeLoader) {
      registerSubClass(activityType.getActivityApiClass());
    }

    ServiceLoader<ConditionImpl> conditionLoader = ServiceLoader.load(ConditionImpl.class);
    for (ConditionImpl condition: conditionLoader) {
      registerSubClass(condition.getApiType());
    }

    ServiceLoader<DataTypeImpl> dataTypeLoader = ServiceLoader.load(DataTypeImpl.class);
    for (DataTypeImpl dataTypeImpl: dataTypeLoader) {
      try {
        Class<? extends DataType> apiClass = dataTypeImpl.getApiClass();
        if (apiClass!=null) {
          registerSubClass(apiClass);
          DataType dataType = apiClass.newInstance();
          dataTypesByClass.put(dataType.getValueType(), dataType);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    // potentially multiple datatypes may map to eg String. 
    // by re-putting these datatypes, we ensure that these basic
    // data types are used when looking up a datatype by value
    dataTypesByClass.put(String.class, TextType.INSTANCE);
    dataTypesByClass.put(Boolean.class, BooleanType.INSTANCE);
    dataTypesByClass.put(Byte.class, NumberType.INSTANCE);
    dataTypesByClass.put(Short.class, NumberType.INSTANCE);
    dataTypesByClass.put(Integer.class, NumberType.INSTANCE);
    dataTypesByClass.put(Long.class, NumberType.INSTANCE);
    dataTypesByClass.put(Float.class, NumberType.INSTANCE);
    dataTypesByClass.put(Double.class, NumberType.INSTANCE);
    dataTypesByClass.put(BigInteger.class, NumberType.INSTANCE);
    dataTypesByClass.put(BigDecimal.class, NumberType.INSTANCE);
  }

  public void registerTypeMapperFactory(JsonTypeMapperFactory jsonTypeMapperFactory) {
    jsonTypeMapperFactories.add(jsonTypeMapperFactory);
  }

  public void registerBaseClass(Class<?> baseClass) {
    registerBaseClass(baseClass, "type");
  }

  public void registerBaseClass(Class<?> baseClass, String typeField) {
    PolymorphicMapping subclassMapping = new PolymorphicMapping(baseClass, typeField);
    polymorphicMappings.put(baseClass, subclassMapping);
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
      for (Class<?> baseClass: polymorphicMappings.keySet()) {
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
    PolymorphicMapping polymorphicMapping = polymorphicMappings.get(baseClass);
    if (polymorphicMapping!=null) {
      TypeMapping typeMapping = getTypeMapping(subClass);
      polymorphicMapping.registerSubtypeMapping(typeName, subClass, typeMapping);
      typeFields.put(subClass, new TypeField(polymorphicMapping.getTypeField(), typeName));
    }
    Class< ? > superClass = baseClass.getSuperclass();
    if (superClass!=null) {
      registerSubClass(superClass, typeName, subClass);
    }
    for (Class<?> i: baseClass.getInterfaces()) {
      registerSubClass(i, typeName, subClass);
    }
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
    TypeMapping typeMapping = getTypeMapping(modelClass);
    if (typeMapping!=null && typeMapping.fieldMappings!=null) {
      for (FieldMapping fieldMapping: typeMapping.fieldMappings) {
        if (fieldMapping.field.getName().equals(fieldName)) {
          return fieldMapping;
        }
      }
    }
    return null;
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
  
  public DataType getTypeByValue(Object value) {
    if (value==null) {
      return null;
    }
    if (value instanceof Collection) {
      return getTypeByCollection((Collection) value);
    }
    if (value instanceof Map) {
      return getTypeByMap((Map) value);
    }
    Class<?> clazz = value.getClass();
    DataType dataType = dataTypesByClass.get(clazz);
    if (dataType!=null) {
      return dataType;
    }
    return new JavaBeanType(clazz);
  }

  private DataType getTypeByMap(Map map) {
    if (map==null || map.isEmpty()) {
      return null;
    }
    DataType valueType = getTypeByCollection(map.values());
    return new ObjectType(valueType);
  }

  private DataType getTypeByCollection(Collection collection) {
    if (collection==null || collection.isEmpty()) {
      return null;
    }
    Iterator iterator = collection.iterator();
    DataType commonDataType = getTypeByValue(iterator.next());
    if (commonDataType instanceof JavaBeanType) {
      JavaBeanType javaBeanType = (JavaBeanType) commonDataType; 
      while (iterator.hasNext()) {
        Object elementValue = iterator.next();
        Class elementValueClass = elementValue.getClass();
        Class javaBeanClass = javaBeanType.getJavaClass();
        while (!javaBeanClass.isAssignableFrom(elementValueClass)
               && javaBeanClass!=Object.class) {
          javaBeanType.setJavaClass(javaBeanClass.getSuperclass());
        }
      }
    }
    return new ListType(commonDataType);
  }

  public JsonTypeMapper getTypeMapper(Type type) {
    JsonTypeMapper jsonTypeMapper = jsonTypeMappers.get(type);
    if (jsonTypeMapper!=null) {
      log.debug("Found type mapper "+jsonTypeMapper+" in cache for type "+Reflection.getSimpleName(type));
      return jsonTypeMapper;
    }

    log.debug("Creating type mapper for type "+Reflection.getSimpleName(type));

    Class clazz = Reflection.getRawClass(type);
    for (JsonTypeMapperFactory factory: jsonTypeMapperFactories) {
      jsonTypeMapper = factory.createTypeMapper(type, clazz, this);
      if (jsonTypeMapper!=null) {
        break;
      }
    }

    if (jsonTypeMapper==null) {
      PolymorphicMapping polymorphicMapping = getPolymorphicMapping(type);
      if (polymorphicMapping!=null) {
        polymorphicMapping = getParameterizedPolymorphicMapping(type, polymorphicMapping);
        jsonTypeMapper = new PolymorphicBeanMapper(polymorphicMapping);
      } else {
        TypeMapping typeMapping = getTypeMapping(type);
        jsonTypeMapper = new BeanMapper(typeMapping);
      }
    }

    log.debug("Created type mapper "+jsonTypeMapper+" for type "+Reflection.getSimpleName(type));

    jsonTypeMapper.setMappings(this);
    jsonTypeMappers.put(type, jsonTypeMapper);
    return jsonTypeMapper;
  }

  /** finds the most concrete polymorphic mapping that matches the given type. */
  public PolymorphicMapping getPolymorphicMapping(Type type) {
    Class<?> clazz = Reflection.getRawClass(type);
    PolymorphicMapping polymorphicMapping = polymorphicMappings.get(clazz);
    while (polymorphicMapping==null && clazz!=Object.class) {
      clazz = clazz.getSuperclass();
      polymorphicMapping = polymorphicMappings.get(clazz);
    }
    return polymorphicMapping;
  }


  private PolymorphicMapping getParameterizedPolymorphicMapping(Type type, PolymorphicMapping untypedPolymorphicMapping) {
    if (!Reflection.isParameterized(type)) {
      return untypedPolymorphicMapping;
    }
    throw new RuntimeException("TODO polymorphic, parameterized types are not yet supported");
  }

  public TypeMapping getTypeMapping(Type type) {
    TypeMapping typeMapping = typeMappings.get(type);
    if (typeMapping!=null) {
      log.debug("Found type mapping "+typeMapping+" in cache for type "+Reflection.getSimpleName(type));
      return typeMapping;
    }
    log.debug("Creating type mapping for "+Reflection.getSimpleName(type));
    Class<?> clazz = Reflection.getRawClass(type);
    typeMapping = new TypeMapping(clazz);
    typeMappings.put(type, typeMapping);
    scanFieldMappings(type, typeMapping);
    log.debug("Creating type mapping "+typeMapping);
    return typeMapping;
  }

  public void scanFieldMappings(Type type, TypeMapping typeMapping) {
    List<FieldMapping> fieldMappings = new ArrayList<>();
    scanFields(fieldMappings, type);
    Class<?> clazz = Reflection.getRawClass(type);
    JsonPropertyOrder jsonPropertyOrder = clazz.getAnnotation(JsonPropertyOrder.class);
    if (jsonPropertyOrder!=null) {
      String[] fieldNamesOrder = jsonPropertyOrder.value();
      for (int i=fieldNamesOrder.length-1; i>=0; i--) {
        String fieldName = fieldNamesOrder[i];
        FieldMapping fieldMapping = removeField(fieldMappings, fieldName);
        if (fieldMapping!=null) {
          fieldMappings.add(0, fieldMapping);
        }
      }
    }
    typeMapping.setFieldMappings(fieldMappings);
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
  
  public static Type resolveFieldType(TypeVariable fieldType, Class<?> clazz, Type type) {
    Map<String,Type> typeArgs = new HashMap<>();
    TypeVariable< ? >[] typeParameters = clazz.getTypeParameters();
    Type[] actualTypeArguments = null;
    if (type instanceof ParameterizedType) {
      actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments(); 
    } else if (type instanceof GenericType) {
      actualTypeArguments = ((GenericType)type).getTypeArgs(); 
    } else {
      return null;
    }
    for (int i=0; i<typeParameters.length; i++) {
      String name = typeParameters[i].getName();
      Type typeArg = actualTypeArguments[i];
      typeArgs.put(name, typeArg);
    }
    String typeArgName = fieldType.toString();
    return typeArgs.get(typeArgName);
  }


  public void scanFields(List<FieldMapping> fieldMappings, Type type) {
    Class<?> clazz = Reflection.getRawClass(type);
    Map<TypeVariable,Type> typeArgs = Reflection.getTypeArgsMap(type);
    
    Field[] declaredFields = clazz.getDeclaredFields();
    if (declaredFields!=null) {
      for (Field field: declaredFields) {
        if (!Modifier.isStatic(field.getModifiers()) 
            && field.getAnnotation(JsonIgnore.class)==null) {
          field.setAccessible(true);
          log.debug("  Scanning "+Reflection.getSimpleName(field));
          Type fieldType = field.getGenericType();
          if (fieldType instanceof TypeVariable) {
            fieldType = typeArgs!=null ? typeArgs.get((TypeVariable)fieldType) : null;
          }
          JsonTypeMapper jsonTypeMapper = getTypeMapper(fieldType);
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
    if (clazz.isEnum()) {
      return;
    }
    Class<? > superclass = clazz.getSuperclass();
    if (Object.class!=superclass) {
      Type supertype = Reflection.getSuperclass(type);
      scanFields(fieldMappings, supertype);
    }
  }

  
  public BpmnTypeMapping getBpmnTypeMapping(Class<?> subClass) {
    if (!bpmnTypeMappingsByClass.containsKey(subClass)) {
      throw new IllegalArgumentException("No BPMN type mapping defined for " + subClass.getName());
    }
    return bpmnTypeMappingsByClass.get(subClass);
  }

  public SortedSet<Class< ? >> getBpmnClasses() {
    // TODO only add condition classes
    SortedSet<Class< ? >> bpmnClasses = new TreeSet(new Comparator<Class<?>>() {
      public int compare(Class c1, Class c2) {
      return c1.getName().compareTo(c2.getName());
      }
    });
    bpmnClasses.addAll(bpmnTypeMappingsByClass.keySet());
    return bpmnClasses;
  }
}