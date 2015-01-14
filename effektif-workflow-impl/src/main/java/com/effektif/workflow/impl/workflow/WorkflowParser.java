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
public class WorkflowParser {
  
  public static final String PROPERTY_LINE = "line";
  public static final String PROPERTY_COLUMN = "column";
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowParser.class);
  
  public WorkflowEngineImpl workflowEngine;
  public WorkflowImpl workflow;
  public LinkedList<String> path;
  public ParseIssues issues;
  public Stack<ValidationContext> contextStack;
  
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

  /** parses the content of workflowApi into workflowImpl and 
   * adds any parse issues to workflowApi.
   * Use one parser for each parse.
   * By returning the parser itself you can access the  */
  public static WorkflowParser parse(WorkflowEngineImpl workflowEngine, Workflow workflowApi) {
    WorkflowParser parse = new WorkflowParser(workflowEngine, workflowApi);
    parse.pushContext(workflowApi);
    parse.workflow = new WorkflowImpl();
    parse.workflow.parse(workflowApi, parse);
    parse.popContext();
    if (!parse.issues.isEmpty()) {
      workflowApi.setIssues(parse.issues);
    }
    return parse;
  }

  public WorkflowParser(WorkflowEngineImpl workflowEngine, Workflow workflowApi) {
    this.workflowEngine = workflowEngine;
    this.path = new LinkedList<>();
    this.contextStack = new Stack<>();
    this.issues = new ParseIssues();
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

  public <T> BindingImpl<T> parseBinding(Activity activityApi, String key, Class<T> bindingValueType) {
    return parseBinding(activityApi, key, bindingValueType, false);
  }

  public <T> BindingImpl<T> parseBinding(Activity activityApi, String key, Class<T> bindingValueType, boolean required) {
    String activityId = activityApi.getId();
    Object configuration = activityApi.getConfiguration(key);
    return parseBinding(configuration, bindingValueType, required, activityId, key);
  }

  public <T> List<BindingImpl<T>> parseBindings(Activity activityApi, String key, Class<T> bindingValueType) {
    return parseBindings(activityApi, key, bindingValueType, false);
  }
  
  public <T> List<BindingImpl<T>> parseBindings(Activity activityApi, String key, Class<T> bindingValueType, boolean required) {
    String activityId = activityApi.getId();
    Object bindingsObject = activityApi.getConfiguration(key);
    if (bindingsObject!=null && !(bindingsObject instanceof List)) {
      addError("Configuration '%s' in activity '%s' should be a list, but was ", key, activityId, bindingsObject.getClass().getSimpleName());
    }
    List<Object> bindingObjectList = (List<Object>) bindingsObject;
    if (bindingObjectList==null || bindingObjectList.isEmpty()) {
      if (required) {
        addWarning("Configuration '%s' in activity '%s' not specified, but is a required list of bindings", key, activityId);
      }
      return null;
    }
    List<BindingImpl<T>> bindingImpls = new ArrayList<>(bindingObjectList.size());
    for (Object configurationElement: (List<Object>)bindingObjectList) {
      bindingImpls.add(parseBinding(configurationElement, bindingValueType, false, activityId, key));
    }
    return bindingImpls;
  }
  
  public String parseString(Activity activityApi, String key, boolean required) {
    Object configurationValue = parseObject(activityApi, key, required);
    if (!(configurationValue instanceof String)) {
      addError("Configuration '%s' in activity '%s' must be a string, but was '%s' (%s)", key, activityApi.getId(), configurationValue.toString(), configurationValue.getClass().getSimpleName());
      return null;
    }
    return (String)configurationValue;
  }

  public Object parseObject(Activity activityApi, String key, boolean required) {
    Object configurationValue = activityApi.getConfiguration(key);
    if (configurationValue==null) {
      if (required) {
        addWarning("Configuration '%s' in activity '%s' not specified, but is required", key, activityApi.getId());
      }
      return null;
    }
    return configurationValue;
  }

  protected <T> BindingImpl<T> parseBinding(Object o, Class<T> bindingValueType, boolean required, String activityId, String key) {
    Binding binding = parseObject(o, Binding.class, required, activityId, key);
    return parseBinding(binding, bindingValueType, activityId, key);
  }
  
  public BindingImpl parseBinding(Binding binding, Class<?> bindingValueType, String activityId, String key) {
    if (binding==null) {
      return null;
    }
    BindingImpl bindingImpl = new BindingImpl(bindingValueType);
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

  public <T> T parseObject(Object object, Class<T> objectType, boolean required, String activityId, String key) {
    if (object==null) {
      if (required) {
        addWarning("Configuration '%s' in activity '%s' not specified, but is required", key, activityId);
      }
      return null;
    }
    if (objectType.isAssignableFrom(object.getClass())) {
      return (T) object;
    }
    if (object instanceof Map) {
      return getJsonService().jsonMapToObject((Map<String,Object>)object, objectType);
    }
    addWarning("Configuration '%s' in activity '%s' must be a binding, but was '%s'", key, activityId, object.getClass().getName());
    return null;
  }

  protected JsonService getJsonService() {
    return workflowEngine.getServiceRegistry().getService(JsonService.class);
  }

  public void addError(String message, Object... messageArgs) {
    ValidationContext currentContext = contextStack.peek();
    issues.addIssue(IssueType.error, getPathText(), currentContext.line, currentContext.column, message, messageArgs);
  }

  public void addWarning(String message, Object... messageArgs) {
    ValidationContext currentContext = contextStack.peek();
    issues.addIssue(IssueType.warning, getPathText(), currentContext.line, currentContext.column, message, messageArgs);
  }
  
  public ParseIssues getIssues() {
    return issues;
  }

  public WorkflowParser checkNoErrors() {
    issues.checkNoErrors();
    return this;
  }

  public WorkflowParser checkNoErrorsAndNoWarnings() {
    issues.checkNoErrorsAndNoWarnings();
    return this;
  }

  public boolean hasErrors() {
    return issues.hasErrors();
  }

  public WorkflowImpl getWorkflow() {
    return workflow;
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
