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
package com.effektif.workflow.impl.data.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.json.GenericType;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.DataType;


/** an object type with inline declared fields.
 * 
 * @author Tom Baeyens
 */
@TypeName("inlineObject")
public class InlineObjectType extends DataType {

  public static final InlineObjectType INSTANCE = new InlineObjectType();

  List<ObjectField> fields;
  
  public List<ObjectField> getFields() {
    return this.fields;
  }
  public void setFields(List<ObjectField> fields) {
    this.fields = fields;
  }
  public InlineObjectType field(ObjectField field) {
    if (fields==null) {
      fields = new ArrayList<ObjectField>();
    }
    fields.add(field);
    return this;
  }

  @Override
  public Type getValueType() {
    return new GenericType(Map.class, Object.class);
  }
}
