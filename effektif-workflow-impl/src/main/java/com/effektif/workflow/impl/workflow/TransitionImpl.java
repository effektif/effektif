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
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.conditions.ConditionImpl;


/**
 * @author Tom Baeyens
 */
public class TransitionImpl {

  public String id;
  public ScopeImpl parent;
  public Configuration configuration;
  public WorkflowImpl workflow;

  public ActivityImpl from;
  public ActivityImpl to;
  public ConditionImpl condition;

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

    String fromId = transition.getFromId();
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
        transition.toId(this.to.getId());
        transition.setToNext(null);
      } else {
        parser.addWarning("Transition has no next");
      }
    } else {
      toId = transition.getToId();
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

  @Override
  public String toString() {
    String fromId = from!=null ? from.id : null;
    String toId = to!=null ? to.id : null;
    return "("+(fromId!=null?fromId:" ")+")--"+(id!=null?id+"--":"")+">("+(toId!=null?toId:" ")+")";
  }

  /**
   * Shallow equals based on ID, instead of deep field comparison.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TransitionImpl that = (TransitionImpl) o;
    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
