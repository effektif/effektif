/* Copyright (c) 2014, Effektif GmbH.
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
import java.util.List;
import java.util.StringTokenizer;

import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.TypedValueImpl;


/**
 * @author Tom Baeyens
 */
public class ExpressionImpl {

  public DataType type;
  public String variableId;
  public List<String> fields;

  public void parse(String expression, WorkflowParser parser) {
    if (expression==null || "".equals(expression)) {
      return;
    }

    StringTokenizer stringTokenizer = new StringTokenizer(expression, ".");
    boolean isError = false;
    while (stringTokenizer.hasMoreTokens() && !isError) {
      String token = stringTokenizer.nextToken();
      if (variableId==null) {
        variableId = token;
        ScopeImpl scope = parser.getCurrentScope();
        if (scope!=null) {
          VariableImpl variableImpl = scope.findVariableByIdRecursive(variableId);
          if (variableImpl == null) {
            parser.addWarning("Variable %s does not exist", variableId);
            isError = true;
          } else {
            type = variableImpl.type;
          }
        }
      } else {
        String field = token;
        if (fields==null) {
          fields = new ArrayList<>();
        }
        fields.add(field);
        if (type!=null) {
          TypedValueImpl typedValue = type.dereference(null, field);
          if (typedValue == null) {
            parser.addWarning("Field '%s' does not exist", field);
            isError = true;
          } else {
            type = typedValue.type;
          }
        }
      }
    }
  }

  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append(variableId);
    if (fields!=null) {
      for (String field: fields) {
        text.append(".");
        text.append(field);
      }
    }
    return text.toString();
  }
}
