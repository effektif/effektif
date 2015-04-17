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
package com.effektif.workflow.api.acl;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.mapper.JsonWriter;


/**
 * @author Tom Baeyens
 */
public class AccessIdentity implements JsonReadable, JsonWritable {
  
  protected String id;

  public AccessIdentity() {
  }

  public AccessIdentity(Object id) {
    if (id instanceof String) {
      this.id = (String) id;
    } if (id!=null) {
      this.id = id.toString();
    }
  }

  @Override
  public void readJson(JsonReader r) {
    id = r.readString("id");
  }

  @Override
  public void writeJson(JsonWriter w) {
    w.writeString("id", id);
  }

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AccessIdentity other = (AccessIdentity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
