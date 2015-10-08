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
package com.effektif.workflow.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.conditions.ConditionService;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Startable;


/**
 * @author Tom Baeyens
 */
public class ConditionServiceImpl implements ConditionService, Startable {

  protected Map<Class<? extends Condition>,Class<? extends ConditionImpl>> impls = new HashMap<>();

  @Override
  public void start(Brewery brewery) {
    ServiceLoader<ConditionImpl> activityTypeLoader = ServiceLoader.load(ConditionImpl.class);
    for (ConditionImpl condition: activityTypeLoader) {
      Class apiType = condition.getApiType();
      impls.put(apiType, condition.getClass());
    }
  }

  @Override
  public ConditionImpl compile(Condition condition, WorkflowParser parser) {
    if (condition==null) {
      return null;
    }
    Class<? extends ConditionImpl> implClass = impls.get(condition.getClass());
    if (implClass==null) {
      parser.addWarning("Unknown condition type: %s", condition.getClass().getName());
      return null;
    }
    try {
      ConditionImpl conditionImpl = implClass.newInstance();
      conditionImpl.parse(condition, this, parser);
      return conditionImpl;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
