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
package com.effektif.workflow.api.workflow;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/** Base class for extensible objects that can store user-defined properties.
 * 
 * Note on serialization and deserialization: 
 * If serialization is used, the Jackson serializer will be able to 
 * serialize most types to Json. But deserialization does not create 
 * java beans since this class does not contain the information about which 
 * types are the property values are.  Instead, Jackson will deserialize 
 * json objects to Maps and json arrays to Lists
 * 
 * @author Tom Baeyens
 */
public abstract class Extensible {

  private static Map<Class,Set<String>> invalidPropertyKeysByClass = new ConcurrentHashMap<>();

  protected Map<String,Object> properties;

  public void addProperties(Map<String,Object> additionalProperties) {
    if (additionalProperties == null) {
      return;
    }
    if (properties == null) {
      properties = new HashMap<>();
    }
    properties.putAll(additionalProperties);
  }

  /** @see Extensible */
  public Map<String,Object> getProperties() {
    return this.properties;
  }
  /** @see Extensible */
  public void setProperties(Map<String,Object> properties) {
    if (properties!=null) {
      for (String key: properties.keySet()) {
        setProperty(key, properties.get(key));
      }
    }
    this.properties = properties;
  }
  /** @see Extensible */
  public Extensible property(String key,Object value) {
    setProperty(key, value);
    return this;
  }
  /** @see Extensible */
  public Extensible propertyOpt(String key,Object value) {
    if (value!=null) {
      setProperty(key, value);
    }
    return this;
  }
  /** @see Extensible */
  public Object getProperty(String key) {
    return properties!=null ? properties.get(key) : null;
  } 
  /** @see Extensible */
  public void setProperty(String key,Object value) {
    checkProperty(key, value);
    if (properties==null) {
      properties = new HashMap<>();
    }
    this.properties.put(key, value);
  }
  /** only sets the property if the value is not null 
   * @see Extensible */
  public void setPropertyOpt(String key,Object value) {
    if (value==null) {
      return;
    }
    setProperty(key, value);
  }
  public Object removeProperty(String key) {
    return properties!=null ? properties.remove(key) : null;
  }

  /** throws RuntimeException if a property is set with an invalid key.
   * All the known fieldnames are invalid values because the properties are 
   * serialized inside the containing object json. 
   * @param value 
   * @see Extensible */
  private void checkProperty(String key, Object value) {
    Set<String> invalidPropertyKeys = getInvalidPropertyKeys(getClass());
    if (key==null || invalidPropertyKeys.contains(key)) {
      throw new RuntimeException("Invalid property '"+key+"'");
    }
    // TODO checkValue(key, value);
    // checkValue still fails on the bpmn tests
  }

  private void checkValue(String key, Object value) {
    if ( value==null
         || (value instanceof String)
         || (value instanceof Number)
         || (value instanceof Boolean) ) {
      return;
    }
    if (value instanceof Map) {
      checkValueMap(key, (Map)value);
      return;
    }
    if (value instanceof Collection) {
      checkValueCollection(key, (Collection)value);
      return;
    }
    throw new RuntimeException("Invalid value in property '"+key+"': "+value+" ("+value.getClass()+") Allowed types: String,Number,Boolean,Collection,Map");
  }
  
  private void checkValueCollection(String key, Collection value) {
    for (Object element: (Collection)value) {
      checkValue(key, element);
    }
  }
  
  private void checkValueMap(String key, Map value) {
    for (Object mapKey: ((Map)value).keySet()) {
      if (!(mapKey instanceof String)) {
        throw new RuntimeException("Invalid key in map in '"+key+"': "+mapKey+" ("+mapKey.getClass()+") Only String's are allowed as map key types: String");
      }
    }
    for (Object element: ((Map)value).values()) {
      checkValue(key, element);
    }
  }
  
  public static Set<String> getInvalidPropertyKeys(Class<?> clazz) {
    Set<String> invalidPropertyKeys = invalidPropertyKeysByClass.get(clazz);
    if (invalidPropertyKeys!=null) {
      return invalidPropertyKeys;
    }
    invalidPropertyKeys = new HashSet<>();
    collectInvalidPropertyKeys(clazz, invalidPropertyKeys);
    invalidPropertyKeysByClass.put(clazz, invalidPropertyKeys);
    return invalidPropertyKeys;
  }

  private static void collectInvalidPropertyKeys(Class<?> clazz, Set<String> invalidPropertyKeys) {
    Field[] fields = clazz.getDeclaredFields();
    if (fields!=null) {
      for (Field field: fields) {
        invalidPropertyKeys.add(field.getName());
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass!=Object.class) {
      collectInvalidPropertyKeys(superclass, invalidPropertyKeys);
    }
  }
}
