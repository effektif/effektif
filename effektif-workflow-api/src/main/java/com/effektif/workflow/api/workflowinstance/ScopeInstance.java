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
package com.effektif.workflow.api.workflowinstance;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.TaskId;


/**
 * @author Tom Baeyens
 */
public class ScopeInstance {
  
  protected LocalDateTime start;
  protected LocalDateTime end;
  protected Long duration;
  protected List<ActivityInstance> activityInstances;
  protected List<VariableInstance> variableInstances;
  protected List<TimerInstance> timerInstances;
  protected TaskId taskId;
  
  public ActivityInstance findOpenActivityInstance(String activityId) {
    if (activityId!=null && activityInstances!=null) {
      for (ActivityInstance activityInstance: activityInstances) {
        ActivityInstance theOne = activityInstance.findOpenActivityInstance(activityId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }
  
  public Object getVariableValue(String variableId) {
    if (variableId==null) {
      return null;
    }
    if (variableInstances!=null) {
      for (VariableInstance variableInstance: variableInstances) {
        if (variableId.equals(variableInstance.getVariableId())) {
          return variableInstance.getValue();
        }
      }
    }
    return null;
  }
  
  public Long getVariableValueLong(String variableId) {
    Object value = getVariableValue(variableId);
    if (value==null) {
      return null;
    }
    if (value instanceof Number) {
      return ((Number)value).longValue();
    }
    throw new RuntimeException("Value is not a number: "+value+" ("+value.getClass().getName()+")");
  }

  public LocalDateTime getVariableValueDate(String variableId) {
    Object value = getVariableValue(variableId);
    if (value==null) {
      return null;
    }
    if (value instanceof LocalDateTime) {
      return ((LocalDateTime)value);
    }
    throw new RuntimeException("Value is not a date: "+value+" ("+value.getClass().getName()+")");
  }

  public Double getVariableValueDouble(String variableId) {
    Object value = getVariableValue(variableId);
    if (value==null) {
      return null;
    }
    if (value instanceof Number) {
      return ((Number)value).doubleValue();
    }
    throw new RuntimeException("Value is not a double: "+value+" ("+value.getClass().getName()+")");
  }

  public LocalDateTime getStart() {
    return this.start;
  }
  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  public LocalDateTime getEnd() {
    return this.end;
  }
  public void setEnd(LocalDateTime end) {
    this.end = end;
  }
  
  public boolean isEnded() {
    return end!=null;
  }
  
  public boolean isOpen() {
    return !isEnded();
  }

  public Long getDuration() {
    return this.duration;
  }
  public void setDuration(Long duration) {
    this.duration = duration;
  }
  
  public List<ActivityInstance> getActivityInstances() {
    return this.activityInstances;
  }
  public void setActivityInstances(List<ActivityInstance> activityInstances) {
    this.activityInstances = activityInstances;
  }
  
  public List<VariableInstance> getVariableInstances() {
    return this.variableInstances;
  }
  public void setVariableInstances(List<VariableInstance> variableInstances) {
    this.variableInstances = variableInstances;
  }

  public List<TimerInstance> getTimerInstances() {
    return this.timerInstances;
  }
  public void setTimerInstances(List<TimerInstance> timerInstances) {
    this.timerInstances = timerInstances;
  }
  
  public TaskId getTaskId() {
    return this.taskId;
  }
  public void setTaskId(TaskId taskId) {
    this.taskId = taskId;
  }
}
