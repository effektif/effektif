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

import com.effektif.workflow.api.json.*;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.impl.data.types.MapType;
import com.effektif.workflow.impl.json.types.BeanMapper;
import com.effektif.workflow.impl.json.types.PolymorphicBeanMapper;
import com.effektif.workflow.impl.util.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

/**
 * Registry for static information used to map API model classes to and from JSON. The purpose of this class is to
 * provide a static cache of class information that is programmatically registered or discovered by reflection.
 *
 * @author Tom Baeyens
 */
public class Mappings {
  
  private static final Logger log = LoggerFactory.getLogger(Mappings.class);

  /** Initialized from the mapping builder information */
  protected Map<Field,String> fieldNames = new HashMap<>();
  
  /** Initialized from the mapping builder information */
  protected Map<Field,FieldMapping> fieldsMappings;

  /** Initialized from the mapping builder information */
  protected Set<Field> inlineFields = new HashSet<>();
  
  /** Initialized from the mapping builder information */
  protected Set<Field> ignoredFields = new HashSet<>();
  
  /** Initialized from the mapping builder information */
  protected List<JsonTypeMapperFactory> jsonTypeMapperFactories = new ArrayList<>();
  
  /** Initialized from the mapping builder information */
  protected Map<Type,DataType> dataTypesByValueClass = new HashMap<>();

  /** Maps registered base classes (like e.g. <code>Activity</code>) to *unparameterized* polymorphic mappings.
   * Polymorphic parameterized types are not yet supported.
   * Initialized from the mapping builder information */
  protected Map<Class<?>, PolymorphicMapping> polymorphicMappings = new HashMap<>();

  /** Initialized from the mapping builder information in registerSubClass */
  protected Map<Class<?>, TypeField> typeFields = new HashMap<>();
  
  /** Type mappings contain the field mappings for each type.  
   * Types can be parameterized.
   * Dynamically initialized */ 
  protected Map<Type, TypeMapping> typeMappings = new HashMap<>();

  /**
   * JSON type mappers are the SPI to plug in support for particular types.
   * Dynamically initialized.
   */
  protected Map<Type, JsonTypeMapper> typeMappers = new HashMap<>();
  
  /** dynamically initialized */
  protected Map<Class<?>, Map<String,Type>> fieldTypes = new HashMap<>();

  public Mappings(MappingsBuilder mappingsBuilder) {
    this.inlineFields = mappingsBuilder.inlineFields;
    this.ignoredFields = mappingsBuilder.ignoredFields;
    this.fieldNames = mappingsBuilder.fieldNames;
    this.fieldsMappings = mappingsBuilder.fieldsMappings;
    this.jsonTypeMapperFactories = mappingsBuilder.typeMapperFactories;
    this.dataTypesByValueClass = mappingsBuilder.dataTypesByValueClass;
    
    for (Class baseClass: mappingsBuilder.baseClasses.keySet()) {
      String typeField = mappingsBuilder.baseClasses.get(baseClass);
      PolymorphicMapping subclassMapping = new PolymorphicMapping(baseClass, typeField);
      polymorphicMappings.put(baseClass, subclassMapping);
    }
    for (Class<?> subClass: mappingsBuilder.subClasses) {
      registerSubClass(subClass);
    }
  }

  public Mappings(Mappings other) {
    this.fieldNames = other.fieldNames;
    this.inlineFields = other.inlineFields;
    this.jsonTypeMapperFactories = other.jsonTypeMapperFactories;
    this.dataTypesByValueClass = other.dataTypesByValueClass;
    this.polymorphicMappings = other.polymorphicMappings;
    this.typeFields = other.typeFields;
    this.typeMappings = other.typeMappings;
    this.typeMappers = other.typeMappers;
    this.fieldTypes = other.fieldTypes;
  }

  public void registerSubClass(Class< ? > subClass) {
    TypeName typeName = subClass.getAnnotation(TypeName.class);
    if (typeName!=null) {
      registerSubClass(subClass, typeName.value(), subClass);
    } else {
      for (Class<?> baseClass: polymorphicMappings.keySet()) {
        if (baseClass.isAssignableFrom(subClass)) {
          throw new RuntimeException(subClass.getName()+" does not declare "+TypeName.class.toString());
        }
      }
    }
  }
  
  protected void registerSubClass(Class<?> baseClass, String typeName, Class<?> subClass) {
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
    DataType dataType = dataTypesByValueClass.get(clazz);
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
    return new MapType(valueType);
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
    JsonTypeMapper jsonTypeMapper = typeMappers.get(type);
    if (jsonTypeMapper!=null) {
      return jsonTypeMapper;
    }

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

    jsonTypeMapper.setMappings(this);
    typeMappers.put(type, jsonTypeMapper);
    return jsonTypeMapper;
  }

  /** finds the most concrete polymorphic mapping that matches the given type. */
  public PolymorphicMapping getPolymorphicMapping(Type type) {
    Class<?> clazz = Reflection.getRawClass(type);
    PolymorphicMapping polymorphicMapping = polymorphicMappings.get(clazz);
    while (polymorphicMapping==null && clazz!=null && clazz!=Object.class) {
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
      // log.debug("Found type mapping "+typeMapping+" in cache for type "+Reflection.getSimpleName(type));
      return typeMapping;
    }
    // log.debug("Creating type mapping for "+Reflection.getSimpleName(type));
    typeMapping = new TypeMapping(type);
    typeMappings.put(type, typeMapping);
    scanFieldMappings(type, typeMapping);
    // log.debug("Creating type mapping "+typeMapping);
    return typeMapping;
  }

  public void scanFieldMappings(Type type, TypeMapping typeMapping) {
    List<FieldMapping> fieldMappings = new ArrayList<>();
    scanFields(fieldMappings, type);
    Class<?> clazz = Reflection.getRawClass(type);
    Set<FieldMapping> inlineFieldMappings = new HashSet<>();
    for (FieldMapping fieldMapping: fieldMappings) {
      // apply the json field name overwriting
      String jsonFieldName = fieldNames.get(fieldMapping.field);
      if (jsonFieldName!=null) {
        fieldMapping.jsonFieldName = jsonFieldName;
      }
      // capture the inline field mappings in a collection
      if (inlineFields.contains(fieldMapping.field)) {
        inlineFieldMappings.add(fieldMapping);
      }
    }
    if (!inlineFieldMappings.isEmpty()) {
      List<String> fieldNames = new ArrayList<>();
      for (FieldMapping fieldMapping: fieldMappings) {
        fieldNames.add(fieldMapping.jsonFieldName);
      }
      for (FieldMapping inlineFieldMapping: inlineFieldMappings) {
        inlineFieldMapping.inline = fieldNames;
      }
    }
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

  /**
   * Updates the given field mappings with mappings for the given type, by recursively scanning its fields.
   */
  public void scanFields(List<FieldMapping> fieldMappings, Type type) {
    if (type == null) {
      throw new IllegalArgumentException("type may not be null");
    }
    Class<?> clazz = Reflection.getRawClass(type);
    Map<TypeVariable,Type> typeArgs = Reflection.getTypeArgsMap(type);
    Field[] declaredFields = clazz.getDeclaredFields();
    if (declaredFields!=null) {
      int index = 0;
      for (Field field: declaredFields) {
        if (!Modifier.isStatic(field.getModifiers())
            && field.getAnnotation(JsonIgnore.class)==null
            && !ignoredFields.contains(field)) {
          field.setAccessible(true);
          FieldMapping fieldMapping = fieldsMappings.get(field);
          if (fieldMapping==null) {
            // log.debug("  Scanning "+Reflection.getSimpleName(field));
            Type fieldType = field.getGenericType();
            if (fieldType instanceof TypeVariable) {
              fieldType = typeArgs!=null ? typeArgs.get((TypeVariable)fieldType) : Object.class;
            }
            JsonTypeMapper fieldTypeMapper = getTypeMapper(fieldType);
            fieldMapping = new FieldMapping(field, fieldTypeMapper);
          }
          // Annotation-based field name override.
          JsonFieldName jsonFieldNameAnnotation = field.getAnnotation(JsonFieldName.class);
          if (jsonFieldNameAnnotation != null) {
            fieldMapping.setJsonFieldName(jsonFieldNameAnnotation.value());
          }
          fieldMappings.add(index, fieldMapping);
          index++;
        }
      }
    }
    if (clazz.isEnum()) {
      return;
    }
    Class<? > superclass = clazz.getSuperclass();
    if (superclass!=null && superclass!=Object.class) {
      Type supertype = Reflection.getSuperclass(type);
      if (supertype!=null) {
        scanFields(fieldMappings, supertype);
      } else {
        // TODO find out which field is not handled properly
        throw new RuntimeException("null supertype for " + type );
      }
    }
  }

  
  public Map<Type, DataType> getDataTypesByValueClass() {
    return dataTypesByValueClass;
  }

  
  public void setDataTypesByValueClass(Map<Type, DataType> dataTypesByValueClass) {
    this.dataTypesByValueClass = dataTypesByValueClass;
  }

  
  public Map<Field, String> getFieldNames() {
    return fieldNames;
  }

  
  public Map<Field, FieldMapping> getFieldsMappings() {
    return fieldsMappings;
  }

  
  public Set<Field> getInlineFields() {
    return inlineFields;
  }

  
  public Set<Field> getIgnoredFields() {
    return ignoredFields;
  }

  
  public List<JsonTypeMapperFactory> getJsonTypeMapperFactories() {
    return jsonTypeMapperFactories;
  }

  
  public Map<Class< ? >, PolymorphicMapping> getPolymorphicMappings() {
    return polymorphicMappings;
  }

  
  public Map<Class< ? >, TypeField> getTypeFields() {
    return typeFields;
  }

  
  public Map<Type, TypeMapping> getTypeMappings() {
    return typeMappings;
  }

  
  public Map<Type, JsonTypeMapper> getTypeMappers() {
    return typeMappers;
  }

  
  public Map<Class< ? >, Map<String, Type>> getFieldTypes() {
    return fieldTypes;
  }

  public boolean isIgnored(Field field) {
    return ignoredFields.contains(field);
  }
}