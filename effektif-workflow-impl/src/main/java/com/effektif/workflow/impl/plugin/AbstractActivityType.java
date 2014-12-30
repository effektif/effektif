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
import java.util.List;

import org.slf4j.Logger;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;




/**
 * @author Walter White
 */
public abstract class AbstractActivityType implements ActivityType {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  public abstract void start(ControllableActivityInstance activityInstance);

  public void message(ControllableActivityInstance activityInstance) {
    activityInstance.onwards();
  }
  
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
    if (!activityInstance.hasOpenActivityInstances()) {
      activityInstance.end();
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void validate(Activity activity, Validator validator) {
    Descriptors activityTypeService = validator.getServiceRegistry().getService(Descriptors.class);
    List<DescriptorField> configurationFields = activityTypeService.getConfigurationFields(this);
    if (configurationFields!=null) {
      for (DescriptorField descriptorField : configurationFields) {
        Field field = descriptorField.field;
        try {
          Object value = field.get(this);
          if (value==null) {
            if (Boolean.TRUE.equals(descriptorField.isRequired)) {
              validator.addError("Configuration field %s is required", descriptorField.label);
            }
          }
          if (value instanceof Binding) {
            validateBinding(activity, validator, descriptorField, (Binding< ? >) value);
          } else if (isListOfBindings(field)) {
            List<Binding> bindings = (List<Binding>) value;
            if (bindings!=null) {
              for (Binding binding: bindings) {
                validateBinding(activity, validator, descriptorField, binding);
              }
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private boolean isListOfBindings(Field field) {
    Type genericType = field.getGenericType();
    if (! (genericType instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType parameterizedType = (ParameterizedType) genericType;
    if ( List.class.isAssignableFrom((Class<?>)parameterizedType.getRawType())
         && parameterizedType.getActualTypeArguments().length==1 ) {
      Type listParameter = parameterizedType.getActualTypeArguments()[0];
      Class<?> rawListParameter = (Class<?>) (listParameter instanceof ParameterizedType ? ((ParameterizedType)listParameter).getRawType() : listParameter);
      return Binding.class.equals(rawListParameter);
    }
    return false;
  }

  private void validateBinding(Activity activity, Validator validator, DescriptorField descriptorField, Binding< ? > binding) {
    binding.dataType = descriptorField.dataType;
    binding.validate(activity, validator, this.getClass().getName()+"."+descriptorField.name);
  }
  
  @Override
  public boolean isAsync(ActivityInstance activityInstance) {
    return false;
  }
}
