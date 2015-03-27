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

import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.script.ConditionService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ExclusiveGatewayImpl extends AbstractActivityType<ExclusiveGateway> {

  private static final String BPMN_ELEMENT_NAME = "exclusiveGateway";

  ConditionService conditionService;
  CompiledScript transitionIdExpression;
  Map<String,CompiledScript> transitionExpressions;
  
  public ExclusiveGatewayImpl() {
    super(ExclusiveGateway.class);
  }

  @Override
  public ExclusiveGateway readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME)) {
      return null;
    }
    ExclusiveGateway gateway = new ExclusiveGateway();
    return gateway;
  }

  @Override
  public void writeBpmn(ExclusiveGateway gateway, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
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
    ConditionImpl condition = outgoingTransition.condition;
    return condition!=null ? condition.eval(activityInstance) : false;
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
