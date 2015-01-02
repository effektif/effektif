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
package com.effektif.workflow.impl.definition;

import java.util.List;

import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Variable;


/**
 * @author Tom Baeyens
 */
public class MultiInstanceImpl {

  public VariableImpl elementVariable;
  public List<BindingImpl<Object>> collection;

  public void validate(MultiInstance apiMultiInstance, WorkflowValidator validator) {
    if (apiMultiInstance.getCollection()!=null) {
      collection = validator.compileBinding(apiMultiInstance.getCollection(), "collection");
    } else {
      validator.addError("Multi instance has no collection");
    }
    Variable apiElementVariable = apiMultiInstance.getElementVariable();
    if (apiElementVariable!=null) {
      elementVariable = new VariableImpl();
      validator.pushContext("elementVariable", apiElementVariable);
      elementVariable.validate(apiElementVariable, validator);
      validator.popContext();
    } else {
      validator.addError("Multi instance has no element variable");
    }
  }
}
