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

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.types.AnyTypeImpl;


/**
 * @author Tom Baeyens
 */
public class VariableImpl {
  
  public WorkflowImpl workflow;  
  public ScopeImpl parent;

  public Variable variable;
  public String id;
  public DataTypeImpl type;
  public Object defaultValue;

  public void parse(Variable variable, ScopeImpl parentImpl, WorkflowParser parser) {
    this.id = variable.getId();
    this.variable = variable;
    if (id==null || "".equals(id)) {
      parser.addError("Variable has no id");
    } else if (id.contains(".")) {
      parser.addError("Variable '%s' has a dot in the name", id);
    } else if (parser.variableIds.contains(id)) {
      parser.addError("Duplicate variable id '%s'", id);
    } else {
      parser.variableIds.add(id);
    }
    this.parent = parentImpl;
    DataTypeService dataTypeService = parser.getConfiguration(DataTypeService.class);
    DataType typeApi = variable.getType();
    if (typeApi!=null) {
      this.type = dataTypeService.createDataType(typeApi);
    } else {
      parser.addWarning("Variable '%s' does not have a type", id);
      this.type = new AnyTypeImpl();
    }
    this.defaultValue = variable.getDefaultValue();
  }

  public Variable serialize() {
    return null;
  }
}
