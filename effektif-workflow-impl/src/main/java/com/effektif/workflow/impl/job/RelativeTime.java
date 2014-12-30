/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.effektif.workflow.impl.job;

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
 * @author Tom Baeyens
 */
public class RelativeTime {
  
  public Long time;
  public String unit;
  public RelativeSource relativeSource;
  public Boolean businessHours; // TODO

  public RelativeTime relativeSource(RelativeSource relativeSource) {
    this.relativeSource = relativeSource;
    return this;
  }

  public RelativeTime time(Long time) {
    this.time = time;
    return this;
  }
  
  public RelativeTime unit(String unit) {
    this.unit = unit;
    return this;
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
  
  public static LocalDateTime get(RelativeTime relativeTime) {
    return get(relativeTime, new LocalDateTime());
  }
  
  public static LocalDateTime get(RelativeTime relativeTime, LocalDateTime base) {
    if (relativeTime==null || relativeTime.time==null || relativeTime.unit==null) {
      return null;
    }

    int time = ((Long)relativeTime.time).intValue();
    if (relativeTime.relativeSource==RelativeSource.BEFORE) {
      time = -time;
    }

    ReadablePeriod period = null;
    if (relativeTime.isUnitDays()) {
      period = Days.days(time);
    } else if (relativeTime.isUnitWeeks()) {
      period = Weeks.weeks(time);
    } else if (relativeTime.isUnitHours()) {
      period = Hours.hours(time);
    } else if (relativeTime.isUnitMonths()) {
      period = Months.months(time);
    } else if (relativeTime.isUnitYears()) {
      period = Years.years(time);
    } else if (relativeTime.isUnitMinutes()) {
      period = Minutes.minutes(time);
    } else if (relativeTime.isUnitSeconds()) {
      period = Seconds.seconds(time);
    } else {
      return null;
    }
    
    return base.plus(period);
  }

  public static LocalDateTime getToEndOfDay(LocalDateTime duedate) {
    return duedate.withTime(23, 59, 59, 999);
  }
}
