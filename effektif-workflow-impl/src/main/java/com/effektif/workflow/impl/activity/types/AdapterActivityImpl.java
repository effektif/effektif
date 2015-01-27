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
package com.effektif.workflow.impl.activity.types;

import com.effektif.workflow.api.activities.AdapterActivity;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class AdapterActivityImpl extends MappableActivityImpl<AdapterActivity> {
  
  protected String adapterId;
  protected String activityKey;

  public AdapterActivityImpl() {
    super(AdapterActivity.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, AdapterActivity adapterActivity, WorkflowParser parser) {
    super.parse(activityImpl, adapterActivity, parser);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
  }

  public String getAdapterId() {
    return this.adapterId;
  }
  public void setAdapterId(String adapterId) {
    this.adapterId = adapterId;
  }
  public AdapterActivityImpl adapterId(String adapterId) {
    this.adapterId = adapterId;
    return this;
  }
  
  public String getActivityKey() {
    return this.activityKey;
  }
  public void setActivityKey(String activityKey) {
    this.activityKey = activityKey;
  }
  public AdapterActivityImpl activityKey(String activityKey) {
    this.activityKey = activityKey;
    return this;
  }
}
