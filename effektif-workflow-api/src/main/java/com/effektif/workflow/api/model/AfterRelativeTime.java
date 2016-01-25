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
package com.effektif.workflow.api.model;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.joda.time.Years;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Binding;


/**
 * @author Tom Baeyens
 */
@TypeName("after")
public class AfterRelativeTime extends RelativeTime {

  protected Integer duration;
  public String durationUnit;

  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);
    this.duration = new Integer(r.readStringAttributeEffektif("duration"));
    this.durationUnit = r.readStringAttributeEffektif("durationUnit");
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeStringAttributeEffektif("type", getType());
    w.writeStringAttributeEffektif("duration", duration);
    w.writeStringAttributeEffektif("durationUnit", durationUnit);
  }

  public String getType() {
    return AFTER;
  }
  
  public Integer getDurationAsInt() {
    return duration;
  }

  public Integer getDuration() {
    return this.duration;
  }
  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public AfterRelativeTime duration(Integer duration) {
    this.duration = duration;
    return this;
  }
  
  public String getDurationUnit() {
    return durationUnit;
  }
  
  public void setDurationUnit(String durationUnit) {
    this.durationUnit = durationUnit;
  }
  public AfterRelativeTime durationUnit(String durationUnit) {
    this.durationUnit = durationUnit;
    return this;
  }

  public boolean isDayResolutionOrBigger() {
    return !(MINUTES.equals(durationUnit)
             || HOURS.equals(durationUnit) );
  }
  
  public LocalDateTime resolve(LocalDateTime base) {
    if (this.duration==null || this.durationUnit==null) {
      return null;
    }

    ReadablePeriod period = null;
    if (DAYS.equals(durationUnit)) {
      period = Days.days(getDurationAsInt());
    } else if (WEEKS.equals(durationUnit)) {
      period = Weeks.weeks(getDurationAsInt());
    } else if (HOURS.equals(durationUnit)) {
      period = Hours.hours(getDurationAsInt());
    } else if (MONTHS.equals(durationUnit)) {
      period = Months.months(getDurationAsInt());
    } else if (YEARS.equals(durationUnit)) {
      period = Years.years(getDurationAsInt());
    } else if (MINUTES.equals(durationUnit)) {
      period = Minutes.minutes(getDurationAsInt());
    } else {
      return null;
    }

    LocalDateTime time = base.plus(period);
    
    if (atHour!=null) {
      LocalDateTime atTime = time.withTime(atHour, atMinute!=null ? atMinute : 0, 0, 0);
      if (atTime.isBefore(time)) {
        time = atTime.plusDays(1);
      } else {
        time = atTime;
      }
    } else if (isDayResolutionOrBigger()) {
      time = time.withTime(23, 59, 59, 999);
    }
    
    return time;
  }
  
  @Override
  public boolean valid() {
    if (duration == null || durationUnit == null) {
      return false;
    }
    return MINUTES.equals(durationUnit)
           || HOURS.equals(durationUnit)
           || DAYS.equals(durationUnit)
           || WEEKS.equals(durationUnit)
           || MONTHS.equals(durationUnit)
           || YEARS.equals(durationUnit);
  }

  @Override
  public AfterRelativeTime base(Binding<LocalDateTime> base) {
    return (AfterRelativeTime) super.base(base);
  }
  
  
}
