/*
 * Copyright (c) 2015, Effektif GmbH. All rights reserved.
 */
package com.effektif.workflow.test.diagram;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.workflow.diagram.Edge;
import com.effektif.workflow.api.workflow.diagram.Point;

public class EdgeTest {

  @Test
  public void testCreateEmptyEdge() {
    Edge edge = new Edge();
    assertNotNull(edge.dockers);
    assertTrue(edge.dockers.isEmpty());
    assertNull(edge.id);
    assertNull(edge.transitionId);
    assertNull(edge.fromId);
    assertNull(edge.toId);
  }
  
  @Test
  public void testSetDockers() {
    Edge edge = new Edge();
    List<Point> dockers = new ArrayList<>();
    dockers.add(Point.of(1.0, 2.0));
    dockers.add(Point.of(4.0, 5.0));
    dockers.add(Point.of(6.0, 7.0));
    edge.dockers(dockers);
    assertEquals(dockers, edge.dockers);
    
    dockers.add(Point.of(8.0, 9.0));
    assertNotEquals(dockers, edge.dockers);
    
    edge.dockers(null);
    assertNull(edge.dockers);
  }
  
  @Test
  public void testSetTransitionId() {
    Edge edge = new Edge();
    String transId1 = "t1";
    edge.transitionId(transId1);
    assertEquals(transId1, edge.transitionId);
    assertEquals(transId1, edge.id);
    
    String transId2 = "t2";
    edge.id(transId2);
    assertEquals(transId2, edge.transitionId);
    assertEquals(transId2, edge.id);
  }
  
  @Test
  public void testSetFromAndTo() {
    String from = "n1";
    String to = "n2";
    Edge edge = new Edge()
      .fromId(from)
      .toId(to);
    assertEquals(from, edge.fromId);
    assertEquals(to, edge.toId);
  }
  
  @Test
  public void testEquals() {
    List<Point> dockers = new ArrayList<>();
    dockers.add(Point.of(1.0, 2.0));
    dockers.add(Point.of(4.0, 5.0));
    dockers.add(Point.of(6.0, 7.0));
    String from = "n1";
    String to = "n2";
    String transId = "t1";
    
    Edge edge1 = new Edge()
      .transitionId(transId)
      .fromId(from)
      .toId(to)
      .dockers(dockers);
    Edge edge2 = new Edge()
      .transitionId(transId)
      .fromId(from)
      .toId(to)
      .dockers(dockers);
    
    assertEquals(edge1, edge2);
    assertEquals(edge1.hashCode(), edge2.hashCode());
  }
}
