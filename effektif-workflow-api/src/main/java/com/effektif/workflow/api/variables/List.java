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
package com.effektif.workflow.api.variables;

import com.effektif.workflow.api.workflow.Variable;


/**
 * @author Tom Baeyens
 */
public class List {

  protected Variable elementType;
  
  public List() {
  }
  public List(Variable elementType) {
    this.elementType = elementType;
  }

  public Variable getElementType() {
    return this.elementType;
  }
  public void setElementType(Variable elementType) {
    this.elementType = elementType;
  }
  
}
