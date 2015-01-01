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
package com.effektif.workflow.api.workflowinstance;

import java.util.List;

import org.joda.time.LocalDateTime;


public class ScopeInstance {
  
  protected String id;

  protected LocalDateTime start;

  protected LocalDateTime end;

  protected Long duration;

  protected List<ActivityInstance> activityInstances;

  protected List<VariableInstance> variableInstances;

  protected List<TimerInstance> timerInstances;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
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
}
