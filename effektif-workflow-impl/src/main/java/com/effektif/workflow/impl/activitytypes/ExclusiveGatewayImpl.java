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

import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.script.Script;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.script.ScriptService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class ExclusiveGatewayImpl extends AbstractActivityType<ExclusiveGateway> {

  ScriptService scriptService;
  CompiledScript transitionIdExpression;
  Map<String,CompiledScript> transitionExpressions;
  
  public ExclusiveGatewayImpl() {
    super(ExclusiveGateway.class);
  }

  @Override
  public void parse(ActivityImpl activityImpl, ExclusiveGateway activityApi, WorkflowParser validator) {
    scriptService = validator.getServiceRegistry().getService(ScriptService.class);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ActivityImpl activity = activityInstance.activity;
    List<TransitionImpl> outgoingTransitions = activity.outgoingTransitions;
    TransitionImpl defaultTransition = activity.defaultTransition;
    // if there are less than two edges, ignore the conditions
    if (outgoingTransitions != null && outgoingTransitions.size() > 1) {  
      TransitionImpl transition = findFirstTransitionThatMeetsCondition(activityInstance, outgoingTransitions);
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

  protected TransitionImpl findFirstTransitionThatMeetsCondition(ActivityInstanceImpl activityInstance, List<TransitionImpl> outgoingTransitions) {
    if (outgoingTransitions != null) {
      for (TransitionImpl outgoingTransition: outgoingTransitions) {
        // condition must be true and the transition must have a target
        if (meetsCondition(outgoingTransition, activityInstance)) {
          return outgoingTransition;
        }
      }
    }
    return null;
  }

  protected boolean meetsCondition(TransitionImpl outgoingTransition, ActivityInstanceImpl activityInstance) {
    Script script = outgoingTransition.conditionScript;
    if (script!=null) {
      ScriptResult scriptResult = evaluateCondition(activityInstance, outgoingTransition, script, scriptService);
      if (Boolean.TRUE.equals(scriptResult.getResult())) {
        return true;
      }
    }
    return false;
  }

  protected ScriptResult evaluateCondition(ActivityInstanceImpl activityInstance, TransitionImpl outgoingTransition, Script script, ScriptService scriptService) {
    return scriptService.evaluateScript(activityInstance, script);
  }
}
