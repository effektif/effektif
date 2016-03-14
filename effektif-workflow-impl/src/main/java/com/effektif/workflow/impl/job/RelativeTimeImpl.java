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
package com.effektif.workflow.impl.job;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class RelativeTimeImpl {

  protected RelativeTime relativeTime;
  protected BindingImpl<LocalDateTime> base;

  public RelativeTimeImpl(RelativeTime relativeTime, WorkflowParser workflowParser) {
    this.relativeTime = relativeTime;
    if (relativeTime.getBase()!=null) {
      base = workflowParser.parseBinding(relativeTime.getBase(), "relative time base");
    }
  }

  public LocalDateTime resolve(ActivityInstanceImpl activityInstance) {
    LocalDateTime baseTime = null;
    if (activityInstance!=null && base!=null) {
      baseTime = activityInstance.getValue(base);
    }
    if (baseTime==null) {
      baseTime = Time.now();
    }
    LocalDateTime time = relativeTime.resolve(baseTime);
    return time;
  }
}
