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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.json.TypeName;

/**
 * A date type with variants date, time and datetime, representing a date in UTC timezone but without time zone information.
 *
 * Note that the UI and database always process dates in the UTC time zone, so this type’s value type is
 * {@link LocalDateTime}, the values are not the user’s local time but
 *
 * @author Peter Hilton
 */
@TypeName("date")
public class DateType extends DataType {

  /**
   * Parser for all three date variants.
   */
  public static DateTimeFormatter PARSER = ISODateTimeFormat.dateTimeParser();

  /**
   * Each date variant has its own formatter, that isn’t used for parsing.
   */
  private enum Kind {
    datetime(ISODateTimeFormat.dateTimeNoMillis()),
    date(ISODateTimeFormat.date()),
    time(ISODateTimeFormat.tTimeNoMillis());

    public DateTimeFormatter formatter;

    Kind(DateTimeFormatter formatter) {
      this.formatter = formatter;
    }
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

  @Override
  public void readBpmn(BpmnReader r) {
    String kindName = r.readStringAttributeEffektif("kind");
    this.kind = kindName == null ? Kind.datetime : Kind.valueOf(kindName);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeStringAttributeEffektif("kind", kind);
  }

  @Override
  public Object readBpmnValue(BpmnReader r) {
    String value = r.readStringAttributeEffektif("value");
    return value == null ? null : PARSER.parseLocalDateTime(value);
  }

  /**
   * Writes a {@link LocalDateTime} value using an ISO date format without milliseconds.
   */
  @Override
  public void writeBpmnValue(BpmnWriter w, Object value) {
    if (value != null && value instanceof LocalDateTime) {
      DateTime dateTime = ((LocalDateTime) value).toDateTime(DateTimeZone.UTC);
      w.writeStringAttributeEffektif("value", kind.formatter.print(dateTime));
    }
  }
}
