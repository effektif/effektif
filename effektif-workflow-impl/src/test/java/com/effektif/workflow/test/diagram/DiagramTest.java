/*
 * Copyright (c) 2015, Effektif GmbH. All rights reserved.
 */
package com.effektif.workflow.test.diagram;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.workflow.diagram.Bounds;
import com.effektif.workflow.api.workflow.diagram.Diagram;
import com.effektif.workflow.api.workflow.diagram.Edge;
import com.effektif.workflow.api.workflow.diagram.Node;
import com.effektif.workflow.api.workflow.diagram.Point;

public class DiagramTest {

  @Test
  public void testCreateEmptyDiagram() {
    Diagram diagram = Diagram.newInstance();
    assertNotNull(diagram.canvas);
    assertNotNull(diagram.version);
    assertEquals(1, diagram.version.longValue());
    assertNull(diagram.canvas.children);
    assertNotNull(diagram.edges);
    assertEquals(0, diagram.edges.size());
  }
  
  @Test
  public void testAddNode() {
    Diagram diagram = Diagram.newInstance();
    String actId = "a1";
    assertFalse(diagram.hasChildren());
    diagram.addNode(actId, 11.0, 12.0, 113.0, 94.0);
    assertTrue(diagram.hasChildren());
    assertEquals(1, diagram.canvas.children.size());
    Node node = diagram.canvas.children.get(0);
    assertEquals(actId, node.bpmnElement);
    assertNotNull(node.bounds);
    assertNotNull(node.bounds.upperLeft);
    assertNotNull(node.bounds.lowerRight);
    assertEquals(11.0, node.bounds.upperLeft.x, 0.0);
    assertEquals(12.0, node.bounds.upperLeft.y, 0.0);
    assertEquals(113.0, node.bounds.lowerRight.x, 0.0);
    assertEquals(94.0, node.bounds.lowerRight.y, 0.0);
  }
  
  @Test
  public void testAddNodeToUninitializedDiagram() {
    Diagram diagram = new Diagram();
    assertFalse(diagram.hasChildren());
    String actId = "a1";
    diagram.addNode(actId, 1.0, 2.0, 3.0, 4.0);
    assertNotNull(diagram.canvas);
    assertTrue(diagram.hasChildren());
    assertEquals(1l, diagram.version.longValue());
    assertEquals(1, diagram.canvas.children.size());
    Node node = diagram.canvas.children.get(0);
    assertEquals(actId, node.bpmnElement);
  }
  
  @Test
  public void testAddEdge() {
    Edge edge = new Edge().id("");
    Diagram diagram = new Diagram();
    assertFalse(diagram.hasEdges());
    
    diagram.addEdge(edge);
    assertTrue(diagram.hasEdges());
    assertEquals(1, diagram.edges.size());
    assertEquals(edge.id, diagram.edges.get(0).id);
    
    diagram.edges(null);
    assertFalse(diagram.hasEdges());
    diagram.addEdge(edge);
    assertTrue(diagram.hasEdges());
    assertEquals(1, diagram.edges.size());
    assertEquals(edge.id, diagram.edges.get(0).id);
    
    diagram.addEdge(null);
    assertTrue(diagram.hasEdges());
    assertEquals(1, diagram.edges.size());
    assertEquals(edge.id, diagram.edges.get(0).id);
  }
  
  @Test
  public void testAddEdgeWithDetails() {
    Diagram diagram = Diagram.newInstance();
    String edgeId = "e1";
    String from = "n1";
    String to = "n2";
    diagram.addEdge(edgeId, from, to, Point.of(1, 2), Point.of(3, 4), Point.of(5, 6));
    assertEquals(1, diagram.edges.size());
    Edge edge = diagram.edges.get(0);
    assertEquals(edgeId, edge.transitionId);
    assertEquals(from, edge.fromId);
    assertEquals(to, edge.toId);
    assertEquals(3, edge.dockers.size());
    assertEquals(Point.of(1, 2), edge.dockers.get(0));
    assertEquals(Point.of(3, 4), edge.dockers.get(1));
    assertEquals(Point.of(5, 6), edge.dockers.get(2));
  }
  
  @Test
  public void testAddEdgeToUninitializedDiagram() {
    Diagram diagram = new Diagram();
    String edgeId = "e1";
    String from = "n1";
    String to = "n2";
    diagram.addEdge(edgeId, from, to, Point.of(1, 2), Point.of(3, 4), Point.of(5, 6));
    assertNotNull(diagram.canvas);
    assertEquals(1l, diagram.version.longValue());
    assertEquals(1, diagram.edges.size());
  }

  @Test
  public void testRemoveEdge() {
    Diagram diagram = new Diagram();
    String edgeId = "e1";
    String from = "n1";
    String to = "n2";
    diagram.addEdge(edgeId, from, to, Point.of(1, 2), Point.of(3, 4), Point.of(5, 6));

    String edgeId2 = "e2";
    String from2 = "n3";
    String to2 = "n4";
    diagram.addEdge(edgeId2, from2, to2, Point.of(1, 2), Point.of(3, 4), Point.of(5, 6));

    assertNotNull(diagram.canvas);
    assertEquals(2, diagram.edges.size());

    diagram.removeEdge(edgeId2);

    assertEquals(1, diagram.edges.size());
    assertEquals(edgeId, diagram.edges.get(0).transitionId);
  }

  @Test
  public void testEnsureCanvas() {
    Diagram diagram = new Diagram();
    assertNotNull(diagram.canvas);
    assertNotNull(diagram.version);
    assertEquals(1l, diagram.version.longValue());
    
    diagram = new Diagram();
    diagram.version(2l);

    assertNotNull(diagram.canvas);
    assertEquals(2l, diagram.version.longValue());
    
    Node canvas = new Node().bounds(Bounds.of(1.0, 2.0, 3.0, 4.0));
    diagram = new Diagram()
      .canvas(canvas)
      .version(2l);

    // should stay untouched
    assertEquals(canvas, diagram.canvas);
    assertEquals(2l, diagram.version.longValue());
  }
  
  @Test
  public void testIncVersion() {
    Diagram diagram = new Diagram();
    assertEquals(1l, diagram.version.longValue());
    diagram.incVersion();
    assertEquals(2l, diagram.version.longValue());
  }
  
  @Test
  public void testSetCanvas() {
    Diagram diagram = Diagram.newInstance();
    assertNotNull(diagram.canvas);
    Node canvas = new Node().bounds(Bounds.of(1.0, 2.0, 3.0, 4.0));
    assertNotEquals(canvas, diagram.canvas);
    diagram.canvas(canvas);
    assertEquals(canvas, diagram.canvas);
    
    diagram.canvas(null);
    assertNull(diagram.canvas);
  }
  
  @Test
  public void testSetEdges() {
    List<Edge> edges = new ArrayList<>();
    edges.add(new Edge().transitionId("e1"));
    edges.add(new Edge().transitionId("e2"));
    Diagram diagram = Diagram.newInstance();
    diagram.edges(edges);
    assertEquals(edges, diagram.edges);
    assertTrue(diagram.hasEdges());

    edges.add(new Edge().transitionId("e3"));
    assertNotEquals(edges, diagram.edges);
    
    diagram.edges(null);
    assertNull(diagram.edges);
    assertFalse(diagram.hasEdges());
  }
  
  @Test
  public void testSetVersion() {
    Diagram diagram = Diagram.newInstance();
    assertEquals(1l, diagram.version.longValue());
    diagram.version(5l);
    assertEquals(5l, diagram.version.longValue());
    
    diagram.version(null);
    assertNull(diagram.version);
  }
  
  @Test
  public void testEquals() {
    Diagram diagram1 = Diagram.newInstance();
    Diagram diagram2 = Diagram.newInstance();
    assertEquals(diagram1, diagram2);
    
    diagram2.canvas(new Node().bounds(Bounds.of(1.0, 2.0, 3.0, 4.0)));
    assertNotEquals(diagram1, diagram2);
    
    diagram1.canvas.bounds(Bounds.of(1.0, 2.0, 3.0, 4.0));
    assertEquals(diagram1, diagram2);
    assertEquals(diagram1.hashCode(), diagram2.hashCode());
    
    diagram2.incVersion();
    assertNotEquals(diagram1, diagram2);
    diagram1.incVersion();
    assertEquals(diagram1, diagram2);
    
    String edgeId = "e1";
    diagram1.addEdge(new Edge().transitionId(edgeId));
    assertNotEquals(diagram1, diagram2);
    
    diagram2.addEdge(new Edge().transitionId(edgeId));
    assertEquals(diagram1, diagram2);
  }

  @Test
  public void testIsValid() {
    Diagram diagram = Diagram.newInstance()
        .addNode("n1", 0.0, 0.0, 100.0, 80.0)
        .addNode("n2", 200.0, 0.0, 300.0, 80.0);
    
    assertTrue(diagram.isValid());
    
    diagram.addNode("n3", 0.0, 0.0, 0.0, 0.0);
    assertFalse(diagram.isValid());
    
    diagram.canvas.children.remove(2);
    assertTrue(diagram.isValid());
    
    diagram.addNode("n4", 0.0, 0.0, 0.0, 80.0);
    assertFalse(diagram.isValid());
    
    diagram.canvas.children.remove(2);
    assertTrue(diagram.isValid());
    
    diagram.addNode("n5", 400.0, 0.0, 500.0, 0.0);
    assertFalse(diagram.isValid());
  }
}
