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
package com.effektif.workflow.impl.definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.validate.ParseIssue.IssueType;
import com.effektif.workflow.api.validate.ParseIssues;
import com.effektif.workflow.api.workflow.Base;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.ExpressionService;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization. */
public class WorkflowValidator {
  
  public static final String PROPERTY_LINE = "line";
  public static final String PROPERTY_COLUMN = "column";
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowValidator.class);
  
  public WorkflowEngineImpl workflowEngine;
  public WorkflowImpl workflow;
  public LinkedList<String> path = new LinkedList<>();
  public ParseIssues parseIssues = new ParseIssues();
  public Set<Object> activityIds = new HashSet<>();
  public Set<Object> variableIds = new HashSet<>();

  public Stack<ValidationContext> contextStack = new Stack<>();
  private class ValidationContext {
    ValidationContext pathElement(String pathElement) {
      this.pathElement = pathElement;
      return this;
    }
    ValidationContext position(Base base) {
      Map<String, Object> properties = base.getProperties();
      if (properties!=null) {
        this.line = (Long) properties.get(PROPERTY_LINE);
        this.column = (Long) properties.get(PROPERTY_COLUMN);
      }
      return this;
    }
    String pathElement;
    Long line;
    Long column;
  }

  public WorkflowValidator(WorkflowEngineImpl processEngine, WorkflowImpl workflow) {
    this.workflowEngine = processEngine;
    this.workflow = workflow;
  }
  
  public void pushContext(Workflow workflow) {
    pushContext()
            .pathElement("workflow")
            .position(workflow);
  }

  public void pushContext(String propertyName, Base base) {
    pushContext()
            .pathElement("."+propertyName)
            .position(base);
  }

  public void pushContext(String propertyName, Base base, int index) {
    String id = base.getId();
    pushContext()
            .pathElement("."+propertyName+"["+(id!=null ? id : "")+"|"+index+"]")
            .position(base);
  }

  public void pushContext(MultiInstanceImpl multiInstance) {
    pushContext()
            .pathElement(".multiInstance");
  }

  public void pushContext(Binding<?> binding, String propertyName) {
    pushContext()
            .pathElement("."+propertyName+".expression");
  }

  ValidationContext pushContext() {
    ValidationContext validationContext = new ValidationContext();
    this.contextStack.push(validationContext);
    return validationContext;
  }
  
  public void popContext() {
    this.contextStack.pop();
  }
  
  protected String getPathText() {
    StringBuilder pathText = new StringBuilder();
    for (ValidationContext validationContext: contextStack) {
      pathText.append(validationContext.pathElement);
    }
    return pathText.toString();
  }

  String getExistingActivityIdsText(ScopeImpl scope) {
    List<Object> activityIds = new ArrayList<>();
    if (scope.activities!=null) {
      for (ActivityImpl activity: scope.activities.values()) {
        if (activity.id!=null) {
          activityIds.add(activity.id);
        }
      }
    }
    return (!activityIds.isEmpty() ? "Should be one of "+activityIds : "No activities defined in this scope");
  }

  public void addError(String message, Object... messageArgs) {
    ValidationContext currentContext = contextStack.peek();
    parseIssues.addIssue(IssueType.error, getPathText(), currentContext.line, currentContext.column, message, messageArgs);
  }

  public void addWarning(String message, Object... messageArgs) {
    ValidationContext currentContext = contextStack.peek();
    parseIssues.addIssue(IssueType.warning, getPathText(), currentContext.line, currentContext.column, message, messageArgs);
  }
  
  public ParseIssues getIssues() {
    return parseIssues;
  }

  public ServiceRegistry getServiceRegistry() {
    return workflowEngine.getServiceRegistry();
  }

  public <T> BindingImpl<T> compileBinding(Binding<T> binding, String propertyName) {
    if (binding!=null && binding.getExpression()!=null) {
      ExpressionService expressionService = workflowEngine.getServiceRegistry().getService(ExpressionService.class);
      pushContext(binding, propertyName);
      try {
        return expressionService.compile(binding);
      } catch (Exception e) {
        addError("Binding expression '%s' couldn't be compiled: %s", propertyName+".expression", e.getMessage());
      } finally {
        popContext();
      }
    }
    return null;
  }
  
  public <T> List<BindingImpl<T>> compileBinding(List<Binding<T>> bindings, String propertyName) {
    if (bindings!=null) {
      List<BindingImpl<T>> compiledBindings = new ArrayList<>();
      int i = 0;
      for (Binding<T> binding : bindings) {
        compiledBindings.add(compileBinding(binding, propertyName + "[" + i + "]"));
        i++;
      }
      return compiledBindings;
    }
    return null;
  }

  protected boolean hasValue(Binding<?> binding) {
    if (binding.getValue()!=null) return true;
    if (binding.getVariableId()!=null) return true;
    if (binding.getExpression()!=null) return true;
    return false;
  }
  
  public List<ActivityImpl> getStartActivities(ScopeImpl scope) {
    List<ActivityImpl> startActivities = new ArrayList<>(scope.activities.values());
    if (scope.transitions!=null) {
      for (TransitionImpl transition: scope.transitions) {
        startActivities.remove(transition.to);
      }
    }
    if (startActivities.isEmpty()) {
      this.addWarning("No start activities in %s", scope.id);
    }
    return startActivities;
  }


}
