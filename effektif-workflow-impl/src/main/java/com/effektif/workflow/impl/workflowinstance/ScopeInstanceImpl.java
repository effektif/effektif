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
package com.effektif.workflow.impl.workflowinstance;

import static com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.deprecated.model.TaskId;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.TimerInstance;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.ExpressionImpl;
import com.effektif.workflow.impl.workflow.ScopeImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;


public abstract class ScopeInstanceImpl extends BaseInstanceImpl {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public ScopeImpl scope;
  public LocalDateTime start;
  public LocalDateTime end;
  public Long duration;
  public List<ActivityInstanceImpl> activityInstances;
  public List<VariableInstanceImpl> variableInstances;
  /** maps variable.id's to variable instances */
  public Map<String, VariableInstanceImpl> variableInstancesMap;
  public List<TimerInstanceImpl> timerInstances;

  // As long as the workflow instance is not saved, the updates collection is null.
  // That means it's not yet necessary to collect the updates. 
  public ScopeInstanceUpdates updates;
  
  public ScopeInstanceImpl() {
  }

  public ScopeInstanceImpl(ScopeInstanceImpl parent, ScopeImpl scope) {
    super(parent);
    this.scope = scope;
    this.start = Time.now();
  }

  public abstract void setEnd(LocalDateTime end); 
  
  public abstract boolean isWorkflowInstance();
  
  protected void toScopeInstance(ScopeInstance scopeInstance) {
    scopeInstance.setStart(start);
    scopeInstance.setEnd(end);
    scopeInstance.setDuration(duration);
    if (activityInstances!=null && !activityInstances.isEmpty()) {
      List<ActivityInstance> activityInstanceApis = new ArrayList<>();
      for (ActivityInstanceImpl activityInstanceImpl: this.activityInstances) {
        activityInstanceApis.add(activityInstanceImpl.toActivityInstance());
      }
      scopeInstance.setActivityInstances(activityInstanceApis);
    }
    if (variableInstances!=null && !variableInstances.isEmpty()) {
      List<VariableInstance> variableInstanceApis = new ArrayList<>();
      for (VariableInstanceImpl variableInstanceImpl: this.variableInstances) {
        variableInstanceApis.add(variableInstanceImpl.toVariableInstance());
      }
      scopeInstance.setVariableInstances(variableInstanceApis);
    }
    if (timerInstances!=null && !timerInstances.isEmpty()) {
      List<TimerInstance> timerInstanceApis = new ArrayList<>();
      for (TimerInstanceImpl timerInstanceImpl: this.timerInstances) {
        timerInstanceApis.add(timerInstanceImpl.toTimerInstance());
      }
      scopeInstance.setTimerInstances(timerInstanceApis);
    }
    scopeInstance.setProperties(this.properties);
  }

  public void execute(ActivityImpl activity) {
    createActivityInstance(activity);
  }

  public ActivityInstanceImpl createActivityInstance(ActivityImpl activity) {
    String activityInstanceId = workflowInstance.generateNextActivityInstanceId();
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl(this, activity, activityInstanceId);
    if (activity.isMultiInstance()) {
      activityInstance.setWorkState(STATE_STARTING_MULTI_CONTAINER);
    } else {
      activityInstance.setWorkState(STATE_STARTING);
    }
    workflowInstance.addWork(activityInstance);
    activityInstance.start = Time.now();
    if (updates!=null) {
      activityInstance.updates = new ActivityInstanceUpdates(true);
      if (parent!=null) {
        parent.propagateActivityInstanceChange();
      }
    }
    addActivityInstance(activityInstance);
    activityInstance.initializeVariableInstances();
    if (log.isDebugEnabled())
      log.debug("Created "+activityInstance);
    return activityInstance;
  }
  
  public void initializeForEachElement(VariableImpl elementVariableDefinition, Object value) {
    VariableInstanceImpl elementVariableInstance = createVariableInstanceLocal(elementVariableDefinition);
    elementVariableInstance.setValue(value);
  }

  public void addActivityInstance(ActivityInstanceImpl activityInstance) {
    if (activityInstances==null) {
      activityInstances = new ArrayList<>();
    }
    activityInstance.parent = this;
    activityInstances.add(activityInstance);
  }
  
  public void initializeVariableInstances() {
    if (scope.variables!=null && !scope.variables.isEmpty()) {
      for (VariableImpl variable: scope.variables.values()) {
        createVariableInstanceLocal(variable);
      }
    }
  }

  public VariableInstanceImpl createVariableInstanceLocal(VariableImpl variable) {
    String variableInstanceId = workflowInstance.generateNextVariableInstanceId();
    VariableInstanceImpl variableInstance = new VariableInstanceImpl(this, variable, variableInstanceId);
    variableInstance.configuration = configuration;
    variableInstance.workflowInstance = workflowInstance;
    variableInstance.type = variable.type;
    variableInstance.setValue(variable.initialValue);
    variableInstance.variable = variable;
    if (updates!=null) {
      variableInstance.updates = new VariableInstanceUpdates(true);
      updates.isVariableInstancesChanged = true;
      if (parent!=null) {
        parent.propagateActivityInstanceChange();
      }
    }
    addVariableInstance(variableInstance);
    return variableInstance;
  }
  
  public VariableInstanceImpl createVariableInstanceLocal(String variableId, DataType dataType) {
    VariableImpl variable = new VariableImpl();
    variable.id = variableId;
    variable.type = dataType;
    return createVariableInstanceLocal(variable);
  }
  
  public void addVariableInstance(VariableInstanceImpl variableInstance) {
    variableInstance.parent = this;
    if (variableInstances==null) {
      variableInstances = new ArrayList<>();
      variableInstancesMap = new HashMap<>();
    }
    variableInstances.add(variableInstance);
    variableInstancesMap.put(variableInstance.variable.id, variableInstance);
  }

  /** to be used by activity implementations */
  public <T> T getValue(BindingImpl<T> binding) {
    if (binding==null) {
      return null;
    }
    if (binding.value!=null) {
      return binding.value;
    }
    if (binding.expression!=null) {
      return (T) getValue(binding.expression);
    }
    return null;
  }
  
  public Object getValue(ExpressionImpl expression) {
    VariableInstanceImpl variableInstance = getVariableInstance(expression);
    if (variableInstance==null) {
      return null;
    }
    if (expression.fields==null) {
      return variableInstance.getValue();
    }
    TypedValueImpl typedValue = getTypedValueField(variableInstance, expression.fields);
    return typedValue!=null ? typedValue.value : null;
  }

  protected VariableInstanceImpl getVariableInstance(ExpressionImpl expression) {
    if (expression==null || expression.variableId==null) {
      return null;
    }
    return findVariableInstance(expression.variableId);
  }

  /** to be used by activity implementations */
  public <T> List<T> getValues(List<BindingImpl<T>> bindings) {
    if (bindings==null) {
      return null;
    }
    List<T> values = new ArrayList<>();
    for (BindingImpl<T> binding: bindings) {
      T value = getValue(binding);
      if (value!=null) {
        if (value instanceof Collection) {
          values.addAll((Collection<T>)value);
        } else {
          values.add(value);
        }
      }
    }
    return values;
  }

  public TypedValueImpl getTypedValue(ExpressionImpl expression) {
    VariableInstanceImpl variableInstance = getVariableInstance(expression);
    if (expression.fields==null) {
      return variableInstance.getTypedValue();
    }
    return getTypedValueField(variableInstance, expression.fields);
  }

  protected TypedValueImpl getTypedValueField(VariableInstanceImpl variableInstance, List<String> fields) {
    TypedValueImpl typedValue = new TypedValueImpl(variableInstance.type, variableInstance.getValue());
    if (fields!=null) {
      for (int i=0; i<fields.size() && typedValue!=null; i++) {
        String field = fields.get(i);
        Object value = typedValue.value;
        if (value!=null) {
          DataType type = typedValue.type;
          if ( (value instanceof Collection)
               && ! (type instanceof ListTypeImpl) ){
            type = new ListTypeImpl((ListType)type);
            type.setConfiguration(configuration);
          }
          typedValue = typedValue.type.dereference(value, field);
        } else {
          typedValue = null; 
        }
      }
    }
    return typedValue;
  }
  
  public Object getValue(String variableId) {
    VariableInstanceImpl variableInstance = findVariableInstance(variableId);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    throw new RuntimeException("Variable "+variableId+" is not defined in "+getClass().getSimpleName()+" "+toString());
  }
  
  public void collectVariableValues(Map<String, Object> variableValues) {
    // parent is added before the local variables to comply with scoping rules.
    if (parent!=null) {
      parent.collectVariableValues(variableValues);
    }
    if (variableInstancesMap!=null) {
      for (String variableId: variableInstancesMap.keySet()) {
        VariableInstanceImpl variableInstance = variableInstancesMap.get(variableId);
        variableValues.put(variableId, variableInstance.value);
      }
    }
  }
  
  public TypedValueImpl getTypedValue(String variableId) {
    VariableInstanceImpl variableInstance = findVariableInstance(variableId);
    if (variableInstance!=null) {
      return variableInstance.getTypedValue();
    }
    throw new RuntimeException("Variable "+variableId+" is not defined in "+getClass().getSimpleName()+" "+toString());
  }

  /** sets all entries individually, variableValues maps variable ids to values */
  public void setVariableValues(Map<String,Object> variableValues) {
    setVariableValues(variableValues, false);
  }

  public void setVariableValues(Map<String,Object> variableValues, boolean deserialize) {
    if (variableValues!=null) {
      for (String variableId: variableValues.keySet()) {
        Object value = variableValues.get(variableId);
        setVariableValue(variableId, value, deserialize);
      }
    }
  }

  public void setVariableValue(String variableId, Object value) {
    setVariableValue(variableId, value, false);
  }

  public void setVariableValue(String variableId, Object value, boolean deserialize) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableId);
      if (variableInstance!=null) {
        setVariableValue(variableInstance, value, deserialize);
        return;
      }
    }
    if (parent!=null) {
      parent.setVariableValue(variableId, value, deserialize);
      return;
    }
    DataTypeService dataTypeService = configuration.get(DataTypeService.class);
    Class<?> valueClass = value!=null ? value.getClass() : null;
    DataType dataType = dataTypeService.getDataTypeByValue(valueClass);
    if (dataType==null) {
      throw new RuntimeException("Couldn't determine data type dynamically for value "+value);
    }
    VariableInstanceImpl variableInstance = createVariableInstanceLocal(variableId, dataType);
    setVariableValue(variableInstance, value, deserialize);
  }

  public void setVariableValue(VariableInstanceImpl variableInstance, Object value, boolean deserialize) {
    if (deserialize) {
      value = variableInstance.type.convertJsonToInternalValue(value);
    }
    variableInstance.setValue(value);
    if (updates!=null) {
      updates.isVariableInstancesChanged = true;
      if (parent!=null) { 
        parent.propagateActivityInstanceChange();
      }
    }
  }
  
  public VariableInstanceImpl findVariableInstance(String variableId) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableId);
      if (variableInstance!=null) {
        return variableInstance;
      }
    }
    if (parent!=null) {
      return parent.findVariableInstance(variableId);
    }
    return null;
  }
  
  protected VariableInstanceImpl getVariableInstanceLocal(String variableId) {
    return variableInstancesMap.get(variableId);
  }

  public void updateVariableInstancesMap() {
    if (variableInstances!=null) {
      variableInstancesMap = new HashMap<>();
      for (VariableInstanceImpl variableInstance: variableInstances) {
        variableInstancesMap.put(variableInstance.variable.id, variableInstance);
      }
    } else {
      variableInstancesMap = null;
    }
  }
  
  public abstract void end();

  public boolean hasOpenActivityInstances() {
    if (activityInstances==null) {
      return false;
    }
    for (ActivityInstanceImpl activityInstance: activityInstances) {
      if (!activityInstance.isEnded()) {
        return true;
      }
    }
    return false;
  }

  
  /** scans this activity and the nested activities */
  public ActivityInstanceImpl findActivityInstance(String activityInstanceId) {
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        ActivityInstanceImpl theOne = activityInstance.findActivityInstance(activityInstanceId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }
  
  public ActivityInstanceImpl findActivityInstanceByActivityId(String activityDefinitionId) {
    if (activityDefinitionId==null) {
      return null;
    }
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        ActivityInstanceImpl theOne = activityInstance.findActivityInstanceByActivityId(activityDefinitionId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }

  // updates ////////////////////////////////////////////////////////////

  public boolean hasUpdates() {
    // As long as the workflow instance is not saved, the updates collection is null.
    // That means it's not yet necessary to collect the updates. 
    return updates!=null;
  }
  
  public ScopeInstanceUpdates getUpdates() {
    return updates;
  }
  
  public void trackUpdates(boolean isNew) {
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        activityInstance.trackUpdates(isNew);
      }
    }
    if (variableInstances!=null) {
      for (VariableInstanceImpl variableInstance: variableInstances) {
        variableInstance.trackUpdates(isNew);
      }
    }
  }
  
  public void propagateActivityInstanceChange() {
    if (updates!=null) {
      updates.isActivityInstancesChanged = true;
      if (parent != null) {
        parent.propagateActivityInstanceChange();
      }
    }
  }

  public boolean hasActivityInstances() {
    return activityInstances!=null && !activityInstances.isEmpty();
  }
  public boolean isEnded() {
    return end!=null;
  }
  
  public boolean hasActivityInstance(String activityInstanceId) {
    if (hasActivityInstances()) {
      for (ActivityInstanceImpl activityInstance : activityInstances) {
        if (activityInstance.hasActivityInstance(activityInstanceId)) {
          return true;
        }
      }
    }
    return false;
  }

  public void activityInstanceEnded(ActivityInstanceImpl endedActivityInstance) {
    if (!hasOpenActivityInstances()) {
      // We also check if there are still joining activities.
      // If so, they need to be fired.
      // We ensure that we fire each activity max once, 
      // even  if there could be multiple joining activity instances in the activity
      List<ActivityInstanceImpl> joiningActivityInstances = null;
      if (activityInstances!=null) {
        for (ActivityInstanceImpl activityInstance: activityInstances) {
          if (activityInstance.isJoining()) {
            if (joiningActivityInstances==null) {
              joiningActivityInstances = new ArrayList<>();
            }
            joiningActivityInstances.add(activityInstance);
          }
        }
      }
      if (joiningActivityInstances!=null && !joiningActivityInstances.isEmpty()) {
        Set<ActivityImpl> onwardedActivities = new HashSet<>();  
        for (ActivityInstanceImpl joiningActivityInstance: joiningActivityInstances) {
          if (!onwardedActivities.contains(joiningActivityInstance.activity)) {
            onwardedActivities.add(joiningActivityInstance.activity);
            joiningActivityInstance.onwards();
          }
        }
      } else {
        end();
      }
    }
  }

  /** only activity instances that are associated with tasks have a taskid reference.
   * workflow instances and multi instance containers of user tasks do not have taskids. 
   * workflow instances have case ids. */
  public TaskId findTaskIdRecursive() {
    return null;
  }
  
  
}
