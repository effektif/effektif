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
package com.effektif.workflow.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.activities.Mapping;
import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.ParseIssue.IssueType;
import com.effektif.workflow.api.workflow.ParseIssues;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.types.MappingImpl;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.MultiInstanceImpl;
import com.effektif.workflow.impl.workflow.ScopeImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization. */
public class WorkflowParser {
  
  public static final String PROPERTY_LINE = "line";
  public static final String PROPERTY_COLUMN = "column";
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowParser.class);
  
  public Configuration configuration;
  public WorkflowImpl workflow;
  public LinkedList<String> path;
  public ParseIssues issues;
  public Stack<ValidationContext> contextStack;
  
  private class ValidationContext {
    ValidationContext pathElement(String pathElement) {
      this.pathElement = pathElement;
      return this;
    }
    ValidationContext position(Map<String, Object> properties) {
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
  public static WorkflowParser parse(Configuration configuration, Workflow workflowApi) {
    WorkflowParser parse = new WorkflowParser(configuration);
    parse.pushContext(workflowApi);
    parse.workflow = new WorkflowImpl();
    parse.workflow.parse(workflowApi, parse);
    parse.popContext();
    if (!parse.issues.isEmpty()) {
      workflowApi.setIssues(parse.issues);
    }
    return parse;
  }

  public WorkflowParser(Configuration configuration) {
    this.configuration = configuration;
    this.path = new LinkedList<>();
    this.contextStack = new Stack<>();
    this.issues = new ParseIssues();
  }
  
  public void pushContext(Workflow workflow) {
    pushContext()
            .pathElement("workflow")
            .position(workflow.getProperties());
  }

  public void pushContext(String propertyName, Scope scope, int index) {
    pushContext(propertyName, scope.getId(), index, scope.getProperties());
  }

  public void pushContext(String propertyName, Variable variable, int index) {
    pushContext(propertyName, variable.getId(), index, variable.getProperties());
  }

  public void pushContext(String propertyName, Timer timer, int index) {
    pushContext(propertyName, timer.getId(), index, timer.getProperties());
  }

  public void pushContext(String propertyName, Transition transition, int index) {
    pushContext(propertyName, transition.getId(), index, transition.getProperties());
  }

  public void pushContext(String propertyName, String id, int index, Map<String, Object> properties) {
    pushContext()
            .pathElement("."+propertyName+"["+(id!=null ? id : "")+(index!=-1 ? "|"+index: "")+"]")
            .position(properties);
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

  public String getExistingActivityIdsText(ScopeImpl scope) {
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

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, Class<T> bindingValueType, boolean required, Activity activityApi, String fieldName) {
    String activityId = activityApi.getId();
    if (binding==null) {
      if (required) {
        addWarning("Configuration '%s' in activity '%s' not specified, but is a required list of bindings", fieldName, activityId);
      }
      return null;
    }
    BindingImpl bindingImpl = new BindingImpl(bindingValueType);
    int values = 0;
    if (binding.getValue()!=null) {
      bindingImpl.typedValue = parseTypedValue(binding.getTypedValue());
      values++;
    }
    if (binding.getVariableId()!=null) {
      bindingImpl.variableId = binding.getVariableId();
      values++;
    }
    if (binding.getExpression()!=null) {
      ExpressionService expressionService = configuration.get(ExpressionService.class);
      try {
        bindingImpl.expression = expressionService.compile(binding.getExpression());
      } catch (Exception e) {
        addError("Expression for input '%s' couldn't be compiled: %s", fieldName+".expression", e.getMessage());
      }
      values++;
    }
    if (values==0) {
      addError("No value specified in binding '%s' for activity '%s'", fieldName, activityId);
    } else if (values>1) {
      addError("Multiple values specified for '%s' for activity '%s'", fieldName, activityId);
    }
    return bindingImpl;
  }

  protected TypedValueImpl parseTypedValue(TypedValue typedValue) {
    if (typedValue==null) {
      return null;
    }
    DataTypeService dataTypeService = configuration.get(DataTypeService.class);
    DataType type = dataTypeService.createDataType(typedValue.getType());
    return new TypedValueImpl(type, typedValue.getValue());
  }

  public <T> List<BindingImpl<T>> parseBindings(List<Binding> bindings, Class<T> bindingValueType, boolean required, Activity activityApi, String fieldName) {
    String activityId = activityApi.getId();
    if (bindings==null || bindings.isEmpty()) {
      if (required) {
        addWarning("Configuration '%s' in activity '%s' %s. A list of bindings is required", fieldName, activityId, bindings==null ? "not specified" : "an empty list");
      }
      return null;
    }
    List<BindingImpl<T>> bindingImpls = new ArrayList<>(bindings.size());
    for (Binding binding: bindings) {
      bindingImpls.add(parseBinding(binding, bindingValueType, false, activityApi, fieldName));
    }
    return bindingImpls;
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

  public <T> T getConfiguration(Class<T> type) {
    return configuration.get(type);
  }

  public List<ActivityImpl> getStartActivities(ScopeImpl scope) {
    if (scope.activities==null) {
      return null;
    }
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

  public List<MappingImpl> parseMappings(List<Mapping> mappingsApi, Activity activity, String fieldName) {
    if (mappingsApi==null) {
      return null;
    }
    List<MappingImpl> mappingImpls = new ArrayList<>(mappingsApi.size());
    int i=0;
    for (Mapping mappingApi: mappingsApi) {
      MappingImpl mappingImpl = new MappingImpl();
      mappingImpl.sourceBinding = this.parseBinding(mappingApi.getSourceBinding(), Object.class, false, activity, fieldName+"["+i+"]");
      mappingImpl.destinationKey = mappingApi.getDestinationKey();
      mappingImpls.add(mappingImpl);
      i++;
    }
    return mappingImpls;
  }
}
