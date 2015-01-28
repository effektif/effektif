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
package com.effektif.workflow.impl.configuration;

import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.types.BindingTypeImpl;
import com.effektif.workflow.impl.data.types.JavaBeanTypeImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.data.types.MapTypeImpl;
import com.effektif.workflow.impl.data.types.NumberTypeImpl;
import com.effektif.workflow.impl.data.types.TextTypeImpl;
import com.effektif.workflow.impl.data.types.UserReferenceTypeImpl;
import com.effektif.workflow.impl.data.types.VariableReferenceTypeImpl;
import com.effektif.workflow.impl.data.types.WorkflowReferenceTypeImpl;


public class DefaultDataTypeService extends DataTypeService {

  @Override
  public void brew(Brewery brewery) {
    super.brew(brewery);
    registerDataType(new BindingTypeImpl());
    registerDataType(new JavaBeanTypeImpl());
    registerDataType(new NumberTypeImpl());
    registerDataType(new ListTypeImpl());
    registerDataType(new MapTypeImpl());
    registerDataType(new TextTypeImpl());
    registerDataType(new UserReferenceTypeImpl());
    registerDataType(new VariableReferenceTypeImpl());
    registerDataType(new WorkflowReferenceTypeImpl());
  }
}
