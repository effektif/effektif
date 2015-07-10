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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Extensible;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.activity.AbstractTriggerImpl;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.types.ArrayMapperFactory;
import com.effektif.workflow.impl.json.types.BindingMapperFactory;
import com.effektif.workflow.impl.json.types.BooleanMapper;
import com.effektif.workflow.impl.json.types.ClassMapper;
import com.effektif.workflow.impl.json.types.EnumMapperFactory;
import com.effektif.workflow.impl.json.types.EnumSetMapperFactory;
import com.effektif.workflow.impl.json.types.ListMapperFactory;
import com.effektif.workflow.impl.json.types.MapMapperFactory;
import com.effektif.workflow.impl.json.types.NumberMapperFactory;
import com.effektif.workflow.impl.json.types.StringMapper;
import com.effektif.workflow.impl.json.types.TypedValueMapperFactory;
import com.effektif.workflow.impl.json.types.ValueMapper;
import com.effektif.workflow.impl.json.types.VariableInstanceMapperFactory;
import com.effektif.workflow.impl.json.types.VariableMapperFactory;

/**
 * @author Tom Baeyens
 */
public class MappingsBuilder {
  
  Map<Class, String> baseClasses = new HashMap<>();
  List<Class> subClasses = new ArrayList<>();
  Set<Field> inlineFields = new HashSet<>();
  Set<Field> ignoredFields = new HashSet<>();
  Map<Field,JsonTypeMapper<?>> fieldsMappers = new HashMap<>();
  Map<Field,String> fieldNames = new HashMap<>();
  List<JsonTypeMapperFactory> typeMapperFactories = new ArrayList<>();
  Map<Type,DataType> dataTypesByValueClass = new HashMap<>();
  
  public MappingsBuilder configureDefaults() {
    inline(Extensible.class, "properties");
    baseClass(Trigger.class);
    baseClass(JobType.class);
    baseClass(Activity.class);
    baseClass(Condition.class);
    baseClass(DataType.class, "name");
    typeMapperFactory(new ValueMapper());
    typeMapperFactory(new StringMapper());
    typeMapperFactory(new BooleanMapper());
    typeMapperFactory(new ClassMapper());
    typeMapperFactory(new NumberMapperFactory());
    typeMapperFactory(new VariableMapperFactory());
    typeMapperFactory(new VariableInstanceMapperFactory());
    typeMapperFactory(new TypedValueMapperFactory());
    typeMapperFactory(new EnumMapperFactory());
    typeMapperFactory(new ArrayMapperFactory());
    typeMapperFactory(new ListMapperFactory());
    typeMapperFactory(new EnumSetMapperFactory());
    typeMapperFactory(new MapMapperFactory());
    typeMapperFactory(new BindingMapperFactory());
    loadPlugins();
    return this;
  }

  public MappingsBuilder baseClass(Class baseClass) {
    return baseClass(baseClass, "type");
  }

  public MappingsBuilder baseClass(Class baseClass, String typeField) {
    baseClasses.put(baseClass, typeField);
    return this;
  }

  public MappingsBuilder subClass(Class subClass) {
    subClasses.add(subClass);
    return this;
  }

  public MappingsBuilder subClasses(Class... subClasses) {
    if (subClasses!=null) {
      for (Class subClass: subClasses) {
        this.subClasses.add(subClass);
      }
    }
    return this;
  }

  public MappingsBuilder inline(Class clazz, String fieldName) {
    inlineFields.add(getField(clazz, fieldName));
    return this;
  }

  public MappingsBuilder ignore(Class clazz, String fieldName) {
    ignoredFields.add(getField(clazz, fieldName));
    return this;
  }

  public MappingsBuilder fieldMapper(Class clazz, String fieldName, JsonTypeMapper fieldMapper) {
    fieldsMappers.put(getField(clazz, fieldName), fieldMapper);
    return this;
  }

  public MappingsBuilder jsonFieldName(Class clazz, String fieldName, String jsonFieldName) {
    fieldNames.put(getField(clazz, fieldName), jsonFieldName);
    return this;
  }

  public MappingsBuilder typeMapperFactory(JsonTypeMapperFactory mapperFactory) {
    typeMapperFactories.add(mapperFactory);
    return this;
  }
  
  public MappingsBuilder loadPlugins() {
    ServiceLoader<ActivityType> activityTypeLoader = ServiceLoader.load(ActivityType.class);
    for (ActivityType activityType: activityTypeLoader) {
      subClass(activityType.getActivityApiClass());
    }
    ServiceLoader<ConditionImpl> conditionLoader = ServiceLoader.load(ConditionImpl.class);
    for (ConditionImpl condition: conditionLoader) {
      subClass(condition.getApiType());
    }
    ServiceLoader<AbstractTriggerImpl> triggerLoader = ServiceLoader.load(AbstractTriggerImpl.class);
    for (AbstractTriggerImpl trigger: triggerLoader) {
        subClass(trigger.getTriggerApiClass());
    }
    ServiceLoader<DataTypeImpl> dataTypeLoader = ServiceLoader.load(DataTypeImpl.class);
    for (DataTypeImpl dataTypeImpl: dataTypeLoader) {
      try {
        Class<? extends DataType> apiClass = dataTypeImpl.getApiClass();
        if (apiClass!=null) {
          subClass(apiClass);
          DataType dataType = apiClass.newInstance();
          dataTypesByValueClass.put(dataType.getValueType(), dataType);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    // potentially multiple datatypes may map to eg String. 
    // by re-putting these datatypes, we ensure that these basic
    // data types are used when looking up a datatype by value
    dataTypesByValueClass.put(String.class, TextType.INSTANCE);
    dataTypesByValueClass.put(Boolean.class, BooleanType.INSTANCE);
    dataTypesByValueClass.put(Byte.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(Short.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(Integer.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(Long.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(Float.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(Double.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(BigInteger.class, NumberType.INSTANCE);
    dataTypesByValueClass.put(BigDecimal.class, NumberType.INSTANCE);
    return this;
  }
  
  public Mappings getMappings() {
    return new Mappings(this);
  }

  protected Field getField(Class clazz, String fieldName) {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
