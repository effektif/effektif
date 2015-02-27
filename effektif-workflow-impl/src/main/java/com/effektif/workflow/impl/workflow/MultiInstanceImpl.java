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

import java.util.List;

import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.InputParameter;
import com.effektif.workflow.impl.data.types.AnyType;


/**
 * @author Tom Baeyens
 */
public class MultiInstanceImpl {
  
  public VariableImpl elementVariable;
  public List<BindingImpl<Object>> valuesBindings;

  public void parse(MultiInstance multiInstanceApi, WorkflowParser parser) {
    parse(multiInstanceApi, parser, null);
  }
  
  public void parse(MultiInstance multiInstanceApi, WorkflowParser parser, ScopeImpl parent) {
    this.valuesBindings = parser.parseBindings(multiInstanceApi.getValuesBindings(), "multiInstance.values");
    Variable elementVariableApi = multiInstanceApi.getVariable();
    if (elementVariableApi!=null) {
      this.elementVariable = new VariableImpl();
      parser.pushContext("elementVariable", elementVariableApi, -1);
      this.elementVariable.parse(elementVariableApi, parser, parent);
      parser.popContext();
    } else {
      parser.addError("Multi instance has no elementVariable");
    }
  }
}
