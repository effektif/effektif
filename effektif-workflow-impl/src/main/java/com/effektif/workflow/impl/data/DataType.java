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
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.activity.Plugin;
import com.effektif.workflow.impl.template.Hints;


public interface DataType<T extends Type> extends Plugin {
  
  void initialize(Configuration configuration);

  Class<?> getValueClass();
  Class<? extends Type> getApiClass();
  boolean isStatic();
  T serialize();

  TypeGenerator getTypeGenerator();

  /** only returns valid internal values. */
  Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException;

  Object convertInternalToJsonValue(Object internalValue);

  String convertInternalToText(Object value, Hints hints);

  /** Read the a binding value from the given XML element. */
  Binding readValue(XmlElement value);

  /** Write the given value as an attribute on the given XML element. */
  void writeValue(XmlElement xml, Object value);
  
  /** The returned value is never null. */
  TypedValueImpl dereference(Object value, String field);
}
