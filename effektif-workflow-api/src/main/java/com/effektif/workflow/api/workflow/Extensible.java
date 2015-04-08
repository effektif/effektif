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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.mapper.JsonWriter;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;


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
public abstract class Extensible implements JsonWritable, JsonReadable {

  protected Map<String,Object> properties;
  
  @Override
  public void readFields(JsonReader r) {
    // properties = r.readOtherProperties();
  }
  
  @Override
  public void writeFields(JsonWriter w) {
    // w.writeFields(properties);
  }

  /** @see Extensible */
  @JsonAnyGetter
  public Map<String,Object> getProperties() {
    return this.properties;
  }
  /** @see Extensible */
  public void setProperties(Map<String,Object> properties) {
    if (properties!=null) {
      for (String key: properties.keySet()) {
        checkPropertyKey(key);
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
  @JsonAnySetter
  public void setProperty(String key,Object value) {
    checkPropertyKey(key);
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
   * @see Extensible */
  protected abstract void checkPropertyKey(String key);
  
  /** convenience method to be use din checkPropertyKey implementations */
  protected void checkPropertyKey(String key, Set<String> invalidPropertyKeys) {
    if (key==null || invalidPropertyKeys.contains(key)) {
      throw new RuntimeException("Invalid property '"+key+"'");
    }
  }
}
