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
package com.effektif.workflow.impl.util;



/**
 * @author Tom Baeyens
 */
public class StringUtil {

  public static String toString(Object o) {
    return o!=null ? o.toString() : "null";
  }

  
  public static String deCamelCase(String identifier) {
    if (identifier==null || identifier.isEmpty()) {
      return null;
    }
    StringBuilder nameBuilder = new StringBuilder();
    for (int i=0; i<identifier.length(); i++) {
      char c = identifier.charAt(i);
      if (i==0) {
        nameBuilder.append(Character.toUpperCase(c));
      } else if (!Character.isLowerCase(c)) {
        nameBuilder.append(" ");
        nameBuilder.append(Character.toUpperCase(c));
      } else {
        nameBuilder.append(c);
      }
    }
    return nameBuilder.toString();
  }

}
