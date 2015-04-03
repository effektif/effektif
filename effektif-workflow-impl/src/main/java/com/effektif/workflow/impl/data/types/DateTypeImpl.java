/*
 * Copyright 2015 Effektif GmbH.
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

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.mapper.deprecated.LocalDateTimeDeserializer;
import com.effektif.workflow.impl.mapper.deprecated.LocalDateTimeSerializer;

/**
 * @author Tom Baeyens
 */
public class DateTypeImpl extends AbstractDataType<DateType> {

  public DateTypeImpl() {
    super(DateType.INSTANCE, Boolean.class);
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) {
      return null;
    }
    String timeString = (String) jsonValue;
    return LocalDateTimeDeserializer.formatter.parseLocalDateTime(timeString);
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    if (internalValue==null) {
      return null;
    }
    return LocalDateTimeSerializer.formatter.print((LocalDateTime)internalValue);
  }
}
