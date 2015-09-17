/*
 * Copyright (c) 2015, Effektif GmbH. All rights reserved.
 */
package com.effektif.workflow.test.diagram;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.workflow.diagram.Point;

public class PointTest {

  @Test
  public void testCreateEmptyPoint() {
    Point p = new Point();
    assertEquals(0.0, p.x, 0.0);
    assertEquals(0.0, p.y, 0.0);
  }
  
  @Test
  public void testCreatePoint() {
    Point p = new Point(2.0, 3.0);
    assertEquals(2.0, p.x, 0.0);
    assertEquals(3.0, p.y, 0.0);
    
    Point p2 = Point.of(2.0, 3.0);
    assertEquals(2.0, p2.x, 0.0);
    assertEquals(3.0, p2.y, 0.0);
    assertEquals(p, p2);
    assertEquals(p.hashCode(), p2.hashCode());
    
    Point p3 = Point.of(2, 3);
    assertEquals(2.0, p2.x, 0.0);
    assertEquals(3.0, p2.y, 0.0);
    assertEquals(p, p3);
    assertEquals(p.hashCode(), p2.hashCode());
  }
  
  @Test
  public void testNotEquals() {
    Point p1 = Point.of(2.0, 3.0);
    Point p2 = Point.of(2.0, 4.0);
    Point p3 = Point.of(1.0, 3.0);
    
    assertNotEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p2, p3);
  }
  
  @Test
  public void testSetX() {
    Point p = new Point();
    p.x(5.0);
    assertEquals(5.0, p.x, 0.01);
    
    p = new Point(2.0, 3.0);
    p.x(5.0);
    assertEquals(5.0, p.x, 0.01);
  }
  
  @Test
  public void testSetY() {
    Point p = new Point();
    p.y(5.0);
    assertEquals(5.0, p.y, 0.01);
    
    p = new Point(2.0, 3.0);
    p.y(5.0);
    assertEquals(5.0, p.y, 0.01);
  }
  
  @Test
  public void testDistanceTo() {
    Point p1 = Point.of(10,  10);
    Point p2 = Point.of(13, 14);
    
    double distance = p1.distanceTo(p2);
    assertEquals(5.0, distance, 0.0);
    
    distance = p2.distanceTo(p1);
    assertEquals(5.0, distance, 0.0);
    
    distance = p1.distanceTo(p1);
    assertEquals(0.0, distance, 0.0);
    
    p1 = Point.of(-10,  10);
    p2 = Point.of(-13, 14);
    distance = p1.distanceTo(p2);
    assertEquals(5.0, distance, 0.0);
  }
}
