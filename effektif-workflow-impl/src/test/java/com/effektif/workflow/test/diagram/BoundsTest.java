/*
 * Copyright (c) 2015, Effektif GmbH. All rights reserved.
 */
package com.effektif.workflow.test.diagram;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.workflow.diagram.Bounds;
import com.effektif.workflow.api.workflow.diagram.Point;

public class BoundsTest {
  
  @Test
  public void testCreateEmptyBounds() {
    Bounds bounds = new Bounds();
    assertNull(bounds.upperLeft);
    assertNull(bounds.lowerRight);
  }
  
  @Test
  public void testCreateBoundsWithPoints() {
    Point ul = Point.of(10.0, 11.0);
    Point lr = Point.of(22.0, 23.0);
    Bounds bounds = new Bounds(ul, lr);
    assertEquals(ul, bounds.upperLeft);
    assertEquals(lr, bounds.lowerRight);
    assertNotEquals(ul, bounds.lowerRight);
  }

  @Test
  public void testCreateBoundsWithCoordinates() {
    double ulx = 11.0;
    double uly = 12.0;
    double lrx = 22.0;
    double lry = 23.0;
    
    Bounds bounds = new Bounds(ulx, uly, lrx, lry);
    assertNotNull(bounds.upperLeft);
    assertEquals(ulx, bounds.upperLeft.x, 0.0);
    assertEquals(uly, bounds.upperLeft.y, 0.0);
    assertNotNull(bounds.lowerRight);
    assertEquals(lrx, bounds.lowerRight.x, 0.0);
    assertEquals(lry, bounds.lowerRight.y, 0.0);
  }
  
  @Test
  public void testSetUpperLeft() {
    Bounds bounds = new Bounds();
    bounds.upperLeft(Point.of(1.0, 2.0));
    assertEquals(1.0, bounds.upperLeft.x, 0.0);
    assertEquals(2.0, bounds.upperLeft.y, 0.0);
    assertNull(bounds.lowerRight);
  }
  
  @Test
  public void testSetLowerRight() {
    Bounds bounds = new Bounds();
    bounds.lowerRight(Point.of(1.0, 2.0));
    assertEquals(1.0, bounds.lowerRight.x, 0.0);
    assertEquals(2.0, bounds.lowerRight.y, 0.0);
    assertNull(bounds.upperLeft);
  }
  
  @Test
  public void testEquals() {
    double ulx = 11.0;
    double uly = 12.0;
    double lrx = 22.0;
    double lry = 23.0;
    
    Bounds b1 = Bounds.of(ulx, uly, lrx, lry);
    Bounds b2 = Bounds.of(ulx, uly, lrx, lry);
    
    assertEquals(b1, b2);
    assertEquals(b1.hashCode(), b2.hashCode());
    
    b1 = new Bounds().upperLeft(Point.of(ulx, uly));
    b2 = new Bounds().upperLeft(Point.of(ulx, uly));
    assertEquals(b1, b2);
    
    b1 = new Bounds().lowerRight(Point.of(ulx, uly));
    b2 = new Bounds().lowerRight(Point.of(ulx, uly));
    assertEquals(b1, b2);
    
    b1 = new Bounds().upperLeft(Point.of(ulx, uly));
    b2 = new Bounds().upperLeft(Point.of(lrx, lry));
    assertNotEquals(b1, b2); 
  }
  
  @Test
  public void testIsValid() {
    Bounds b = Bounds.of(10.0, 10.0, 13.0, 14.0);
    assertTrue(b.isValid());
    b = Bounds.of(0.0, 0.0, 0.0, 0.0);
    assertFalse(b.isValid());
    b = new Bounds();
    assertFalse(b.isValid());
    b = Bounds.of(0.0, 10.0, 0.0, 14.0);
    assertFalse(b.isValid());
    b = Bounds.of(10.0, 0.0, 13.0, 0.0);
    assertFalse(b.isValid());
  }
}
