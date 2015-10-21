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
package com.effektif.workflow.impl.data;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.template.Hints;


public interface DataTypeImpl<T extends DataType> {
  
  void setConfiguration(Configuration configuration);

  T getDataType();
  Class<? extends DataType> getApiClass();

  /**
   * Indicates that this type doesn’t have to be serialised, because it’s okay to always use a single instance of the
   * type. A static type is always a primitive (single-valued) type, but a primitive type is not static if it has
   * additional {@link TypeDescriptor#configuration}.
   */
  boolean isStatic();

  T serialize();

//  /** only returns valid internal values. */
//  Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException;
//
//  Object convertInternalToJsonValue(Object internalValue);

  String convertInternalToText(Object value, Hints hints);

  /** Performs additional validation on a value that has successfully been deserialised, such as checking that a
   * {@link ChoiceType} value is actually one of the choice options.
   * @param internalValue is never null
   * @return null if the value is valid or an error message if the value is not valid. */
  String validateInternalValue(Object internalValue);

  DataTypeImpl parseDereference(String field, WorkflowParser parser);
  
  TypedValueImpl dereference(Object value, String field);

  String getFieldLabel(String field);
}
