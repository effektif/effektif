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
package com.effektif.workflow.api.model;

import com.effektif.workflow.api.types.DataType;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class DataContainer {

  protected Map<String,TypedValue> data;

  // transientData is not serialized (and DataContainer is not persisted)
  protected Map<String,Object> transientData;

  public Object getData(String key) {
    TypedValue value = data!=null ? data.get(key) : null;
    return value!=null ? value.getValue() : null;
  }
  
  public void setData(String key, Object value) {
    setTypedValue(key, new TypedValue(value));
  }

  public void setData(String key, Object value, DataType dataType) {
    setTypedValue(key, new TypedValue(value, dataType));
  }
  
  public void setTypedValue(String key, TypedValue value) {
    if (data==null) {
      data = new HashMap<>();
    }
    data.put(key, value);
  }
  
  public DataContainer data(String key, Object value) {
    setData(key, value);
    return this;
  }

  public DataContainer data(String key, Object value, DataType dataType) {
    setData(key, value, dataType);
    return this;
  }

  public DataContainer typedValue(String key, TypedValue value) {
    setTypedValue(key, value);
    return this;
  }

  public DataContainer data(Map<String,Object> data) {
    if (data!=null) {
      for (String key: data.keySet()) {
        Object value = data.get(key);
        if (value instanceof TypedValue) {
          setTypedValue(key, (TypedValue) value);
        } else {
          setData(key, value);
        }
      }
    }
    return this;
  }

  public void removeData(String key) {
    if (data != null && key != null) {
      data.remove(key);
    }
  }

  public Map<String, TypedValue> getData() {
    return data;
  }

  public Map<String,Object> getTransientData() {
    return this.transientData;
  }
  public void setTransientData(Map<String,Object> transientData) {
    this.transientData = transientData;
  }
  public Object getTransientData(String key) {
    return transientData !=null ? transientData.get(key) : null;
  }
  public DataContainer transientData(String key,Object value) {
    if (transientData ==null) {
      transientData = new HashMap<>();
    }
    this.transientData.put(key, value);
    return this;
  }
  public DataContainer transientDataOpt(String key,Object value) {
    if (value!=null) {
      transientData(key, value);
    }
    return this;
  }
  public Object removeTransientData(String key) {
    return transientData !=null ? transientData.remove(key) : null;
  }
}
