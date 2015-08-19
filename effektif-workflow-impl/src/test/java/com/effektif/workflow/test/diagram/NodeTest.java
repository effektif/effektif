/*
 * Copyright (c) 2015, Effektif GmbH. All rights reserved.
 */
package com.effektif.workflow.test.diagram;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.workflow.diagram.Bounds;
import com.effektif.workflow.api.workflow.diagram.Node;
import com.effektif.workflow.api.workflow.diagram.Point;

public class NodeTest {

  @Test
  public void testCreateEmptyNode() {
    Node node = new Node();
    assertNull(node.elementId);
    assertNull(node.bounds);
    assertNull(node.children);
  }
  
  @Test
  public void testSetActivityId() {
    String actId = "1";
    Node node = new Node();
    
    node.elementId(actId);
    assertEquals(actId, node.elementId);
    assertEquals(actId, node.id);
    
    String actId2 = "2";
    node.id(actId2);
    assertEquals(actId2, node.elementId);
    assertEquals(actId2, node.id);
  }
  
  @Test
  public void testSetBounds() {
    Bounds bounds = new Bounds(Point.of(1, 2), Point.of(3, 4));
    Node node = new Node();
    node.bounds(bounds);
    
    assertNotNull(bounds);
    assertEquals(new Bounds(Point.of(1, 2), Point.of(3, 4)), node.bounds);
    
    node.bounds(null);
    assertNull(node.bounds);
    
  }
  
  @Test
  public void testSetChildren() {
    List<Node> children = new ArrayList<>();
    children.add(new Node().id("n1"));
    children.add(new Node().id("n2"));
    Node parent = new Node();
    parent.children(children);
    assertEquals(children, parent.children);

    children.add(new Node().id("n3"));
    assertNotEquals(children, parent.children);
    
    parent.children(null);
    assertNull(parent.children);    
  }
  
  @Test
  public void testAddNode() {
    Node node = new Node().id("n1");
    Node canvas = new Node();
    assertFalse(canvas.hasChildren());
    
    canvas.addNode(node);
    assertTrue(canvas.hasChildren());
    assertEquals(1, canvas.children.size());
    assertEquals(node.id, canvas.children.get(0).id);
    
    canvas.children(null);
    assertFalse(canvas.hasChildren());
    canvas.addNode(node);
    assertTrue(canvas.hasChildren());
    assertEquals(1, canvas.children.size());
    assertEquals(node.id, canvas.children.get(0).id);
    
    canvas.addNode(null);
    assertTrue(canvas.hasChildren());
    assertEquals(1, canvas.children.size());
    assertEquals(node.id, canvas.children.get(0).id);
  }
  
  @Test
  public void testEquals() {
    List<Node> children = new ArrayList<>();
    children.add(new Node().id("n1"));
    children.add(new Node().id("n2"));
    String actId = "a1";
    Bounds bounds = new Bounds(Point.of(1, 2), Point.of(3, 4));
    
    Node node1 = new Node()
      .elementId(actId)
      .bounds(bounds)
      .children(children);
    
    Node node2 = new Node()
      .elementId(actId)
      .bounds(bounds)
      .children(children);
    
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());
  }
  
}
