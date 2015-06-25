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
package com.effektif.mongo;

import com.mongodb.BasicDBObject;


public class Fields {
  
  protected BasicDBObject fields = new BasicDBObject();

  public Fields include(String... fields) {
    if (fields!=null) {
      for (String field: fields) {
        this.fields.put(field, 1);
      }
    }
    return this;
  }

  public Fields exclude(String... fields) {
    if (fields!=null) {
      for (String field: fields) {
        this.fields.put(field, 0);
      }
    }
    return this;
  }

  public BasicDBObject get() {
    return fields;
  }

  public boolean isEmpty() {
    return fields.isEmpty();
  }
}
