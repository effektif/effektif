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
package com.effektif.workflow.impl.job;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Startable;
import com.effektif.workflow.impl.util.Exceptions;


/**
 * @author Tom Baeyens
 */
public class TimerTypeService implements Startable {
  
  protected Configuration configuration;

  // maps timer api configuration classes to timer type implementation classes
  protected Map<Class<?>, Class<? extends TimerType>> timerTypeClasses = new HashMap<>();
  protected Map<Class<?>, TimerType> timerTypes = new LinkedHashMap<>();

  public TimerTypeService() {
  }

  @Override
  public void start(Brewery brewery) {
    this.configuration = brewery.get(Configuration.class);
    initializeTimerTypes();
  }

  protected void initializeTimerTypes() {
    ServiceLoader<TimerType> loader = ServiceLoader.load(TimerType.class);
    for (TimerType type: loader) {
      registerTimerType(type);
    }
  }

  public void registerTimerType(TimerType timerType) {
    Class<? extends Timer> timerApiClass = timerType.getTimerApiClass();
    timerTypeClasses.put(timerApiClass, timerType.getClass());
    timerTypes.put(timerApiClass, timerType);
    
    TypeName jsonTypeName = timerApiClass.getAnnotation(TypeName.class);
    if (jsonTypeName==null) {
      throw new RuntimeException("Please add @TypeName annotation to "+timerApiClass);
    }
  }
  
  public TimerType instantiateTimerType(Timer timer) {
    Exceptions.checkNotNullParameter(timer, "timer");
    Class<? extends TimerType> timerTypeClass = timerTypeClasses.get(timer.getClass());
    if (timerTypeClass==null) {
      throw new RuntimeException("No TimerType defined for "+timer.getClass().getName());
    }
    try {
      return timerTypeClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+timerTypeClass+": "+e.getMessage(), e);
    }
  }

  public Collection<TimerType> getTimerTypes() {
    return timerTypes.values();
  }
}
