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

import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;


public class VariableImpl {
  
  public WorkflowImpl workflow;  
  public ScopeImpl parent;

  public String id;
  public DataType type;
  public Object initialValue;

  public void parse(Variable variableApi, WorkflowParser parser, ScopeImpl parent) {
    this.id = variableApi.getId();
    if (id==null || "".equals(id)) {
      parser.addError("Variable has no id");
    } else if (id.contains(".")) {
      parser.addError("Variable '%s' has a dot in the name", id);
    } else if (parser.variableIds.contains(id)) {
      parser.addError("Duplicate variable id '%s'", id);
    } else {
      parser.variableIds.add(id);
    }
    this.parent = parent;
    DataTypeService dataTypeService = parser.getConfiguration(DataTypeService.class);
    Type typeApi = variableApi.getType();
    if (typeApi!=null) {
      this.type = dataTypeService.createDataType(typeApi);
    } else {
      parser.addError("Variable '%s' does not have a type", id);
    }
  }

  public Variable serialize() {
    return null;
  }
}
