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

import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.impl.data.AbstractDataType;


/**
 * @author Tom Baeyens
 */
public class BooleanTypeImpl extends AbstractDataType<BooleanType> {
  
  public BooleanTypeImpl() {
    super(BooleanType.INSTANCE);
  }
  
  @Override
  public String validateInternalValue(Object internalValue) {
    if (! (internalValue instanceof Boolean)) {
      return "Booleans must be of type "+Boolean.class.getName();
    }
    return null;
  }
}
