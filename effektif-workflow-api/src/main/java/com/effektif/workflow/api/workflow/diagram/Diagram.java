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
import java.util.Arrays;
import java.util.List;

/**
 * A BPMN diagram.
 */
public class Diagram {

  public String id;
  public Node canvas;
  public List<Edge> edges;
  public Long version;

  public Diagram() {
    ensureCanvas();
  }

  public Diagram canvas(Node canvas) {
    this.canvas = canvas;
    return this;
  }

  public Diagram edges(List<Edge> edges) {
    if (edges != null) {
      this.edges = new ArrayList<>(edges);
    } else {
      this.edges = null;
    }
    return this;
  }

  public Diagram version(Long version) {
    this.version = version;
    return this;
  }
  
  public void incVersion() {
    if (this.version != null) {
      this.version++;
    } else {
      this.version = 1l;
    }
  }
  
  protected void ensureCanvas() {
    if (this.canvas == null) {
      this.canvas(new Node());
    }
    if (this.version == null) {
      this.version = 1l;
    }
    if (this.edges == null) {
      this.edges = new ArrayList<>();
    }
  }
  
  public boolean hasChildren() {
    return canvas != null && canvas.hasChildren();
  }
  
  public boolean hasEdges() {
    return edges != null && !edges.isEmpty();
  }

  public Diagram addNodes(List<Node> nodes) {
    ensureCanvas();
    for (Node node : nodes) {
      canvas.addNode(node);
    }
    return this;
  }

  public Diagram addNode(String activityId, double ulx, double uly, double lrx, double lry) {
    ensureCanvas();
    canvas.addNode(new Node()
      .elementId(activityId)
      .bounds(new Bounds(ulx, uly, lrx, lry)));
    return this;
  }

  public Node getNode(String id) {
    if (canvas != null) {
      return canvas.getChild(id);
    }
    return null;
  }

  public Diagram addEdge(Edge edge) {
    if (edge != null) {
      ensureCanvas();
      edges.add(edge);
    }
    return this;
  }
  
  public Diagram addEdge(String transitionId, String fromId, String toId, Point...dockers) {
    return addEdge(new Edge()
      .transitionId(transitionId)
      .fromId(fromId)
      .toId(toId)
      .dockers(Arrays.asList(dockers)));
  }

  public Edge getEdge(String id) {
    if (id != null && edges != null) {
      for (Edge edge : edges) {
        if (id.equals(edge.id)) {
          return edge;
        }
      }
    }
    return null;
  }

  public Diagram removeEdge(String transitionId) {
    if (transitionId != null) {
      Edge edge = getEdge(transitionId);
      if (edge != null) {
        edges.remove(edge);
      }
    }
    return this;
  }
  
  public boolean isValid() {
    // we don't check the canvas for validaty only the children
    // the canvas itself won't be valid because its bounds are zero
    if (canvas != null && canvas.children != null) {
      for (Node node : canvas.children) {
        if (!node.isValid()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((canvas == null) ? 0 : canvas.hashCode());
    result = prime * result + ((edges == null) ? 0 : edges.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    Diagram other = (Diagram) obj;
    if (canvas == null) {
      if (other.canvas != null)
        return false;
    } else if (!canvas.equals(other.canvas))
      return false;
    if (edges == null) {
      if (other.edges != null)
        return false;
    } else if (!edges.equals(other.edges))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  public static Diagram newInstance() {
    Diagram diagram = new Diagram();
    diagram.ensureCanvas();
    return diagram;
  }
  
  // TODO Migrate.
//  public void onClone(CloningContext ctx) {
//    if (this.canvas != null) {
//      this.canvas.onClone(ctx);
//    }
//    if (this.edges != null) {
//      List<Edge> remove = new LinkedList<>();
//      for (Edge edge : this.edges) {
//        edge.onClone(ctx);
//        // remove edges without a valid id
//        if (edge.id == null) {
//          remove.add(edge);
//        }
//      }
//      this.edges.removeAll(remove);
//    }
//  }

}
