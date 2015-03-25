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
package com.effektif.workflow.impl.conditions;

import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Property;

import com.effektif.imports.CloningContext;
import com.effektif.model.Variable;
import com.effektif.model.engine.ExecutionContext;
import com.effektif.model.types.binding.Binding;
import com.effektif.rest.json.Jsonnable;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.Joiner;


/**
 * @author Tom Baeyens
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public abstract class ConditionExpression {
  
  public abstract boolean eval(ActivityInstanceImpl activityInstance);

  public abstract String toString(ExecutionContext executionContext);
  
  public static String getVariableName(ObjectId variableId, ExecutionContext executionContext) {
    if (executionContext==null || executionContext.processDefinition==null) {
      return variableId.toString();
    }
    Variable variable = executionContext.processDefinition.findVariable(variableId);
    return (variable!=null && variable.name!=null ? variable.name : variableId.toString());
  }
  
  public static String toString(Binding binding, ExecutionContext executionContext) {
    if (binding==null || binding.variableId==null) return "null";
    Variable variable = executionContext.activityInstance.getVariable(binding.variableId);
    if (variable==null) return "bug";
    return "{"+(variable.name!=null ? variable.name : (variable.id!=null ? variable.id : System.identityHashCode(variable.id)))
            +(binding.fields!=null ? "}."+Joiner.on(".").join(binding.fields) : "}");
  }
}
