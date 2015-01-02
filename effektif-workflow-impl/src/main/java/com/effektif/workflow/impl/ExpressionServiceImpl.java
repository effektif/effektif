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
package com.effektif.workflow.impl;

import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.definition.BindingImpl;
import com.effektif.workflow.impl.instance.ScopeInstanceImpl;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.script.Script;
import com.effektif.workflow.impl.script.ScriptService;


/**
 * @author Tom Baeyens
 */
public class ExpressionServiceImpl implements ExpressionService {
  
  protected ScriptService scriptService;

  public ExpressionServiceImpl(ServiceRegistry serviceRegistry) {
    this.scriptService = serviceRegistry.getService(ScriptService.class);
  }

  public <T> BindingImpl<T> compile(Binding<T> binding) {
    BindingImpl bindingImpl = new BindingImpl<T>();
    bindingImpl.value = binding.getValue();
    bindingImpl.variableId = binding.getVariableId();
    bindingImpl.expression = scriptService.compile(binding.getExpression());
    return bindingImpl;
  }

  public Object execute(Object compiledscript, ScopeInstanceImpl scopeInstance) {
    return scriptService.evaluateScript(scopeInstance, (Script)compiledscript);
  }

}
