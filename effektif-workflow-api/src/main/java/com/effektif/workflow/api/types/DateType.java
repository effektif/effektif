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
package com.effektif.workflow.api.types;

import java.lang.reflect.Type;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.json.TypeName;

/**
 * @author Peter Hilton
 */
@TypeName("date")
public class DateType extends DataType {

  private enum Kind {
    datetime, date, time;
  }

  public static final DateType DATETIME = new DateType();
  public static final DateType DATE = new DateType(Kind.date);
  public static final DateType TIME = new DateType(Kind.time);

  private Kind kind;

  public DateType() {
    this(Kind.datetime);
  }

  public DateType(Kind kind) {
    this.kind = kind;
  }

  @Override
  public Type getValueType() {
    return LocalDateTime.class;
  }

  public String getKind() {
    return kind.toString();
  }

  public boolean isDate() {
    return Kind.date.equals(kind);
  }

  public boolean isDateTime() {
    return Kind.datetime.equals(kind);
  }

  public boolean isTime() {
    return Kind.time.equals(kind);
  }


  public DateType date() {
    kind = Kind.date;
    return this;
  }

  public DateType time() {
    kind = Kind.time;
    return this;
  }
}
