/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.activitytypes;

import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Validator;
import com.effektif.workflow.impl.script.Script;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.script.ScriptService;
import com.fasterxml.jackson.annotation.JsonIgnore;


public class ExclusiveGateway extends AbstractActivityType {

  @JsonIgnore
  ScriptService scriptService;
  CompiledScript transitionIdExpression;
  Map<String,CompiledScript> transitionExpressions;
  
  @Override
  public void validate(Activity activity, Validator validator) {
    super.validate(activity, validator);
    scriptService = validator.getServiceRegistry().getService(ScriptService.class);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    Activity activity = activityInstance.getActivity();
    List<Transition> outgoingTransitions = activity.getOutgoingTransitions();
    Transition defaultTransition = activity.getDefaultTransition();
    // if there are less than two edges, ignore the conditions
    if (outgoingTransitions != null && outgoingTransitions.size() > 1) {  
      Transition transition = findFirstTransitionThatMeetsCondition(activityInstance, outgoingTransitions);
      if (transition != null) {
        activityInstance.takeTransition(transition);
      } else if (defaultTransition != null) {
        activityInstance.takeTransition(defaultTransition);
      } else {
        activityInstance.end(true);
      }
      return;
    }

    // no outgoing transitions. just end here and notify the parent this execution path ended.
    activityInstance.end();
  }

  protected Transition findFirstTransitionThatMeetsCondition(ControllableActivityInstance activityInstance, List<Transition> outgoingTransitions) {
    if (outgoingTransitions != null) {
      for (Transition outgoingTransition: outgoingTransitions ) {
        // condition must be true and the transition must have a target
        if (meetsCondition(outgoingTransition, activityInstance)) {
          return outgoingTransition;
        }
      }
    }
    return null;
  }

  protected boolean meetsCondition(Transition outgoingTransition, ControllableActivityInstance activityInstance) {
    Script script = outgoingTransition.getConditionScript();
    if (script!=null) {
      ScriptResult scriptResult = evaluateCondition(activityInstance, outgoingTransition, script, scriptService);
      if (Boolean.TRUE.equals(scriptResult.getResult())) {
        return true;
      }
    }
    return false;
  }

  protected ScriptResult evaluateCondition(ControllableActivityInstance activityInstance, Transition outgoingTransition, Script script, ScriptService scriptService) {
    return scriptService.evaluateScript(activityInstance, script);
  }
}
