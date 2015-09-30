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

import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.template.Hints;

/**
 * @author Tom Baeyens
 */
public class DateTypeImpl extends AbstractDataType<DateType> {

  public static DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();
  public static DateTimeFormatter printer = ISODateTimeFormat.dateTime();

  public DateTypeImpl() {
    super(DateType.DATETIME);
  }

  public DateTypeImpl(DateType type) {
    super(type);
  }

  @Override
  public String convertInternalToText(Object value, Hints hints) {
    DateTimeFormatter textFormatter = DateTimeFormat.longDate().withLocale(getLocale());
    return value!=null ? textFormatter.print((ReadablePartial)value) : null;
  }

  protected Locale getLocale() {
    return Locale.getDefault();
  }

  @Override
  public boolean isStatic() {
    return false;
  }
  
  @Override
  public String validateInternalValue(Object internalValue) {
    if (! (internalValue instanceof LocalDateTime)) {
      return "Dates must be "+LocalDateTime.class.getName();
    }
    return null;
  }
}
