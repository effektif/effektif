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

import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

import com.effektif.imports.CloningContext;
import com.effektif.model.FormField;
import com.effektif.model.engine.ExecutionContext;
import com.effektif.model.types.Type;
import com.effektif.model.types.binding.Binding;
import com.effektif.rest.json.Jsonnable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/**
 * @author Tom Baeyens
 */
public class ValueExpression extends Jsonnable {
  
  public static final String DB_binding = "b";
  @Embedded(DB_binding)
  public Binding binding;
  public ValueExpression binding(Binding binding) {
    this.binding = binding;
    return this;
  }
  
  public static final String DB_type = "t";
  @Embedded(DB_type)
  public Type type;
  public ValueExpression type(Type type) {
    this.type = type;
    return this;
  }

  public static final String DB_value = "vl";
  @Transient
  public Object value;
  public ValueExpression value(Object value) {
    this.value = value;
    return this;
  }

  @PrePersist
  public void onPrePersist(DBObject dbObject) {
    BasicDBObject basicdbObject = (BasicDBObject) dbObject;
    if (value!=null) {
      basicdbObject.put(FormField.DB_value, value);
    }
  }

  @PostLoad
  public void onPostLoad(DBObject dbObject) {
    BasicDBObject basicdbObject = (BasicDBObject) dbObject;
    value = basicdbObject.get(FormField.DB_value);
    if (value instanceof Integer) {
      value = ((Integer)value).longValue();
    }
  }
  
  @Override
  public void onPostParse() {
    if ((value instanceof Number)
        && !(value instanceof Double)){
      value = ((Number)value).doubleValue();
    }
  }

  public TypedValue eval(ExecutionContext executionContext) {
    if (binding!=null) {
      TypedValue typedValue = executionContext.activityInstance.getVariableTypedValue(binding);
      return new TypedValue(typedValue.type, typedValue.getValueInExpressionFormat());
    }
    return new TypedValue(type, value);
  }

  public void collectVariableIdsUsed(Set<ObjectId> variableIdsUsed) {
    ConditionExpression.addVariableIdUsed(variableIdsUsed, binding);
  }
  
  public String toString(ExecutionContext executionContext) {
    if (binding!=null) {
      return ConditionExpression.toString(binding, executionContext);
    }
    return (value!=null ? value.toString() : "null");
  }

  public void onClone(CloningContext ctx) {
    if (binding!=null) {
      binding.onClone(ctx);
    }
    if (value instanceof Number) {
      value = ((Number)value).doubleValue();
    }
  }
}
