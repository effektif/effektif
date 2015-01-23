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
package com.effektif.workflow.impl.configuration;

import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.activity.types.CallImpl;
import com.effektif.workflow.impl.activity.types.EmailTaskImpl;
import com.effektif.workflow.impl.activity.types.EmbeddedSubprocessImpl;
import com.effektif.workflow.impl.activity.types.EndEventImpl;
import com.effektif.workflow.impl.activity.types.ExclusiveGatewayImpl;
import com.effektif.workflow.impl.activity.types.HttpServiceTaskImpl;
import com.effektif.workflow.impl.activity.types.JavaServiceTaskImpl;
import com.effektif.workflow.impl.activity.types.NoneTaskImpl;
import com.effektif.workflow.impl.activity.types.ParallelGatewayImpl;
import com.effektif.workflow.impl.activity.types.ScriptTaskImpl;
import com.effektif.workflow.impl.activity.types.StartEventImpl;
import com.effektif.workflow.impl.activity.types.UserTaskImpl;


public class DefaultActivityTypeService extends ActivityTypeService {
  
  @Override
  public void initialize(Brewery brewery) {
    super.initialize(brewery);
    registerActivityType(new StartEventImpl());
    registerActivityType(new EndEventImpl());
    registerActivityType(new EmailTaskImpl());
    registerActivityType(new EmbeddedSubprocessImpl());
    registerActivityType(new ExclusiveGatewayImpl());
    registerActivityType(new ParallelGatewayImpl());
    registerActivityType(new CallImpl());
    registerActivityType(new ScriptTaskImpl());
    registerActivityType(new UserTaskImpl());
    registerActivityType(new NoneTaskImpl());
    registerActivityType(new JavaServiceTaskImpl());
    registerActivityType(new HttpServiceTaskImpl());
  }

}
