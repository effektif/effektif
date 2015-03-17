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
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.types.AnyType;
import com.effektif.workflow.impl.data.types.AnyTypeImpl;
import com.effektif.workflow.impl.data.types.AttachmentTypeImpl;
import com.effektif.workflow.impl.data.types.BooleanTypeImpl;
import com.effektif.workflow.impl.data.types.ChoiceTypeImpl;
import com.effektif.workflow.impl.data.types.CustomTypeImpl;
import com.effektif.workflow.impl.data.types.EmailIdTypeImpl;
import com.effektif.workflow.impl.data.types.EmailTypeImpl;
import com.effektif.workflow.impl.data.types.FileIdTypeImpl;
import com.effektif.workflow.impl.data.types.FileTypeImpl;
import com.effektif.workflow.impl.data.types.GroupIdTypeImpl;
import com.effektif.workflow.impl.data.types.GroupTypeImpl;
import com.effektif.workflow.impl.data.types.JavaBeanTypeImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.data.types.NumberTypeImpl;
import com.effektif.workflow.impl.data.types.TextTypeImpl;
import com.effektif.workflow.impl.data.types.UserIdTypeImpl;
import com.effektif.workflow.impl.data.types.UserTypeImpl;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Factory methods for the Effektif type system data types.
 *
 * @author Tom Baeyens
 */
public class DataTypeService implements Brewable {
  
  // private static final Logger log = LoggerFactory.getLogger(DataTypeService.class);
  
  protected Configuration configuration;
  protected ObjectMapper objectMapper;
  
  protected Map<Class<? extends Type>,DataType> singletons = new ConcurrentHashMap<>();
  protected Map<Class<? extends Type>,Constructor<?>> dataTypeConstructors = new ConcurrentHashMap<>();
  protected Map<Class<?>, JavaBeanTypeImpl> javaBeanTypes = new HashMap<>();
  protected Map<Class<?>, DataType> dataTypesByValueClass = new HashMap<>();

  @Override
  public void brew(Brewery brewery) {
    this.configuration = brewery.get(Configuration.class);
    this.objectMapper = brewery.get(ObjectMapper.class);
    initializeDataTypes();
  }

  protected void initializeDataTypes() {
    // For undeclared variables a new variable instance 
    // will be created on the fly when a value is set.  
    // dataType.getValueClass(); is used.  Since more 
    // dataTypes have String as a value type, 
    // we need to register the types we want to use 
    // during auto-creation first.
    registerDataType(new BooleanTypeImpl(configuration));
    registerDataType(new NumberTypeImpl(configuration));
    registerDataType(new TextTypeImpl(configuration));

    ServiceLoader<DataType> dataTypeLoader = ServiceLoader.load(DataType.class);
    for (DataType dataType: dataTypeLoader) {
      dataType.initialize(configuration);
      registerDataType(dataType);
    }
  }
  
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
  
  public void registerDataType(DataType dataType) {
    Class apiClass = dataType.getApiClass();
    if (dataType.isStatic()) {
      singletons.put(apiClass, dataType);
    } else {
      Constructor<?> constructor = findDataTypeConstructor(dataType.getClass());
      dataTypeConstructors.put(apiClass, constructor);
    }
    Class valueClass = dataType.getValueClass();
    if (valueClass!=null) {
      if (!dataTypesByValueClass.containsKey(valueClass)) {
        dataTypesByValueClass.put(valueClass, dataType);
      }
      objectMapper.registerSubtypes(valueClass);
    }
    objectMapper.registerSubtypes(apiClass);
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
    throw new RuntimeException("Constructor not found "+dataTypeClass.getName()+"("+Type.class.getName()+","+Configuration.class.getName()+")");
  }

  public void registerJavaBeanType(Class<?> javaBeanClass) {
    JavaBeanType javaBeanTypeApi = new JavaBeanType().javaClass(javaBeanClass);
    JavaBeanTypeImpl javaBeanTypeImpl = new JavaBeanTypeImpl(javaBeanTypeApi, configuration);
    javaBeanTypes.put(javaBeanClass, javaBeanTypeImpl);
    registerDataType(javaBeanTypeImpl);
  }
  
  public DataType getDataTypeByValue(Object value) {
    DataType dataType = null;
    if (value!=null) {
      dataType = dataTypesByValueClass.get(value.getClass());
    }
    if (dataType==null) {
      dataType = new AnyTypeImpl(AnyType.INSTANCE, configuration);
    }
    return dataType;
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
