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
package com.effektif.workflow.impl.workflow;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.TimerType;
import com.effektif.workflow.impl.job.TimerTypeService;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.util.Date;

/**
 * @author Tom Baeyens
 */
public class TimerImpl {

  public String id;
  public ScopeImpl parent;
  public Configuration configuration;
  public WorkflowImpl workflow;
  public Timer timer;
  public TimerType timerType;

  public void parse(Timer timer, ScopeImpl parentImpl, WorkflowParser parser) {
    this.configuration = parser.configuration;
    this.timer = timer;
    this.id = timer.getId();
    if (parentImpl!=null) {
      this.parent = parentImpl;
      this.workflow = parentImpl.workflow;
    }

    if (timer.getRepeatExpression() != null && timer.getDueDateExpression() != null) {
      parser.addError("TimeDuration and TimeDate on TimerEventDefinition are both set, but mutually exclusive, please remove one of them.");
    }

    TimerTypeService timerTypeService = parser.getConfiguration(TimerTypeService.class);
    this.timerType = timerTypeService.instantiateTimerType(timer);
    // some activity types need to validate incoming and outgoing transitions, 
    // that's why they are NOT parsed here, but after the transitions.
    if (this.timerType==null) {
      parser.addError("Activity '%s' has no activityType configured", id);
    }
  }

  // TODO add a section in ScopeInstanceImpl.toScopeInstance that 
  // uses this method when serializing timers in 
  public Timer toTimer() {
    Timer timer = new Timer();
    // TODO serialize this into a timer
    return timer;
  }

  public Job createJob(ScopeInstanceImpl scopeInstance) {
    Job job = new Job();
    job.workflowId = scopeInstance.workflow.id;
    job.workflowInstanceId = scopeInstance.workflowInstance.id;
    job.dueDate = calculateDueDate();
    job.jobType = timerType.getJobType(scopeInstance, this);
    return job;
  }

  public LocalDateTime calculateDueDate() {

    String repeatExpression = timer.getRepeatExpression();

    try {
      if (repeatExpression != null) {
        Duration f = DatatypeFactory.newInstance().newDuration(repeatExpression);
        Date date = new Date();
        f.addTo(date);

        return new LocalDateTime(date.getTime());
      }
    } catch (DatatypeConfigurationException ex) {
      throw new RuntimeException(ex);
    }

    return null;

  }
}
