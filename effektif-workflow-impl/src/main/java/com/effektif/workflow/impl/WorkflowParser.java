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
package com.effektif.workflow.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.effektif.workflow.api.condition.Unspecified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Element;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.ParseIssue.IssueType;
import com.effektif.workflow.api.workflow.ParseIssues;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.conditions.ConditionService;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.job.RelativeTimeImpl;
import com.effektif.workflow.impl.template.Hint;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.ExpressionImpl;
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
  public Stack<ParseContext> contextStack;
  public Set<String> activityIds = new HashSet<>();
  public Set<String> variableIds = new HashSet<>();
  public Set<String> transitionIds = new HashSet<>();
  public WorkflowParseListener workflowParseListener;
  
  public class ParseContext {
    ParseContext(String property, Object element, Object elementImpl, Integer index) {
      this.property = property;
      this.element = element;
      this.elementImpl = elementImpl;
      String indexText = null;
      if (element instanceof Element) {
        indexText = getIdText(element);
      }
      if (indexText==null && index!=null) {
        indexText = Integer.toString(index);
      }
    }
    public Object element;
    public String property;
    public String index;
    public Object elementImpl;
    public String toString() {
      if (index!=null) {
        return property+"["+index+"]";
      } else {
        return property;
      }
    }
    public Long getLine() {
      if (element instanceof Element) {
        Number line = (Number) ((Element)element).getProperty(PROPERTY_LINE);
        return line!=null ? line.longValue() : null;
      }
      return null;
    }
    public Long getColumn() {
      if (element instanceof Element) {
        Number column = (Number) ((Element)element).getProperty(PROPERTY_COLUMN);
        return column!=null ? column.longValue() : null;
      }
      return null;
    }
  }
  
  public static String getIdText(Object object) {
    if (object instanceof Activity) {
      return ((Activity)object).getId();
    } else if (object instanceof Transition) {
      return ((Transition)object).getId();
    } else if (object instanceof Variable) {
      return ((Variable)object).getId();
    } else if (object instanceof ExecutableWorkflow) {
      WorkflowId workflowId = ((ExecutableWorkflow)object).getId();
      return workflowId!=null ? workflowId.getInternal() : null;
    }
    return null;
  }

  public WorkflowParser(Configuration configuration) {
    this.configuration = configuration;
    this.path = new LinkedList<>();
    this.contextStack = new Stack<>();
    this.issues = new ParseIssues();
    
    // this cast is necessary to get the workflow parse listener optional
    // because the brewery.getOpt method is not available on the configuration itself
    if (configuration instanceof DefaultConfiguration) {
      DefaultConfiguration defaultConfiguration = (DefaultConfiguration)configuration;
      Brewery brewery = defaultConfiguration.getBrewery();
      this.workflowParseListener = brewery.getOpt(WorkflowParseListener.class);
    }
  }

  /**
   * Parses the content of <code>workflowApi</code> into <code>workflowImpl</code> and
   * adds any parse issues to <code>workflowApi</code>.
   * Use one parser for each parse.
   */
  public WorkflowImpl parse(AbstractWorkflow workflowApi) {
    workflow = new WorkflowImpl();
    workflow.id = workflowApi.getId();
    pushContext("workflow", workflowApi, workflow, null);
    workflow.parse(workflowApi, this);
    popContext();
    if (this.workflowParseListener!=null) {
      this.workflowParseListener.workflowParsed(workflowApi, workflow, this);
    }
    return workflow;
  }

  public void pushContext(String property, Object element, Object elementImpl, Integer index) {
    this.contextStack.push(new ParseContext(property, element, elementImpl, index));
  }
  
  public void popContext() {
    this.contextStack.pop();
  }
  
  protected String getPathText() {
    StringBuilder pathText = new StringBuilder();
    String dot = null;
    for (ParseContext validationContext: contextStack) {
      if (dot==null) {
        dot = ".";
      } else {
        pathText.append(dot);
      }
      pathText.append(validationContext.toString());
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

  public <T> List<BindingImpl<T>> parseBindings(List<Binding<T>> bindings, String bindingName) {
    if (bindings==null) {
      return null;
    }
    List<BindingImpl<T>> bindingImpls = new ArrayList<>();
    for (Binding<T> binding: bindings) {
      BindingImpl<T> bindingImpl = parseBinding(binding, bindingName, false);
      bindingImpls.add(bindingImpl);
    }
    return bindingImpls;
  }

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, String bindingName) {
    return parseBinding(binding, bindingName, false, null);
  }

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, String bindingName, boolean isRequired) {
    return parseBinding(binding, bindingName, isRequired, null);
  }

  /** @param type is only provided if the binding is untyped.  in that case the jackson deserialization didn't
   * instantiate the correct type and the deserialization needs to completed here based on the type.
   * only provide the type if the binding is untyped, otherwise use null or {@link #parseBinding(Binding, String, boolean)}. */
  public <T> BindingImpl<T> parseBinding(Binding<T> binding, String bindingName, boolean isRequired, DataType type) {
    pushContext(bindingName, binding, null, null);
    BindingImpl<T> bindingImpl = parseBinding(binding, type);
    int values = 0;
    if (bindingImpl!=null) {
      if (bindingImpl.value!=null) values++;
      if (bindingImpl.expression!=null) values++;
    }
    if (isRequired && values==0) {
      addWarning("Binding '%s' required and not specified", bindingName);
    } else if (values>1) {
      addWarning("Multiple values specified for binding '%s'", bindingName);
    }
    popContext();
    return bindingImpl;
  }

  protected <T> BindingImpl<T> parseBinding(Binding<T> binding, DataType targetType) {
    if (binding==null) {
      return null;
    }
    BindingImpl<T> bindingImpl = new BindingImpl<>();
    if (binding.getValue()!=null) {
      bindingImpl.value = binding.getValue();
      DataTypeService ds = configuration.get(DataTypeService.class);
      DataType type = binding.getType(); 
      if (type==null && targetType!=null) {
        type = targetType;
      }
      bindingImpl.type = ds.createDataType(type);
    }
    String expression = binding.getExpression();
    if (expression!=null) {
      bindingImpl.expression = new ExpressionImpl();
      pushContext("expression", expression, bindingImpl.expression, null);
      bindingImpl.expression.parse(expression, this);
      popContext();
    }
    if (binding.getMetadata() != null) {
      bindingImpl.metadata = binding.getMetadata();
    }
    String template = binding.getTemplate();
    if (template!=null) {
      bindingImpl.template = parseTextTemplate(template);
    }
    return bindingImpl;
  }
  
  public void addError(String message, Object... messageArgs) {
    ParseContext currentContext = contextStack.peek();
    issues.addIssue(IssueType.error, getPathText(), currentContext.getLine(), currentContext.getColumn(), message, messageArgs);
  }

  public void addWarning(String message, Object... messageArgs) {
    ParseContext currentContext = contextStack.peek();
    issues.addIssue(IssueType.warning, getPathText(), currentContext.getLine(), currentContext.getColumn(), message, messageArgs);
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
      this.addWarning("No start activities in %s", scope.getIdText());
    }
    return startActivities;
  }

  public MultiInstanceImpl parseMultiInstance(MultiInstance multiInstance) {
    if (multiInstance==null) {
      return null;
    }
    MultiInstanceImpl multiInstanceImpl = new MultiInstanceImpl();
    multiInstanceImpl.parse(multiInstance, this);
    return multiInstanceImpl;
  }

  public ConditionImpl parseCondition(Condition condition) {
    if (condition==null || condition instanceof Unspecified) {
      return null;
    }
    try {
      return configuration
              .get(ConditionService.class)
              .compile(condition, this);
    } catch (Exception e) {
      addWarning("Invalid condition '%s' : %s", condition, e.getMessage());
    }
    return null;
  }
  
  public TextTemplate parseTextTemplate(String templateText, Hint... hints) {
    if (templateText==null) {
      return null;
    }
    return new TextTemplate(templateText, hints, this);
  }
  
  public RelativeTimeImpl parseRelativeTime(RelativeTime relativeTime) {
    if (relativeTime==null || !relativeTime.valid()) {
      return null;
    }
    return new RelativeTimeImpl(relativeTime, this);
  }
  
  public ScopeImpl getCurrentScope() {
    for (int i=contextStack.size()-1; i>=0; i--) {
      Object elementImpl = contextStack.get(i).elementImpl;
      if (elementImpl instanceof ScopeImpl) {
        return (ScopeImpl) elementImpl;
      }
    }
    return null;
  }
}
