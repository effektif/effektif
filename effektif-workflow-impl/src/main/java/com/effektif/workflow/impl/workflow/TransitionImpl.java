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

import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.conditions.ConditionImpl;


/**
 * @author Tom Baeyens
 */
public class TransitionImpl implements BpmnModel<Transition> {

  private static final String BPMN_ELEMENT_NAME = "sequenceFlow";

  public String id;
  public ScopeImpl parent;
  public Configuration configuration;
  public WorkflowImpl workflow;

  public ActivityImpl from;
  public ActivityImpl to;
  public ConditionImpl condition;

//  public Transition serialize() {
//    Transition transition = new Transition();
//    if (from!=null) transition.setFrom(from.id);
//    if (to!=null) transition.setFrom(to.id);
//    transition.setCondition(conditionScript);
//    return transition;
//  }

  @Override
  public Transition readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME)) {
      return null;
    }
    Transition transition = new Transition();
    transition.id(reader.readBpmnAttribute(xml, "id"));
    transition.setName(reader.readBpmnAttribute(xml, "name"));
    transition.setFrom(reader.readBpmnAttribute(xml, "sourceRef"));
    transition.setTo(reader.readBpmnAttribute(xml, "targetRef"));
    return transition;
  }

  @Override
  public void writeBpmn(Transition transition, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
    writer.writeBpmnAttribute(xml, "id", transition.getId());
    writer.writeBpmnAttribute(xml, "sourceRef", transition.getFrom());
    writer.writeBpmnAttribute(xml, "targetRef", transition.getTo());
  }

  public void parse(Transition transition, ScopeImpl parentImpl, Map<String, ActivityImpl> activitiesByDefaultTransitionId, WorkflowParser parser) {
    this.id = transition.getId();
    if (id!=null) {
      if (parser.transitionIds.contains(id)) {
        parser.addError("Duplicate transition id '%s'", id);
      } else {
        parser.transitionIds.add(id);
      }
    }
    this.configuration = parser.configuration;
    if (parentImpl!=null) {
      this.parent = parentImpl;
      this.workflow = parentImpl.workflow;
    }

    ActivityImpl activityHavingThisAsDefault = activitiesByDefaultTransitionId.remove(id);
    if (activityHavingThisAsDefault!=null) {
      activityHavingThisAsDefault.defaultTransition = this;
    }

    String fromId = transition.getFrom();
    if (fromId==null) {
      parser.addWarning("Transition has no 'from' specified");
    } else {
      this.from = parentImpl.getActivityByIdLocal(fromId);
      if (this.from!=null) {
        this.from.addOutgoingTransition(this);
        if (activityHavingThisAsDefault!=null && activityHavingThisAsDefault!=from) {
          parser.addWarning("Default transition '%s' does not leave from activity '%s'", id, activityHavingThisAsDefault.id);
        }
      } else {
        parser.addWarning("Transition has an invalid value for 'from' (%s) : %s", fromId, parser.getExistingActivityIdsText(parentImpl));
      }
    }
    String toId = null;
    if (transition.isToNext()) {
      this.to = parentImpl.getNextActivity(from);
      if (this.to!=null) {
        this.to.addIncomingTransition(this);
        transition.to(this.to.getId());
        transition.setToNext(null);
      } else {
        parser.addWarning("Transition has no next");
      }
    } else {
      toId = transition.getTo();
      if (toId==null) {
        parser.addWarning("Transition has no 'to' specified");
      } else {
        this.to = parentImpl.getActivityByIdLocal(toId);
        if (this.to!=null) {
          this.to.addIncomingTransition(this);
        } else {
          parser.addWarning("Transition has an invalid value for 'to' (%s) : %s", toId, parser.getExistingActivityIdsText(parentImpl));
        }
      }
    }
    if (transition.getCondition()!=null) {
      parser.pushContext(transition.toString(), condition, null, null);
      this.condition = parser.parseCondition(transition.getCondition());
      parser.popContext();
    }
  }
}
