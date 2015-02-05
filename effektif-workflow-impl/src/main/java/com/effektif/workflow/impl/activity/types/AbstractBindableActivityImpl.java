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

import java.util.Map;

import com.effektif.workflow.api.activities.AbstractBindableActivity;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;


public abstract class AbstractBindableActivityImpl<T extends AbstractBindableActivity> extends AbstractActivityType<T> {

  protected Map<String,BindingImpl> inputBindings; 
  protected Map<String,String> outputBindings; 

  public AbstractBindableActivityImpl(Class<T> activityApiClass) {
    super(activityApiClass);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, T activityApi, WorkflowParser parser) {
    super.parse(activityImpl, activityApi, parser);
    this.inputBindings = parser.parseInputBindings(activityApi.getInputBindings(), activityApi, getDescriptor());
    this.outputBindings = activityApi.getOutputBindings();
  }
}
