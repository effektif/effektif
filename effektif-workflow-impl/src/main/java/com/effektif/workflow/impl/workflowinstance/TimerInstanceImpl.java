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
package com.effektif.workflow.impl.workflowinstance;

import com.effektif.workflow.api.workflowinstance.TimerInstance;


public class TimerInstanceImpl extends BaseInstanceImpl {
  
  protected Long duedate;

  public TimerInstance toTimerInstance() {
    TimerInstance timerInstance = new TimerInstance();
    timerInstance.setDuedate(duedate);
    return timerInstance;
  }

  public Long getDuedate() {
    return this.duedate;
  }
  public void setDuedate(Long duedate) {
    this.duedate = duedate;
  }
}
