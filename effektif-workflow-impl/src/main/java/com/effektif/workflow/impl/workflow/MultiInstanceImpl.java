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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Variable;


/**
 * @author Tom Baeyens
 */
public class MultiInstanceImpl {

  public VariableImpl elementVariable;
  public List<BindingImpl<Object>> valueBindings;

  public void parse(MultiInstance apiMultiInstance, ScopeImpl parent, WorkflowParse parser) {
    if (apiMultiInstance.getValueBindings()!=null) {
      valueBindings = new ArrayList<>();
      for (Binding valueBinding: apiMultiInstance.getValueBindings()) {
        valueBindings.add(parser.parseBinding(valueBinding, Object.class, parent.id, "valueBindings"));
      }
    } else {
      parser.addError("Multi instance has no valueBindings");
    }
    Variable apiElementVariable = apiMultiInstance.getVariable();
    if (apiElementVariable!=null) {
      elementVariable = new VariableImpl();
      parser.pushContext("elementVariable", apiElementVariable);
      elementVariable.parse(apiElementVariable, parent, parser);
      parser.popContext();
    } else {
      parser.addError("Multi instance has no elementVariable");
    }
  }
}
