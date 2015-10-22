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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.util.StringUtil;


/**
 * @author Tom Baeyens
 */
public class ExpressionImpl {

  public DataTypeImpl type;
  public String variableId;
  public List<String> fieldKeys;
  public List<ExpressionField> fields;
  
  public static class ExpressionField {
    public String fieldKey;
    public String fieldName;
    public DataTypeImpl type;
    public ExpressionField(String fieldKey, String fieldName, DataTypeImpl type) {
      this.fieldKey = fieldKey;
      this.fieldName = fieldName;
      this.type = type;
    }
  }

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
        if (fieldKeys==null) {
          fieldKeys = new ArrayList<>();
          fields = new ArrayList<>();
        }
        fieldKeys.add(field);
        
        String fieldName = null;
        if (type!=null) {
          fieldName = type.getFieldLabel(field);
          type = type.parseDereference(field, parser);
        }
        
        if (fieldName==null) {
          fieldName = StringUtil.deCamelCase(field);
        }
        
        fields.add(new ExpressionField(field, fieldName, type));
      }
    }
  }

  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append(variableId);
    if (fieldKeys!=null) {
      for (String field: fieldKeys) {
        text.append(".");
        text.append(field);
      }
    }
    return text.toString();
  }
}
