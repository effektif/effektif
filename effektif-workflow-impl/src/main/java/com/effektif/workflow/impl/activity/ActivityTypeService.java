/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.activity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.ObjectType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.data.types.ObjectTypeImpl;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.util.Exceptions;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ActivityTypeService implements Brewable {
  
  private static final Logger log = LoggerFactory.getLogger(ActivityTypeService.class);
  
  protected ObjectMapper objectMapper;
  protected Configuration configuration;

  // maps activity api configuration classes to activity type implementation classes
  protected Map<Class<?>, ObjectType> activityTypeDescriptors = new LinkedHashMap<>();
  protected Map<Class<?>, Class<? extends ActivityType>> activityTypeClasses = new HashMap<>();
  protected Map<Class<?>, ActivityType> activityTypes = new HashMap<>();
  protected Map<Class<?>, ObjectTypeImpl> activityTypeSerializers = new HashMap<>();

  public ActivityTypeService() {
  }

  @Override
  public void brew(Brewery brewery) {
    this.objectMapper = brewery.get(ObjectMapper.class);
    this.configuration = brewery.get(Configuration.class);
  }

  public void registerActivityType(ActivityType activityType) {
    Class activityTypeApiClass = activityType.getApiClass();
    ObjectType descriptor = activityType.getDescriptor();
    activityTypeClasses.put(activityTypeApiClass, activityType.getClass());
    activityTypeDescriptors.put(activityTypeApiClass, descriptor);
    activityTypes.put(activityTypeApiClass, activityType);
    objectMapper.registerSubtypes(activityTypeApiClass);
    if (descriptor!=null) {
      ObjectTypeImpl serializer = new ObjectTypeImpl(descriptor, configuration, activityTypeApiClass);
      activityTypeSerializers.put(activityTypeApiClass, serializer);
    }
  }
  
  @Deprecated
  public void registerJobType(Class<? extends JobType> jobType) {
    objectMapper.registerSubtypes(jobType);
  }

  public ActivityType instantiateActivityType(Activity activityApi) {
    Exceptions.checkNotNullParameter(activityApi, "apiActivity");
    Class<? extends ActivityType> activityTypeClass = activityTypeClasses.get(activityApi.getClass());
    if (activityTypeClass==null) {
      throw new RuntimeException("No ActivityType defined for "+activityApi.getClass().getName());
    }
    try {
      return activityTypeClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+activityTypeClass+": "+e.getMessage(), e);
    }
  }
  
  public ObjectTypeImpl getActivityTypeSerializer(Class<? extends Activity> activityApiClass) {
    return activityTypeSerializers.get(activityApiClass);
  }

//  public Type getTypeByValue(Object value) {
//    if (value==null) {
//      return null;
//    }
//    Class<?> valueClass = value.getClass();
//    if (String.class.isAssignableFrom(valueClass)) {
//      return new TextType();
//    }
//    if (Number.class.isAssignableFrom(valueClass)) {
//      return new NumberType();
//    }
//    if (Collection.class.isAssignableFrom(valueClass)) {
//      ListType listType = new ListType();
//      Iterator iterator = ((Collection)value).iterator();
//      if (iterator.hasNext()) {
//        Object elementValue = iterator.next();
//        Type elementType = getTypeByValue(elementValue);
//        listType.elementType(elementType);
//      }
//      return listType;
//
//    } else if (javaBeanTypes.containsKey(valueClass)) {
//      return new JavaBeanType(valueClass);
//    }
//    throw new RuntimeException("No data type found for value "+value+" ("+valueClass.getName()+")");
//  }
//
//  public DataType createDataType(Type type) {
//    DataType dataType = instantiateDataType(type);
//    dataType.initialize(type, serviceRegistry);
//    return dataType;
//  }
//  
//  protected DataType instantiateDataType(Type type) {
//    Exceptions.checkNotNullParameter(type, "type");
//    Class<? extends DataType> dataTypeClass = dataTypeClasses.get(type.getClass());
//    if (dataTypeClass==null) {
//      throw new RuntimeException("No DataType defined for "+type.getClass().getName());
//    }
//    try {
//      return dataTypeClass.newInstance();
//    } catch (Exception e) {
//      throw new RuntimeException("Couldn't instantiate "+dataTypeClass+": "+e.getMessage(), e);
//    }
//  }

//  public List<ObjectType> activityTypeDescriptors = new ArrayList<>();
//  public List<ObjectType> dataTypeDescriptors = new ArrayList<>();
//
//  @JsonIgnore
//  public ObjectMapper objectMapper;
//  @JsonIgnore
//  public Map<Type,ObjectType> dataTypeDescriptorsByValueType = new HashMap<>();
//  @JsonIgnore
//  public Map<Class<?>, ObjectType> activityTypeDescriptorsByClass = new HashMap<>();
//  
//  
//  public ToolingService() {
//  }
//
//  @Override
//  public void initialize(ServiceRegistry serviceRegistry, WorkflowEngineConfiguration configuration) {
//    this.objectMapper = serviceRegistry.getService(ObjectMapper.class);
//  }
//
//  public Descriptor registerDataType(DataType dataType) {
//    Class configurationClass = dataType.getConfigurationClass();
//    List<DescriptorField> descriptorFields = findDescriptorFields(configurationClass);
//    Descriptor descriptor = new Descriptor(configurationClass, descriptorFields, dataType);
//    addDataTypeDescriptor(descriptor);
//    dataTypeClasses.put(descriptor.configurationClass, dataType.getClass());
//    return descriptor;
//  }
//
//  public Descriptor registerActivityType(ActivityType activityType) {
//    Class configurationClass = activityType.getConfigurationClass();
//    List<DescriptorField> descriptorFields = findDescriptorFields(configurationClass);
//    Descriptor descriptor = new Descriptor(configurationClass, descriptorFields, activityType);
//    addActivityTypeDescriptor(descriptor);
//    activityTypeDescriptorsByClass.put(activityType.getClass(), descriptor);
//    activityTypeClasses.put(descriptor.configurationClass, activityType.getClass());
//    return descriptor;
//  }
//  
//  
//  public Descriptor registerJavaBeanType(Class<?> javaBeanClass) {
//    Exceptions.checkNotNullParameter(javaBeanClass, "javaBeanClass");
//    objectMapper.registerSubtypes(javaBeanClass);
//    return registerDataType(new JavaBeanType(javaBeanClass)); 
//  }
//  
//  protected void addDataTypeDescriptor(Descriptor descriptor) {
//    dataTypeDescriptors.add(descriptor);
//    DataType dataType = descriptor.getDataType();
//    objectMapper.registerSubtypes(dataType.getClass());
//    Class<?> dataTypeValueClass = dataType.getValueType();
//    if (dataTypeValueClass!=null) {
//      dataTypeDescriptorsByValueType.put(dataTypeValueClass, descriptor);
//      objectMapper.registerSubtypes(dataTypeValueClass);
//    }
//  }
//
//  protected List<DescriptorField> findDescriptorFields(Class< ? > configurationClass) {
//    List<DescriptorField> configurationFields = null;
//    List<Field> fields = Reflection.getNonStaticFieldsRecursive(configurationClass);
//    if (!fields.isEmpty()) {
//      configurationFields = new ArrayList<DescriptorField>(fields.size());
//      for (Field field : fields) {
//        Configuration configuration = field.getAnnotation(Configuration.class);
//        if (field.getAnnotation(Configuration.class) != null) {
//          Descriptor fieldDescriptor = getDataTypeDescriptor(field);
//          DescriptorField descriptorField = new DescriptorField(field, fieldDescriptor.getDataType(), configuration);
//          configurationFields.add(descriptorField);
//        }
//      }
//    }
//    return configurationFields;
//  }
//  
//
//  public Descriptor getDataTypeDescriptor(Field field) {
//    return getDataTypeDescriptor(field.getGenericType(), field);
//  }
//  
//  protected static final Descriptor TEXT_DESCRIPTOR = new Descriptor(new TextType(), "Text", "Any text");
//
//  protected Descriptor getDataTypeDescriptor(Type type, Field field /* passed for error message only */) {
//    Descriptor descriptor = dataTypeDescriptorsByValueType.get(type);
//    if (descriptor!=null) {
//      return descriptor;
//    }
//    if (String.class.equals(type)) {
//      return TEXT_DESCRIPTOR;
//    } else if (type instanceof ParameterizedType) {
//      ParameterizedType parametrizedType = (ParameterizedType) type;
//      Type rawType = parametrizedType.getRawType();
//      Type[] typeArgs = parametrizedType.getActualTypeArguments();
//
//      descriptor = createDataTypeDescriptor(rawType, typeArgs, field);
//      dataTypeDescriptorsByValueType.put(type, descriptor);
//      return descriptor;
//    }
//    throw new RuntimeException("Don't know how to handle "+type+"'s.  It's used in configuration field: "+field);
//  }
//  
//  protected Descriptor createDataTypeDescriptor(Type rawType, Type[] typeArgs, Field field /* passed for error message only */) {
//    if (Binding.class==rawType) {
//      Descriptor argDescriptor = getDataTypeDescriptor(typeArgs[0], field);
//      BindingType bindingType = new BindingType(argDescriptor.getDataType());
//      return new Descriptor(bindingType);
//    } else if (List.class==rawType) {
//      Descriptor argDescriptor = getDataTypeDescriptor(typeArgs[0], field);
//      ListType listType = new ListType(argDescriptor.getDataType());
//      return new Descriptor(listType);
//    } 
//    throw new RuntimeException("Don't know how to handle generic type "+rawType+"'s.  It's used in configuration field: "+field);
//  }
//  
//
//  public List<Descriptor> getDataTypeDescriptors() {
//    return dataTypeDescriptors;
//  }
//
//  public List<Descriptor> getActivityTypeDescriptors() {
//    return activityTypeDescriptors;
//  }
//
//  protected void addActivityTypeDescriptor(Descriptor descriptor) {
//    activityTypeDescriptors.add(descriptor);
//    objectMapper.registerSubtypes(descriptor.getActivityType().getClass());
//  }
//  
//  public List<DescriptorField> getConfigurationFields(ActivityType activityType) {
//    Descriptor descriptor = activityTypeDescriptorsByClass.get(activityType.getClass());
//    if (descriptor==null) {
//      return null;
//    }
//    return descriptor.getFields();
//  }
//
//  public void registerJobType(Class<? extends JobType> jobType) {
//    objectMapper.registerSubtypes(jobType);
//  }
}
