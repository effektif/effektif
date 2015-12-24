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

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.model.AfterRelativeTime;
import com.effektif.workflow.api.model.NextRelativeTime;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.TimeInDay;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class RelativeTimeTest extends WorkflowTest {
  
  private static final Logger log = LoggerFactory.getLogger(RelativeTimeTest.class);
  
  @Test
  public void testAfterRelativeTime() {
    assertNull(wirizeAfter(AfterRelativeTime.minutes(5)).getAt());
    assertEquals(5, (int) wirizeAfter(AfterRelativeTime.minutes(5)).getDuration());
    assertEquals(AfterRelativeTime.MINUTES, wirizeAfter(AfterRelativeTime.minutes(5)).getDurationUnit());
    assertEquals(AfterRelativeTime.HOURS, wirizeAfter(AfterRelativeTime.hours(5)).getDurationUnit());
    assertEquals(AfterRelativeTime.DAYS, wirizeAfter(AfterRelativeTime.days(5)).getDurationUnit());
    assertEquals(AfterRelativeTime.WEEKS, wirizeAfter(AfterRelativeTime.weeks(5)).getDurationUnit());
    assertEquals(AfterRelativeTime.MONTHS, wirizeAfter(AfterRelativeTime.months(5)).getDurationUnit());
    assertEquals(AfterRelativeTime.YEARS, wirizeAfter(AfterRelativeTime.years(5)).getDurationUnit());
    
    TimeInDay at = wirizeAfter(AfterRelativeTime.minutes(5)
        .at(11,45)).getAt();
    assertEquals(11, (int) at.getHour());
    assertEquals(45, (int) at.getMinutes());
  }

  @Test
  public void testNextRelativeTime() {
    assertNull(wirizeNext(NextRelativeTime.hourInDay(5)).getAt());
    assertEquals(5, (int) wirizeNext(NextRelativeTime.hourInDay(5)).getIndex());
    assertEquals(NextRelativeTime.HOUR_IN_DAY, wirizeNext(NextRelativeTime.hourInDay(5)).getIndexUnit());
    assertEquals(NextRelativeTime.DAY_IN_WEEK, wirizeNext(NextRelativeTime.dayInWeek(5)).getIndexUnit());
    assertEquals(NextRelativeTime.DAY_IN_MONTH, wirizeNext(NextRelativeTime.dayInMonth(5)).getIndexUnit());
    
    TimeInDay at = wirizeNext(NextRelativeTime.dayInMonth(5)
        .at(11,45)).getAt();
    assertEquals(11, (int) at.getHour());
    assertEquals(45, (int) at.getMinutes());
  }

  /** serialize to json string and deserialize back to bean */ 
  public AfterRelativeTime wirizeAfter(RelativeTime time) {
    return (AfterRelativeTime) wirize(time);
  }

  /** serialize to json string and deserialize back to bean */ 
  public NextRelativeTime wirizeNext(RelativeTime time) {
    return (NextRelativeTime) wirize(time);
  }

  /** serialize to json string and deserialize back to bean */ 
  public RelativeTime wirize(RelativeTime time) {
    JsonStreamMapper streamMapper = configuration.get(JsonStreamMapper.class);
    String json = streamMapper.write(time);
    log.debug("Serialized relative time : "+json);
    return streamMapper.readString(json, RelativeTime.class);
  }
}
