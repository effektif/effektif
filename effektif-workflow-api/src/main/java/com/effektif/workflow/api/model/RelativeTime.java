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
package com.effektif.workflow.api.model;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.workflow.Binding;


/**
 * A duration (length of time), with an integer length and units years, months, weeks, hours, minutes or seconds.
 *
 * @author Tom Baeyens
 */
public abstract class RelativeTime implements BpmnReadable, BpmnWritable {
  

  public static final Class[] SUBCLASSES = new Class[]{
    AfterRelativeTime.class,
    BeforeRelativeTime.class,
    NextRelativeTime.class
    // if you add a subclass here, make sure you also update method readBpmnPolymorphic below
  };

  public static final String NEXT = "next";
  public static final String AFTER = "after";
  public static final String BEFORE = "before";

  public static final String MINUTES = "minutes";
  public static final String HOURS = "hours";
  public static final String DAYS = "days";
  public static final String WEEKS = "weeks";
  public static final String MONTHS = "months";
  public static final String YEARS = "years";

  protected Binding<LocalDateTime> base;
  protected Integer atHour;
  protected Integer atMinute;

  public static RelativeTime readBpmnPolymorphic(BpmnReader r) {
    String type = r.readStringAttributeEffektif("type");
    if (type==null) {
      String after = r.readStringAttributeEffektif("after");
      if (after!=null) {
        return parseBackwardsCompatibleString(after);
      }
      return null;
    }
    RelativeTime relativeTime = null; 
    if (AFTER.equals(type)) {
      relativeTime = new AfterRelativeTime();
    } else if (BEFORE.equals(type)) {
      relativeTime = new BeforeRelativeTime();
    } else if (NEXT.equals(type)) {
      relativeTime = new NextRelativeTime();
    } else {
      throw new RuntimeException("TODO: Find out how to report parsing warning");
    }
    
    relativeTime.readBpmn(r);

    return relativeTime;
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.writeBinding("base", base);
    w.writeIntegerAttributeEffektif("atHour", atHour);
    w.writeIntegerAttributeEffektif("atMinute", atMinute);
  }

  @Override
  public void readBpmn(BpmnReader r) {
    base = r.readBinding("base", LocalDateTime.class);
    atHour = r.readIntegerAttributeEffektif("atHour");
    atMinute = r.readIntegerAttributeEffektif("atMinute");
  }

  /**
   * Parses a string formatted using {@link #toString()} as a relative time.
   */
  public static RelativeTime parseBackwardsCompatibleString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Cannot parse relative time from value ‘" + value + "’");
    }
    String[] parts = value.trim().split(" ");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Cannot parse relative time from value ‘" + value + "’");
    }
    try {
      int time = Integer.parseInt(parts[0]);
      RelativeTime relativeTime = new AfterRelativeTime().duration(time).durationUnit(parts[1]);
      if (!relativeTime.valid()) {
        throw new IllegalArgumentException("Invalid time unit in relative time ‘" + value + "’");
      }
      return relativeTime;
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid time value in relative time ‘" + value + "’");
    }
  }


  public Integer getAtHour() {
    return atHour;
  }
  public void setAtHour(Integer atHour) {
    this.atHour = atHour;
  }

  public Integer getAtMinute() {
    return atMinute;
  }
  public void setAtMinute(Integer atMinute) {
    this.atMinute = atMinute;
  }

  public RelativeTime at(Integer atHour, Integer atMinute) {
    this.atHour = atHour;
    this.atMinute = atMinute;
    return this;
  }
  
  public Binding<LocalDateTime> getBase() {
    return this.base;
  }
  public void setBase(Binding<LocalDateTime> base) {
    this.base = base;
  }
  public RelativeTime base(Binding<LocalDateTime> base) {
    this.base = base;
    return this;
  }
  
  public static AfterRelativeTime minutes(int minutes) {
    return new AfterRelativeTime() 
      .duration(minutes)
      .durationUnit(MINUTES);
  }

  public static AfterRelativeTime hours(int hours) {
    return new AfterRelativeTime() 
      .duration(hours)
      .durationUnit(HOURS);
  }

  public static AfterRelativeTime days(int days) {
    return new AfterRelativeTime() 
      .duration(days)
      .durationUnit(DAYS);
  }

  public static AfterRelativeTime weeks(int weeks) {
    return new AfterRelativeTime() 
      .duration(weeks)
      .durationUnit(WEEKS);
  }

  public static AfterRelativeTime months(int months) {
    return new AfterRelativeTime() 
      .duration(months)
      .durationUnit(MONTHS);
  }

  public static AfterRelativeTime years(int years) {
    return new AfterRelativeTime() 
      .duration(years)
      .durationUnit(YEARS);
  }

  public abstract LocalDateTime resolve(LocalDateTime base);
  public abstract boolean valid();

  public String appendAt(String message) {
    if (atHour==null) return message;
    return " at "+atHour+":"+atMinute;
  }
}
