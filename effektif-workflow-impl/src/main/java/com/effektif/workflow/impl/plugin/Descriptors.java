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
package com.effektif.workflow.impl.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.DataTypes;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.type.BindingType;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.type.JavaBeanType;
import com.effektif.workflow.impl.type.ListType;
import com.effektif.workflow.impl.type.TextType;
import com.effektif.workflow.impl.util.Exceptions;
import com.effektif.workflow.impl.util.Reflection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Walter White
 */
public class Descriptors implements DataTypes {
  
  public List<Descriptor> activityTypeDescriptors = new ArrayList<>();
  public List<Descriptor> dataTypeDescriptors = new ArrayList<>();

  @JsonIgnore
  public ObjectMapper objectMapper;
  @JsonIgnore
  public Map<Type,Descriptor> dataTypeDescriptorsByValueType = new HashMap<>();
  @JsonIgnore
  public Map<Class<?>, Descriptor> activityTypeDescriptorsByClass = new HashMap<>();

  public Descriptors() {
  }

  public Descriptors(ServiceRegistry serviceRegistry) {
    this.objectMapper = serviceRegistry.getService(ObjectMapper.class);
  }

  public Descriptor registerDataType(DataType dataType) {
    Descriptor descriptor = createTypeDescriptor(dataType);
    addDataTypeDescriptor(descriptor);
    return descriptor;
  }

  public Descriptor registerActivityType(ActivityType activityType) {
    Descriptor descriptor = createTypeDescriptor(activityType);
    addActivityTypeDescriptor(descriptor);
    activityTypeDescriptorsByClass.put(activityType.getClass(), descriptor);
    return descriptor;
  }

  public Descriptor registerJavaBeanType(Class<?> javaBeanClass) {
    Exceptions.checkNotNullParameter(javaBeanClass, "javaBeanClass");
    objectMapper.registerSubtypes(javaBeanClass);
    return registerDataType(new JavaBeanType(javaBeanClass)); 
  }
  
  @Override
  public DataType list(DataType elementDataType) {
    return new ListType(elementDataType);
  }
  
  @Override
  public DataType javaBean(Class<?> userDefinedJavaBeanClass) {
    return new JavaBeanType(userDefinedJavaBeanClass);
  }
  
  protected void addDataTypeDescriptor(Descriptor descriptor) {
    dataTypeDescriptors.add(descriptor);
    DataType dataType = descriptor.getDataType();
    objectMapper.registerSubtypes(dataType.getClass());
    Class<?> dataTypeValueClass = dataType.getValueType();
    if (dataTypeValueClass!=null) {
      dataTypeDescriptorsByValueType.put(dataTypeValueClass, descriptor);
      objectMapper.registerSubtypes(dataTypeValueClass);
    }
  }

  public Descriptor createTypeDescriptor(Plugin plugin) {
    Descriptor descriptor = new Descriptor();
    if (plugin instanceof DataType) {
      descriptor.setDataType((DataType) plugin);
    } else if (plugin instanceof ActivityType) {
      descriptor.setActivityType((ActivityType) plugin);
    } else if (plugin instanceof DataType) {
      descriptor.setDataType((DataType) plugin);
    }
    
    Class<?> pluginClass = plugin.getClass();
    List<Field> fields = Reflection.getNonStaticFieldsRecursive(pluginClass);
    if (!fields.isEmpty()) {
      List<DescriptorField> configurationFields = new ArrayList<DescriptorField>(fields.size());
      descriptor.setConfigurationFields(configurationFields);
      for (Field field : fields) {
        ConfigurationField configurationField = field.getAnnotation(ConfigurationField.class);
        if (field.getAnnotation(ConfigurationField.class) != null) {
          Descriptor fieldDescriptor = getDataTypeDescriptor(field);
          DescriptorField descriptorField = new DescriptorField(field, fieldDescriptor.getDataType(), configurationField);
          configurationFields.add(descriptorField);
        }
      }
    }
    Label label = pluginClass.getAnnotation(Label.class);
    if (label!=null) {
      descriptor.setLabel(label.value());
    }
    return descriptor;
  }
  
  public Descriptor getDataTypeDescriptor(Field field) {
    return getDataTypeDescriptor(field.getGenericType(), field);
  }

  protected Descriptor getDataTypeDescriptor(Type type, Field field /* passed for error message only */) {
    Descriptor descriptor = dataTypeDescriptorsByValueType.get(type);
    if (descriptor!=null) {
      return descriptor;
    }
    if (String.class.equals(type)) {
      return getDataTypeDescriptor(TextType.class, null);
    } else  if (type instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) type;
      Type rawType = parametrizedType.getRawType();
      Type[] typeArgs = parametrizedType.getActualTypeArguments();

      descriptor = createDataTypeDescriptor(rawType, typeArgs, field);
      dataTypeDescriptorsByValueType.put(type, descriptor);
      return descriptor;
    }
    throw new RuntimeException("Don't know how to handle "+type+"'s.  It's used in configuration field: "+field);
  }
  
  protected Descriptor createDataTypeDescriptor(Type rawType, Type[] typeArgs, Field field /* passed for error message only */) {
    if (Binding.class==rawType) {
      Descriptor argDescriptor = getDataTypeDescriptor(typeArgs[0], field);
      BindingType bindingType = new BindingType(argDescriptor.getDataType());
      return new Descriptor(bindingType);
    } else if (List.class==rawType) {
      Descriptor argDescriptor = getDataTypeDescriptor(typeArgs[0], field);
      ListType listType = new ListType(argDescriptor.getDataType());
      return new Descriptor(listType);
    } 
    throw new RuntimeException("Don't know how to handle generic type "+rawType+"'s.  It's used in configuration field: "+field);
  }
  

  public List<Descriptor> getDataTypeDescriptors() {
    return dataTypeDescriptors;
  }

  public List<Descriptor> getActivityTypeDescriptors() {
    return activityTypeDescriptors;
  }

  protected void addActivityTypeDescriptor(Descriptor descriptor) {
    activityTypeDescriptors.add(descriptor);
    objectMapper.registerSubtypes(descriptor.getActivityType().getClass());
  }
  
  public List<DescriptorField> getConfigurationFields(ActivityType activityType) {
    Descriptor descriptor = activityTypeDescriptorsByClass.get(activityType.getClass());
    if (descriptor==null) {
      return null;
    }
    return descriptor.getConfigurationFields();
  }

  public void registerJobType(Class<? extends JobType> jobType) {
    objectMapper.registerSubtypes(jobType);
  }

//  /** creates a descriptor for a configurable dataType */
//  public TypeDescriptor registerSingletonDataType(DataType dataType) {
//    return registerSingletonDataType(dataType, getJsonTypeName(dataType), null);
//  }
//
//  /** creates a descriptor for a configurable dataType */
//  public TypeDescriptor registerSingletonDataType(DataType dataType, String typeId) {
//    return registerSingletonDataType(dataType, typeId, null);
//  }
//
//  public TypeDescriptor registerSingletonDataType(DataType dataType, Class<?> valueClass) {
//    return registerSingletonDataType(dataType, getJsonTypeName(dataType), valueClass);
//  }
//
//  /** @param valueClass is used when scanning configurations: this dataType will be used for 
//   * all configuration fields of this valueClass */
//  public TypeDescriptor registerSingletonDataType(DataType dataType, String typeId, Class<?> valueClass) {
//    Exceptions.checkNotNullParameter(dataType, "dataType");
//    Exceptions.checkNotNull(typeId, "Activity type "+dataType.getClass()+" doesn't have JsonTypeName annotation");
//
//    addDataTypeById(typeId, dataType);
//    if (couldBeConfigured(dataType)) {
//      // we need to keep track of the singleton object and reference it 
//      dataType = new DataTypeReference(typeId);
//    } // else we can just let json use the default constructor 
//
//    TypeDescriptor dataTypeDescriptor = createTypeDescriptor(dataType);
//    addDataTypeDescriptor(dataTypeDescriptor);
//
//    if (valueClass!=null) {
//      dataTypeDescriptorsByType.put(valueClass, dataTypeDescriptor);
//    }
//    
//    return dataTypeDescriptor;
//  }
//
//  protected void addDataTypeById(String typeId, DataType dataType) {
//    if (dataTypesById.containsKey(typeId)) {
//      throw new RuntimeException("Duplicate type declaration for id "+typeId);
//    }
//    dataTypesById.put(typeId, dataType);
//  }
//
//  public DataType findByTypeId(String typeId) {
//    return dataTypesById.get(typeId);
//  }
//
//  public DataType createDataTypeReference(String dataTypeId) {
//    DataType delegate = dataTypesById.get(dataTypeId);
//    return new DataTypeReference(dataTypeId, delegate);
//  }
//
//  /** this class has to be registered with @link {@link ProcessEngineImpl#registerJavaBeanType(Class)} */
//  @Override
//  public DataType javaBean(Class<?> userDefinedJavaBeanClass) {
//    return new DataTypeReference(userDefinedJavaBeanClass.getName(), new JavaBeanType(userDefinedJavaBeanClass, jsonService));
//  }
//  
//  public TypeDescriptor registerSingletonActivityType(ActivityType activityType) {
//    return registerSingletonActivityType(activityType, PluginHelper.getJsonTypeName(activityType));
//  }
//
//  /** creates a descriptor for a configurable activityType */
//  public TypeDescriptor registerSingletonActivityType(ActivityType activityType, String typeId) {
//    addActivityTypeById(typeId, activityType);
//    
//    if (couldBeConfigured(activityType)) {
//      // we need to keep track of the singleton object and reference it 
//      activityType = new ActivityTypeReference(typeId);
//    } // else we can just let json use the default constructor 
//
//    TypeDescriptor descriptor = dataTypeService.createTypeDescriptor(activityType);
//    addactivityTypeDescriptor(descriptor);
//    return descriptor;
//  }
//
//  protected void addActivityTypeById(String typeId, ActivityType activityType) {
//    if (activityTypesById.containsKey(typeId)) {
//      throw new RuntimeException("Duplicate type declaration for id "+typeId);
//    }
//    activityTypesById.put(typeId, activityType);
//  }
}
