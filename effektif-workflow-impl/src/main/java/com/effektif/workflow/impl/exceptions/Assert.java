/*
 * Copyright (c) 2015, Effektif GmbH.
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

package com.effektif.workflow.impl.exceptions;


/**
 * Helper for throwing a {@link IllegalArgumentException} with a fluent API.
 *
 * TODO After effektif3 merge:
 * - rename class to IllegalArgument
 * - replace calls to ‘checkTrue’ that have negative conditions with calls to ‘when’ with the condition flipped
 * - inline the deprecated methods to use the new names.
 *
 * @author Peter Hilton
 */
public class Assert {

  public static void ifNull(Object object) {
    if (object==null) {
      throw new IllegalArgumentException("object is null");
    }
  }

  public static void unless(boolean condition) {
    when(!condition, null);
  }

  public static void unless(boolean condition, String message) {
    when(!condition, message);
  }

  public static void when(boolean condition) {
    when(condition, null);
  }

  public static void when(boolean condition, String message) {
    if (condition) {
      throw new IllegalArgumentException((message!=null ? message : "condition not true"));
    }
  }

  @Deprecated
  public static void ensureTrue(boolean condition) {
    unless(condition);
  }

  @Deprecated
  public static void ensureTrue(boolean condition, String message) {
    unless(condition, null);
  }

  @Deprecated
  public static void notNull(Object object) {
    ifNull(object);
  }
}
