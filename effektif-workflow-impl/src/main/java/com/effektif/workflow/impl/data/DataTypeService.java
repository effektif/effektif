/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.impl.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.types.BooleanTypeImpl;
import com.effektif.workflow.impl.data.types.CustomTypeImpl;
import com.effektif.workflow.impl.data.types.GroupIdTypeImpl;
import com.effektif.workflow.impl.data.types.JavaBeanTypeImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.data.types.NumberTypeImpl;
import com.effektif.workflow.impl.data.types.TextTypeImpl;
import com.effektif.workflow.impl.data.types.UserIdTypeImpl;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class DataTypeService implements Brewable {
  
  // private static final Logger log = LoggerFactory.getLogger(DataTypeService.class);
  
  protected Configuration configuration;
  protected ObjectMapper objectMapper;
  
  // protected Map<Class<? extends Type>, Class<? extends DataType>> dataTypeClasses = new HashMap<>();
  protected Map<Class<? extends Type>,DataType> singletons = new ConcurrentHashMap<>();
  protected Map<Class<? extends Type>,Constructor<?>> dataTypeConstructors = new ConcurrentHashMap<>();
  protected Map<Class<?>, JavaBeanTypeImpl> javaBeanTypes = new HashMap<>();
  protected Map<Class<? extends Type>, TypeGenerator> typeGenerators = new HashMap<>();
  protected Map<Class<?>, DataType> dataTypesByValueClass = new HashMap<>();
  
  
  @Override
  public void brew(Brewery brewery) {
    this.configuration = brewery.get(Configuration.class);
    this.objectMapper = brewery.get(ObjectMapper.class);
    initializeDataTypes();
  }

  protected void initializeDataTypes() {
    registerDataType(new BooleanTypeImpl());
    registerDataType(new CustomTypeImpl());
    registerDataType(new JavaBeanTypeImpl());
    registerDataType(new GroupIdTypeImpl(configuration));
    registerDataType(new NumberTypeImpl());
    registerDataType(new ListTypeImpl());
    registerDataType(new TextTypeImpl());
    registerDataType(new UserIdTypeImpl(configuration));
  }
  
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
  
  public void registerDataType(DataType dataType) {
    Class apiClass = dataType.getApiClass();
    // dataTypeClasses.put(apiClass, dataType.getClass());
    TypeGenerator typeGenerator = dataType.getTypeGenerator();
    if (typeGenerator!=null) {
      typeGenerators.put(apiClass, typeGenerator);
    }
    if (dataType.isStatic()) {
      singletons.put(apiClass, dataType);
    } else {
      Constructor<?> constructor = findDataTypeConstructor(dataType.getClass());
      dataTypeConstructors.put(apiClass, constructor);
    }
    Class valueClass = dataType.getValueClass();
    if (valueClass!=null) {
      dataTypesByValueClass.put(valueClass, dataType);
      objectMapper.registerSubtypes(valueClass);
    }
    objectMapper.registerSubtypes(apiClass);
  }
  
  
  
  public JavaType createJavaType(Type type) {
    if (type==null) {
      return null;
    }
    TypeGenerator typeGenerator = typeGenerators.get(type.getClass());
    if (typeGenerator==null) {
      return null;
    }
    return typeGenerator.createJavaType(type, objectMapper.getTypeFactory(), this);
  }

  protected Constructor< ? > findDataTypeConstructor(Class< ? extends DataType> dataTypeClass) {
    for (Constructor<?> constructor: dataTypeClass.getDeclaredConstructors()) {
      Class< ? >[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length==2
          && Type.class.isAssignableFrom(parameterTypes[0])
          && Configuration.class.isAssignableFrom(parameterTypes[1])) {
        return constructor;
      }
    }
    throw new RuntimeException("Constructor not found "+dataTypeClass.getName()+"("+Type.class.getName()+","+DataTypeService.class.getName()+")");
  }

  public void registerJavaBeanType(Class<?> javaBeanClass) {
    JavaBeanType javaBeanTypeApi = new JavaBeanType().javaClass(javaBeanClass);
    JavaBeanTypeImpl javaBeanTypeImpl = new JavaBeanTypeImpl(javaBeanTypeApi, configuration);
    javaBeanTypes.put(javaBeanClass, javaBeanTypeImpl);
    registerDataType(javaBeanTypeImpl);
  }
  
  public DataType getDataTypeByValue(Object value) {
    throw new RuntimeException("implement me");
  }

  public Type getTypeByValue(Object value) {
    if (value==null) {
      return null;
    }
    Class<?> valueClass = value.getClass();
    if (String.class.isAssignableFrom(valueClass)) {
      return new TextType();
    }
    if (Number.class.isAssignableFrom(valueClass)) {
      return new NumberType();
    }
    if (Collection.class.isAssignableFrom(valueClass)) {
      ListType listType = new ListType();
      Iterator iterator = ((Collection)value).iterator();
      if (iterator.hasNext()) {
        Object elementValue = iterator.next();
        Type elementType = getTypeByValue(elementValue);
        listType.elementType(elementType);
      }
      return listType;

    } else if (javaBeanTypes.containsKey(valueClass)) {
      return new JavaBeanType(valueClass);
    }
    throw new RuntimeException("No data type found for value "+value+" ("+valueClass.getName()+")");
  }

  public DataType createDataType(Type type) {
    if (type==null) {
      return null;
    }
    DataType singleton = singletons.get(type.getClass());
    if (singleton!=null) {
      return singleton;
    }
    Constructor<?> constructor = dataTypeConstructors.get(type.getClass());
    if (constructor!=null) {
      try {
        return (DataType) constructor.newInstance(new Object[]{type, configuration});
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException("Couldn't instantiate data type "+constructor.getDeclaringClass()+": "+e.getMessage(), e);
      }
    }
    throw new RuntimeException("No DataType defined for "+type.getClass().getName());
  }

  
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
