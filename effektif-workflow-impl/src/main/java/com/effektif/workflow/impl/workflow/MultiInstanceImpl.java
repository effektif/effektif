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
package com.effektif.workflow.impl.workflow;

import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowParser;


/**
 * @author Tom Baeyens
 */
public class MultiInstanceImpl {

  public VariableImpl elementVariable;
  public List<BindingImpl<Object>> valueBindings;

  public void parse(Activity activityApi, ScopeImpl parent, WorkflowParser parser) {
    MultiInstance multiInstanceApi = activityApi.getMultiInstance();
    valueBindings = parser.parseBindings(multiInstanceApi.getValueBindings(), Object.class, false, activityApi, "multiInstance.valueBindings");
    Variable elementVariableApi = multiInstanceApi.getVariable();
    if (elementVariableApi!=null) {
      elementVariable = new VariableImpl();
      parser.pushContext("elementVariable", elementVariableApi, -1);
      elementVariable.parse(elementVariableApi, parent, parser);
      parser.popContext();
    } else {
      parser.addError("Multi instance has no elementVariable");
    }
  }
}
