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

import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractTriggerImpl<T extends Trigger> {
  
  Class<T> triggerApiClass;
  
  public AbstractTriggerImpl(Class<T> triggerApiClass) {
    this.triggerApiClass = triggerApiClass;
  }

  public ActivityDescriptor getDescriptor() {
    return null;
  }

  public Class<T> getTriggerApiClass() {
    return triggerApiClass; 
  }

  /**
   * Parses the {@link com.effektif.workflow.api.workflow.Trigger} to set up this object.
   */
  public void parse(WorkflowImpl workflow, T trigger, WorkflowParser parser) {
  }

  public void published(WorkflowImpl workflow) {
  }

  /**
   * Copies the trigger data from the given {@link com.effektif.workflow.api.model.TriggerInstance} to the given
   * {@link com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl} when starting a workflow.
   */
  public void applyTriggerData(WorkflowInstanceImpl workflowInstance, TriggerInstance triggerInstance, boolean deserialize) {
  }

  public DataType<?> getDataTypeForTriggerKey(String triggerKey) {
    return null;
  }
}
