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
package com.effektif.workflow.impl.activity;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Startable;
import com.effektif.workflow.impl.data.types.ObjectTypeImpl;
import com.effektif.workflow.impl.util.Exceptions;


/**
 * @author Tom Baeyens
 */
public class ActivityTypeService implements Startable {
  
  // private static final Logger log = LoggerFactory.getLogger(ActivityTypeService.class);
  
  protected Configuration configuration;

  // maps json type names to activity descriptors
  protected Map<String, ActivityDescriptor> activityTypeDescriptors = new LinkedHashMap<>();
  // maps activity api configuration classes to activity type implementation classes
  protected Map<Class<?>, Class<? extends ActivityType>> activityTypeClasses = new HashMap<>();
  protected Map<Class<?>, ActivityType> activityTypes = new LinkedHashMap<>();
  protected Map<Class<?>, ObjectTypeImpl> activityTypeSerializers = new HashMap<>();

  protected Map<Class<?>, Class<? extends AbstractTriggerImpl>> triggerClasses = new HashMap<>();

  public ActivityTypeService() {
  }

  @Override
  public void start(Brewery brewery) {
    this.configuration = brewery.get(Configuration.class);
    initializeActivityTypes();
    initializeTriggerTypes();
  }

  protected void initializeActivityTypes() {
    ServiceLoader<ActivityType> loader = ServiceLoader.load(ActivityType.class);
    for (ActivityType type: loader) {
      registerActivityType(type);
    }
  }

  protected void initializeTriggerTypes() {
    ServiceLoader<AbstractTriggerImpl> loader = ServiceLoader.load(AbstractTriggerImpl.class);
    for (AbstractTriggerImpl type: loader) {
      registerTriggerType(type);
    }
  }

  public void registerActivityType(ActivityType activityType) {
    Class<? extends Activity> activityTypeApiClass = activityType.getActivityApiClass();
    ActivityDescriptor descriptor = activityType.getDescriptor();
    activityTypeClasses.put(activityTypeApiClass, activityType.getClass());
    activityTypes.put(activityTypeApiClass, activityType);
    
    TypeName jsonTypeName = activityTypeApiClass.getAnnotation(TypeName.class);
    if (jsonTypeName==null) {
      throw new RuntimeException("Please add @TypeName annotation to "+activityTypeApiClass);
    }
    activityTypeDescriptors.put(jsonTypeName.value(), descriptor);
  }
  
  public ActivityDescriptor getActivityDescriptor(String jsonTypeName) {
    return activityTypeDescriptors.get(jsonTypeName);
  }

  public void registerTriggerType(AbstractTriggerImpl trigger) {
    Class triggerApiClass = trigger.getTriggerApiClass();
    triggerClasses.put(triggerApiClass, trigger.getClass());
  }

  public ActivityType instantiateActivityType(Activity activityApi) {
    Exceptions.checkNotNullParameter(activityApi, "activityApi");
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

  public AbstractTriggerImpl instantiateTriggerType(Trigger trigger) {
    Exceptions.checkNotNullParameter(trigger, "trigger");
    Class<? extends AbstractTriggerImpl> triggerImplClass = triggerClasses.get(trigger.getClass());
    if (triggerImplClass==null) {
      throw new RuntimeException("No trigger type defined for "+trigger.getClass().getName());
    }
    try {
      return triggerImplClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+triggerImplClass+": "+e.getMessage(), e);
    }
  }

  public Collection<ActivityType> getActivityTypes() {
    return activityTypes.values();
  }

  public ActivityType<Activity> getActivityType(Class<? extends Activity> activityType) {
    return activityTypes.get(activityType);
  }
}
