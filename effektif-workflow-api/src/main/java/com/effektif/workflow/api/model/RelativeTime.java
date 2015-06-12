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

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;


/**
 * A duration (length of time), with an integer length and units years, months, weeks, hours, minutes or seconds.
 *
 * @author Tom Baeyens
 */
public class RelativeTime {
  
  protected String before;
  protected Integer time;

  public String getBefore() {
    return this.before;
  }
  public void setBefore(String before) {
    this.before = before;
  }

  public RelativeTime before(String before) {
    this.before = before;
    return this;
  }
  
  public Integer getTime() {
    return this.time;
  }
  public void setTime(Integer time) {
    this.time = time;
  }

  public RelativeTime time(Integer time) {
    this.time = time;
    return this;
  }
  
  public String unit;

  public RelativeTime unit(String unit) {
    this.unit = unit;
    return this;
  }

  public static RelativeTime seconds(int seconds) {
    return new RelativeTime() 
      .time(seconds)
      .unit("seconds");
  }

  public static RelativeTime minutes(int minutes) {
    return new RelativeTime() 
      .time(minutes)
      .unit("minutes");
  }

  public static RelativeTime hours(int hours) {
    return new RelativeTime() 
      .time(hours)
      .unit("hours");
  }

  public static RelativeTime days(int days) {
    return new RelativeTime() 
      .time(days)
      .unit("days");
  }

  public static RelativeTime weeks(int weeks) {
    return new RelativeTime() 
      .time(weeks)
      .unit("weeks");
  }

  public static RelativeTime months(int months) {
    return new RelativeTime() 
      .time(months)
      .unit("months");
  }

  public static RelativeTime years(int years) {
    return new RelativeTime() 
      .time(years)
      .unit("years");
  }

  protected boolean isUnitSeconds() {
    return "seconds".equalsIgnoreCase(unit)
            || "second".equalsIgnoreCase(unit);
  }

  protected boolean isUnitMinutes() {
    return "minutes".equalsIgnoreCase(unit)
            || "minute".equalsIgnoreCase(unit);
  }

  protected boolean isUnitYears() {
    return "years".equalsIgnoreCase(unit)
            || "year".equalsIgnoreCase(unit);
  }

  protected boolean isUnitMonths() {
    return "months".equalsIgnoreCase(unit)
            || "month".equalsIgnoreCase(unit);
  }

  protected boolean isUnitHours() {
    return "hours".equalsIgnoreCase(unit)
            || "hour".equalsIgnoreCase(unit);
  }

  protected boolean isUnitWeeks() {
    return "weeks".equalsIgnoreCase(unit)
            || "week".equalsIgnoreCase(unit);
  }

  protected boolean isUnitDays() {
    return "days".equalsIgnoreCase(unit)
        || "day".equalsIgnoreCase(unit);
  }

  public boolean isDayResolutionOrBigger() {
    return !(isUnitSeconds() || isUnitMinutes() || isUnitHours());
  }
  
  public LocalDateTime resolve(LocalDateTime base) {
    if (this.time==null || this.unit==null) {
      return null;
    }

    int time = this.time;
    if (this.before!=null) {
      time = -time;
    }

    ReadablePeriod period = null;
    if (this.isUnitDays()) {
      period = Days.days(time);
    } else if (this.isUnitWeeks()) {
      period = Weeks.weeks(time);
    } else if (this.isUnitHours()) {
      period = Hours.hours(time);
    } else if (this.isUnitMonths()) {
      period = Months.months(time);
    } else if (this.isUnitYears()) {
      period = Years.years(time);
    } else if (this.isUnitMinutes()) {
      period = Minutes.minutes(time);
    } else if (this.isUnitSeconds()) {
      period = Seconds.seconds(time);
    } else {
      return null;
    }
    
    return base.plus(period);
  }

  public static LocalDateTime getToEndOfDay(LocalDateTime duedate) {
    return duedate.withTime(23, 59, 59, 999);
  }

  /**
   * Parses a string formatted using {@link #toString()} as a relative time.
   */
  public static RelativeTime parse(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Cannot parse relative time from value ‘" + value + "’");
    }
    String[] parts = value.trim().split(" ");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Cannot parse relative time from value ‘" + value + "’");
    }
    try {
      int time = Integer.parseInt(parts[0]);
      RelativeTime t = new RelativeTime().time(time).unit(parts[1]);
      boolean validUnit = t.isUnitYears() || t.isUnitMonths() || t.isUnitWeeks() || t.isUnitDays() || t.isUnitHours() ||
        t.isUnitMinutes() || t.isUnitSeconds();
      if (!validUnit) {
        throw new IllegalArgumentException("Invalid time unit in relative time ‘" + value + "’");
      }
      return t;
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid time value in relative time ‘" + value + "’");
    }
  }

  @Override
  public String toString() {
    return time + " " + unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RelativeTime that = (RelativeTime) o;
    if (before != null ? !before.equals(that.before) : that.before != null) return false;
    if (!time.equals(that.time)) return false;
    if (!unit.equals(that.unit)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = before != null ? before.hashCode() : 0;
    result = 31 * result + time.hashCode();
    result = 31 * result + unit.hashCode();
    return result;
  }
}
