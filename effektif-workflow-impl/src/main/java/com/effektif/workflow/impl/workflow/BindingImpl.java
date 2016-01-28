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

import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.template.TextTemplate;

import java.util.Map;

/**
 * @author Tom Baeyens
 */
public class BindingImpl<T> {

  public T value;
  public DataTypeImpl type;
  public ExpressionImpl expression;
  public Map<String, Object> metadata;
  public TextTemplate template;

  @Override
  public String toString() {
    if (value!=null) {
      return value.toString();
    }
    if (expression!=null) {
      return expression.toString();
    }
    if (template!=null) {
      return template.toString();
    }
    return "(emptybinding)";
  }

}
