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

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.json.TypeName;


/**
 * @author Tom Baeyens
 */
@TypeName("next")
public class NextRelativeTime extends RelativeTime {

  public static final String HOUR_IN_DAY = "hourInDay";
  public static final String DAY_IN_WEEK = "dayInWeek";
  public static final String DAY_IN_MONTH = "dayInMonth";
  
  protected Integer index;
  protected String indexUnit;

  public NextRelativeTime() {
  }
  
  public NextRelativeTime(Integer index, String indexUnit) {
    this.index = index;
    this.indexUnit = indexUnit;
  }

  public static NextRelativeTime hourInDay(Integer hourInDay) {
    return new NextRelativeTime(hourInDay, HOUR_IN_DAY);
  }
  public static NextRelativeTime dayInWeek(Integer dayInWeek) {
    return new NextRelativeTime(dayInWeek, DAY_IN_WEEK);
  }
  public static NextRelativeTime dayInMonth(Integer dayInMonth) {
    return new NextRelativeTime(dayInMonth, DAY_IN_MONTH);
  }

  public Integer getIndex() {
    return this.index;
  }
  public void setIndex(Integer index) {
    this.index = index;
  }

  public String getIndexUnit() {
    return this.indexUnit;
  }
  public void setIndexUnit(String indexUnit) {
    this.indexUnit = indexUnit;
  }

  public String toString() {
    if (HOUR_IN_DAY.equals(indexUnit)) {
      return "next time it's "+index+":00"+toString(at);
    } else if (DAY_IN_WEEK.equals(indexUnit)) {
      return "next "+dayInWeekToString(index)+toString(at);
    } else if (DAY_IN_MONTH.equals(indexUnit)) {
      return "next time it's the "+index+"th day of the month"+toString(at);
    }
    return "next unspecified relative time";
  }

  public static String dayInWeekToString(Integer dayInWeek) {
    if (dayInWeek==null) return "unspecified day";
    else if (dayInWeek==DateTimeConstants.MONDAY) return "Monday"; // 1
    else if (dayInWeek==DateTimeConstants.TUESDAY) return "Tuesday"; // 2
    else if (dayInWeek==DateTimeConstants.WEDNESDAY) return "Wednesday"; // 3
    else if (dayInWeek==DateTimeConstants.THURSDAY) return "Thursday"; // 4
    else if (dayInWeek==DateTimeConstants.FRIDAY) return "Friday"; // 5
    else if (dayInWeek==DateTimeConstants.SATURDAY) return "Saturday"; // 6
    else if (dayInWeek==DateTimeConstants.SUNDAY) return "Sunday"; // 7
    return "invalid day in week "+dayInWeek;
  }

  @Override
  public LocalDateTime resolve(LocalDateTime base) {
    return null;
  }

  @Override
  public boolean valid() {
    return false;
  }
}
