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
package com.effektif.workflow.impl.script;

import org.mozilla.javascript.Scriptable;

import com.effektif.workflow.impl.data.TypedValueImpl;


/**
 * @author Tom Baeyens
 */
public class RhinoObject extends org.mozilla.javascript.ScriptableObject {
  
  private static final long serialVersionUID = 1L;
  
  TypedValueImpl typedValue;

  public RhinoObject(TypedValueImpl typedValue) {
    this.typedValue = typedValue;
  }

  @Override
  public String getClassName() {
    return null;
  }

  @Override
  public Object get(String field, Scriptable arg1) {
    TypedValueImpl typedFieldValue = typedValue.type.dereference(typedValue.value, field);
    return new RhinoObject(typedFieldValue);
  }
}
