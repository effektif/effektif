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
package com.effektif.workflow.test.timer;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.effektif.workflow.api.model.NextRelativeTime;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class RelativeTimeTest extends WorkflowTest {
  
  @Test
  public void testIn5Minutes() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.minutes(5).resolve(base);
    assertEquals(base.plusMinutes(5), absoluteTime);
  }

  @Test
  public void testIn7Hours() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.hours(7).resolve(base);
    assertEquals(base.plusHours(7), absoluteTime);
  }

  @Test
  public void testIn20Days() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.days(20).resolve(base);
    assertEquals(endOfDay(base.plusDays(20)), absoluteTime);
  }

  @Test
  public void testIn3Weeks() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.weeks(3).resolve(base);
    assertEquals(endOfDay(base.plusWeeks(3)), absoluteTime);
  }

  @Test
  public void testIn9Months() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.months(9).resolve(base);
    assertEquals(endOfDay(base.plusMonths(9)), absoluteTime);
  }

  @Test
  public void testIn4Years() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.years(4).resolve(base);
    assertEquals(endOfDay(base.plusYears(4)), absoluteTime);
  }

  @Test
  public void testTomorrowAt11() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = RelativeTime.days(1).at(11,00).resolve(base);
    assertEquals(base
            .plusDays(1)
            .withTime(11, 0, 0, 0), absoluteTime);
  }

  @Test
  public void testNextThursday() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = NextRelativeTime
      .dayOfWeek(DateTimeConstants.THURSDAY)
      .resolve(base);
    assertEquals(base
            .withDayOfWeek(DateTimeConstants.THURSDAY)
            .withTime(0, 0, 0, 0), absoluteTime);
  }

  @Test
  public void testNextThursdayAt15() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = NextRelativeTime
      .dayOfWeek(DateTimeConstants.THURSDAY)
      .at(15,30)
      .resolve(base);
    assertEquals(base
            .withDayOfWeek(DateTimeConstants.THURSDAY)
            .withTime(15, 30, 0, 0), absoluteTime);
  }

  @Test
  public void testNextFirstOfTheMonth() {
    LocalDateTime base = new LocalDateTime(2015, 12, 28, 9, 0, 0, 0);
    LocalDateTime absoluteTime = NextRelativeTime
      .dayOfMonth(1)
      .resolve(base);
    assertEquals(new LocalDateTime(2016, 1, 1, 0, 0, 0, 0), absoluteTime);
  }

  private Object endOfDay(LocalDateTime time) {
    return time.withTime(23, 59, 59, 999);
  }
}
