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
package com.effektif.workflow.impl.util;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;


/**
 * Provides the current date-time in a way that tests can override, to
 *
 * @author Tom Baeyens
 */
public class Time {
  
  public static LocalDateTime now = null;
  
  public static LocalDateTime now() {
    if (now!=null) {
      return now;
    }
    return new LocalDateTime(DateTimeZone.UTC);
  }
}
