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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.serialization.json.GenericType;
import com.effektif.workflow.api.serialization.json.JsonReadable;
import com.effektif.workflow.api.serialization.json.JsonReader;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.util.Reflection;


/**
 * Implements the parts of JSON deserialisation that are not specific to one of the concrete implementations in its
 * subclasses.
 *
 * @author Tom Baeyens
 */
public abstract class AbstractJsonReader implements JsonReader {
  
  public static final Logger log = LoggerFactory.getLogger(AbstractJsonReader.class);
  
  protected Map<String,Object> jsonObject;
  protected Class<?> readableClass;
  protected Mappings mappings; 

  public AbstractJsonReader() {
    this(new Mappings());
  }

  public AbstractJsonReader(Mappings mappings) {
    this.mappings = mappings;
  }

  private static final Class< ? >[] ID_CONSTRUCTOR_PARAMETERS = new Class< ? >[] { String.class };
  @Override
  public <T extends Id> T readId(String fieldName) {
    Object id = jsonObject.remove(fieldName);
    Class<T> idType = (Class<T>) mappings.getFieldType(readableClass, fieldName);
    return toId(id, idType);
  }

  public String readString(String fieldName) {
    return (String) jsonObject.remove(fieldName);
  }
  

  @Override
  public Boolean readBoolean(String fieldName) {
    return (Boolean) jsonObject.remove(fieldName);
  }

  @Override
  public Long readLong(String fieldName) {
    Number number = (Number) jsonObject.remove(fieldName);
    return number instanceof Long ? (Long) number : number.longValue();
  }

  @Override
  public Double readDouble(String fieldName) {
    Number number = (Number) jsonObject.remove(fieldName);
    return number instanceof Double ? (Double) number : number.doubleValue();
  }

  @Override
  public LocalDateTime readDate(String fieldName) {
    Object dateValue = jsonObject.remove(fieldName);
    return readDateValue(dateValue);
  }

  @Override
  public Class< ? > readClass(String fieldName) {
    String className = (String) jsonObject.remove(fieldName);
    return Reflection.loadClass(className);
  }

  @Override
  public <T> T readObject(String fieldName) {
    Map<String, Object> json = (Map<String, Object>) jsonObject.remove(fieldName);
    Class<T> readableType = (Class<T>) mappings.getFieldType(readableClass, fieldName);
    return toObject(json, readableType);
  }

  @Override
  public <T> Map<String, T> readMap(String fieldName) {
    Map<String,Object> jsonMap = (Map<String,Object>) jsonObject.remove(fieldName);
    if (jsonMap==null) {
      return null;
    }
    ParameterizedType mapType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return toMap(jsonMap, mapType.getActualTypeArguments()[1]);
  }
  
  @Override
  public <T> List<T> readList(String fieldName) {
    List<Object> jsons = (List<Object>) jsonObject.remove(fieldName);
    if (jsons==null) {
      return null;
    }
    ParameterizedType listType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return toList(jsons, listType.getActualTypeArguments()[0]);
  }

  @Override
  public <T> Binding<T> readBinding(String fieldName) {
    Map<String,Object> jsonBinding = (Map<String, Object>) jsonObject.remove(fieldName);
    if (jsonBinding==null || jsonBinding.isEmpty()) {
      return null;
    }
    ParameterizedType bindingType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return toBinding(jsonBinding, bindingType.getActualTypeArguments()[0]);
  }
  
  @Override
  public Map<String, Object> readProperties() {
    return jsonObject;
  }

  public Object toObject(Object json, Type type) {
    if (json==null) {
      return null;
    }
    
    Class<?> clazz = null;
    
    if (type instanceof Class) {
      clazz = (Class< ? >) type;
    } 

    if (clazz==Boolean.class || json instanceof Boolean) {
      if (!(json instanceof Boolean)) {
        throw new RuntimeException("Expected boolean, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      return json;
    
    } else if (clazz==String.class) {
      if (!(json instanceof String)) {
        throw new RuntimeException("Expected string, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      return json;
    
    } else if (clazz==LocalDateTime.class || json instanceof Date) {
      return readDateValue(json);
    }
    
    Type[] typeArgs = null;
    WildcardType wildcardType = null;

    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      clazz = (Class) parameterizedType.getRawType();
      typeArgs = parameterizedType.getActualTypeArguments();
    } else if (type instanceof WildcardType) {
      wildcardType = (WildcardType) type;
      clazz = (Class< ? >) wildcardType.getUpperBounds()[0];
    } else if (type instanceof GenericType) {
      GenericType genericType = (GenericType) type;
      clazz = (Class) genericType.getBaseType();
      typeArgs = genericType.getTypeArgs();
    }

    if (clazz==null || clazz==Object.class) {
      return json;
      
    } else if (Id.class.isAssignableFrom(clazz)) {
      return toId(json, (Class<Id>)type);
    
    } else if (Number.class.isAssignableFrom(clazz)) {
      if (!(json instanceof Number)) {
        throw new RuntimeException("Expected number, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      return toNumber(json, type);
    
    } else if (clazz==Map.class) {
      if (!(json instanceof Map)) {
        throw new RuntimeException("Expected object, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      return toMap((Map<String,Object>) json, typeArgs!=null ? typeArgs[1] : null);
    
    } else if (clazz==List.class) {
      if (!(json instanceof List)) {
        throw new RuntimeException("Expected array, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      return toList((List<Object>) json, typeArgs!=null ? typeArgs[0] : null);
    
    } else if (clazz==Binding.class) {
      return toBinding((Map<String,Object>) json, typeArgs!=null ? typeArgs[0] : null);
    
    } else if (clazz.isEnum()) {
      if (!(json instanceof String)) {
        throw new RuntimeException("Expected enum string, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      return Enum.valueOf((Class)clazz, (String)json);
      
    } else if (clazz.isArray()) {
      if (!(json instanceof List)) {
        throw new RuntimeException("Expected array, but was "+json+" ("+json.getClass().getName()+")"); 
      }
      List<Object> list = toList((List<Object>) json, clazz.getComponentType());
      return list.toArray((Object[]) Array.newInstance(clazz.getComponentType(), list.size()));

    } else if (clazz==Class.class) {
      return readClass((String) json);
    } 

    if (json instanceof Map) {
      Map<String, Object> jsonMap = (Map<String, Object>) json;
      if (JsonReadable.class.isAssignableFrom(clazz)) {
        return toReadable(jsonMap, (Class<JsonReadable>)clazz);
      } else if (Map.class.isAssignableFrom(clazz)){
        return toMap(jsonMap, Object.class);
      } else {
        return toObject(jsonMap, clazz);
      }
    }
    
    throw new RuntimeException("Couldn't parse "+json+" ("+json.getClass().getName()+")");
  }

  protected Object toNumber(Object json, Type type) {
    if (json==null) {
      return null;
    }
    if (!(json instanceof Number)) {
      throw new RuntimeException("Can't convert json "+json+" ("+json.getClass().getName()+") to a number");
    }
    Number number = (Number) json;
    if (type==null) {
      return json;
    }
    if (type==Long.class
        || type==long.class) {
      return number.longValue();
    }
    if (type==Integer.class
        || type==int.class) {
      return number.intValue();
    }
    if (type==Double.class) {
      return number.doubleValue();
    }
    throw new RuntimeException("The model should not contain fields with other number types than Long, Integer or Double: "+type);
  }

  public abstract LocalDateTime readDateValue(Object jsonDate);
  
  public static <T extends Id> T toId(Object jsonId, Class<T> idType) {
    if (jsonId==null) {
      return null;
    }
    try {
      jsonId = jsonId.toString();
      Constructor<T> c = idType.getDeclaredConstructor(ID_CONSTRUCTOR_PARAMETERS);
      return (T) c.newInstance(new Object[] { jsonId });
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T extends JsonReadable> T toReadable(Map<String, Object> json, Class<T> clazz) {
    if (json==null) {
      return null;
    }
    try {
      Map<String,Object> parentJson = jsonObject;
      Class<?> parentClass = readableClass; 
      jsonObject = json;
      readableClass = mappings.getConcreteClass(jsonObject, clazz);
      T object = (T) readableClass.newInstance();
      object.readJson(this);
      this.jsonObject = parentJson;
      this.readableClass = parentClass;
      return object;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T toObject(Map<String, Object> json, Class<T> clazz) {
    if (json==null) {
      return null;
    }
    try {
      Map<String,Object> parentJson = jsonObject;
      Class<?> parentClass = readableClass; 
      jsonObject = json;
      readableClass = mappings.getConcreteClass(jsonObject, clazz);
      T object = (T) readableClass.newInstance();
      readFieldsInto(object, clazz);
      this.jsonObject = parentJson;
      this.readableClass = parentClass;
      return object;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void readFieldsInto(Object o, Class<?> clazz) {
    List<Field> fields = mappings.getAllFields(o.getClass());
    if (fields!=null) {
      for (Field field : fields) {
        readFieldInto(o, field);
      }
    }
  }
  
  public void readFieldInto(Object o, Field field) {
    try {
      Type fieldType = field.getGenericType();
      Object fieldJson = jsonObject.get(getJsonFieldName(field));
      if (fieldJson!=null) {
        // log.debug("Parsing json "+fieldJson+" for "+field);
        Object fieldValue = toObject(fieldJson, fieldType);
        // log.debug("Setting parsed value "+fieldValue+" into "+field);
        field.set(o, fieldValue);
      }
    } catch (Exception e) {
      throw new RuntimeException("Problem reading field "+field.toString()+": "+e.getMessage(), e);
    }
  }

  protected String getJsonFieldName(Field field) {
    return field.getName();
  }

  public <T> Map<String, T> toMap(Map<String, Object> jsonMap, Type valueType) {
    if (jsonMap==null) {
      return null;
    }
    Map<String,T> map = new HashMap<>();
    for (String key: jsonMap.keySet()) {
      Object jsonFieldValue = jsonMap.get(key);
      T fieldValue = (T) toObject(jsonFieldValue, valueType);
      map.put(key, fieldValue);
    }
    return map;
  }

  public <T> List<T> toList(List<Object> jsonList, Type elementType) {
    if (jsonList==null) {
      return null;
    }
    List<T> objects = new ArrayList<>();
    for (Object jsonElement: jsonList) {
      T object = (T) toObject(jsonElement, elementType);
      objects.add(object);
    }
    return objects;
  }

  public <T> Binding<T> toBinding(Map<String, Object> jsonBinding, Type valueType) {
    if (jsonBinding==null) {
      return null;
    }
    Binding<T> binding = new Binding();
    Object jsonValue = jsonBinding.get("value");
    if (jsonValue!=null) {
      Object value = toObject(jsonValue, valueType);
      binding.setValue((T) value);
    }
    String expression = (String) jsonBinding.get("expression");
    if (expression!=null) {
      binding.setExpression(expression);
    }
    return binding;
  }
}
