/* Copyright 2014 Effektif GmbH.
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

package com.effektif.workflow.impl.util;

public class IllegalArgument {
  
  public static void checkTrue(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }
  
  public static void checkNotNull(Object o, String variableName) {
    if (o==null) {
      throw new IllegalArgumentException(variableName+" is null");
    }
  }
  
  public static void checkNotEmpty(String string, String variableName) {
    if (string==null || string.isEmpty()) {
      throw new IllegalArgumentException(variableName+" is "+(string==null? "null" : "empty string"));
    }
  }
}