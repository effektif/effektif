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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.*;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Startable;
import com.effektif.workflow.impl.data.types.*;
import com.effektif.workflow.impl.util.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Factory methods for the Effektif type system data types.
 *
 * @author Tom Baeyens
 */
public class DataTypeService implements Startable {
  
  // private static final Logger log = LoggerFactory.getLogger(DataTypeService.class);
  
  protected Configuration configuration;
  
  protected Map<Class<? extends DataType>,DataTypeImpl> singletons = new ConcurrentHashMap<>();
  protected Map<Class<? extends DataType>,Constructor<?>> dataTypeConstructors = new ConcurrentHashMap<>();
  protected Map<Class<?>, JavaBeanTypeImpl> javaBeanTypes = new HashMap<>();
  protected Map<Type, DataTypeImpl> dataTypesByValueClass = new HashMap<>();
  
  public DataTypeService() {
    ServiceLoader<DataTypeImpl> dataTypeLoader = ServiceLoader.load(DataTypeImpl.class);
    for (DataTypeImpl dataType: dataTypeLoader) {
      // log.debug("Registering dynamically loaded data type "+dataType.getClass().getSimpleName());
      registerDataType(dataType);
    }
    
    // For undeclared variables a new variable instance 
    // will be created on the fly when a value is set.  
    // dataType.getValueClass(); is used.  Since more 
    // dataTypes have String as a value type, 
    // we need to register the types we want to use 
    // during auto-creation afterwards.
    // See (*) in registerDataType(DataTypeImpl dataTypeImpl) below
    BooleanTypeImpl booleanTypeImpl = new BooleanTypeImpl();
    booleanTypeImpl.setConfiguration(configuration);
    registerDataType(booleanTypeImpl);
    DateTypeImpl dateTypeImpl = new DateTypeImpl();
    dateTypeImpl.setConfiguration(configuration);
    registerDataType(dateTypeImpl);
    NumberTypeImpl numberTypeImpl = new NumberTypeImpl();
    numberTypeImpl.setConfiguration(configuration);
    registerDataType(numberTypeImpl);
    registerValueClasses(numberTypeImpl, int.class, short.class, byte.class, long.class, float.class, double.class,
                                         Integer.class, Short.class, Byte.class, Long.class, Float.class, Double.class);
    TextTypeImpl textTypeImpl = new TextTypeImpl();
    textTypeImpl.setConfiguration(configuration);
    registerDataType(textTypeImpl);
    ObjectTypeImpl objectTypeImpl = new ObjectTypeImpl();
    objectTypeImpl.setConfiguration(configuration);
    registerDataType(objectTypeImpl);
  }

  @Override
  public void start(Brewery brewery) {
    this.configuration = brewery.get(Configuration.class);
    for (DataTypeImpl dataType: singletons.values()) {
      dataType.setConfiguration(configuration);
    }
  }

  public void registerDataType(DataTypeImpl dataTypeImpl) {
    Class apiClass = dataTypeImpl.getApiClass();
    if (apiClass==null) {
      return;
    }
    if (dataTypeImpl.isStatic()) {
      singletons.put(apiClass, dataTypeImpl);
    } else {
      Constructor< ? > constructor = findDataTypeConstructor(dataTypeImpl.getClass());
      dataTypeConstructors.put(apiClass, constructor);
    }
    try {
      DataType dataType = (DataType) apiClass.newInstance();
      Type valueType = dataType.getValueType();
      if (valueType!=null) {
        // (*) If multiple datatypes have the same valueType (like string, date etc),
        // the last one wins
        dataTypesByValueClass.put(valueType, dataTypeImpl);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void registerValueClasses(DataTypeImpl dataTypeImpl, Type... valueTypes) {
    if (valueTypes!=null) {
      for (Type valueType: valueTypes) {
        dataTypesByValueClass.put(valueType, dataTypeImpl);
      }
    }
  }
  
  protected Constructor< ? > findDataTypeConstructor(Class< ? extends DataTypeImpl> dataTypeClass) {
    for (Constructor<?> constructor: dataTypeClass.getDeclaredConstructors()) {
      Class< ? >[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length==1
          && DataType.class.isAssignableFrom(parameterTypes[0])) {
        return constructor;
      }
    }
    throw new RuntimeException("Constructor not found "+dataTypeClass.getName()+"("+DataType.class.getName()+")");
  }

  public void registerJavaBeanType(Class<?> javaBeanClass) {
    JavaBeanType javaBeanTypeApi = new JavaBeanType().javaClass(javaBeanClass);
    JavaBeanTypeImpl javaBeanTypeImpl = new JavaBeanTypeImpl(javaBeanTypeApi);
    javaBeanTypeImpl.setConfiguration(configuration);
    javaBeanTypes.put(javaBeanClass, javaBeanTypeImpl);
    registerDataType(javaBeanTypeImpl);
  }

  public DataTypeImpl getDataTypeByValue(Type type) {
    Class< ? > rawClass = Reflection.getRawClass(type);
    if (rawClass==List.class) {
      Type elementType = Reflection.getTypeArg(type, 0);
      DataTypeImpl elementDataType = getDataTypeByValue(elementType);
      return new ListTypeImpl(elementDataType);
    }
    return getDataTypeByValue(rawClass);
  }

  public DataTypeImpl getDataTypeByValue(Class<?> valueClass) {
    DataTypeImpl dataType = null;
    if (valueClass!=null) {
      dataType = dataTypesByValueClass.get(valueClass);
    }
    if (dataType==null) {
      if (valueClass != null && Map.class.isAssignableFrom(valueClass)) {
        dataType = new ObjectTypeImpl();
      } else {
        dataType = new AnyTypeImpl();
      }
      dataType.setConfiguration(configuration);
    }
    return dataType;
  }

  public DataType getTypeByValue(Object value) {
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
        DataType elementType = getTypeByValue(elementValue);
        listType.elementType(elementType);
      }
      return listType;

    } else if (javaBeanTypes.containsKey(valueClass)) {
      return new JavaBeanType(valueClass);
    }
    throw new RuntimeException("No data type found for value "+value+" ("+valueClass.getName()+")");
  }

  public DataTypeImpl createDataType(DataType type) {
    if (type==null) {
      return null;
    }
    DataTypeImpl singleton = singletons.get(type.getClass());
    if (singleton!=null) {
      return singleton;
    }
    Constructor<?> constructor = dataTypeConstructors.get(type.getClass());
    if (constructor!=null) {
      try {
        DataTypeImpl dataType = (DataTypeImpl) constructor.newInstance(new Object[]{type});
        dataType.setConfiguration(configuration);
        return dataType;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException("Couldn't instantiate data type "+constructor.getDeclaringClass()+": "+e.getMessage(), e);
      }
    }
    throw new RuntimeException("No DataType defined for "+type.getClass().getName());
  }
}
