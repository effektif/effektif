/*
 * Copyright 2015 Effektif GmbH.
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
package com.effektif.workflow.api.workflow.diagram;

import java.util.ArrayList;
import java.util.List;

/**
 * A connector - such as a sequence flow - between nodes on a BPMN diagram.
 */
public class Edge {

  public String id;
  public List<Point> dockers = new ArrayList<>();
  public String fromId;
  public String toId;
  public String transitionId;

  public Edge dockers(List<Point> dockers) {
    if (dockers != null) {
      this.dockers = new ArrayList<>(dockers);
    } else {
      this.dockers = null;
    }
    return this;
  }

  public boolean hasDockers() {
    return dockers != null && !dockers.isEmpty();
  }

  public Edge fromId(String fromId) {
    this.fromId = fromId;
    return this;
  }
  
  public Edge id(String id) {
    this.id = id;
//    this.transitionId = id;
    return this;
  }
  
  public Edge toId(String toId) {
    this.toId = toId;
    return this;
  }
  

  public Edge transitionId(String transitionId) {
//    this.id = transitionId;
    this.transitionId = transitionId;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dockers == null) ? 0 : dockers.hashCode());
    result = prime * result + ((fromId == null) ? 0 : fromId.hashCode());
    result = prime * result + ((toId == null) ? 0 : toId.hashCode());
    result = prime * result + ((transitionId == null) ? 0 : transitionId.hashCode());
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
    Edge other = (Edge) obj;
    if (dockers == null) {
      if (other.dockers != null)
        return false;
    } else if (!dockers.equals(other.dockers))
      return false;
    if (fromId == null) {
      if (other.fromId != null)
        return false;
    } else if (!fromId.equals(other.fromId))
      return false;
    if (toId == null) {
      if (other.toId != null)
        return false;
    } else if (!toId.equals(other.toId))
      return false;
    if (transitionId == null) {
      if (other.transitionId != null)
        return false;
    } else if (!transitionId.equals(other.transitionId))
      return false;
    return true;
  }
  
}
