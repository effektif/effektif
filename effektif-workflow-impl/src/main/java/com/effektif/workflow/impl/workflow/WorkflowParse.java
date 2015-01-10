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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Base;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.ParseIssue.IssueType;
import com.effektif.workflow.api.workflow.ParseIssues;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.ExpressionService;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization. */
public class WorkflowParse {
  
  public static final String PROPERTY_LINE = "line";
  public static final String PROPERTY_COLUMN = "column";
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowParse.class);
  
  public WorkflowEngineImpl workflowEngine;
  public WorkflowImpl workflow;
  public LinkedList<String> path = new LinkedList<>();
  public ParseIssues parseIssues = new ParseIssues();
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

  public static WorkflowParse parse(WorkflowEngineImpl workflowEngine, Workflow apiWorkflow) {
    if (log.isDebugEnabled()) {
      log.debug("Parsing workflow");
    }
    WorkflowParse parse = new WorkflowParse(workflowEngine);
    parse.pushContext(apiWorkflow);
    parse.workflow = new WorkflowImpl();
    parse.workflow.parse(apiWorkflow, parse);
    parse.popContext();
    return null;
  }

  public WorkflowParse(WorkflowEngineImpl workflowEngine) {
    this.workflowEngine = workflowEngine;
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
    pushContext().pathElement(".multiInstance");
  }

  public void pushContext(String key) {
    pushContext().pathElement("."+key);
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

  public <T> BindingImpl<T> parseBinding(Activity activityApi, String key, Class<T> valueType) {
    return parseBinding(activityApi, key, valueType, false);
  }

  public <T> BindingImpl<T> parseBinding(Activity activityApi, String key, Class<T> valueType, boolean required) {
    String activityId = activityApi.getId();
    Object configuration = activityApi.getConfiguration(key);
    return parseBinding(configuration, key, valueType, required, activityId);
  }

  protected <T> BindingImpl<T> parseBinding(Object configuration, String key, Class<T> valueType, boolean required, String activityId) {
    if (configuration==null) {
      if (required) {
        addWarning("Configuration '%s' in activity '%s' not specified, but is a required binding", key, activityId);
      }
      return null;
    }
    Binding binding = null;
    if (configuration instanceof Map) {
      JsonService jsonService = workflowEngine.getServiceRegistry().getService(JsonService.class);
      binding = jsonService.jsonMapToObject((Map<String,Object>)configuration, Binding.class);
    } else if (!(configuration instanceof Binding)) {
      addWarning("Configuration '%s' in activity '%s' must be a binding, but was '%s'", key, activityId, configuration.getClass());
      return null;
    } else {
      binding = (Binding) configuration;
    }
    return parseBinding(binding, key, valueType, activityId);
  }

  public <T> List<BindingImpl<T>> parseBindings(Activity activityApi, String key, Class<T> valueType) {
    return parseBindings(activityApi, key, valueType, false);
  }
  
  public <T> List<BindingImpl<T>> parseBindings(Activity activityApi, String key, Class<T> valueType, boolean required) {
    String activityId = activityApi.getId();
    List<Object> configurations = (List<Object>) activityApi.getConfiguration(key);
    if (required && (configurations==null || configurations.isEmpty())) {
      addWarning("Configuration '%s' in activity '%s' not specified, but is a required list of bindings", key, activityId);
      return null;
    }
    List<BindingImpl<T>> bindingImpls = new ArrayList<>(configurations.size());
    for (Object configuration: (List<Object>)configurations) {
      bindingImpls.add(parseBinding(configuration, key, valueType, false, activityId));
    }
    return bindingImpls;
  }

  public BindingImpl parseBinding(Binding binding, String key, Class<?> valueType, String activityId) {
    BindingImpl bindingImpl = new BindingImpl(valueType);
    int values = 0;
    if (binding.getValue()!=null) {
      bindingImpl.value = binding.getValue();
      values++;
    }
    if (binding.getVariableId()!=null) {
      bindingImpl.variableId = binding.getVariableId();
      values++;
    }
    if (binding.getExpression()!=null) {
      ExpressionService expressionService = workflowEngine.getServiceRegistry().getService(ExpressionService.class);
      try {
        bindingImpl.expression = expressionService.compile(binding.getExpression());
      } catch (Exception e) {
        addError("Expression for input '%s' couldn't be compiled: %s", key+".expression", e.getMessage());
      }
      values++;
    }
    if (values==0) {
      addError("No value specified in binding '%s' for activity '%s'", key, activityId);
    } else if (values>1) {
      addError("Multiple values specified for '%s' for activity '%s'", key, activityId);
    }
    return bindingImpl;
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
