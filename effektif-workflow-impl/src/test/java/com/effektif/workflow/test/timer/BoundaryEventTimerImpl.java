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
package com.effektif.workflow.test.timer;

import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobController;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.job.TimerType;
import com.effektif.workflow.impl.workflow.TimerImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class BoundaryEventTimerImpl implements TimerType, JobType {

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
  }
}
