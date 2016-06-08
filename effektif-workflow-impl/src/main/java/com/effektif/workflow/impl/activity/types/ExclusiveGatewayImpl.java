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
package com.effektif.workflow.impl.activity.types;

import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;

import org.slf4j.Logger;

import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.conditions.ConditionService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ExclusiveGatewayImpl extends AbstractActivityType<ExclusiveGateway> {

  private static final Logger log = WorkflowEngineImpl.log;

  ConditionService conditionService;
  CompiledScript transitionIdExpression;
  Map<String, CompiledScript> transitionExpressions;

  public ExclusiveGatewayImpl() {
    super(ExclusiveGateway.class);
  }

  @Override
  public void parse(ActivityImpl activityImpl, ExclusiveGateway exclusiveGateway, WorkflowParser parser) {
    super.parse(activityImpl, exclusiveGateway, parser);
    conditionService = parser.getConfiguration(ConditionService.class);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ActivityImpl activity = activityInstance.activity;
    List<TransitionImpl> outgoingTransitions = activity.outgoingTransitions;

    TransitionImpl transition = findFirstTransitionThatMeetsCondition(activityInstance, outgoingTransitions);
    if (transition == null) {
      if (activity.defaultTransition != null) {
        transition = activity.defaultTransition;
      } else if (outgoingTransitions != null && outgoingTransitions.size() == 1) {
        transition = outgoingTransitions.get(0);
      } else if (outgoingTransitions != null && outgoingTransitions.size() > 1) {
        transition = handleUndefinedSelection(activityInstance, outgoingTransitions);
      }
    }

    if (transition != null) {
      activityInstance.takeTransition(transition);
    } else {
      log.debug("No transition selected. Gateway " + activity + " ends flow");
      activityInstance.end();
      activityInstance.propagateToParent();
    }
  }

  /** called when this exclusive gateway is 'underspecified' so we have 
   * to guess what's best to do at this point.
   *   
   *  a) there is no outgoing transition with a condition that resolves to true 
   *  b) there is no default transition specified
   *  c) and there is more than 1 transition
   */
  protected TransitionImpl handleUndefinedSelection(ActivityInstanceImpl activityInstance, List<TransitionImpl> outgoingTransitions) {
    return findFirstTransitionWithoutCondition(activityInstance, outgoingTransitions);
  }

  protected TransitionImpl findFirstTransitionWithoutCondition(ActivityInstanceImpl activityInstance, List<TransitionImpl> outgoingTransitions) {
    if (outgoingTransitions != null) {
      for (TransitionImpl outgoingTransition : outgoingTransitions) {
        if (outgoingTransition.condition == null) {
          return outgoingTransition;
        }
      }
    }
    return null;
  }

  protected TransitionImpl findFirstTransitionThatMeetsCondition(ActivityInstanceImpl activityInstance, List<TransitionImpl> outgoingTransitions) {
    if (outgoingTransitions != null) {
      for (TransitionImpl outgoingTransition: outgoingTransitions) {
        // Skip the default transition, which is only used if no others match.
        boolean defaultTransition = outgoingTransition.equals(activityInstance.activity.defaultTransition);
        if (defaultTransition) {
          continue;
        }

        // condition must be true and the transition must have a target
        if (meetsCondition(outgoingTransition, activityInstance)) {
          log.debug("Excl gw takes transition "+outgoingTransition);
          return outgoingTransition;
        } else {
          log.debug("Excl gw condition "+outgoingTransition.condition+" not met: "+outgoingTransition);
        }
      }
    }
    return null;
  }

  protected boolean meetsCondition(TransitionImpl outgoingTransition, ActivityInstanceImpl activityInstance) {
    boolean meetsCondition = false;
    if (outgoingTransition.condition!=null) {
      meetsCondition = outgoingTransition.condition.eval(activityInstance);
    }
    return meetsCondition;
  }

  @Override
  public boolean isFlushSkippable() {
    return true;
  }

  @Override
  public boolean saveTransitionsTaken() {
    return true;
  }
}
