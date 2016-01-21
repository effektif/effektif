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
package com.effektif.workflow.impl.workflow.boundary;

import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.impl.job.JobController;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.job.TimerType;
import com.effektif.workflow.impl.workflow.TimerImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
@TypeName("boundaryEventTimer")
public class BoundaryEventTimerImpl implements TimerType, JobType {
  ;
  private static final Logger log = LoggerFactory.getLogger(BoundaryEventTimerImpl.class);

  @Override
  public Class< ? extends Timer> getTimerApiClass() {
    return BoundaryEventTimer.class;
  }

  @Override
  public JobType getJobType(ScopeInstanceImpl scopeInstance, TimerImpl timerImpl) {
    return this;
  }

  @Override
  public int getMaxRetries() {
    return 0;
  }

  @Override
  public int getRetryDelayInSeconds(long retry) {
    return 0;
  }

  @Override
  public void execute(JobController jobController) {

    ActivityInstanceImpl activityInstance =
        jobController.getWorkflowInstance().findActivityInstance(jobController.getJob().getActivityInstanceId());

    if (activityInstance != null) {
      for (TimerImpl timer : activityInstance.activity.getTimers()) {
        if (timer.timer instanceof BoundaryEventTimer) {
          BoundaryEventTimer boundaryEventTimer = (BoundaryEventTimer) timer.timer;
          for (String transitionId : boundaryEventTimer.boundaryEvent.getToTransitionIds()) {
            activityInstance.takeTransition(
                jobController.getWorkflowInstance().getWorkflow().findTransitionByIdLocal(transitionId));
          }
        }
      }
      jobController.getWorkflowInstance().executeWork();
    } else {
      if (log.isDebugEnabled()) log.debug("activityInstance is null, job is not executed. Looked for activityInstance: " + jobController.getJob().getActivityInstanceId());
    }
  }
}
